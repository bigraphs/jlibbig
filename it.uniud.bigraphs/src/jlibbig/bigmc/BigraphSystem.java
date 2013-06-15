package jlibbig.bigmc;

import jlibbig.core.*;
import java.util.*;


public class BigraphSystem{
	private Signature signature;
	private Set<Bigraph> bigraphs;
	private Set<String> outerNames;
	private Map<RedexBigraph , RedexBigraph> reactionRules;
			
	public BigraphSystem(){
		signature = null;
		bigraphs = new HashSet<>();
		outerNames = new HashSet<>();
		reactionRules = new HashMap<>();
	}
			
	public void setSignature( Signature sig ){ 
		signature = sig; 
	}
	
	public void addName( String name ){
		outerNames.add( name );
	}
			
	public Signature getSignature(){ 
		return signature; 
	}

	public Set<String> getOuterNames(){
		return Collections.unmodifiableSet( outerNames );			
	}

	public void addBigraph( Bigraph b ){
		bigraphs.add( b );
	}
			
	public void addReaction( RedexBigraph redex , RedexBigraph reactum ){
		if( redex.getRoots().size() != reactum.getRoots().size() )
			throw new RuntimeException("The number of roots in redex and reactum must be the same");
		reactionRules.put( redex , reactum );
	}
			
	public Set<Bigraph> getBigraphs(){
		return Collections.unmodifiableSet( bigraphs );
	}
			
	public Map<RedexBigraph , RedexBigraph> getReactions(){
		return Collections.unmodifiableMap( reactionRules );
	}
}
