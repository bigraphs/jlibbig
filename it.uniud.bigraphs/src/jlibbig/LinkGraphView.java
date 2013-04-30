package jlibbig;

import java.util.Set;

public class LinkGraphView implements LinkGraphAbst {

	final private LinkGraphAbst graph;

	public LinkGraphView(LinkGraphAbst graph) {
		this.graph = graph;
	}

	@Override
	public Signature<LinkGraphControl> getSignature() {
		return graph.getSignature();
	}

	@Override
	public Set<LinkGraphNode> getNodes() {
		return graph.getNodes();
	}
	

	@Override
	public Set<Port> getPorts() {
		return graph.getPorts();
	}


	@Override
	public Set<Edge> getEdges() {
		return graph.getEdges();
	}

	@Override
	public LinkGraphFace getInnerFace() {
		return graph.getInnerFace();
	}
	
	@Override
	public Set<InnerName> getInnerNames() {
		return graph.getInnerNames();
	}

	@Override
	public LinkGraphFace getOuterFace() {
		return graph.getOuterFace();
	}
	
	@Override
	public Set<OuterName> getOuterNames() {
		return graph.getOuterNames();
	}

	@Override
	public Linker getLink(Linked l) {
		return graph.getLink(l);
	}

	@Override
	public Set<Linked> getLinked(Linker l) {
		return graph.getLinked(l);
	}

	@Override
	public boolean isEmpty() {
		return graph.isEmpty();
	}

	@Override
	public boolean isAgent(){
		return graph.isAgent();
	}
	
}
