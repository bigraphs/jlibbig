package jlibbig.core.exceptions;

import jlibbig.core.AbstractBigraph;

public class IncompatibleInterfacesException extends RuntimeException {

	private static final long serialVersionUID = 4039399498108629133L;

	protected AbstractBigraph g1, g2;
	
	public IncompatibleInterfacesException(AbstractBigraph g1, AbstractBigraph g2) {
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstractBigraph g1, AbstractBigraph g2,String arg0) {
		super(arg0);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstractBigraph g1, AbstractBigraph g2,Throwable arg0) {
		super(arg0);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstractBigraph g1, AbstractBigraph g2,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.g1=g1;
		this.g2=g2;
	}

	public IncompatibleInterfacesException(AbstractBigraph g1, AbstractBigraph g2,String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		this.g1=g1;
		this.g2=g2;
	}

	public AbstractBigraph getGraph1(){
		return this.g1;
	}


	public AbstractBigraph getGraph2(){
		return this.g2;
	}

}
