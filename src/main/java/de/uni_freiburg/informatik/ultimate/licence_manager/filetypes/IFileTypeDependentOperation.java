package de.uni_freiburg.informatik.ultimate.licence_manager.filetypes;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicenceTemplate;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public interface IFileTypeDependentOperation {
	boolean computeHasLicence(final LicenceTemplate template);
	
	FileType getFileType();
	
	String getFirstLine();
	
	String getLastLine();
	
	String getLicenceIndent();

	String removeComments(String str);
}
