package it.uniud.mads.jlibbig.core;

/**
 * Interface for mutable directed bigraphs.
 * 
 * @see DirectedBigraphHandler
 */
public interface DirectedBigraphBuilder<C extends Control> extends DirectedBigraphHandler<C> {

	public abstract DirectedBigraph<C> makeBigraph();
}
