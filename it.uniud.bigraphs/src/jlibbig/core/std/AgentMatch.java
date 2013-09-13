package jlibbig.core.std;

import java.util.*;

public class AgentMatch extends BigraphMatch {

	protected final List<Bigraph> params;
	protected final Bigraph lambda;
	protected final Bigraph id;

	AgentMatch(Bigraph context, Bigraph redexImage, Bigraph redexLinkId,
			Bigraph paramWiring, Bigraph[] params,
			Map<Node, EditableNode> nodesEmbedding) {
		super(context, redexImage, null, null, redexLinkId, null,
				nodesEmbedding);
		this.params = Collections.unmodifiableList(Arrays.asList(params));
		this.lambda = paramWiring;
		this.id = redexLinkId;
	}

	/**
	 * @see jlibbig.core.Match#getContext()
	 */
	@Override
	public Bigraph getContext() {
		return this.context;
	}

	/**
	 * @see jlibbig.core.Match#getParam()
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

}
