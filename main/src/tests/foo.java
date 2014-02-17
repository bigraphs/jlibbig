package tests;

import java.util.*;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.attachedProperties.*;
import it.uniud.mads.jlibbig.core.std.*;
import it.uniud.mads.jlibbig.core.Owner;

@SuppressWarnings("unused")
public class foo {
	public static void main(String[] args) {
		test5();
	}
	
	private static void test12(){
		int MAXNUMDHCPIP = 0;
		int MAXNUMPORTS = 0;
		
		SignatureBuilder sb = new SignatureBuilder();
		sb.add( "router" , true , 0);
		sb.add( "host" , true , 0 );
		sb.add( "dhcp" , true , 0 );
		sb.add( "if" , true , 1 );
		sb.add( "ip" , true , 1 );
		sb.add( "udp" , true , 1 );

				
		Signature signature = sb.makeSignature();
		
		//CONOSCENZA INIZIALE
		BigraphBuilder abb = new BigraphBuilder( signature );
		Root dominio = abb.addRoot();
		Root r1 = abb.addRoot();
		Node router = abb.addNode("router", dominio);
		Node dhcp = abb.addNode( "dhcp" , router );
		Node if0 = abb.addNode( "if", router );
		abb.addNode( "if" , router );
		Node ip = abb.addNode( "ip" , if0 );
		for(int i = 0; i<MAXNUMDHCPIP ; ++i )
			abb.addNode("ip" , dhcp );
		for(int i = 0; i<MAXNUMPORTS ; ++i ){
			abb.addNode("udp" , ip );
		}
		abb.addNode( "ip" , abb.addNode( "if" , abb.addNode( "router", r1 ) ) ); //ROUTER ESTERNO
		
		//AGGIUNGO HOST
		for(int j = 0; j < 2; ++j ){
		Node host = abb.addNode("host", dominio);
		Node ifhost = abb.addNode( "if" , host , if0.getPort(0).getHandle());
		Node iphost = abb.addNode( "ip", ifhost, ip.getPort(0).getHandle());
		for(int i = 0; i<MAXNUMPORTS ; ++i ){
			abb.addNode("udp" , iphost );
		}
		}
		//REDEX: COLLEGO A RETE ESTERNA
		BigraphBuilder redex = new BigraphBuilder( signature );
		Root regione0 = redex.addRoot();
		Root regione1 = redex.addRoot();
		Node router0 = redex.addNode("router", regione0 );
		redex.addSite( regione0 );
		Node interfaccia0 = redex.addNode( "if" , router0 );
		Node iprouter0 =  redex.addNode( "ip" , interfaccia0 );
		redex.addSite( iprouter0 );
		Node interfaccia1 = redex.addNode( "if" , router0 );
		redex.addInnerName( "a" , iprouter0.getPort(0).getHandle() );
		redex.addInnerName( "b" , interfaccia0.getPort(0).getHandle() );
		redex.addSite( router0 );
		redex.addNode( "ip" , redex.addNode( "if" , regione1 ));
		
		//REACTUM: COLLEGO A RETE ESTERNA
		BigraphBuilder reactum = new BigraphBuilder( signature );
		Root rr0 = reactum.addRoot();
		Root rr1 = reactum.addRoot();
		Node rrouter = reactum.addNode("router", rr0 );
		reactum.addSite( rr0 );
		Node rif0 = reactum.addNode( "if" , rrouter );
		Node rip =  reactum.addNode( "ip" , rif0 );
		reactum.addSite( rip );
		Node rif1 = reactum.addNode( "if" , rrouter );
		Node rip2 = reactum.addNode( "ip" , rif1 , rip.getPort(0).getHandle() );
		reactum.addInnerName( "a" , rip.getPort(0).getHandle() );
		reactum.addInnerName( "b" , rif0.getPort(0).getHandle() );
		reactum.addSite( rrouter );
		Node rexif = reactum.addNode( "if" , rr1 , rif1.getPort(0).getHandle() );
		Node rexip = reactum.addNode( "ip" , rexif , rip.getPort(0).getHandle() );
		for(int i = 1; i<=MAXNUMPORTS ; ++i ){
			reactum.addNode("udp" , rip2 );
			reactum.addNode("udp" , rexip );
		}
		
		Matcher matcher = new WeightedMatcher(){
			protected int matchingWeight(Bigraph agent, Node agentNode,
					Bigraph redex, Node redexNode) {
				return 1;
			}
		};
		for(Bigraph b :(new RewritingRule(matcher, redex.makeBigraph(), reactum.makeBigraph() , 0, 1 , 2)).apply( abb.makeBigraph())){
			System.out.println("RISULTATO");
			System.out.println(b);
		}	
		System.out.println("fine");
	}
	
