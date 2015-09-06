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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public final class FileUtils {

	/**
	 * Combines a relative path with the base directory of this plugin, i.e. you
	 * can say getPathFromHere("../../examples/settings/") to reach the setting
	 * directory
	 * 
	 * @param path
	 *            A string representing a relative path. Please use "/" as path
	 *            separator regardless of OS. Java will recognize \\, but this
	 *            wont work under Linux
	 * @return A string representing the absolute path to the relative path
	 *         based on the actual position of this package
	 */
	public static String getPathFromHere(final String path) {
		final File here = new File(System.getProperty("user.dir"));
		final File relative = new File(here.getAbsolutePath() + File.separator
				+ path);
		return relative.getAbsolutePath();
	}

	/***
	 * Filters a list of files based on a given regex. Returns a collection of
	 * files of which the path matches the regex.
	 * 
	 * @param files
	 * @param regex
	 * @return
	 */
	public static Collection<File> filterFiles(final Collection<File> files,
			final String regex) {
		final ArrayList<File> singleFiles = new ArrayList<File>();

		for (final File f : files) {
			final String path = f.getAbsolutePath();
			if (path.matches(regex)) {
				singleFiles.add(f);
			}
		}

		return singleFiles;
	}

	public static Collection<File> getFiles(final File root,
			final String[] endings) {
		final ArrayList<File> rtr = new ArrayList<File>();

		if (root.isFile()) {
			for (final String s : endings) {
				if (root.getAbsolutePath().endsWith(s)) {
					rtr.add(root);
					break;
				}
			}
			return rtr;
		}

		final File[] list = root.listFiles();

		if (list == null) {
			return rtr;
		}

		for (final File f : list) {
			if (f.isDirectory()) {
				rtr.addAll(getFiles(f, endings));
			} else {
				if (endings == null || endings.length == 0) {
					rtr.add(f);
				} else {
					for (final String s : endings) {
						if (f.getAbsolutePath().endsWith(s)) {
							rtr.add(f);
							break;
						}
					}
				}
			}
		}
		return rtr;
	}

	/**
	 * Returns recursively all files in a directory that have a path whose
	 * suffix beyond root is matched by regex. If root is a file, a collection
	 * containing root is returned (ignoring the regex) E.g., your file root has
	 * the absolute path /home/horst/ultimate/ and your regex is *horst* you
	 * obtain the files that contain the String "horst" if the prefix
	 * "/home/horst/ultimate/" was removed.
	 * 
	 * @param root
	 * @param regex
	 * @return
	 */
	public static Collection<File> getFilesRegex(final File root,
			final String[] regex) {
		return getFilesRegex(root.getAbsolutePath(), root, regex);
	}

	/**
	 * Returns recursively all files in a directory that have a path whose
	 * suffix beyond the String prefix is matched by regex. If root is a file, a
	 * collection containing root is returned (ignoring the regex).
	 * 
	 * @param root
	 * @param regex
	 * @return
	 */
	private static Collection<File> getFilesRegex(final String prefix,
			final File root, final String[] regex) {
		if (!root.getAbsolutePath().startsWith(prefix)) {
			throw new IllegalArgumentException(
					"prefix is no prefix of root.getAbsolutePath()");
		}
		final ArrayList<File> rtr = new ArrayList<File>();

		if (root.isFile()) {
			rtr.add(root);
			return rtr;
		}

		final File[] list = root.listFiles();

		if (list == null) {
			return rtr;
		}

		for (final File f : list) {
			if (f.isDirectory()) {
				rtr.addAll(getFilesRegex(prefix, f, regex));
			} else {
				if (regex == null || regex.length == 0) {
					rtr.add(f);
				} else {
					for (final String s : regex) {
						final String suffix = f.getAbsolutePath().substring(
								prefix.length());
						if (suffix.matches(s)) {
							rtr.add(f);
							break;
						}
					}
				}
			}
		}
		return rtr;
	}

	public static <E> Collection<E> uniformN(final Collection<E> collection,
			final int n) {
		final ArrayList<E> rtr = new ArrayList<E>(n);
		final int size = collection.size();

		int i = 1;
		int step = 1;
		if (n != 0) {
			step = (int) Math.floor(((double) size) / ((double) n));
		}

		for (final E elem : collection) {
			if (i % step == 0) {
				rtr.add(elem);
			}
			++i;
		}
		return rtr;
	}

	/**
	 * Load a text file contents as a <code>String<code>.
	 * This method does not perform enconding conversions
	 *
	 * @param file
	 *            The input file
	 * @return The file contents as a <code>String</code>
	 * @exception IOException
	 *                IO Error
	 */
	public static String readFileAsString(final File file) throws IOException {
		int len;
		final char[] chr = new char[4096];
		final StringBuffer buffer = new StringBuffer();
		final FileReader reader = new FileReader(file);
		try {
			while ((len = reader.read(chr)) > 0) {
				buffer.append(chr, 0, len);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	public static <T> boolean streamEquals(Stream<T> a, Stream<T> b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}

		final Iterator<T> iterA = a.iterator();
		final Iterator<T> iterB = b.iterator();

		while (iterA.hasNext() && iterB.hasNext()) {
			final T nextA = iterA.next();
			final T nextB = iterB.next();

			if (nextA != null && nextB != null) {
				if (!nextA.equals(nextB)) {
					return false;
				}

			} else if (nextA == null && nextB == null) {
				continue;
			} else {
				return false;
			}
		}

		if (iterA.hasNext() || iterB.hasNext()) {
			return false;
		}

		return true;
	}

}
