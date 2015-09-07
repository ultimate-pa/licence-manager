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
package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class LicenceAuthorProvider implements IAuthorProvider {

	@Override
	public List<Author> getAuthors(LicencedFile file,
			IFileTypeDependentOperation operation) {
		final List<Author> rtr = new ArrayList<Author>();
		final Iterator<String> iter = file.getCachedFileStream().getStream()
				.iterator();
		while (iter.hasNext()) {
			final String next = iter.next();

			Pattern pattern = Pattern
					.compile("Copyright \\(C\\) (\\d{4})\\s*-*\\s*(\\d{4})*\\s(.*)");
			Matcher matcher = pattern.matcher(operation.removeComments(next));
			if (matcher.matches()) {
				if (matcher.groupCount() > 2) {
					rtr.add(new Author(matcher.group(3), matcher.group(1),
							matcher.group(2)));
				} else {
					rtr.add(new Author(matcher.group(2), matcher.group(1), null));
				}
			}

			if (next.endsWith(operation.getLastLine())) {
				break;
			}
		}
		return rtr;
	}

	@Override
	public boolean isUsable(LicencedFile file, FileType fileType) {
		return fileType != FileType.UNKNOWN;
	}

}
