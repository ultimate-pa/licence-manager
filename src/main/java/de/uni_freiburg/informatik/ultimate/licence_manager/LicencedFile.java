/*
 * Copyright (C) 2000-2015 University of Freiburg
 * Copyright (C) 2002-2015 Dietsch dietsch@informatik.uni-freiburg.de
 */
package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			final Stream<String> currentContent;
			final List<Author> authors;
			if (hasLicence()) {
				final List<Author> licencedAuthors = getAuthorsFromExistingLicence();
				final List<String> commentAuthors = getAuthorNamesFromFileContent();
				authors = Stream
						.concat(commentAuthors
								.stream()
								.filter(commentAuthor -> !licencedAuthors
										.stream()
										.anyMatch(
												licencedAuthor -> licencedAuthor.Name
														.equals(commentAuthor)))
								.map(a -> new Author(a, null, null)),
								licencedAuthors.stream()).distinct()
						.collect(Collectors.toList());
				currentContent = removeLicence();
			} else {
				authors = getAuthorNamesFromFileContent().stream()
						.map(a -> new Author(a, null, null))
						.collect(Collectors.toList());
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

	/**
	 * Assumes that there is a licence
	 * 
	 * @return
	 */
	private List<Author> getAuthorsFromExistingLicence() {
		final List<Author> rtr = new ArrayList<Author>();
		final Iterator<String> iter = mFile.getStream().iterator();
		while (iter.hasNext()) {
			final String next = iter.next();

			Pattern pattern = Pattern
					.compile("Copyright \\(C\\) (\\d{4})\\s*-*\\s*(\\d{4})*\\s(.*)");
			Matcher matcher = pattern.matcher(mOperations.removeComments(next));
			if (matcher.matches()) {
				if (matcher.groupCount() > 2) {
					rtr.add(new Author(matcher.group(3), matcher.group(1),
							matcher.group(2)));
				} else {
					rtr.add(new Author(matcher.group(2), matcher.group(1), null));
				}
			}

			if (next.endsWith(mOperations.getLastLine())) {
				break;
			}
		}
		return rtr;
	}

	private boolean computeHasLicence(final LicenceTemplate template) {
		return mOperations.computeHasLicence(template);
	}

	private List<String> getAuthorNamesFromFileContent() {
		if (mAuthors == null) {
			final Pattern pattern = Pattern.compile("\\W*@author\\s(.+)");
			mAuthors = mFile.getStream().map((line) -> {
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

	@Override
	public String toString() {
		return mFile.getFile().getAbsolutePath()
				+ " (licenced="
				+ mHasLicence
				+ ", type="
				+ mOperations.getFileType()
				+ (mAuthors == null || mAuthors.isEmpty() ? ""
						: (", content-authors=" + String.join(", ", mAuthors)))+")";
	}

}
