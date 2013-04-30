package jlibbig;

import java.util.Set;

/**
 * A mutable bigraph. The class is meant as a helper for bigraph construction
 * and manipulation in presence of series of operations since {#link Bigraph} 
 * is immutable: e.g. {@link Bigraph#compose(Bigraph, Bigraph)} or 
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object. 
 */
public class BigraphBuilder implements BigraphAbst {
	private final Bigraph _big;
		
	public BigraphBuilder(Signature<BigraphControl> sig){
		_big = Bigraph.makeEmpty(sig);
	}
	
	public BigraphBuilder(Bigraph big){
		_big = big;
	}
	
	/*
	 * @see jlibbig.BigraphAbst#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return _big.isEmpty();
	}

	/*
	 * @see jlibbig.BigraphAbst#isAgent()
	 */
	@Override
	public boolean isAgent() {
		return _big.isAgent();
	}

	/*
	 * @see jlibbig.BigraphAbst#getPlaceGraphView()
	 */
	@Override
	public PlaceGraphView getPlaceGraphView() {
		return _big.getPlaceGraphView();
	}

	/*
	 * @see jlibbig.BigraphAbst#getPlaceGraph()
	 */
	@Override
	public PlaceGraph getPlaceGraph() {
		return _big.getPlaceGraph();
	}

	/*
	 * @see jlibbig.BigraphAbst#getLinkGraphView()
	 */
	@Override
	public LinkGraphView getLinkGraphView() {
		return _big.getLinkGraphView();
	}

	/*
	 * @see jlibbig.BigraphAbst#getLinkGraph()
	 */
	@Override
	public LinkGraph getLinkGraph() {
		return _big.getLinkGraph();
	}

	/*
	 * @see jlibbig.BigraphAbst#getSignature()
	 */
	@Override
	public Signature<BigraphControl> getSignature() {
		return _big.getSignature();
	}

	/*
	 * @see jlibbig.BigraphAbst#getInnerFace()
	 */
	@Override
	public BigraphFace getInnerFace() {
		return _big.getInnerFace();
	}

	/*
	 * @see jlibbig.BigraphAbst#getOuterFace()
	 */
	@Override
	public BigraphFace getOuterFace() {
		return _big.getOuterFace();
	}

	/*
	 * @see jlibbig.BigraphAbst#getNodes()
	 */
	@Override
	public Set<BigraphNode> getNodes() {
		return _big.getNodes();
	}

	/*
	 * @see jlibbig.BigraphAbst#getEdges()
	 */
	@Override
	public Set<Edge> getEdges() {
		return _big.getEdges();
	}
	
	/** Creates a new bigraph from its inner one.
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph(){
		synchronized(_big){
			return _big.clone();
		}
	} 
	
	public void juxtaposeTo(Bigraph graph) {
		_big.juxtaposeTo(graph);
	}
	
	public void composeTo(Bigraph graph) {
		_big.composeTo(graph);
	}
	
	// Derived operations
	public void nest(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void parallelProductWith(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void mergeProductWith(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public static Bigraph fromString(){
		/*TODO parse fromString
		 * parse BigMC extended language
		 * allow bigger signatures than those found into the language
		 * make parser optional (but BigMC default)
		 */
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	//TODO factory methods for Milner's elementary bigraphs (e.g. ions, identities, merges)
	
	public static BigraphControl makeControl(boolean active,int arity){
		return new BGControl(active,arity);
	}
	public static BigraphControl makeControl(String name, boolean active,int arity){
		return new BGControl(name,active,arity);
	}
	
	private static class BGControl extends Named implements BigraphControl{
		private final boolean active;
		private final int arity;
		
		protected BGControl(boolean active,int arity){
			super("C_" + generateName());
			this.arity = arity;
			this.active = active;
		}
		
		protected BGControl(String name,boolean active, int arity){
			super(name);
			this.arity = arity;
			this.active = active;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + arity;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			BGControl other = (BGControl) obj;
			if (arity != other.arity || super.getName() != other.getName())
				return false;
			return true;
		}
	
		@Override
		public String toString() {
			return getName() + ":" + arity;
		}

		@Override
		public int getArity() {
			return arity;
		}

		@Override
		public boolean isActive() {
			return active;
		}
		
	}


}
