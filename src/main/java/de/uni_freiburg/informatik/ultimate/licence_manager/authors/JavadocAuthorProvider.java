package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.FileType;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class JavadocAuthorProvider implements IAuthorProvider {

	@Override
	public List<Author> getAuthors(LicencedFile file,
			IFileTypeDependentOperation operation) {
		final Pattern pattern = Pattern.compile("\\W*@author\\s(.+)");
		return file.getCachedFileStream().getStream().map((line) -> {
			final Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				return matcher.group(1).trim();
			}
			return null;
		}).filter(s -> s != null).map(s -> s.split("@author"))
				.flatMap(Arrays::stream).map(s -> s.trim())
				.map(str -> new Author(str, null, null))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isUsable(FileType fileType) {
		return fileType == FileType.JAVA;
	}
}
