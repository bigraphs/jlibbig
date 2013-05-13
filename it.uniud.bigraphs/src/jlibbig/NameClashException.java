package jlibbig;

public class NameClashException extends RuntimeException {
	private static final long serialVersionUID = 7906403825825798881L;
	
	public NameClashException(String message){
		super(message);
	}
}
