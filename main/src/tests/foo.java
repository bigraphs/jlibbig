package tests;

import java.util.*;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.attachedProperties.*;
import it.uniud.mads.jlibbig.core.std.*;
import it.uniud.mads.jlibbig.core.Owner;

@SuppressWarnings("unused")
public class foo {
	public static void main(String[] args) {
		test1();
	}

	private static void test6() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("router", true, 2);
		sb.put("lan", true, 1);
		sb.put("ip", false, 1);
		sb.put("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		// BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		bb.addNode("lan", bb.addNode("router", bb.addRoot()));
		bb.addNode("host", bb.addNode("ip", bb.addRoot()));

		// REDEX E REACTUM (STESSO BIGRAFO)
		BigraphBuilder redex = new BigraphBuilder(s);
		redex.addSite(redex.addNode("router", redex.addRoot()));
		redex.addSite(redex.addNode("ip", redex.addRoot()));

		AgentRewritingRule arr = new AgentRewritingRule(redex.makeBigraph(),
				redex.makeBigraph(), 0, 1);
		System.out.println(arr.apply(bb.makeBigraph()).iterator().next()
				.toString());

	}

	private static void test5() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("router", true, 2);
		sb.put("lan", true, 1);
		sb.put("ip", false, 1);
		sb.put("host", true, 1);
		Signature s = sb.makeSignature("MySig");

		// BIGRAFO
		BigraphBuilder bb = new BigraphBuilder(s);
		bb.addNode("ip", bb.addNode("lan", bb.addRoot()));

		// REDEX E REACTUM
		BigraphBuilder redex = new BigraphBuilder(s);
		redex.addSite(redex.addNode("lan", redex.addRoot()));

		BigraphBuilder reactum = new BigraphBuilder(s);
		reactum.addNode("lan", reactum.addRoot());

		AgentRewritingRule arr = new AgentRewritingRule(
				redex.makeBigraph(true), reactum.makeBigraph(true));

		Bigraph b = bb.makeBigraph(true);

		int i = 1;
		while ((b = arr.apply(b).iterator().next()) != null) {
			System.out
					.println("-----------------------------------------------");
			System.out.println("riscrittura #" + i++ + ":");
			System.out.println(b);
		}
	}

	private static void test4() {

		SignatureBuilder sb = new SignatureBuilder();
		sb.put("router", true, 2);
		sb.put("lan", true, 1);
		sb.put("ip", false, 1);
		sb.put("host", true, 1);
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
		redex.relink(ip.getPort(0), hostip);
		BigraphBuilder rrtap = new BigraphBuilder(s);
		rrtap.addSite(rrtap.addRoot());
		rrtap.addInnerName("host_ip");
		redex.outerCompose(rrtap.makeBigraph());

		Bigraph bigReactum = redex.makeBigraph();

		AgentRewritingRule arr = new AgentRewritingRule(bigRedex, bigReactum, 0);

		Bigraph k = rete.makeBigraph();

		for (AgentMatch m : AgentMatcher.DEFAULT.match(k, bigRedex)) {
			for (Node n : bigRedex.getNodes()) {
				if (m.getImage(n) == null) {
					System.out
							.println("################################# ARGH!!!!!!!!!!!!!");
				}
			}
		}

		int i = 1;
		while ((k = arr.apply(k).iterator().next()) != null) {
			System.out
					.println("-----------------------------------------------");
			System.out.println("riscrittura #" + i++ + ":");
			System.out.println(k);
		}
	}

	private static void test3() {
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("router", true, 2);
		sb.put("lan", true, 1);
		sb.put("ip", false, 1);
		sb.put("host", true, 1);
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
		redex.relink(ip.getPort(0), hostip);
		BigraphBuilder rrtap = new BigraphBuilder(s);
		rrtap.addSite(rrtap.addRoot());
		rrtap.addInnerName("host_ip");
		redex.outerCompose(rrtap.makeBigraph());

		Bigraph bigReactum = printBig(redex.makeBigraph());

		AgentRewritingRule arr = new AgentRewritingRule(bigRedex, bigReactum, 0);
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
		for (AgentMatch m : new AgentMatcher().match(rete.makeBigraph(),
				bigRedex)) {
			mc++;
		}
		Long t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " matches in " + (t1 - t0) + " ms");

		AgentRewritingRule ar = new AgentRewritingRule(bigRedex, bigReactum, 0);
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
		sb.put("a", true, 1);
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
		for (AgentMatch t : new AgentMatcher().match(bbA.makeBigraph(true),
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

			protected void onReplicates(Node original, Node copy) {
				System.out.println("- REPLICATION DETECTED FOR " + original
						+ " => " + copy + ".");
			}

			protected void onOwnerChanges(Node node, Owner oldValue,
					Owner newValue) {
				System.out.println("- OWNER CHANGE DETECTED FOR " + node + ".");
			}
		};

		SignatureBuilder sb = new SignatureBuilder();
		sb.put("a", true, 0);
		sb.put("b", true, 1);
		sb.put("c", false, 2);
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
		bbB.relink(m.getPort(0), bbB.addOuterName("x"));
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
		for (AgentMatch t : new AgentMatcher().match(b2, b2)) {
			mc++;
		}
		Long t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " matches in " + (t1 - t0) + " ms");

		AgentRewritingRule ar = new AgentRewritingRule(b2, b2);
		System.out.println("ground rewrite test...");
		t0 = System.currentTimeMillis();
		mc = 0;
		for (Bigraph b3 : ar.apply(b2)) {
			mc++;
		}
		t1 = System.currentTimeMillis();
		System.out.println("done: " + mc + " rewrites in " + (t1 - t0) + " ms");
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