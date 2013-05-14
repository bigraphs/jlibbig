package tests;

import java.util.*;
import jlibbig.*;

@SuppressWarnings("unused")
public class foo {
	/**
	 * @param args
	 * @throws NameClashException 
	 * @throws IncompatibleSignatureException 
	 */
	public static void main(String[] args) throws IncompatibleSignatureException, NameClashException {
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
						
		BigraphBuilder bb = new BigraphBuilder(b1);
		printBB(bb);
		bb.outerCompose(b3);
		printBB(bb);
		bb.leftJuxtapose(Bigraph.makeId(s,0,b2.getOuterFace().getNames()));
		printBB(bb);
		bb.innerCompose(b2);
		printBB(bb);
		bb.innerCompose(b0);
		printBB(bb);
		
		printBig(bb.makeBigraph());
		
	}
	private static BigraphBuilder printBB(BigraphBuilder b){
		return printBB("Builder",b);
	}
	private static BigraphBuilder printBB(String prefix, BigraphBuilder b){
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges() + " " + b.getInnerFace() + " -> " + b.getOuterFace());
		return b;
	}
	
	private static Bigraph printBig(Bigraph b){
		return printBig("Bigraph", b);
	}

	private static Bigraph printBig(String prefix, Bigraph b){
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges() + " " + b.getInnerFace() + " -> " + b.getOuterFace());
		return b;
	}
	
}
