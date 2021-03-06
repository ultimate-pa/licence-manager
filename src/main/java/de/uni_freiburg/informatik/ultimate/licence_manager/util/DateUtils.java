/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE licence-manager.
 * 
 * The ULTIMATE licence-manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE licence-manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE licence-manager. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE licence-manager, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE licence-manager grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.licence_manager.util;

import java.text.ParseException;
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

	public static String getYear(String date, String dateformat)
			throws ParseException {
		final SimpleDateFormat formatter = new SimpleDateFormat(dateformat);
		final SimpleDateFormat year = new SimpleDateFormat("yyyy");
		return year.format(formatter.parse(date));
	}

	public static String min(String a, String b) {
		if (a == null && b == null) {
			return null;
		}
		if (a != null && b == null) {
			return a;
		}
		if (b != null && a == null) {
			return b;
		}
		if (Integer.valueOf(a) < Integer.valueOf(b)) {
			return a;
		}
		return b;
	}

	public static String max(String a, String b) {
		if (a == null && b == null) {
			return null;
		}
		if (a != null && b == null) {
			return a;
		}
		if (b != null && a == null) {
			return b;
		}
		if (Integer.valueOf(a) > Integer.valueOf(b)) {
			return a;
		}
		return b;
	}

}
