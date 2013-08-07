package jlibbig.bigmc;

import java.util.List;
import java.util.Set;

import jlibbig.core.*;

/**
 * Class used to store immutable ground Bigraph (Agent).
 * For a mutable version, users can use {@link jlibbig.bigmc.AgentBigraphBuilder}
 */
public class AgentBigraph implements AbstBigraph{
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
	public Set<? extends OuterName> getOuterNames() {
		return bigraph.getOuterNames();
	}

	@Override
	public Set<? extends InnerName> getInnerNames() {
		return bigraph.getInnerNames();
	}

	@Override
	public Set<? extends Node> getNodes() {
		return bigraph.getNodes();
	}

	@Override
	public Set<? extends Edge> getEdges() {
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
	
	/**
	 * Juxtapose two AgentBigraph.
	 * @param left
	 * 			The first AgentBigraph. Its roots will precede those of the second AgentBigraph.
	 * @param right
	 * 			The second AgentBigraph.
	 * @return
	 * 			The resulting AgentBigraph.
	 */
	public static AgentBigraph juxtapose( AgentBigraph left , AgentBigraph right ){
		AgentBigraphBuilder l = new AgentBigraphBuilder( left );
		l.rightJuxtapose( right );
		return l.makeAgent();
	}
	
}
