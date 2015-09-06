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
package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.authors.Author;
import de.uni_freiburg.informatik.ultimate.licence_manager.authors.Authors;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.CachedFileStream;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.FileUtils;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class LicencedFile {

	private final CachedFileStream mFile;
	private final boolean mHasLicence;
	private final LicenceTemplate mTemplate;
	private final IFileTypeDependentOperation mOperations;

	private List<String> mNewContent;
	private List<String> mAuthors;

	public LicencedFile(final CachedFileStream file,
			final LicenceTemplate template,
			final IFileTypeDependentOperation operations) {
		mFile = file;
		mOperations = operations;
		mTemplate = template;
		mHasLicence = computeHasLicence(template);
	}

	public boolean hasLicence() {
		return mHasLicence;
	}

	public boolean needsWriting() {
		if (!hasLicence()) {
			return true;
		}
		if (!FileUtils.streamEquals(mFile.getStream(), getNewContent())) {
			return true;
		}
		return false;
	}

	public Stream<String> getNewContent() {
		if (mNewContent == null) {
			final ArrayList<String> newContent = new ArrayList<String>();
			final List<Author> authors = Authors.getAuthors(this, mOperations);
			final Stream<String> currentContent;
			if (hasLicence()) {
				currentContent = removeLicence();
			} else {
				currentContent = mFile.getStream();
			}

			newContent.add(mOperations.getFirstLine());
			mTemplate.getWritableTemplate(authors).forEach(
					line -> newContent.add(mOperations.getLicenceIndent()
							+ line));
			newContent.add(mOperations.getLastLine());
			currentContent.forEach(line -> newContent.add(line));
			mNewContent = newContent;
		}
		return mNewContent.stream();
	}

	public Stream<String> getContentWithoutLicence() {
		return removeLicence();
	}

	public File getFile() {
		return mFile.getFile();
	}

	public CachedFileStream getCachedFileStream() {
		return mFile;
	}

	/**
	 * Assumes that there is a licence
	 * 
	 * @return
	 */
	private Stream<String> removeLicence() {
		final Iterator<String> iter = mFile.getStream().iterator();
		while (iter.hasNext()) {
			if (iter.next().endsWith(mOperations.getLastLine())) {
				final List<String> rtr = new ArrayList<String>();
				iter.forEachRemaining(s -> rtr.add(s));
				return rtr.stream();
			}
		}
		return Stream.empty();
	}

	private boolean computeHasLicence(final LicenceTemplate template) {
		return mOperations.computeHasLicence(template);
	}

	@Override
	public String toString() {
		return mFile.getFile().getAbsolutePath()
				+ " (licenced="
				+ mHasLicence
				+ ", type="
				+ mOperations.getFileType()
				+ (mAuthors == null || mAuthors.isEmpty() ? ""
						: (", content-authors=" + String.join(", ", mAuthors)))
				+ ")";
	}

}
