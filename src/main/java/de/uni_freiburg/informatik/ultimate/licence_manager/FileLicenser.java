package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.JavaOperations;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.UnknownOperations;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.XmlOperations;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.CachedFileStream;

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
		mTemplate = new LicenceTemplate(new CachedFileStream(template));
		mFiles = getLicencedFiles(template, allFiles);
		mWriter = writer;
	}

	public void writeFiles() {
		for (final LicencedFile file : mFiles) {
			if (file.needsWriting()) {
				mWriter.accept(file);
			}
		}
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
				.map(f -> createLicencedFile(f, mTemplate))
				.collect(Collectors.toList());
	}

	private LicencedFile createLicencedFile(final File file,
			final LicenceTemplate template) {
		final FileType fileType = getFileType(file);
		final IFileTypeDependentOperation operation;
		final CachedFileStream fstream = new CachedFileStream(file);
		switch (fileType) {
		case JAVA:
			operation = new JavaOperations(() -> fstream.getStream());
			break;
		case Unknown:
			operation = new UnknownOperations();
			break;
		case XML:
			operation = new XmlOperations(() -> fstream.getStream());
			break;
		default:
			throw new UnsupportedOperationException("Implement missing cases");
		}
		return new LicencedFile(fstream, mTemplate, operation);
	}

	private FileType getFileType(File file) {
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
}
