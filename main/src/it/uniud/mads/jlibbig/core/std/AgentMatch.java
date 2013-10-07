package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.util.BidMap;

import java.util.*;

public class AgentMatch extends BigraphMatch {

	protected final List<Bigraph> params;
	protected final Bigraph lambda;

	AgentMatch(Bigraph context, Bigraph redexImage, Bigraph redexId,
			Bigraph paramWiring, Bigraph[] params,
			BidMap<Node, Node> nodeEmbedding) {
		super(context, redexImage, redexId, null, nodeEmbedding);
		this.params = Collections.unmodifiableList(Arrays.asList(params));
		this.lambda = paramWiring;
	}

	/**
	 * @see it.uniud.mads.jlibbig.core.Match#getContext()
	 */
	@Override
	public Bigraph getContext() {
		return this.context;
	}

	/**
	 * @see it.uniud.mads.jlibbig.core.Match#getParam()
	 */
	@Override
	public Bigraph getParam() {
		if (param == null) {
			BigraphBuilder bb = new BigraphBuilder(this.context.signature);
			for (Bigraph prm : this.params) {
				bb.rightParallelProduct(prm);
			}
			bb.outerCompose(lambda);
			param = bb.makeBigraph();
		}
		return param;
	}

	public List<Bigraph> getParams() {
		return this.params;
	}

	public Bigraph getParamWiring() {
		return this.lambda;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentMatch:\ncontext = ").append(super.context)
				.append("\nredexImage = ").append(super.rdxImage)
				.append("\nredexId = ").append(super.rdxId)
				.append("\nparamWiring = ").append(lambda);
		int i = 0;
		for(Bigraph prm : params){
			builder.append("\nparam[").append(i).append("] = ").append(params.get(i));
		}		
		return builder.toString();
	}
	
	

}
