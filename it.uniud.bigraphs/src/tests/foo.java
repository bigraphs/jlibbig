package tests;

import jlibbig.core.*;

@SuppressWarnings("unused")
public class foo {
	public static void main(String[] args){
				
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
		bb2.addOuterName("y");
		bb2.addSite(n);
		Bigraph b2 = bb2.makeBigraph();
		
		bb1.outerNest(b2);
		printBB("outerNest",bb1);
		
		b1 = bb1.makeBigraph();
		bb2.innerNest(b1);
		printBB("innerNest",bb2);

		bb2.ground();
		printBB("ground",bb2);
		b2 = bb2.makeBigraph();

		for(Match<Bigraph> m : BigraphMatcher.DEFAULT.match(b2, b1)){
			String sp = "------";
			System.out.println("BEGIN MATCH:");
			System.out.println("--- ctx ---" + sp);
			System.out.println(m.getContext());
			System.out.println("--- rdx ---" + sp);
			System.out.println(m.getRedex());
			int i = 0;
			for (Bigraph prm : m.getParams()) {
				System.out.println("--- PRM "+ i + " ---" + sp.substring(2+(int) Math.floor(Math.log10(i+1))));
				System.out.println(prm);
				i++;
			}
			System.out.println("END MATCH");
		}
		
		
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
