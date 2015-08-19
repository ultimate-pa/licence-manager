package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.licence_manager.util.FileUtils;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public final class LicenceManager {

	private final File mDirectory;
	private final String[] mFileendings;
	private final String mTemplateName;

	public LicenceManager(String directory, String[] fileendings,
			String templatename) {
		mDirectory = new File(directory);
		mFileendings = fileendings;
		mTemplateName = templatename;
	}

	public void delete() throws IOException {
		final Collection<File> allFiles = getAllFiles();
		final Collection<FileLicenser> licencers = getAllLicencers(allFiles,
				fileToLicence -> {
					System.out.println(fileToLicence);
					fileToLicence.getNewContent().limit(5).forEach(System.out::println);
				});

		licencers.forEach(t -> t.writeFiles());
	}

	private Collection<FileLicenser> getAllLicencers(
			final Collection<File> allFiles, final Consumer<LicencedFile> writer)
			throws IOException {
		return FileUtils
				.getFilesRegex(mDirectory,
						new String[] { ".*" + mTemplateName }).stream()
				.map(f -> createFileLicencerSafe(f, allFiles, writer))
				.collect(Collectors.toList());
	}

	private FileLicenser createFileLicencerSafe(final File f,
			final Collection<File> allFiles, Consumer<LicencedFile> writer) {
		try {
			return new FileLicenser(f, allFiles, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<File> getAllFiles() {
		return FileUtils.getFiles(mDirectory, mFileendings);
	}
}
