package jlibbig.core;

public class NameClashException extends RuntimeException {
	private static final long serialVersionUID = 7906403825825798881L;
	
	private final String name;
	
	public NameClashException(String message){
		this(message,null);
	}
		
	public NameClashException(String message, String name){
		super(message);
		this.name = name;
	}
		
	public String getName(){
		return this.name;
	}

	@Override
	public String toString() {
		if(name != null){
			return super.toString() + "\nClash over '" + name + "'";
		}else{
			return super.toString();
		}
	}
	
	
}
