package jlibbig.core;

import java.util.*;

public class AbstMatch<A extends AbstBigraph>  implements Match<A>{

	protected final A context;
	protected final A redex;
	protected final List<A> params;
	
	protected AbstMatch(A context, A redex, List<A> params){
		this.context = context;
		this.redex = redex;
		this.params = Collections.unmodifiableList(new  LinkedList<A>(params));
	}
		
	@Override
	public A getContext() {
		return this.context;
	}

	@Override
	public A getRedex() {
		return this.redex;
	}

	@Override
	public List<A> getParams() {
		return this.params;
	}
	
}
