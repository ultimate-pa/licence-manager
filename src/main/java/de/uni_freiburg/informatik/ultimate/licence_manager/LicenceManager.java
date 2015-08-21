/*
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2015 dietsch@informatik.uni-freiburg.de
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.exception.RuntimeIOException;
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

	public void writeDry() {
		consumeAll(getWriteConsumer(getLimitedPrintConsumer()));
	}

	public void deleteDry() {
		consumeAll(getDeleteConsumer(getLimitedPrintConsumer()));
	}

	public void delete() {
		consumeAll(getDeleteConsumer(getWriteConsumer()));
	}

	public void write() {
		consumeAll(getWriteConsumer(getWriteConsumer()));
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

	private Consumer<LicencedFile> getWriteConsumer(
			Consumer<FileAndStream> contentProcessor) {
		return fileToLicence -> {
			System.out.print(fileToLicence);
			if (!fileToLicence.needsWriting()) {
				System.out.println("..... will not be changed.");
				return;
			}
			System.out.println("..... licence will be changed/added.");
			contentProcessor.accept(new FileAndStream(fileToLicence.getFile(),
					fileToLicence.getNewContent()));
		};
	}

	private Consumer<LicencedFile> getDeleteConsumer(
			Consumer<FileAndStream> consumer) {
		return fileToLicence -> {
			System.out.print(fileToLicence);
			if (!fileToLicence.hasLicence()) {
				System.out.println("..... will not be changed.");
				return;
			}
			System.out.println("..... licence will be deleted.");
			consumer.accept(new FileAndStream(fileToLicence.getFile(),
					fileToLicence.getContentWithoutLicence()));
		};
	}

	private Consumer<FileAndStream> getLimitedPrintConsumer() {
		return fs -> {
			System.out.println("Result:");
			fs.Stream.limit(5).forEach(System.out::println);
			System.out.println("[...]");
		};
	}

	private Consumer<FileAndStream> getWriteConsumer() {
		return fs -> {
			try {
				Files.write(fs.File.toPath(),
						fs.Stream.collect(Collectors.toList()));
			} catch (IOException e) {
				throw new RuntimeIOException(e);
			}
		};
	}

	private static class FileAndStream {
		private Stream<String> Stream;
		private File File;

		private FileAndStream(File file, Stream<String> stream) {
			File = file;
			Stream = stream;
		}
	}
}
