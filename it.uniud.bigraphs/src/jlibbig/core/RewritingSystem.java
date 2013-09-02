package jlibbig.core;

import java.util.*;

public interface RewritingSystem<B extends AbstractBigraph> extends ReactiveSystem<B> {

    @Override
	public abstract Signature getSignature();

    @Override
	public abstract Set<? extends RewritingRule<? extends B>> getRules();

    @Override
	public abstract Set<? extends B> getBigraphs();
}
