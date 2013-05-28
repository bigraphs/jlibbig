package tests;

import java.util.*;

import jlibbig.core.*;

@SuppressWarnings("unused")
public class foo {
	/**
	 * @param args
	 * @throws NameClashException 
	 * @throws IncompatibleSignatureException 
	 */
	public static void main(String[] args) throws IncompatibleSignatureException, NameClashException {
				
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("a",true,0);
		sb.put("b",true,1);
		sb.put("c",false,2);
		Signature s = sb.makeSignature();
		
		System.out.println("Signature: " + s);
		
		BigraphBuilder bb = new BigraphBuilder(s);
		bb.addSite(bb.addNode("b", bb.addRoot()));
		
		Bigraph b = bb.makeBigraph();
		bb.innerCompose(b);
		bb.outerCompose(b);
		printBig(b);
		
		Handle h = bb.addOuterName("y");
		Point p = bb.addInnerName("x", h);
		printBB(bb);
		Node n = bb.addNode("b",bb.getRoots().get(0), h);
		bb.relink(n.getPort(0), p);
		printBB(bb);
		bb.relink(n.getPort(0), h);
		bb.relink(p, h);
		printBB(bb);
		bb.leftJuxtapose(b);
		bb.rightMergeProduct(b);
		printBB(bb);
		printBig(bb.makeBigraph());
		
		bb = new BigraphBuilder(s);
		bb.addSite(bb.addRoot());
		bb.addInnerName("x", bb.addOuterName("x"));
		printBig(bb.makeBigraph());
		
		/*
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
		*/
		
	}
	
	private static BigraphBuilder printBB(BigraphBuilder b){
		return printBB("Builder",b);
	}
	private static BigraphBuilder printBB(String prefix, BigraphBuilder b){
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges() + " <" + b.getSites().size() + "," + b.getInnerNames() + "> -> <" +b.getRoots().size() + "," + b.getOuterNames() + ">");
		return b;
	}
	
	private static Bigraph printBig(Bigraph b){
		return printBig("Bigraph", b);
	}

	private static Bigraph printBig(String prefix, Bigraph b){
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges() + " <" + b.getSites().size() + "," + b.getInnerNames() + "> -> <" +b.getRoots().size() + "," + b.getOuterNames() + ">");
		return b;
	}
	
	
}
