package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {

	public RuntimeIOException(IOException e) {
		super(e);
	}

	private static final long serialVersionUID = -5363924549604636030L;

}
