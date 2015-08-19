package de.uni_freiburg.informatik.ultimate.licence_manager.filetypes;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicenceTemplate;

public class UnknownOperations implements IFileTypeDependentOperation{

	@Override
	public boolean computeHasLicence(LicenceTemplate template) {
		return false;
	}

	@Override
	public FileType getFileType() {
		return FileType.Unknown;
	}

	@Override
	public String getFirstLine() {
		return "";
	}

	@Override
	public String getLastLine() {
		return "";
	}

	@Override
	public String getLicenceIndent() {
		return "";
	}

	@Override
	public String removeComments(String str) {
		return str;
	}
}
