package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.LicencedFile;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class FileLicenser {

	private final LicenceTemplate mTemplate;
	private final Collection<LicencedFile> mFiles;
	private final Consumer<LicencedFile> mWriter;

	public FileLicenser(final File template, final Collection<File> allFiles,
			final Consumer<LicencedFile> writer) throws IOException {
		mTemplate = new LicenceTemplate(template);
		mFiles = getLicencedFiles(template, allFiles);
		mWriter = writer;
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
			if (file.needsWriting()) {
				mWriter.accept(file);
			}
		}
	}
}
