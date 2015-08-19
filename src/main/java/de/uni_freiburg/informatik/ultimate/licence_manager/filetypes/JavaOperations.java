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
