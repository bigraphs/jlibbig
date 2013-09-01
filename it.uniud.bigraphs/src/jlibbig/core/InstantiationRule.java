package jlibbig.core;

public interface InstantiationRule<B extends AbstractBigraph> {
	Iterable<? extends B> instantiate(B parameters);
}
