package tests;
import java.util.*;
import jlibbig.core.*;
import jlibbig.core.attachedProperties.SimpleProperty;

@SuppressWarnings("unused")
public class foo {
	public static void main(String[] args){
		test1();
	}
	
	private static void test1(){
		NodeChaser nc = new NodeChaser(){
//			protected void onNodeAdded(Node node){
//				System.out.println("- CHEASING " + node + ".");
//			}
//			protected void onReplicates(Node original, Node copy){
//				System.out.println("- REPLICATION DETECTED FOR " + original + " => " + copy + ".");
//			}
//			protected void onOwnerChanges(Node node,Owner oldValue,Owner newValue){
//				System.out.println("- OWNER CHANGE DETECTED FOR " + node + ".");
//			}
		};
		
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("a",true,0);
		sb.put("b",true,1);
		sb.put("c",false,2);
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

		//build B
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
		h = bb1.addInnerName("x",h).getHandle();
		Node n = bb1.addNode("c", r, h);

		nc.chase(n);

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

		nc.chase(n);

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

		Long t0 = System.currentTimeMillis();
		Long mc = 0L;
		System.out.println("match test...");
		for(Match<Bigraph> t : BigraphMatcher.DEFAULT.match(b2, b1)){
			mc++;
//			String sp = "------";
//			System.out.println("BEGIN MATCH:");
//			System.out.println("--- ctx ---" + sp);
//			System.out.println(t.getContext());
//			System.out.println("--- rdx ---" + sp);
//			System.out.println(t.getRedex());
//			int i = 0;
//			for (Bigraph prm : t.getParams()) {
//				System.out.println("--- PRM "+ i + " ---" + sp.substring(2+(int) Math.floor(Math.log10(i+1))));
//				System.out.println(prm);
//				i++;
//			}
//			System.out.println("END MATCH");
		}
		Long t1 = System.currentTimeMillis();
		System.out.println("done: #" + mc + " " + (t1 - t0) + "(ms)");


		RewritingRule<Bigraph> ar =  new BigraphRewritingRule(b2,b2);
		t0 = System.currentTimeMillis();
		mc = 0L;
		System.out.println("ground rewrite test...");
		for(Bigraph b3 : ar.apply(b2)){
			mc++;
		}
		t1 = System.currentTimeMillis();
		System.out.println("done: #" + mc + " " + (t1 - t0) + "(ms)");
	}
	
	private static void test2(){
		SignatureBuilder sb = new SignatureBuilder();
		sb.put("router",true,2);
		sb.put("lan",true,1);
		sb.put("ip",false,1);
		sb.put("host", true, 1);
		Signature s = sb.makeSignature("MySig");
		// RETE:
		BigraphBuilder rete = new BigraphBuilder(s);
		OuterName on = rete.addOuterName("r_to_lan");
		OuterName r_ip = rete.addOuterName("r_ip");
		Root r0 = rete.addRoot();
		Node router = rete.addNode( "router" , r0 , on , r_ip );
		router.attachProperty( new SimpleProperty<Integer>("net_addr" , 2000 ) );
		
		Node lan = rete.addNode( "lan" , r0 , on );
		
		rete.addNode("ip" , lan , r_ip ).attachProperty( new SimpleProperty<Integer>("ip_addr" , 1 ) );
		
		for(int i = 2 ; i<255 ; ++i ){
			rete.addNode("ip", lan ).attachProperty( new SimpleProperty<Integer>("ip_addr" , i ) );
		}
		BigraphBuilder tap = new BigraphBuilder(s);
		tap.addSite( tap.addRoot() );
		tap.addInnerName("r_to_lan");
		tap.addInnerName("r_ip");
		rete.outerCompose( tap.makeBigraph() );
		//fine rete
		
		BigraphBuilder redex = new BigraphBuilder(s);
		Root rr0 = redex.addRoot();
		OuterName rron = redex.addOuterName( "r_to_lan" );
		OuterName rrip = redex.addOuterName( "r_ip" );
		redex.addNode("router" , rr0 ,  rron , rrip );
		Root rr1 = redex.addRoot();
		Node rlan = redex.addNode( "lan", rr1 , rron );
		redex.addSite( rlan );
		Node ip = redex.addNode( "ip" , rlan );
		
		BigraphBuilder rtap = new BigraphBuilder(s);
		rtap.addSite( rtap.addRoot() );
		rtap.addSite( rtap.addRoot() );
		rtap.addInnerName("r_to_lan" );
		rtap.addInnerName("r_ip" , rtap.addOuterName() );
		
		redex.outerCompose( rtap.makeBigraph() );
		
		//redex.closeOuterName(rron);
		//redex.renameOuterName(rrip, "francobollo");
		
		Bigraph bigRedex = redex.makeBigraph();
		System.out.println(redex);
		OuterName hostip = redex.addOuterName( "host_ip" );
		redex.relink( ip.getPort(0) , hostip );
		System.out.println(redex);
		redex.addNode( "host" , rr0 , hostip ).attachProperty( new SimpleProperty<String>( "id" , "net200" ) );
		
		BigraphBuilder rrtap = new BigraphBuilder(s);
		rrtap.addSite( rrtap.addRoot() );
		rrtap.addSite( rrtap.addRoot() );
		rrtap.addInnerName("host_ip" );
		rrtap.addInnerName("r_ip" , rtap.addOuterName() );
		redex.outerCompose( rrtap.makeBigraph() );
		
		Bigraph bigReactum = redex.makeBigraph();
		
		AgentRewritingRule arr = new AgentRewritingRule( bigRedex , bigReactum , 0 , 1 );
		int i = 0;
		for( Bigraph b : arr.apply( rete.makeBigraph() ) ){
			i++;
		}
		
		System.out.println("la rete ha " + i + " match!");
	
	}
	
	private static void printT(){
		System.out.println(System.currentTimeMillis());
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