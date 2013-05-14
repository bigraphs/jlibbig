package jlibbig;

import java.util.Set;

/**
 * The class is meant as a helper for bigraph construction
 * and manipulation in presence of series of operations since {#link Bigraph} 
 * is immutable: e.g. {@link Bigraph#compose(Bigraph, Bigraph)} or 
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object. 
 */
public class BigraphBuilder{
	private final Bigraph _big;
	private final Signature<BigraphControl> _sig;
	
	/*
	public BigraphBuilder(){
		_sig = new Signature<>();
		_big = Bigraph.makeEmpty(_sig);
	}
	*/
	
	public BigraphBuilder(Signature<BigraphControl> sig){
		_sig = sig.clone();
		_big = Bigraph.makeEmpty(_sig);
	}
	
	public BigraphBuilder(Bigraph big){ 
		_big = big.clone(); 
		_sig = _big.getSignature();
	}
	
	
	public boolean isAgent() {
		return _big.isAgent();
	}

	public boolean isEmpty() {
		return _big.isEmpty();
	}
	public Signature<BigraphControl> getSignature() {
		return _big.getSignature();
	}

	public BigraphFace getInnerFace() {
		return _big.getInnerFace();
	}

	public BigraphFace getOuterFace() {
		return _big.getOuterFace();
	}

	public Set<BigraphNode> getNodes() {
		return _big.getNodes();
	}

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
	
	/*
	public void addControl(BigraphControl control){
		_sig.extendWith(control);
	}
	
	public void addControl(String name, boolean active,int arity){
		addControl(new BGControl(name,active,arity));
	}*/
	
	public void merge() {
		_big.outerCompose(Bigraph.makeMerge(_sig, _big.getOuterFace()));
	}
	
	public void leftJuxtapose(Bigraph graph) throws IncompatibleSignatureException, NameClashException {
		_big.leftJuxtapose(graph);
	}
	
	public void rightJuxtapose(Bigraph graph) throws IncompatibleSignatureException, NameClashException {
		_big.rightJuxtapose(graph);
	}
	
	public void innerCompose(Bigraph graph) {
		_big.innerCompose(graph);
	}
	
	public void outerCompose(Bigraph graph) {
		_big.outerCompose(graph);
	}
	
	// Derived operations
	public void nestBefore(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void nestAfter(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void parallelProductRight(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void parallelProductLeft(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void mergeProductRight(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	

	public void mergeProductBefore(Bigraph graph){
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
