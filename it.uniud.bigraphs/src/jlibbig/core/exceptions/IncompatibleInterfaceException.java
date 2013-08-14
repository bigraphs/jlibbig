package jlibbig.core.exceptions;

import jlibbig.core.AbstBigraph;

public class IncompatibleInterfaceException extends RuntimeException {

	private static final long serialVersionUID = 4039399498108629133L;

	protected AbstBigraph g;
	
	public IncompatibleInterfaceException(AbstBigraph g) {
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstBigraph g,String arg0) {
		super(arg0);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstBigraph g,Throwable arg0) {
		super(arg0);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstBigraph g,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.g=g;
	}

	public IncompatibleInterfaceException(AbstBigraph g,String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		this.g=g;
	}

	public AbstBigraph getGraph(){
		return this.g;
	}

}
