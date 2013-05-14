package jlibbig;

public class IncompatibleInterfaces extends RuntimeException {
	private static final long serialVersionUID = -6375339326720462453L;
	
	private GraphFace _face1, _face2;
	//FaceOrig _faceOrig;
	
	public IncompatibleInterfaces(GraphFace face1, GraphFace face2){
		this._face1 = face1;
		this._face2 = face2;
	}
	
	public IncompatibleInterfaces(String message, GraphFace face1, GraphFace face2){
		super(message);
		this._face1 = face1;
		this._face2 = face2;
	}
	/*
	IncompatibleInterfaces(String message, GraphFace face1, GraphFace face2, FaceOrig orig){
		super(message);
		this._face1 = face1;
		this._face2 = face2;
		this._faceOrig = orig;
	}
	*/
	
	GraphFace getFace1(){
		return _face1;
	}
	
	GraphFace getFace2(){
		return _face2;
	}

	@Override
	public String toString() {
		return "IncompatibleInterfaces: " + _face1 + " vs " + _face2;
	}
	
	/*
	enum FaceOrig{
		InnerInner,
		InnerOuter,
		OuterInner,
		OuterOuter
	}
	*/
}
