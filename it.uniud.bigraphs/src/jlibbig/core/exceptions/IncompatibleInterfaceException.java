package jlibbig.core.exceptions;

import jlibbig.core.AbstractBigraph;

public class IncompatibleInterfaceException extends RuntimeException {

	private static final long serialVersionUID = 4039399498108629133L;

	protected AbstractBigraph g;
	
	public IncompatibleInterfaceException(AbstractBigraph g) {
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstractBigraph g,String arg0) {
		super(arg0);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstractBigraph g,Throwable arg0) {
		super(arg0);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstractBigraph g,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstractBigraph g,String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		this.g=g;
	}

	public AbstractBigraph getGraph(){
		return this.g;
	}

}
