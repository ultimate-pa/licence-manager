package de.uni_freiburg.informatik.ultimate.licence_manager.filetypes;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicenceTemplate;

public class XmlOperations implements IFileTypeDependentOperation{

	private final Supplier<Stream<String>> mContentSupplier;
	private final Pattern mPatternComments;

	public XmlOperations(Supplier<Stream<String>> contentSupplier) {
		mContentSupplier = contentSupplier;
		mPatternComments = Pattern.compile("\\s*<!--\\s*|\\s*--!>\\s*");
	}

	@Override
	public boolean computeHasLicence(LicenceTemplate template) {
		return mContentSupplier.get().map(this::removeComments)
				.anyMatch(line -> template.isFirstLine(line));
	}

	@Override
	public FileType getFileType() {
		return FileType.XML;
	}

	@Override
	public String getFirstLine() {
		return "<!--";
	}

	@Override
	public String getLastLine() {
		return "-->";
	}

	@Override
	public String getLicenceIndent() {
		return "    ";
	}

	@Override
	public String removeComments(String str) {
		return mPatternComments.matcher(str).replaceAll("");
	}
}
