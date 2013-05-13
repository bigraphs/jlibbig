package jlibbig;

public class IncompatibleBigraphFaces extends IncompatibleInterfaces {
	private static final long serialVersionUID = -7336147920197064911L;
	
	private BigraphFace _face1,_face2;
	
	IncompatibleBigraphFaces(BigraphFace face1, BigraphFace face2){
		super(face1,face2);
		this._face1 = face1;
		this._face2 = face2;
	}
	
	BigraphFace getFace1(){
		return _face1;
	}
	
	BigraphFace getFace2(){
		return _face2;
	}

}
