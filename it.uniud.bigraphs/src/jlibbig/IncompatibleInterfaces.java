package jlibbig;

public class IncompatibleInterfaces extends RuntimeException {
	private static final long serialVersionUID = -6375339326720462453L;
	
	private GraphFace _face1, _face2; 
	
	public IncompatibleInterfaces(GraphFace face1, GraphFace face2){
		this._face1 = face1;
		this._face2 = face2;
	}
	
	public IncompatibleInterfaces(String message, GraphFace face1, GraphFace face2){
		super(message);
		this._face1 = face1;
		this._face2 = face2;
	}
	
	GraphFace getFace1(){
		return _face1;
	}
	
	GraphFace getFace2(){
		return _face2;
	}
}
