/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
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
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class FileLicenser {

	private final LicenceTemplate mTemplate;
	private final Collection<LicencedFile> mFiles;
	private final Consumer<LicencedFile> mConsumer;

	public FileLicenser(final File template, final Collection<File> allFiles,
			final Consumer<LicencedFile> consumer) throws IOException {
		mTemplate = new LicenceTemplate(new CachedFileStream(template));
		mFiles = getLicencedFiles(template, allFiles);
		mConsumer = consumer;
	}

	public void consume() {
		for (final LicencedFile file : mFiles) {
			mConsumer.accept(file);
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
		case UNKNOWN:
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
			return FileType.UNKNOWN;
		}
		final String ending = splitName[splitName.length - 1].toLowerCase();
		switch (ending) {
		case "xml":
			return FileType.XML;
		case "java":
			return FileType.JAVA;
		default:
			return FileType.UNKNOWN;
		}
	}
}
