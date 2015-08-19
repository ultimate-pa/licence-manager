package de.uni_freiburg.informatik.ultimate.licence_manager.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.exception.RuntimeIOException;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class CachedFileStream {

	private final File mFile;
	private List<String> mFileContent;

	public CachedFileStream(File file) {
		mFile = file;
	}

	public List<String> getList() {
		if (mFileContent == null) {
			try {
				mFileContent = Files.lines(mFile.toPath()).sequential()
						.collect(Collectors.toList());
			} catch (IOException e) {
				throw new RuntimeIOException(e);
			}
		}
		return mFileContent;
	}

	public Stream<String> getStream() {
		return getList().stream();
	}

	public File getFile() {
		return mFile;
	}

}
