package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.util.BidMap;

/**
 * Objects created from this class are weighted matches for bigraphs with abstract
 * internal names. 
 * 
 * @see Match
 * @see WeightedMatcher
 */
public class WeightedMatch extends Match {

	private int weight = 0;

	protected WeightedMatch(Bigraph context, Bigraph redexImage,
			Bigraph redexId, Bigraph param, BidMap<Node, Node> nodeEmbedding,
			int weight) {
		super(context, redexImage, redexId, param, nodeEmbedding);
		this.weight = weight;
	}

	public int getWeight() {
		return this.weight;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Match:\nweight = ").append(weight)
				.append("\ncontext = ").append(context)
				.append("\nredexImage = ").append(rdxImage)
				.append("\nredexId = ").append(rdxId).append("\nparam = ")
				.append(param);
		return builder.toString();
	}
}