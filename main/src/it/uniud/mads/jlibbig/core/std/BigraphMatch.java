package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.AbstractMatch;
import it.uniud.mads.jlibbig.core.util.BidMap;

public class BigraphMatch extends AbstractMatch<Bigraph> {

	protected Bigraph rdxImage;
	protected Bigraph rdxId;

	private BidMap<Node, EditableNode> emb_nodes;

	protected BigraphMatch(Bigraph context, Bigraph redexImage,
			Bigraph redexId, Bigraph param, BidMap<Node, EditableNode> nodeEmbedding) {
		super(context, null, param);
		// if(context == null)
		// throw new IllegalArgumentException("Context can not be null.");
		// if(redexImage == null)
		// throw new IllegalArgumentException("RedexImage can not be null.");
		Signature sig = context.signature;
		this.rdxImage = redexImage;
		this.rdxId = (redexId != null) ? redexId : Bigraph
				.makeEmpty(sig);
		this.emb_nodes = nodeEmbedding;// new HashMap<>(nodesEmbedding);

		// if (!redexImage.signature.equals(sig)
		// || !redexLeftId.signature.equals(sig)
		// || !redexRightId.signature.equals(sig)
		// || !redexLinkId.signature.equals(sig))
		// throw new IllegalArgumentException(
		// "Components of a matching should have the same signature.");
	}

	public EditableNode getImage(Node node) {
		return emb_nodes.get(node);
	}
	
	public EditableNode getPreImage(Node node) {
		return (EditableNode) emb_nodes.getKey(node);
	}

	@Override
	public Bigraph getRedex() {
		if (super.redex == null) {
			super.redex = Bigraph.juxtapose(this.rdxImage, this.rdxId);
		}
		return super.redex;
	}

	public Bigraph getRedexImage() {
		return this.rdxImage;
	}

	public Bigraph getRedexId() {
		return this.rdxId;
	}
}