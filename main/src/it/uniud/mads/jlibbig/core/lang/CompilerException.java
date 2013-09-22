package it.uniud.mads.jlibbig.core.lang;

public class CompilerException extends RuntimeException {
	public CompilerException() {
		super();
	}

	public CompilerException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public CompilerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CompilerException(String arg0) {
		super(arg0);
	}

	public CompilerException(Throwable arg0) {
		super(arg0);
	}

	private static final long serialVersionUID = -4391309562727925569L;

}
