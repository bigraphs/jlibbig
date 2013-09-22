package it.uniud.mads.jlibbig.core;

public interface InstantiationRule<B extends Bigraph<?>> {
	Iterable<? extends B> instantiate(B parameters);
}
