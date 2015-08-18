/*
 * Copyright (C) 2000 University of Freiburg
 */
package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

import de.uni_freiburg.informatik.ultimate.licence_manager.comments.CommentStyle;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class LicencedFile {

	private final File mFile;
	private final boolean mHasLicence;
	private final CommentStyle mCommentStyle;

	public LicencedFile(final File file, final LicenceTemplate template) {
		mFile = file;
		mCommentStyle = getCommentStyle(file);
		mHasLicence = hasLicence(file, template);
	}

	private CommentStyle getCommentStyle(File file) {
		final String[] splitName = file.getName().split("\\.");
		if (splitName.length <= 1) {
			return CommentStyle.Unknown;
		}
		final String ending = splitName[splitName.length - 1].toLowerCase();
		switch (ending) {
		case "xml":
			return CommentStyle.XML;
		case "java":
			return CommentStyle.JAVA;
		default:
			return CommentStyle.Unknown;
		}
	}

	private boolean hasLicence(final File file, LicenceTemplate template) {
		try {
			switch (mCommentStyle) {
			case JAVA:
				return hasLicenceJava(file.toPath(), template);
			default:
				return false;
			}
		} catch (IOException ex) {
			return false;
		}
	}

	private boolean hasLicenceJava(Path path, LicenceTemplate template)
			throws IOException {
		final Pattern pattern = Pattern.compile("/\\*|\\s*\\*\\s*");
		return Files.lines(path).sequential()
				.map(line -> pattern.matcher(line).replaceAll(""))
				.anyMatch(line -> template.isFirstLine(line));
	}

	public Collection<String> getAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return mFile.getAbsolutePath() + " -- "
				+ (mHasLicence ? "Licenced" : "Not licenced") + ", type="
				+ mCommentStyle;
	}

}
