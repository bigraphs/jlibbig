package jlibbig.core.abstractions;

import java.util.*;

import jlibbig.core.Signature;

public interface RewritingSystem<A extends Bigraph<?>,B extends Bigraph<?>> extends ReactiveSystem<A> {

    @Override
	public abstract Signature getSignature();

    @Override
	public abstract Set<? extends RewritingRule<? extends A,? extends B>> getRules();

    @Override
	public abstract Set<? extends A> getBigraphs();
}