	private static void test11(){
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("lan",true,1);
		sb.add("ip",false,1);

		Signature s = sb.makeSignature("MySig");
		
		//BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		Root r = bb.addRoot();
		Node lan = bb.addNode( "lan" , r );
		bb.addNode( "ip" , r , lan.getPort(0).getHandle() );
		
		//MATCH
		BigraphBuilder redex = new BigraphBuilder(s);
		Root rr = redex.addRoot();
		Node rlan = redex.addNode( "lan" , rr , redex.addOuterName("a") );
		Node rip = redex.addNode( "ip" , rr , redex.addOuterName("b"));
		
		for(Match m : Matcher.DEFAULT.match( bb.makeBigraph(true) , redex.makeBigraph(true))){
			System.out.println(m);
		}
	}
	
	private static void test10(){
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("lan",true,1);
		sb.add("ip",false,1);

		Signature s = sb.makeSignature("MySig");
		
		//BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		Root r = bb.addRoot();
		Node lan = bb.addNode( "lan" , r );
		bb.addNode( "ip" , r , lan.getPort(0).getHandle() );
		
		//MATCH
		BigraphBuilder m = new BigraphBuilder(s);
		Root rr = m.addRoot();
		Node rlan = m.addNode( "lan" , rr );
		m.addSite( rr );
		m.addInnerName( "a" , rlan.getPort(0).getHandle() );

		for(Bigraph b :(new RewritingRule( m.makeBigraph(), m.makeBigraph() , 0)).apply( bb.makeBigraph())){
			System.out.println(b);
		}
	}
	
	private static void test9(){
		SignatureBuilder sb = new SignatureBuilder();
		sb.add( "if" , true , 1 );
		Signature signature = sb.makeSignature();
		
		//BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(signature);
		Root r0 = bb.addRoot();
		OuterName a = bb.addOuterName("a");
		Node bif1 = bb.addNode( "if" , r0 ,a );
		Node bif2 = bb.addNode("if" , r0 , a);
		
		//REDEX
		BigraphBuilder redex = new BigraphBuilder(signature);
		redex.addSite( redex.addNode( "if" , redex.addRoot() , redex.addOuterName("a") ) );
				
		for(Match m : Matcher.DEFAULT.match( bb.makeBigraph(true) , redex.makeBigraph(true))){
			System.out.println(m);
		}
	}

	private static void test8(){
		SignatureBuilder sb = new SignatureBuilder();
		sb.add( "dominio" , true , 0);
		sb.add( "router" , true , 0);
		sb.add( "host" , true , 0 );
		sb.add( "if" , true , 1 );
		sb.add( "ip" , true , 1 );
		Signature signature = sb.makeSignature();
		
		//BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(signature);
		Node bdominio = bb.addNode("dominio" , bb.addRoot() );
		Node brouter = bb.addNode( "router" , bdominio );
		Node bif0 = bb.addNode( "if" , brouter );
		Node bif1 = bb.addNode( "if" , brouter );
		Node bipr = bb.addNode("ip" , bif0 );
		Node bhost = bb.addNode("host", bdominio);
		Node bifhost = bb.addNode("if" , bhost , bif0.getPort(0).getHandle());
		Node biphost = bb.addNode("ip" , bifhost , bipr.getPort(0).getHandle());
		
		//REDEX
		BigraphBuilder redex = new BigraphBuilder(signature);
		Node dominio = redex.addNode("dominio" , redex.addRoot() );
		Node router = redex.addNode( "router" , dominio );
		redex.addSite( router );
		Node if0 = redex.addNode( "if" , router );
		Node ip = redex.addNode( "ip" , if0 , redex.addOuterName("a") );
		redex.addSite( dominio );
		redex.addInnerName("b" , if0.getPort(0).getHandle() );
		
		for(Match m : Matcher.DEFAULT.match( bb.makeBigraph(true) , redex.makeBigraph(true))){
			System.out.println(m);
		}
	}
	
