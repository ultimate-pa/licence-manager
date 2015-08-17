package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class FileUtils {

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
	public static String getPathFromHere(String path) {
		File here = new File(System.getProperty("user.dir"));
		File relative = new File(here.getAbsolutePath() + File.separator + path);
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
	public static Collection<File> filterFiles(Collection<File> files,
			String regex) {
		ArrayList<File> singleFiles = new ArrayList<File>();

		for (File f : files) {
			String path = f.getAbsolutePath();
			if (path.matches(regex)) {
				singleFiles.add(f);
			}
		}

		return singleFiles;
	}

	public static List<File> getFiles(File root, String[] endings) {
		ArrayList<File> rtr = new ArrayList<File>();

		if (root.isFile()) {
			for (String s : endings) {
				if (root.getAbsolutePath().endsWith(s)) {
					rtr.add(root);
					break;
				}
			}
			return rtr;
		}

		File[] list = root.listFiles();

		if (list == null) {
			return rtr;
		}

		for (File f : list) {
			if (f.isDirectory()) {
				rtr.addAll(getFiles(f, endings));
			} else {

				if (endings == null || endings.length == 0) {
					rtr.add(f);
				} else {
					for (String s : endings) {
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
	public static Collection<File> getFilesRegex(File root, String[] regex) {
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
	private static Collection<File> getFilesRegex(String prefix, File root,
			String[] regex) {
		if (!root.getAbsolutePath().startsWith(prefix)) {
			throw new IllegalArgumentException(
					"prefix is no prefix of root.getAbsolutePath()");
		}
		ArrayList<File> rtr = new ArrayList<File>();

		if (root.isFile()) {
			rtr.add(root);
			return rtr;
		}

		File[] list = root.listFiles();

		if (list == null) {
			return rtr;
		}

		for (File f : list) {
			if (f.isDirectory()) {
				rtr.addAll(getFilesRegex(prefix, f, regex));
			} else {

				if (regex == null || regex.length == 0) {
					rtr.add(f);
				} else {
					for (String s : regex) {
						String suffix = f.getAbsolutePath().substring(
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

	public static <E> Collection<E> uniformN(Collection<E> collection, int n) {
		ArrayList<E> rtr = new ArrayList<E>(n);
		int i = 1;
		int size = collection.size();
		int step = 1;
		if (n != 0) {
			step = (int) Math.floor(((double) size) / ((double) n));
		}

		for (E elem : collection) {
			if (i % step == 0) {
				rtr.add(elem);
			}
			++i;
		}
		return rtr;
	}
}
