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
	public boolean isUsable(FileType fileType) {
		return fileType != FileType.UNKNOWN;
	}

}
