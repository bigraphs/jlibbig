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
		sb.put(BigraphBuilder.makeControl("a",true,0));
		sb.put(BigraphBuilder.makeControl("b",true,1));
		sb.put(BigraphBuilder.makeControl("c",false,2));
		Signature<BigraphControl> s = sb.makeSignature();
		
		System.out.println("Signature: " + s);
		
		BigraphAbst b0 = Bigraph.makeMerge(s,0);
		Bigraph b1 = Bigraph.makeIon(s, s.getByName("a"),"s");
		Bigraph b2 = Bigraph.makeIon(s, s.getByName("b"),"u","z");
		Bigraph b3 = Bigraph.makeIon(s, s.getByName("c"),"v","x","y");
		
		//System.out.println("Bigraph: " + b0.getNodes()+ " " + b0.getInnerFace() + " -> " + b0.getOuterFace());
		
		b3.composeTo(Bigraph.makeMerge(s, 0));
		System.out.println("Bigraph: " + b3.getNodes() + " " + b3.getEdges() + " " +b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.juxtaposeTo(Bigraph.makeId(s, b2.getOuterFace()));
		System.out.println("Bigraph: " + b3.getNodes() + " " + b3.getEdges() + " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.composeTo(b2);
		System.out.println("Bigraph: " + b3.getNodes() + " " + b3.getEdges() + " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
		b3.composeTo(b1);
		System.out.println("Bigraph: " + b3.getNodes() + " " + b3.getEdges() + " " + b3.getInnerFace() + " -> " + b3.getOuterFace());
	}

}
