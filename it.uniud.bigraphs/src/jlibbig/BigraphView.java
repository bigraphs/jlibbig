package jlibbig;

import java.util.Set;

/**
 * Provides a read-only bigraphs.
 */
public class BigraphView implements BigraphAbst {

	private final Bigraph graph;
	
	public BigraphView(Bigraph graph) {
		this.graph = graph;
	}
	
	/**
	 * @see jlibbig.BigraphAbst#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return graph.isEmpty();
	}

	/**
	 * @see jlibbig.BigraphAbst#isAgent()
	 */
	@Override
	public boolean isAgent() {
		return graph.isAgent();
	}

	/**
	 * @see jlibbig.BigraphAbst#getPlaceGraphView()
	 */
	@Override
	public PlaceGraphView getPlaceGraphView() {
		return graph.getPlaceGraphView();
	}

	/**
	 * @see jlibbig.BigraphAbst#getPlaceGraph()
	 */
	@Override
	public PlaceGraph getPlaceGraph() {
		return graph.getPlaceGraph();
	}

	/**
	 * @see jlibbig.BigraphAbst#getLinkGraphView()
	 */
	@Override
	public LinkGraphView getLinkGraphView() {
		return graph.getLinkGraphView();
	}

	/**
	 * @see jlibbig.BigraphAbst#getLinkGraph()
	 */
	@Override
	public LinkGraph getLinkGraph() {
		return graph.getLinkGraph();
	}

	/**
	 * @see jlibbig.BigraphAbst#getSignature()
	 */
	@Override
	public Signature<BigraphControl> getSignature() {
		return graph.getSignature();
	}

	/**
	 * @see jlibbig.BigraphAbst#getInnerFace()
	 */
	@Override
	public BigraphFace getInnerFace() {
		return graph.getInnerFace();
	}

	/**
	 * @see jlibbig.BigraphAbst#getOuterFace()
	 */
	@Override
	public BigraphFace getOuterFace() {
		return graph.getOuterFace();
	}

	/**
	 * @see jlibbig.BigraphAbst#getNodes()
	 */
	@Override
	public Set<BigraphNode> getNodes() {
		return graph.getNodes();
	}

	/**
	 * @see jlibbig.BigraphAbst#getEdges()
	 */
	@Override
	public Set<Edge> getEdges() {
		return graph.getEdges();
	}

}
