package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author dietsch
 *
 */
public class DietschRenamer implements IAuthorRenamer {

	private static final String sUsedName = "Daniel Dietsch (dietsch@informatik.uni-freiburg.de)";

	private static final Set<String> sKnownNames = new HashSet<String>(
			Arrays.asList(new String[] {
					"Daniel Dietsch (dietsch@informatik.uni-freiburg.de)",
					"dietsch", "firefox", "dietsch@informatik.uni-freiburg.de" }));

	@Override
	public boolean shouldRename(Author author) {
		return sKnownNames.contains(author.Name)
				&& !author.Name.equals(sUsedName);
	}

	@Override
	public String newName(Author author) {
		return sUsedName;
	}

}
