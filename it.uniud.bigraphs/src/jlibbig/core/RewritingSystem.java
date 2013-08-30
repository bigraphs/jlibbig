package jlibbig.core;

import java.util.*;

public interface RewritingSystem<B extends AbstBigraph> extends ReactiveSystem<B> {
	public abstract Signature getSignature();
	public abstract Set<? extends RewritingRule<? extends B>> getRules();
	public abstract Set<? extends B> getBigraphs();
}
