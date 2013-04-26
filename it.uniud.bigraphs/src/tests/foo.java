package tests;

import java.util.*;
import jlibbig.*;

@SuppressWarnings("unused")
public class foo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SignatureBuilder<BigraphControl> sb = new SignatureBuilder<>();
		sb.put(BigraphBuilder.makeControl("a",0));
		sb.put(BigraphBuilder.makeControl("b",1));
		sb.put(BigraphBuilder.makeControl("c",2));
		Signature<BigraphControl> s = sb.makeSignature();
		
		System.out.println("Signature: " + s);
		
		Bigraph b0 = Bigraph.makeMerge(s,0);
		Bigraph b1 = Bigraph.makeIon(s, s.getByName("a"),"s");
		Bigraph b2 = Bigraph.makeIon(s, s.getByName("b"),"u","z");
		Bigraph b3 = Bigraph.makeIon(s, s.getByName("c"),"v","x","y");
		
		//System.out.println("Bigraph: " + b0.getNodes()+ " " + b0.getInnerFace() + " -> " + b0.getOuterFace());
		
		b3.compose(Bigraph.makeMerge(s, 0));
		System.out.println("Bigraph: " + b3.getNodes()+ " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.juxtapose(Bigraph.makeId(s, b2.getOuterFace()));
		System.out.println("Bigraph: " + b3.getNodes()+ " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.compose(b2);
		System.out.println("Bigraph: " + b3.getNodes()+ " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.compose(b1);
		System.out.println("Bigraph: " + b3.getNodes()+ " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
	}

}
