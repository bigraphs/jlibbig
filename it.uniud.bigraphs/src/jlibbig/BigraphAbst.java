package jlibbig;

import java.util.Set;

/**
 * Represents a bigraph over a fixed signature
 */
public interface BigraphAbst {

	/**
	 * @return {@literal true} if the bigraph has empty interfaces and support.
	 */
	public abstract boolean isEmpty();

	/**
	 * @return {@literal true} if the bigraph has empty inner interface
	 */
	public abstract boolean isAgent();

	/**
	 * Returns read-only view of the place graph composing the bigraph.
	 * Use {@link jlibbig.Bigraph#getPlaceGraph()} for a writable copy.
	 * 
	 * @return a view of the place graph composing the bigraph
	 */
	public abstract PlaceGraphView getPlaceGraphView();

	/**
	 * Returns a copy of the place graph composing the bigraph. For read-only
	 * access use {@link jlibbig.Bigraph#getPlaceGraphView()}.
	 * 
	 * @return a copy of the place graph composing the bigraph
	 */
	public abstract PlaceGraph getPlaceGraph();

	/**
	 * Returns read-only view of the link graph composing the bigraph.
	 * Use {@link jlibbig.Bigraph#getLinkGraph()} for a writable copy.
	 * 
	 * @return a view of the link graph composing the bigraph
	 */
	public abstract LinkGraphView getLinkGraphView();

	/**
	 * Returns a copy of the link graph composing the bigraph. For read-only
	 * access use {@link jlibbig.Bigraph#_linking}.
	 * 
	 * @return a copy of the link graph composing the bigraph
	 */
	public abstract LinkGraph getLinkGraph();

	/**
	 * Returns the signature on which the bigraph is defined.
	 * 
	 * @return the signature
	 */
	public abstract Signature<BigraphControl> getSignature();

	/**
	 * Returns the inner interface of the bigraph
	 * 
	 * @return the inner interface
	 */
	public abstract BigraphFace getInnerFace();

	/**
	 * Returns the outer interface of the bigraph
	 * 
	 * @return the outer interface
	 */
	public abstract BigraphFace getOuterFace();

	/**
	 * Return the set of nodes composing the bigraph.
	 * 
	 * @return the set of nodes
	 */
	public abstract Set<BigraphNode> getNodes();

	/**
	 * Return the set of edges composing the bigraph.
	 * 
	 * @return the set of edges
	 */
	public abstract Set<Edge> getEdges();

}