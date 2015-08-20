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

	public void delete() {
		consumeAll(getDryRunDeleteConsumer(5));
	}

	private void consumeAll(final Consumer<LicencedFile> consumer) {
		final Collection<File> allFiles = getAllFiles();
		final Collection<FileLicenser> licencers = getAllLicencers(allFiles,
				consumer);
		licencers.forEach(t -> t.consume());
	}

	private Collection<FileLicenser> getAllLicencers(
			final Collection<File> allFiles, final Consumer<LicencedFile> writer) {
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

	private Consumer<LicencedFile> getDryRunWriteConsumer(int limit) {
		return fileToLicence -> {
			System.out.print(fileToLicence);
			if (!fileToLicence.needsWriting()) {
				System.out.println("..... will not be changed.");
				return;
			}
			System.out.println("..... licence will be changed/added. Result:");
			if (limit > 0) {
				fileToLicence.getNewContent().limit(limit)
						.forEach(System.out::println);
			} else {
				fileToLicence.getNewContent().forEach(System.out::println);
			}
			System.out.println("[...]");
		};
	}

	private Consumer<LicencedFile> getDryRunDeleteConsumer(int limit) {
		return fileToLicence -> {
			System.out.print(fileToLicence);
			if (!fileToLicence.hasLicence()) {
				System.out.println("..... will not be changed.");
				return;
			}
			System.out.println("..... licence will be deleted. Result:");
			if (limit > 0) {
				fileToLicence.getContentWithoutLicence().limit(limit)
						.forEach(System.out::println);
			} else {
				fileToLicence.getContentWithoutLicence().forEach(System.out::println);
			}
			System.out.println("[...]");
		};
	}
}