	private static void test6() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("router", true, 2);
		sb.add("lan", true, 1);
		sb.add("ip", false, 1);
		sb.add("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		// BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		bb.addNode("lan", bb.addNode("router", bb.addRoot()));
		bb.addNode("host", bb.addNode("ip", bb.addRoot()));

		// REDEX E REACTUM (STESSO BIGRAFO)
		BigraphBuilder redex = new BigraphBuilder(s);
		redex.addSite(redex.addNode("router", redex.addRoot()));
		redex.addSite(redex.addNode("ip", redex.addRoot()));

		RewritingRule arr = new RewritingRule(redex.makeBigraph(),
				redex.makeBigraph(), 0, 1);
		System.out.println(arr.apply(bb.makeBigraph()).iterator().next()
				.toString());

	}

	private static void test5() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("router", true, 2);
		sb.add("lan", true, 1);
		sb.add("ip", false, 1);
		sb.add("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		// BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		bb.addNode("ip", bb.addNode("lan", bb.addRoot()));

		// REDEX E REACTUM
		BigraphBuilder redex = new BigraphBuilder(s);
		redex.addSite(redex.addNode("lan", redex.addRoot()));

		BigraphBuilder reactum = new BigraphBuilder(s);
		reactum.addNode("lan", reactum.addRoot());

		RewritingRule arr = new RewritingRule(
				redex.makeBigraph(true), reactum.makeBigraph(true));
		
		Bigraph b = bb.makeBigraph(true);

		int i = 1;
		Iterator<Bigraph> ib = arr.apply(b).iterator();
		while (ib.hasNext()) {
			b = ib.next();
			ib = arr.apply(b).iterator();
			System.out
					.println("-----------------------------------------------");
			System.out.println("riscrittura #" + i++ + ":");
			System.out.println(b);
		}
	}

