package it.uniud.mads.jlibbig.core.exceptions;

public class UnexpectedOwnerException extends IllegalArgumentException {

	public UnexpectedOwnerException() {
		super();
	}

	public UnexpectedOwnerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UnexpectedOwnerException(String arg0) {
		super(arg0);
	}

	public UnexpectedOwnerException(Throwable arg0) {
		super(arg0);
	}

	private static final long serialVersionUID = 8020463181723768714L;

}
