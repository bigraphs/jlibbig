package it.uniud.mads.jlibbig.core.exceptions;

public class IncompatibleInterfaceException extends RuntimeException {

	private static final long serialVersionUID = 4039399498108629133L;

	public IncompatibleInterfaceException() {}
	
	public IncompatibleInterfaceException(String message) {
		super(message);
	}

	public IncompatibleInterfaceException(Throwable arg0) {
		super(arg0);
	}

	public IncompatibleInterfaceException(String message, Throwable arg1) {
		super(message, arg1);
	}

	public IncompatibleInterfaceException(String message,
			Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
	}

}
