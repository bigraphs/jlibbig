package tests;
import java.util.*;
import jlibbig.core.*;
import jlibbig.udlang.*;

public class ProvaParser{
	public static void main(String[] args) throws Exception{
		
		BigraphParser parser = new BigraphParser();
		BigraphParser.ExtendedBRS brs = parser.parse( "%active c : 2; %passive d : 1; %inner x; %outer y; c[x,y].$0 || d.nil;\n" );
		
		System.out.println( brs.getSignature().toString() );
		System.out.println( "REACTIONS:" );
		for( Map.Entry<Bigraph , Bigraph> rr : brs.getReactions().entrySet() ){
			printBig( rr.getKey() );
			System.out.println( "" );
			printBig( rr.getValue() );
			System.out.println( "--" );
		}
		System.out.println( "BIGRAPHS:" );
		for( Bigraph b : brs.getBigraphs() )
			printBig( b );
		
	}
	
	private static Bigraph printBig(Bigraph b){
		return printBig("Bigraph", b);
	}

	private static Bigraph printBig(String prefix, Bigraph b){
		System.out.println(prefix + ": " + b.getNodes() + " " + b.getEdges() + " <" + b.getSites().size() + "," + b.getInnerNames() + "> -> <" +b.getRoots().size() + "," + b.getOuterNames() + ">");
		System.out.println( b.toString() );
		return b;
	}
		
}