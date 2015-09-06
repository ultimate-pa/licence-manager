package de.uni_freiburg.informatik.ultimate.licence_manager.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class DateUtils {

	private static String sCurrentYear;

	public static String getCurrentYear() {
		if (sCurrentYear == null) {
			sCurrentYear = new SimpleDateFormat("yyyy").format(Calendar
					.getInstance().getTime());
		}
		return sCurrentYear;
	}

}
