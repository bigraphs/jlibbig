package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.AbstractMatch;

public class BigraphMatch extends AbstractMatch<Bigraph> {

	protected Bigraph rdxImage;
	protected Bigraph rdxLeftId;
	protected Bigraph rdxRightId;
	protected Bigraph rdxLinkId;

	private Map<Node, EditableNode> emb_nodes;

	protected BigraphMatch(Bigraph context, Bigraph redexImage,
			Bigraph redexLeftId, Bigraph redexRightId, Bigraph redexLinkId,
			Bigraph param, Map<Node, EditableNode> nodesEmbedding) {
		super(context, null, param);
		// if(context == null)
		// throw new IllegalArgumentException("Context can not be null.");
		// if(redexImage == null)
		// throw new IllegalArgumentException("RedexImage can not be null.");
		Signature sig = context.signature;
		this.rdxImage = redexImage;
		this.rdxLeftId = (redexLeftId != null) ? redexLeftId : Bigraph
				.makeEmpty(sig);
		this.rdxRightId = (redexRightId != null) ? redexRightId : Bigraph
				.makeEmpty(sig);
		this.rdxLinkId = (redexLinkId != null) ? redexLinkId : Bigraph
				.makeEmpty(sig);
		this.emb_nodes = nodesEmbedding;// new HashMap<>(nodesEmbedding);

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

	@Override
	public Bigraph getRedex() {
		if (super.redex == null) {
			BigraphBuilder bb = new BigraphBuilder(this.rdxImage);
			bb.leftJuxtapose(this.rdxLeftId);
			bb.rightJuxtapose(this.rdxRightId);
			bb.leftJuxtapose(this.rdxLinkId);
			super.redex = bb.makeBigraph(true);
		}
		return super.redex;
	}

	public Bigraph getRedexImage() {
		return this.rdxImage;
	}

	public Bigraph getRedexLeftId() {
		return this.rdxLeftId;
	}

	public Bigraph getRedexRightId() {
		return this.rdxRightId;
	}

	public Bigraph getRedexLinkId() {
		return this.rdxLinkId;
	}
}