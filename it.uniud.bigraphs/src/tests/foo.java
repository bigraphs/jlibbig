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
		
		BigraphBuilder bb1 = new BigraphBuilder(s);
		Root r = bb1.addRoot();
		Handle h = bb1.addOuterName("x");
		bb1.addInnerName("x", h);
		h = bb1.addInnerName("x",h).getHandle();
		Node n = bb1.addNode("c", r, h);
		bb1.addSite(r);
		//System.out.println(bb + "\n");
		Bigraph b1 = bb1.makeBigraph();
		//System.out.println(b1+ "\n");
		//printBig("B1",b1);
		bb1.outerCompose(b1);
		printBB("outerCompose",bb1);
		bb1.innerCompose(b1);
		printBB("innerCompose",bb1);
		
		BigraphBuilder bb2 = new BigraphBuilder(s);
		r = bb2.addRoot();
		h = bb2.addOuterName("x");
		n = bb2.addNode("b", r, h);
		bb2.addSite(n);
		Bigraph b2 = bb2.makeBigraph();
		
		bb1.outerNest(b2);
		printBB("outerNest",bb1);
		
		b1 = bb1.makeBigraph();
		bb2.innerNest(b1);
		printBB("innerNest",bb2);
		
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
