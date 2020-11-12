package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.util.BidMap;

import java.util.*;

/**
 * Objects created from this class are particular matches for bigraphs with
 * abstract internal names where the bigraph where the redex has to be matched is ground
 * (its inner interface is empty, hence it is an agent) whereas
 * the redex can be any bigraph over the same signature. Matches are triples
 * &lt;C,R,P&gt; of bigraphs over the same signature such that they can be composed in
 * the ground bigraph C;R;P. The three components are called Context, Redex and
 * Parameter respectively.
 * 
 * Given two bigraphs F and G (over the same signature), a match of F in G is a
 * triple &lt;C,R,P&gt; such that their composition C;R;P yields G as for
 * {@link it.uniud.mads.jlibbig.core.Match}. Like {@link it.uniud.mads.jlibbig.core.Match} the redex
 * R is the juxtaposition of F and a suitable identity; these are called the
 * redex image and the redex id respectively. Moreover, the parameter P is given
 * in discrete normal form as the composition of a wiring described
 * {@link #getParamWiring()} and the juxtaposition of the of discrete prime
 * ground bigraphs described by {@link #getParams()} which are indexed by the
 * sites of the redex ({@link #getRedexImage()}).
 */
public class DirectedAgentMatch extends DirectedMatch {

	protected final List<DirectedBigraph> params;
	protected final DirectedBigraph lambda;

	DirectedAgentMatch(DirectedBigraph context, DirectedBigraph redexImage, DirectedBigraph redexId,
			DirectedBigraph paramWiring, DirectedBigraph[] params,
			BidMap<Node, Node> nodeEmbedding) {
		super(context, redexImage, redexId, null, nodeEmbedding);
		this.params = Collections.unmodifiableList(Arrays.asList(params));
		this.lambda = paramWiring;
	}

	@Override
	public DirectedBigraph getParam() {
		if (param == null) {
			DirectedBigraphBuilder bb = new DirectedBigraphBuilder(this.context.signature);
			for (DirectedBigraph prm : this.params) {
				bb.rightParallelProduct(prm);
			}
			bb.outerCompose(lambda);
			param = bb.makeBigraph();
		}
		return param;
	}

	/**
	 * The match parameter ({@link #getParam()}) is given in discrete normal
	 * form as the composition of a wiring described {@link #getParamWiring()}
	 * and the juxtaposition of the of discrete prime ground bigraphs indexed by
	 * the sites of the redex ({@link #getRedexImage()}) and described by this
	 * method.
	 * 
	 * @return the list of the actual parameters of the match.
	 */
	public List<DirectedBigraph> getParams() {
		return this.params;
	}

	/**
	 * The match parameter ({@link #getParam()}) is given in discrete normal
	 * form as the composition of a wiring described by this method and the
	 * juxtaposition of the of discrete prime ground bigraphs described by
	 * {@link #getParams()} which are indexed by the sites of the redex (
	 * {@link #getRedexImage()}).
	 * 
	 * @return the bigraph wiring the outer faces of the actual parameters to
	 *         the inners of the match redex.
	 */
	public DirectedBigraph getParamWiring() {
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
		for (DirectedBigraph prm : params) {
			builder.append("\nparam[").append(i++).append("] = ").append(prm);
		}
		return builder.toString();
	}

}
