package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class FileLicenser {

	private final LicenceTemplate mTemplate;
	private final Collection<LicencedFile> mFiles;

	public FileLicenser(final File template, final Collection<File> allFiles)
			throws IOException {
		mTemplate = new LicenceTemplate(template);
		mFiles = getLicencedFiles(template, allFiles);
	}

	private Collection<LicencedFile> getLicencedFiles(final File template,
			final Collection<File> allFiles) {
		if (allFiles == null) {
			return null;
		}
		return allFiles
				.stream()
				.filter(f -> f.getAbsolutePath().startsWith(
						template.getParent()))
				.map(f -> new LicencedFile(f, mTemplate))
				.collect(Collectors.toList());
	}

	public void writeFiles() {
		for (final LicencedFile file : mFiles) {
			writeFile(file);
		}
	}

	private void writeFile(LicencedFile file) {
		final Collection<String> authors = file.getAuthors();

		System.out.println(file);
	}
}
