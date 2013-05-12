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
				
		Bigraph b0 = printBig(Bigraph.makeMerge(s,0)); // 0->1
		Bigraph b1 = printBig(Bigraph.makeIon(s, s.getByName("a"),"s"));
		Bigraph b2 = printBig(Bigraph.makeIon(s, s.getByName("b"),"u","z"));
		Bigraph b3 = printBig(Bigraph.makeIon(s, s.getByName("c"),"v","x","y")); // 1->1
		
		//System.out.println("Bigraph: " + b0.getNodes()+ " " + b0.getInnerFace() + " -> " + b0.getOuterFace());
						
		BigraphBuilder bb = new BigraphBuilder(b3);
		
		bb.leftJuxtapose(Bigraph.makeId(s, b2.getOuterFace()));
		bb.innerCompose(b2);
		bb.innerCompose(b1);
		bb.innerCompose(b0);
		
		Bigraph b = bb.makeBigraph();
		
		System.out.println("Bigraph: " + b.getNodes() + " " + b.getEdges() + " " + b.getInnerFace() + " -> " + b.getOuterFace());
	}
	
	private static Bigraph printBig(Bigraph b){
		System.out.println("Bigraph: " + b.getNodes() + " " + b.getEdges() + " " + b.getInnerFace() + " -> " + b.getOuterFace());
		return b;
	}

}
