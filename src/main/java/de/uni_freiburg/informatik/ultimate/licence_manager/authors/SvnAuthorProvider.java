/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE licence-manager.
 * 
 * The ULTIMATE licence-manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE licence-manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE licence-manager. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE licence-manager, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE licence-manager grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.Main;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.DateUtils;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.ProcessUtils;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.ProcessUtils.StreamGobbler;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class SvnAuthorProvider implements IAuthorProvider {

	private static final boolean SVN_AVAILABLE = isSvnAvailable();
	private static final float THRESHOLD_PERCENTAGE = 10;
	private static String sSvnVersion;

	@Override
	public List<Author> getAuthors(LicencedFile file,
			IFileTypeDependentOperation operation) {
		return getAuthors(file.getFile());
	}

	@Override
	public boolean isUsable(LicencedFile file, FileType fileType) {
		return SVN_AVAILABLE && !Main.hasOption(Main.OPTION_DISABLE_SVN)
				&& fileType != FileType.UNKNOWN
				&& isUnderVersionControl(file.getFile().getAbsolutePath());
	}

	public String getSvnVersion() {
		return sSvnVersion;
	}

	private static boolean isSvnAvailable() {
		final ProcessBuilder pbuilder = new ProcessBuilder("svn", "--version");
		try {
			final Process process = pbuilder.start();
			if (process.waitFor(1000, TimeUnit.MILLISECONDS)) {
				final List<String> lines = IOUtils.readLines(
						process.getInputStream(), Charset.defaultCharset());
				if (lines == null || lines.isEmpty()) {
					return false;
				}
				sSvnVersion = lines.get(0);
				return true;
			}
		} catch (IOException | InterruptedException e) {
			System.err
					.println("Could not find 'svn' executable, disabling author provider");
			return false;
		}
		return false;
	}

	private boolean isUnderVersionControl(String absolutePath) {
		final ProcessBuilder pbuilder = new ProcessBuilder("svn", "info",
				absolutePath);
		try {
			final Process process = pbuilder.start();
			if (process.waitFor(1000, TimeUnit.MILLISECONDS)) {
				final List<String> lines = IOUtils.readLines(
						process.getInputStream(), Charset.defaultCharset());
				if (lines == null || lines.isEmpty()) {
					return false;
				}
				return !lines.get(0).contains("is not a working copy");
			}
		} catch (IOException | InterruptedException e) {
			System.err
					.println("Could not find 'svn' executable, disabling author provider");
			return false;
		}
		return false;

	}

	private List<Author> getAuthors(File file) {
		final List<Author> rtr = new ArrayList<Author>();
		try {
			final InputStream output = getSvnBlameProcess(file
					.getAbsolutePath());
			final SvnBlameSAXHandler handler = parseSvnBlameOutput(output);
			for (final Entry<String, YearAndLineCount> entry : handler.mLinesPerAuthor
					.entrySet()) {
				final float percentage = (float) entry.getValue().LineCount
						/ (float) handler.mMaxLineNumber * 100;
				if (percentage > THRESHOLD_PERCENTAGE) {
					rtr.add(new Author(entry.getKey(), entry.getValue().Year,
							null));
				}
			}

		} catch (IOException | InterruptedException
				| ParserConfigurationException | SAXException e) {
			System.err.println("Error while executing 'svn blame': "
					+ e.getMessage());
		}
		return rtr;
	}

	private InputStream getSvnBlameProcess(String absolutePath)
			throws IOException, InterruptedException {
		final Process process = new ProcessBuilder("svn", "blame", "--xml",
				absolutePath).start();
		ProcessUtils.inheritIO(process.getErrorStream(), System.err);
		StreamGobbler gobbler = ProcessUtils.attachGobbler(process
				.getInputStream());
		while (!process.waitFor(2500, TimeUnit.MILLISECONDS)) {
			System.out.print(".");
		}
		return gobbler.getFreshStream();
	}

	private SvnBlameSAXHandler parseSvnBlameOutput(InputStream input)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		SvnBlameSAXHandler handler = new SvnBlameSAXHandler();
		saxParser.parse(input, handler);
		return handler;
	}

	private static class SvnBlameSAXHandler extends DefaultHandler {

		private int mMaxLineNumber;
		private Map<String, YearAndLineCount> mLinesPerAuthor;

		private boolean mTagAuthor;
		private boolean mTagDate;
		private String mCurrentAuthor;

		private SvnBlameSAXHandler() {
			mLinesPerAuthor = new HashMap<String, YearAndLineCount>();
			mMaxLineNumber = 0;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			if (qName.equals("author")) {
				mTagAuthor = true;
				mMaxLineNumber++;
			}

			if (qName.equals("date")) {
				mTagDate = true;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			super.characters(ch, start, length);

			if (mTagAuthor) {
				mCurrentAuthor = new String(ch, start, length);
				YearAndLineCount lineCount = getYearAndLineCount(mCurrentAuthor);
				lineCount.LineCount++;
				mTagAuthor = false;
				return;
			}

			if (mTagDate) {
				final String date = new String(ch, start, length);
				YearAndLineCount lineCount = getYearAndLineCount(mCurrentAuthor);
				try {
					// <date>2013-10-23T08:21:21.894117Z</date>
					lineCount.Year = DateUtils.min(DateUtils.getYear(date,
							"yyyy-MM-dd'T'HH:mm:ss.SSSSSSX"), lineCount.Year);
				} catch (ParseException e) {
					System.err.print(e.getMessage());
				}
				mTagDate = false;
				return;
			}
		}

		private YearAndLineCount getYearAndLineCount(final String authorName) {
			YearAndLineCount lineCount = mLinesPerAuthor.get(authorName);
			if (lineCount == null) {
				lineCount = new YearAndLineCount(DateUtils.getCurrentYear(), 0);
				mLinesPerAuthor.put(authorName, lineCount);
			}
			return lineCount;
		}
	}

	private static class YearAndLineCount {
		private String Year;
		private int LineCount;

		private YearAndLineCount(String year, int linecount) {
			Year = year;
			LineCount = linecount;
		}
	}

}
