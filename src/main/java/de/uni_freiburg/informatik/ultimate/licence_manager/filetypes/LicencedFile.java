/*
 * Copyright (C) 2000-2015 University of Freiburg
 */
package de.uni_freiburg.informatik.ultimate.licence_manager.filetypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.Author;
import de.uni_freiburg.informatik.ultimate.licence_manager.FileUtils;
import de.uni_freiburg.informatik.ultimate.licence_manager.LicenceTemplate;
import de.uni_freiburg.informatik.ultimate.licence_manager.RuntimeIOException;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class LicencedFile {

	private final File mFile;
	private final boolean mHasLicence;
	private final FileType mFileType;
	private final List<String> mFileContent;
	private final LicenceTemplate mTemplate;

	private final Pattern mPatternComments;

	private List<String> mNewContent;
	private List<String> mAuthors;

	public LicencedFile(final File file, final LicenceTemplate template) {
		mFile = file;
		try {
			mFileContent = Files.lines(file.toPath()).sequential()
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
		mPatternComments = Pattern.compile("\\s*/*\\*\\s*");

		mTemplate = template;
		mFileType = getCommentStyle(file);
		mHasLicence = computeHasLicence(file, template);

	}

	public boolean needsWriting() {
		if (!hasLicence()) {
			return true;
		}

		if (!FileUtils.streamEquals(getFreshStream(), getNewContent())) {
			return true;
		}

		return false;
	}

	public Stream<String> getNewContent() {
		if (mNewContent == null) {
			switch (mFileType) {
			case JAVA:
				mNewContent = computeNewContentJava();
				break;
			default:
				mNewContent = new ArrayList<String>();
				break;
			}
		}
		return mNewContent.stream();
	}

	private List<String> computeNewContentJava() {
		ArrayList<String> newContent = new ArrayList<String>();
		final Stream<String> currentContent;
		final List<Author> authors;
		if (hasLicence()) {
			final List<Author> licencedAuthors = getAuthorsFromLicence();
			authors = Stream.concat(
					getAuthorNamesFromComments()
							.stream()
							.map(a -> new Author(a, null, null))
							.filter(a -> licencedAuthors.stream().anyMatch(
									la -> la.Name.equals(a.Name))),
					licencedAuthors.stream()).distinct().collect(Collectors.toList());
			currentContent = removeLicence();
		} else {
			authors = getAuthorNamesFromComments().stream()
					.map(a -> new Author(a, null, null))
					.collect(Collectors.toList());
			currentContent = getFreshStream();
		}

		newContent.add("/*");
		mTemplate.getWritableTemplate(authors).forEach(
				line -> newContent.add(" * " + line));
		newContent.add(" */");
		currentContent.forEach(line -> newContent.add(line));
		return newContent;

	}

	/**
	 * Assumes that there is a licence
	 * 
	 * @return
	 */
	private Stream<String> removeLicence() {
		final Iterator<String> iter = getFreshStream().iterator();
		while (iter.hasNext()) {
			if (iter.next().endsWith("*/")) {
				final List<String> rtr = new ArrayList<String>();
				iter.forEachRemaining(s -> rtr.add(s));
				return rtr.stream();
			}
		}
		return Stream.empty();
	}

	/**
	 * Assumes that there is a licence
	 * 
	 * @return
	 */
	private List<Author> getAuthorsFromLicence() {
		final List<Author> rtr = new ArrayList<Author>();
		final Iterator<String> iter = getFreshStream().iterator();
		while (iter.hasNext()) {
			final String next = iter.next();

			Pattern pattern = Pattern
					.compile("Copyright \\(C\\) (\\d{4})\\s*-*\\s*(\\d{4})*\\s(.*)");
			Matcher matcher = pattern.matcher(removeComments(next));
			if (matcher.matches()) {
				if (matcher.groupCount() > 2) {
					rtr.add(new Author(matcher.group(3), matcher.group(1),
							matcher.group(2)));
				} else {
					rtr.add(new Author(matcher.group(2), matcher.group(1), null));
				}
			}

			if (next.endsWith("*/")) {
				break;
			}
		}
		return rtr;
	}

	private FileType getCommentStyle(File file) {
		final String[] splitName = file.getName().split("\\.");
		if (splitName.length <= 1) {
			return FileType.Unknown;
		}
		final String ending = splitName[splitName.length - 1].toLowerCase();
		switch (ending) {
		case "xml":
			return FileType.XML;
		case "java":
			return FileType.JAVA;
		default:
			return FileType.Unknown;
		}
	}

	private boolean hasLicence() {
		return mHasLicence;
	}

	private boolean computeHasLicence(final File file, LicenceTemplate template) {
		switch (mFileType) {
		case JAVA:
			return computeHasLicenceJava(file.toPath(), template);
		default:
			return false;
		}
	}

	private boolean computeHasLicenceJava(Path path, LicenceTemplate template) {
		return getFreshStream().map(this::removeComments).anyMatch(
				line -> template.isFirstLine(line));
	}

	private String removeComments(String str) {
		return mPatternComments.matcher(str).replaceAll("");
	}

	private List<String> getAuthorNamesFromComments() {
		if (mAuthors == null) {
			final Pattern pattern = Pattern.compile("\\W*@author\\s(.+)");
			mAuthors = getFreshStream().map((line) -> {
				final Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					return matcher.group(1).trim();
				}
				return null;
			}).filter(s -> s != null).map(s -> s.split("@author"))
					.flatMap(Arrays::stream).map(s -> s.trim())
					.collect(Collectors.toList());
		}
		return mAuthors;
	}

	private Stream<String> getFreshStream() {
		return mFileContent.stream();
	}

	@Override
	public String toString() {
		return mFile.getAbsolutePath()
				+ " -- "
				+ (mHasLicence ? "Licenced" : "Not licenced")
				+ ", type="
				+ mFileType
				+ (mAuthors == null || mAuthors.isEmpty() ? ""
						: (" authors=" + String.join(", ", mAuthors)));
	}

}