	private static void test4() {

		SignatureBuilder sb = new SignatureBuilder();
		sb.add("router", true, 2);
		sb.add("lan", true, 1);
		sb.add("ip", false, 1);
		sb.add("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		BigraphBuilder rete = new BigraphBuilder(s);
		Root r0 = rete.addRoot();
		OuterName on = rete.addOuterName("r_to_lan");
		OuterName r_ip = rete.addOuterName("r_ip");
		rete.addNode("router", r0, on, r_ip);
		Node lan = rete.addNode("lan", r0, on);

		rete.addNode("ip", lan, r_ip);

		rete.addNode("ip", lan);
		rete.addNode("ip", lan);

		BigraphBuilder tap = new BigraphBuilder(s);
		tap.addSite(tap.addRoot());
		tap.addInnerName("r_to_lan");
		tap.addInnerName("r_ip");
		rete.outerCompose(tap.makeBigraph());
		// fine rete

		BigraphBuilder redex = new BigraphBuilder(s);
		Root rr0 = redex.addRoot();
		OuterName rron = redex.addOuterName("r_to_lan");
		redex.addNode("router", rr0, rron, redex.addInnerName("r_ip")
				.getHandle());
		Root rr1 = redex.addRoot();
		Node rlan = redex.addNode("lan", rr1, rron);
		redex.addSite(rlan);
		Node ip = redex.addNode("ip", rlan);

		BigraphBuilder rtap = new BigraphBuilder(s);
		rtap.addSite(rtap.addRoot());
		rtap.addSite(rtap.addRoot());
		rtap.addInnerName("r_to_lan");

		redex.outerCompose(rtap.makeBigraph());
		redex.merge();

		Bigraph bigRedex = redex.makeBigraph();

		OuterName hostip = redex.addOuterName("host_ip");
		redex.addNode("host", redex.getRoots().get(0), hostip);
		redex.relink( hostip,ip.getPort(0));
		BigraphBuilder rrtap = new BigraphBuilder(s);
		rrtap.addSite(rrtap.addRoot());
		rrtap.addInnerName("host_ip");
		redex.outerCompose(rrtap.makeBigraph());

		Bigraph bigReactum = redex.makeBigraph();

		RewritingRule arr = new RewritingRule(bigRedex, bigReactum, 0);

		Bigraph b = rete.makeBigraph();

		int i = 1;
		Iterator<Bigraph> ib = arr.apply(b).iterator();
		while (ib.hasNext()) {
			b = ib.next();
			ib = arr.apply(b).iterator();
			System.out
					.println("-----------------------------------------------");
			System.out.println("riscrittura #" + i++ + ":");
			System.out.println(b);
		}
	}

	private static void test3() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("router", true, 2);
		sb.add("lan", true, 1);
		sb.add("ip", false, 1);
		sb.add("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		// RETE:
		BigraphBuilder rete = new BigraphBuilder(s);
		Root r0 = rete.addRoot();
		OuterName on = rete.addOuterName("r_to_lan");
		OuterName r_ip = rete.addOuterName("r_ip");
		rete.addNode("router", r0, on, r_ip).attachProperty(
				new SimpleProperty<Integer>("net_addr", 2000));
		Node lan = rete.addNode("lan", r0, on);

		rete.addNode("ip", lan, r_ip).attachProperty(
				new SimpleProperty<Integer>("ip_addr", 1));

		for (int i = 0; i < 10; ++i)
			rete.addNode("ip", lan).attachProperty(
					new SimpleProperty<Integer>("ip_addr", i));
		BigraphBuilder tap = new BigraphBuilder(s);
		tap.addSite(tap.addRoot());
		tap.addInnerName("r_to_lan");
		tap.addInnerName("r_ip");
		rete.outerCompose(tap.makeBigraph());
		// fine rete

		BigraphBuilder redex = new BigraphBuilder(s);
		Root rr0 = redex.addRoot();
		OuterName rron = redex.addOuterName("r_to_lan");
		redex.addNode("router", rr0, rron, redex.addInnerName("r_ip")
				.getHandle());
		Root rr1 = redex.addRoot();
		Node rlan = redex.addNode("lan", rr1, rron);
		redex.addSite(rlan);
		Node ip = redex.addNode("ip", rlan);

		BigraphBuilder rtap = new BigraphBuilder(s);
		rtap.addSite(rtap.addRoot());
		rtap.addSite(rtap.addRoot());
		rtap.addInnerName("r_to_lan");

		redex.outerCompose(rtap.makeBigraph());
		redex.merge();

		Bigraph bigRedex = printBig(redex.makeBigraph());

		OuterName hostip = redex.addOuterName("host_ip");
		redex.addNode("host", redex.getRoots().get(0), hostip).attachProperty(
				new SimpleProperty<String>("id", "net200"));
		redex.relink(hostip, ip.getPort(0));
		BigraphBuilder rrtap = new BigraphBuilder(s);
		rrtap.addSite(rrtap.addRoot());
		rrtap.addInnerName("host_ip");
		redex.outerCompose(rrtap.makeBigraph());

		Bigraph bigReactum = printBig(redex.makeBigraph());

		RewritingRule arr = new RewritingRule(bigRedex, bigReactum, 0);
		int i = 0;
		Bigraph k = rete.makeBigraph();

		System.out.println("################################ " + i);
		Iterator<Bigraph> j = arr.apply(k).iterator();
		while (j.hasNext()) {
			k = j.next();
			i++;
			System.out.println("################################ " + i);
			j = arr.apply(k).iterator();
			i++;
		}

		System.out.println("match test...");
		Long t0 = System.currentTimeMillis();
		int mc = 0;
		for (Match m : new Matcher().match(rete.makeBigraph(),
				bigRedex)) {
			mc++;
		}
		Long t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " matches in " + (t1 - t0) + " ms");

		RewritingRule ar = new RewritingRule(bigRedex, bigReactum, 0);
		System.out.println("ground rewrite test...");
		t0 = System.currentTimeMillis();
		mc = 0;
		for (Bigraph b : ar.apply(rete.makeBigraph())) {
			mc++;
		}
		t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " rewrites in " + (t1 - t0) + " ms");
	}

	private static void test2() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.add("a", true, 1);
		Signature s = sb.makeSignature("MySig");

		Handle h;
		Parent p;
		Node n;

		BigraphBuilder bbA = new BigraphBuilder(s);
		p = bbA.addRoot();
		n = bbA.addNode("a", p);
		bbA.addNode("a", p);
		bbA.addRoot();
		bbA.addOuterName("v");
		bbA.addOuterName("w");

		BigraphBuilder bbR = new BigraphBuilder(s);
		h = bbR.addOuterName("x");
		p = bbR.addRoot();
		bbR.addNode("a", p, h);
		bbR.addSite(p);
		bbR.addRoot();
		bbR.addInnerName("y", h);
		bbR.addInnerName("z", bbR.addOuterName("z"));

		Long t0 = System.currentTimeMillis();
		int mc = 0;
		for (Match t : new Matcher().match(bbA.makeBigraph(true),
				bbR.makeBigraph(true))) {
			mc++;
		}
		Long t1 = System.currentTimeMillis();
		System.out.println(mc + " matches in " + (t1 - t0) + " ms");
	}

	private static void test1() {
		NodeChaser nc = new NodeChaser() {
			protected void onNodeAdded(Node node) {
				System.out.println("- CHEASING " + node + ".");
			}

			protected void onReplicated(Node original, Node copy) {
				System.out.println("- REPLICATION DETECTED FOR " + original
						+ " => " + copy + ".");
			}

			protected void onOwnerChanged(Node node, Owner oldValue,
					Owner newValue) {
				System.out.println("- OWNER CHANGE DETECTED FOR " + node + ".");
			}
		};

		SignatureBuilder sb = new SignatureBuilder();
		sb.add("a", true, 0);
		sb.add("b", true, 1);
		sb.add("c", false, 2);
		
		Control c_a = sb.get("a");
		c_a.attachProperty(new SimpleProperty<Integer>("TEST_1",1));
		c_a.attachProperty(new SimpleProperty<Integer>("TEST_2",2));
		c_a.attachProperty(new SimpleProperty<String>("TEST_3"));
		
		System.out.println(c_a.toString(true));
		
		Signature s = sb.makeSignature("MySig");
		// build A
		BigraphBuilder bbA = new BigraphBuilder(s);
		bbA.addSite(bbA.addRoot());
		Handle o = bbA.addOuterName("x");
		bbA.addInnerName("x7", o);
		bbA.addInnerName("x", o);
		bbA.addInnerName("x8", o);

		Bigraph bA = bbA.makeBigraph();

		System.out.println("- A -------------------------");
		System.out.println(bA);

		// build B
		BigraphBuilder bbB = new BigraphBuilder(s);
		Node m = bbB.addNode("c", bbB.addRoot());

		nc.chase(m);

		bbB.addSite(m);
		bbB.addInnerName("x", bbB.addOuterName("x7"));
		bbB.relink(bbB.addOuterName("x"),m.getPort(0));
		bbB.addInnerName("y", bbB.addOuterName("x8"));

		Bigraph bB = bbB.makeBigraph();

		System.out.println("- B --------------------------");
		System.out.println(bB);

		System.out.println("-----------------------------");

		bbB.outerCompose(bA);
		bbA.innerCompose(bB);

		System.out.println("Signature: " + s);
		BigraphBuilder bb1 = new BigraphBuilder(s);
		Root r = bb1.addRoot();
		Handle h = bb1.addOuterName("x");
		h = bb1.addInnerName("x", h).getHandle();
		Node n = bb1.addNode("c", r, h);

		nc.chase(n);

		bb1.addSite(r);
		// System.out.println(bb + "\n");
		Bigraph b1 = bb1.makeBigraph();
		// System.out.println(b1+ "\n");
		// printBig("B1",b1);
		bb1.outerCompose(b1);
		printBB("outerCompose", bb1);
		bb1.innerCompose(b1);
		printBB("innerCompose", bb1);

		BigraphBuilder bb2 = new BigraphBuilder(s);
		r = bb2.addRoot();
		h = bb2.addOuterName("x");
		n = bb2.addNode("b", r, h);

		nc.chase(n);

		bb2.addOuterName("y");
		bb2.addSite(n);
		Bigraph b2 = bb2.makeBigraph();

		bb1.outerNest(b2);
		printBB("outerNest", bb1);

		b1 = bb1.makeBigraph();
		bb2.innerNest(b1);
		printBB("innerNest", bb2);

		bb2.ground();
		printBB("ground", bb2);
		b2 = bb2.makeBigraph();

		System.out.println("match test...");
		Long t0 = System.currentTimeMillis();
		int mc = 0;
		for (Match t : new Matcher().match(b2, b2)) {
			mc++;
		}
		Long t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " matches in " + (t1 - t0) + " ms");

		RewritingRule ar = new RewritingRule(b2, b2);
		System.out.println("ground rewrite test...");
		t0 = System.currentTimeMillis();
		mc = 0;
		for (Bigraph b3 : ar.apply(b2)) {
			mc++;
		}
		t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " rewrites in " + (t1 - t0) + " ms");
		
		System.out.println("chased nodes: " + nc.getAll().size());
		for(int i = 1;i<=3;i++){
			System.out.println("sleeping...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.gc();
			System.out.println("GC...");
			System.out.println("chased nodes after GC #"+i+": " + nc.getAll().size());
		}
		System.out.println("chased nodes owned by bA: " + nc.getAll(bA).size());
		System.out.println("chased nodes owned by bB: " + nc.getAll(bB).size());
		System.out.println("chased nodes owned by b1: " + nc.getAll(b1).size());
		System.out.println("chased nodes owned by b2: " + nc.getAll(b2).size());
		System.out.println("chased nodes owned by bb1: " + nc.getAll(bb1).size());
		System.out.println("chased nodes owned by bb2: " + nc.getAll(bb2).size());
		System.out.println("chased nodes owned by ar.redex: " + nc.getAll(ar.getRedex()).size());
		System.out.println("chased nodes owned by ar.reactum: " + nc.getAll(ar.getReactum()).size());
		nc.releaseAll();
		System.out.println("chased nodes after releaseAll: " + nc.getAll().size());
		
		
	}

	private static void printT() {
		System.out.println(System.currentTimeMillis());
	}

	private static BigraphBuilder printBB(BigraphBuilder b) {
		return printBB("Builder", b);
	}

	private static BigraphBuilder printBB(String prefix, BigraphBuilder b) {
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges()
				+ " <" + b.getSites().size() + "," + b.getInnerNames()
				+ "> -> <" + b.getRoots().size() + "," + b.getOuterNames()
				+ ">");
		return b;
	}

	private static Bigraph printBig(Bigraph b) {
		return printBig("Bigraph", b);
	}

	private static Bigraph printBig(String prefix, Bigraph b) {
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges()
				+ " <" + b.getSites().size() + "," + b.getInnerNames()
				+ "> -> <" + b.getRoots().size() + "," + b.getOuterNames()
				+ ">");
		return b;
	}

}