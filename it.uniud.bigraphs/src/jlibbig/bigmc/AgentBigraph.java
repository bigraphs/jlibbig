package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

/**
 * Class used to store immutable ground Bigraph (Agent).
 * For a mutable version, users can use {@link jlibbig.bigmc.AgentBigraphBuilder}
 */
public class AgentBigraph implements jlibbig.core.abstractions.Bigraph<Control>{
	final Bigraph bigraph;
	
	/**
	 * Generate an AgentBigraph from a Ground Bigraph.
	 * @param bigraph
	 * 			The bigraph used for the new AgentBigraph.
	 * @throws
	 * 			If the bigraph in input isn't ground.
	 */
	public AgentBigraph( Bigraph bigraph ) throws IllegalArgumentException{
		if( !bigraph.isGround() )
			throw new IllegalArgumentException("Bigraph must be ground to be converted to a BigMC's Agent.");
		for( Edge edge : bigraph.getEdges() ){
			if( edge.getPoints().size() > 1 )
				throw new IllegalArgumentException( "This bigraph can't be converted to a BigMC's Agent. Every edge must have only one handled point." );
		}
		if( bigraph.getRoots().size() != 1 )
			throw new IllegalArgumentException("This bigraph can't be converted to a BigMC's Agent. The place graph's outerface must be 1 (exactly one root).");
		this.bigraph = bigraph;
	}
	
	/**
	 * Generate an AgentBigraph from an AgentBigraphBuilder
	 * @param abb
	 * 			The AgentBigraphBuilder used for the new AgentBigraph.
	 */
	public AgentBigraph( AgentBigraphBuilder abb ) throws IllegalArgumentException{
		bigraph = abb.bigraph.makeBigraph();
	}
	
	/**
	 * Clone the current AgentBigraph.
	 */
    @Override
	public AgentBigraph clone(){
		return new AgentBigraph( this.bigraph.clone() );
	}
	
	@Override
	public Signature getSignature() {
		return bigraph.getSignature();
	}

	@Override
	public boolean isEmpty() {
		return bigraph.isEmpty();
	}

	@Override
	public boolean isGround() {
		return bigraph.isGround();
	}

	@Override
	public List<? extends Root> getRoots() {
		return bigraph.getRoots();
	}

	@Override
	public List<? extends Site> getSites() {
		return bigraph.getSites();
	}

	@Override
	public Collection<? extends OuterName> getOuterNames() {
		return bigraph.getOuterNames();
	}

	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return bigraph.getInnerNames();
	}

	@Override
	public Collection<? extends Node> getNodes() {
		return bigraph.getNodes();
	}

	@Override
	public Collection<? extends Edge> getEdges() {
		return bigraph.getEdges();
	}
	
	/**
	 * Extract a Bigraph from the current AgentBigraph
	 * @return
	 * 			The Ground Bigraph generated from the current AgentBigraph
	 */
	public Bigraph asBigraph(){
		return bigraph;
	}
}
