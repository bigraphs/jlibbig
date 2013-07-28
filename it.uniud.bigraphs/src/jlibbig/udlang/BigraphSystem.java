package jlibbig.udlang;

import jlibbig.core.*;
import java.util.*;

/**
 * This class stores a set of bigraphs and a set of reactions.
 * Bigraphs, redexes and reacta of the same system share the same signature and this signature (system's signature) can't change.
 */
public class BigraphSystem{
	private Signature signature;
	private Set<Bigraph> bigraphs;
	private Set<Reaction<Bigraph>> reactions;
	
	/**
	 * @param sig signature used for every bigraph and reaction of this system.	
	 */
	public BigraphSystem( Signature sig ){
		if( sig == null )
			throw new IllegalArgumentException( "Signature can't be null" );
		signature = sig;
		bigraphs = new LinkedHashSet<>();
		reactions = new HashSet<>();
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
	 * Get the set of system's bigraphs.
	 * @return The set of bigraphs stored in the system.
	 * @see Bigraph
	 */
	public Set<Bigraph> getBigraphs(){
		return Collections.unmodifiableSet( bigraphs );
	}

	/**
	 * Get the set of reactions.
	 * @return A set of Reaction.
	 * @see Bigraph
	 * @see Reaction
	 */
	public Set<Reaction<Bigraph>> getReactions(){
		return Collections.unmodifiableSet( reactions );
	}

	/**
	 * Add a bigraph to the system. Its signature must match with the system's signature.
	 * @param b the bigraph that will be added to the system.
	 * @throws RuntimeException if both signatures don't match.
	 * @see Bigraph
	 */
	public void addBigraph( Bigraph b ){
		if( signature != b.getSignature() )
			throw new RuntimeException( "Can't add a Bigraph to a BigraphSystem. Its Signature must be equal to the BigraphSystem's signature" );
		bigraphs.add( b );
	}
	
	/**
	 * Add a reaction to the system. Signatures of redex and reactum must match with the system's signature.
	 * @param redex the bigraph representing reaction's redex
	 * @param reactum the bigraph representing reaction's reactum
	 * @throws RuntimeException if signatures don't match
	 * @see Bigraph
	 */
	public void addReaction( Bigraph redex , Bigraph reactum ){
		if( signature != redex.getSignature() || signature != reactum.getSignature() )
			throw new RuntimeException( "Can't add a Reaction to a BigraphSystem. Both ( redex and reactum ) Signatures must be equal to the BigraphSystem's signature" );
		if( redex.getRoots().size() != reactum.getRoots().size() )
			throw new RuntimeException("The number of roots in redex and reactum must be the same");
		reactions.add( new Reaction<Bigraph>( redex , reactum ) );
	}

	/**
	 * Return a view of the system.
	 * @return a String representing the system.
	 * @see Bigraph#toString()
	 */
	public String toString(){
		String nl = System.getProperty("line.separator");
		StringBuilder str = new StringBuilder();

		str.append("REACTIONS:" + nl );
		for( Reaction<Bigraph> reac : reactions ){
			str.append( reac.getRedex().toString() + nl + "-->" + nl + reac.getReactum().toString() + nl + nl );
		}
		
		str.append("BIGRAPHS:" + nl );
		for( Bigraph b : bigraphs ){
			str.append( b.toString() + nl + nl );
		}
		
		return str.toString();
	}
}
