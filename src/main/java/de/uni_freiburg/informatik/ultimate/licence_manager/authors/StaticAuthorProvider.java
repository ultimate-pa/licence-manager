package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.DateUtils;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class StaticAuthorProvider implements IAuthorProvider {

	@Override
	public List<Author> getAuthors(LicencedFile file,
			IFileTypeDependentOperation operation) {
		final List<Author> rtr = new ArrayList<Author>();
		rtr.add(new Author("University of Freiburg",
				DateUtils.getCurrentYear(), null));
		return rtr;
	}

	@Override
	public boolean isUsable(FileType fileType) {
		return fileType != FileType.UNKNOWN;
	}

}
