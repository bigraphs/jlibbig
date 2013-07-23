package jlibbig.bigmc;

import jlibbig.core.*;
import java.util.*;

/**
 * This class stores a set of bigraphs and a set of reactions.
 * It also stores the set of outernames that aren't free names in the reactions.
 * Bigraphs, redexes and reacta of the same system share the same signature and this signature (system's signature) can't change.
 */
public class BigraphSystem{
	private Signature signature;
	private Set<Bigraph> bigraphs;
	private Set<String> outerNames;
	private Map<RedexBigraph , RedexBigraph> reactionRules;
	
	/**
	 * @param sig signature used for every bigraph and reaction of this system.	
	 */
	public BigraphSystem( Signature sig ){
		if( sig == null )
			throw new IllegalArgumentException( "Signature can't be null" );
		signature = sig;
		bigraphs = new HashSet<>();
		outerNames = new HashSet<>();
		reactionRules = new HashMap<>();
	}
	
	/**
	 * Add an outername to the list of non-free outernames.
	 * @param name name (string) that will be added to the list.
	 */
	public void addName( String name ){
		outerNames.add( name );
	}
	
	/**
	 * Get the system's signature
	 * @return The system's signature
	 * @see Signature
	 */
	public Signature getSignature(){ 
		return signature; 
	}

	/**
	 * Get the list of non-free outernames stored in the system.
	 * @return The list of outernames.
	 */
	public Set<String> getOuterNames(){
		return Collections.unmodifiableSet( outerNames );			
	}

	/**
	 * Add a bigraph to the system. Its signature must match with the system's signature.
	 * @param b the bigraph that will be added to the system.
	 * @throws RuntimeException if both signatures don't match.
	 * @see Bigraph
	 */
	public void addBigraph( Bigraph b ){
		if( !signature.containsAll( b.getSignature() ) || !b.getSignature().containsAll( signature ) )
			throw new RuntimeException( "Can't add a Bigraph to a BigraphSystem. Its Signature must be equal to the BigraphSystem's signature" );
		bigraphs.add( b );
	}
	
	/**
	 * Add a reaction to the system. Signatures of redex and reactum must match with the system's signature.
	 * @param redex the bigraph representing reaction's redex
	 * @param reactum the bigraph representing reaction's reactum
	 * @throws RuntimeException if signatures don't match or if redex and reactum don't have the same number of roots
	 * @see RedexBigraph
	 */
	public void addReaction( RedexBigraph redex , RedexBigraph reactum ){
		if( !signature.containsAll( redex.getSignature() ) || !redex.getSignature().containsAll( signature ) || !signature.containsAll( reactum.getSignature() ) || !reactum.getSignature().containsAll( signature ) )
			throw new RuntimeException( "Can't add a Reaction to a BigraphSystem. Both ( redex and reactum ) Signatures must be equal to the BigraphSystem's signature" );
		if( redex.getRoots().size() != reactum.getRoots().size() )
			throw new RuntimeException("The number of roots in redex and reactum must be the same");
		reactionRules.put( redex , reactum );
	}
	
	/**
	 * Get the set of system's bigraphs.
	 * @return The set of bigraphs stored in the system.
	 * @see Bigraph
	 */
	public Set<Bigraph> getBigraphs(){
		return Collections.unmodifiableSet( bigraphs );
	}
	
	/**
	 * Get the "set" of reactions.
	 * @return A map ( redex , reactum ). Each entry of this map represent a reaction.
	 * @see Bigraph
	 */
	public Map<RedexBigraph , RedexBigraph> getReactions(){
		return Collections.unmodifiableMap( reactionRules );
	}
}
