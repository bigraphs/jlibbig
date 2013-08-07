package jlibbig.core.exceptions;

import jlibbig.core.AbstBigraph;

public class IncompatibleInterfacesException extends RuntimeException {

	private static final long serialVersionUID = 4039399498108629133L;

	protected AbstBigraph g1, g2;
	
	public IncompatibleInterfacesException(AbstBigraph g1, AbstBigraph g2) {
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstBigraph g1, AbstBigraph g2,String arg0) {
		super(arg0);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstBigraph g1, AbstBigraph g2,Throwable arg0) {
		super(arg0);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstBigraph g1, AbstBigraph g2,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstBigraph g1, AbstBigraph g2,String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		this.g1=g1;
		this.g2=g2;
	}

	public AbstBigraph getGraph1(){
		return this.g1;
	}


	public AbstBigraph getGraph2(){
		return this.g2;
	}

}
