package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.util.BidMap;

/**
 * Objects created from this class are matches for directed local bigraphs with
 * abstract internal names. Such matches are triples <C,R,P> of bigraphs over 
 * the same signature such that they can be composed in the bigraph C;R;P. The 
 * three components are called Context, Redex and Parameter respectively.
 * 
 * Given two local directed bigraphs F and G (over the same signature), a match
 * of F in G is a triple <C,R,P> such that their composition C;R;P yields G as 
 * for {@link it.uniud.mads.jlibbig.core.Match}. Furthermore, the redex R is the
 * juxtaposition of F and a suitable identity; these are called the redex image
 * and the redex id respectively.
 */
public class DirectedMatch extends it.uniud.mads.jlibbig.core.AbstractDirectedMatch
		<DirectedBigraph> {

	protected DirectedBigraph rdxImage;
	protected DirectedBigraph rdxId;

	private BidMap<Node, Node> emb_nodes;

	protected DirectedMatch(DirectedBigraph context, DirectedBigraph redexImage,
			DirectedBigraph redexId, DirectedBigraph param, BidMap<Node, Node> nodeEmbedding) {
		super(context, null, param);
		// if(context == null)
		// throw new IllegalArgumentException("Context can not be null.");
		// if(redexImage == null)
		// throw new IllegalArgumentException("RedexImage can not be null.");
		DirectedSignature sig = context.signature;
		this.rdxImage = redexImage;
		this.rdxId = (redexId != null) ? redexId : DirectedBigraph.makeEmpty(sig);
		this.emb_nodes = nodeEmbedding;// new HashMap<>(nodesEmbedding);

		// if (!redexImage.signature.equals(sig)
		// || !redexLeftId.signature.equals(sig)
		// || !redexRightId.signature.equals(sig)
		// || !redexLinkId.signature.equals(sig))
		// throw new IllegalArgumentException(
		// "Components of a matching should have the same signature.");
	}

	/**
	 * This object describes a match of F in G computed from two objects
	 * describing these two bigraphs. The object returned by
	 * {@link #getRedexImage} may not be the same used to compute the match.
	 * This method provides a bridge between the two instances and in practice
	 * describe an embedding of the nodes of F into G. The other way round is
	 * provided by {@link #getPreImage(Node)} which maps nodes of the object
	 * returned by {@link #getRedexImage} to nodes from the object initially
	 * used to compute the match.
	 * 
	 * @param node
	 *            a node from the redex.
	 * @return the image of the given node in the match redex.
	 */
	public Node getImage(Node node) {
		return emb_nodes.get(node);
	}

	/**
	 * @see #getImage(Node)
	 * @param node
	 *            a node from the match redex.
	 * @return the image of the given node in the redex.
	 */
	public Node getPreImage(Node node) {
		return emb_nodes.getKey(node);
	}

	@Override
	public DirectedBigraph getRedex() {
		if (super.redex == null) {
			super.redex = DirectedBigraph.juxtapose(this.rdxImage, this.rdxId);
		}
		return super.redex;
	}

	/**
	 * The match redex is the juxtaposition of the redex image and some identity.
	 * This method return the former.
	 * 
	 * @return the redex image in the match.
	 */
	public DirectedBigraph getRedexImage() {
		return this.rdxImage;
	}

	/**
	 * The match redex is the juxtaposition of the redex image and some identity.
	 * This method return the latter.
	 * 
	 * @return the id part of the match redex.
	 */
	public DirectedBigraph getRedexId() {
		return this.rdxId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Match:\ncontext = ").append(context)
				.append("\nredexImage = ").append(rdxImage)
				.append("\nredexId = ").append(rdxId).append("\nparam = ")
				.append(param);
		return builder.toString();
	}
}
