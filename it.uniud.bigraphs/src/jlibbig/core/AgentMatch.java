package jlibbig.core;

import java.util.*;

public class AgentMatch extends BigraphMatch{

	protected final List<Bigraph> params;
	protected final Bigraph lambda;
	protected Bigraph param;
	
	protected AgentMatch(Bigraph context, Bigraph redex, Bigraph lambda, List<Bigraph> params){
		super(context,redex,null);
		this.params = Collections.unmodifiableList(new  LinkedList<Bigraph>(params));
		this.lambda = lambda;
	}
		
	/**
	 * @see jlibbig.core.Match#getContext()
	 */
	@Override
	public Bigraph getContext() {
		return this.context;
	}

	/**
	 * @see jlibbig.core.Match#getRedex()
	 */
	@Override
	public Bigraph getRedex() {
		return this.redex;
	}
	/**
	 * @see jlibbig.core.Match#getParam()
	 */
	@Override
	public Bigraph getParam() {
		if(this.param == null){
			BigraphBuilder bb = new BigraphBuilder(this.context.signature);
			for(Bigraph prm : this.params){
				bb.rightParallelProduct(prm);
			}
			bb.outerCompose(lambda);
			this.param = bb.makeBigraph();
		}
		return this.param;
	}
	
	public List<Bigraph> getParams() {
		return this.params;
	}
	
	public Bigraph getLinking() {
		return this.lambda;
	}
	
	
}
