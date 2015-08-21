/*
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.licence_manager.filetypes;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicenceTemplate;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class JavaOperations implements IFileTypeDependentOperation {

	private final Supplier<Stream<String>> mContentSupplier;
	private final Pattern mPatternComments;

	public JavaOperations(Supplier<Stream<String>> contentSupplier) {
		mContentSupplier = contentSupplier;
		mPatternComments = Pattern.compile("\\s*/*\\*\\s*/*");
	}

	@Override
	public boolean computeHasLicence(final LicenceTemplate template) {
		return mContentSupplier.get().map(this::removeComments)
				.anyMatch(line -> template.isFirstLine(line));
	}

	@Override
	public FileType getFileType() {
		return FileType.JAVA;
	}

	@Override
	public String getFirstLine() {
		return "/*";
	}

	@Override
	public String getLastLine() {
		return " */";
	}

	@Override
	public String getLicenceIndent() {
		return " * ";
	}

	@Override
	public String removeComments(String str) {
		return mPatternComments.matcher(str).replaceAll("");
	}
}
