package jlibbig.core;

public interface InstantiationRule<B extends AbstBigraph> {
	Iterable<? extends B> instantiate(B parameters);
}
