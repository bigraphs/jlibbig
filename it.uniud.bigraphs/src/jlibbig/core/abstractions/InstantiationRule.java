package jlibbig.core.abstractions;


public interface InstantiationRule<B extends Bigraph<?>> {
	Iterable<? extends B> instantiate(B parameters);
}
