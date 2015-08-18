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
		final Collection<FileLicenser> licencers = getAllLicencers(allFiles);

		licencers.forEach(t -> t.writeFiles());

		/**
		 * 1. find licence templates and their directory 2. list all files at
		 * the same level or below that match the regexp (file ending) 3. mark
		 * whether those files already have a template 4. mark whether those
		 * files have an author
		 */
	}

	public void write() {
	}

	private Collection<FileLicenser> getAllLicencers(
			final Collection<File> allFiles) throws IOException {
		return FileUtils
				.getFilesRegex(mDirectory,
						new String[] { ".*" + mTemplateName }).stream()
				.map(f -> createFileLicencerSafe(f, allFiles))
				.collect(Collectors.toList());
	}

	private FileLicenser createFileLicencerSafe(final File f,
			final Collection<File> allFiles) {
		try {
			return new FileLicenser(f, allFiles);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<File> getAllFiles() {
		return FileUtils.getFiles(mDirectory, mFileendings);
	}
}
