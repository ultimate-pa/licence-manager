package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public interface IAuthorProvider {

	List<Author> getAuthors(LicencedFile file,
			IFileTypeDependentOperation operation);

	boolean isUsable(FileType fileType);
}
