package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public interface IAuthorRenamer {
	
	public boolean shouldRename(Author author);
	
	public String newName(Author author);
	
}
