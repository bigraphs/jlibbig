package jlibbig;

import java.util.*;

/**
 * The class is meant as a helper for bigraph construction
 * and manipulation in presence of series of operations since {#link Bigraph} 
 * is immutable: e.g. {@link Bigraph#compose(Bigraph, Bigraph)} or 
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object. 
 */
public class BigraphBuilder{
	private final Bigraph big;
	private final Signature sig;
	
	public BigraphBuilder(Signature sig){
		this.big = Bigraph.makeEmpty(sig);
		this.sig = sig;
	}
	
	public BigraphBuilder(Bigraph big){ 
		this.big = big.clone(); 
		this.sig = big.getSignature();
	}
	
	/** Creates a new bigraph from its inner one.
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph(){
		synchronized(big){
			return big.clone();
		}
	} 
	
	//TODO common read-only interface with bigraph
	
	public void merge(){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void leftJuxtapose(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void rightJuxtapose(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void innerCompose(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void outerCompose(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	// Derived operations
	public void innerNest(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void outerNest(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void rightParallelProduct(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void leftParallelProduct(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void rightMergeProduct(Bigraph graph){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void leftMergeProduct(Bigraph graph){
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

}
