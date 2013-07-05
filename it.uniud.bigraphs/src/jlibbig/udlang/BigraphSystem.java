package jlibbig.udlang;

import jlibbig.core.*;
import java.util.*;


public class BigraphSystem{
	private Signature signature;
	private Set<Bigraph> bigraphs;
	private Map<Bigraph , Bigraph> reactions;
			
	public BigraphSystem( Signature sig ){
		signature = sig;
		bigraphs = new LinkedHashSet<>();
		reactions = new HashMap<>();
	}
			
	public Signature getSignature(){
		return signature; 
	}

	public Set<Bigraph> getBigraphs(){
		return bigraphs;
	}

	public Map<Bigraph , Bigraph> getReactions(){
		return reactions;
	}

	public void addBigraph( Bigraph b ){
		if( !signature.containsAll( b.getSignature() ) || !b.getSignature().containsAll( signature ) )
			throw new RuntimeException( "Can't add a Bigraph to a BigraphSystem. Its Signature must be equal to the BigraphSystem's signature" );
		bigraphs.add( b );
	}
	
	public void addReaction( Bigraph redex , Bigraph reactum ){
		//TODO signature control?
		reactions.put( redex , reactum );
	}

	public String toString(){
		String nl = System.getProperty("line.separator");
		StringBuilder str = new StringBuilder();

		str.append("REACTIONS:" + nl );
		for( Map.Entry<Bigraph , Bigraph> reac : reactions.entrySet() ){
			str.append( reac.getKey().toString() + nl + "-->" + nl + reac.getValue().toString() + nl + nl );
		}
		
		str.append("BIGRAPHS:" + nl );
		for( Bigraph b : bigraphs ){
			str.append( b.toString() + nl + nl );
		}
		
		return str.toString();
	}
}
