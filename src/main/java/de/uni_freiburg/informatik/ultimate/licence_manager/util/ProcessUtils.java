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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class ProcessUtils {

	public static void inheritIO(final InputStream src, final PrintStream dest) {
		new Thread(new Runnable() {
			public void run() {
				final Scanner sc = new Scanner(src);
				while (sc.hasNextLine()) {
					dest.println(sc.nextLine());
				}
				sc.close();
			}
		}).start();
	}

	public static StreamGobbler attachGobbler(final InputStream src) {
		final StreamGobbler gobbler = new StreamGobbler(src);
		gobbler.start();
		return gobbler;
	}

	public static class StreamGobbler extends Thread {
		private InputStream mInput;
		private byte[] mOutput;

		private StreamGobbler(final InputStream is) {
			mInput = is;
		}

		public InputStream getFreshStream() {
			return new ByteArrayInputStream(mOutput);
		}

		@Override
		public void run() {
			try {
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				IOUtils.copy(mInput, output);
				mOutput = output.toByteArray();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
