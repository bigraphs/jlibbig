package jlibbig.core;

import java.util.*;

public class BigraphMatch extends AbstractMatch<Bigraph>{

	private WeakHashMap<Node,EditableNode> emb_nodes;
	
	protected BigraphMatch(Bigraph context, Bigraph redex, Bigraph param, Map<Node,EditableNode> nodesEmbedding) {
		super(context,redex,param);
		this.emb_nodes = new WeakHashMap<>(nodesEmbedding);
	}
	
	protected EditableNode getImage(Node node){
		return emb_nodes.get(node);
	}
}