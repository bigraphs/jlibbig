package jlibbig;

import java.util.List;
import java.util.Set;

/**
 * 
 */
public class PlaceGraphView implements PlaceGraphAbst {

	final private PlaceGraphAbst graph;
	
	public PlaceGraphView(PlaceGraphAbst graph) {
		this.graph = graph;
	}

	@Override
	public PlaceGraphFace getOuterFace() {
		return graph.getOuterFace();
	}

	@Override
	public PlaceGraphFace getInnerFace() {
		return graph.getInnerFace();
	}

	@Override
	public Set<PlaceGraphNode> getNodes() {
		return graph.getNodes();
	}

	@Override
	public List<Root> getRoots() {
		return graph.getRoots();
	}

	@Override
	public List<Site> getSites() {
		return graph.getSites();
	}

	@Override
	public Parent getParentOf(Child c) {
		return graph.getParentOf(c);
	}

	@Override
	public Set<Child> getChildrenOf(Parent p) {
		return graph.getChildrenOf(p);
	}

	@Override
	public boolean isEmpty() {
		return graph.isEmpty();
	}
	

	@Override
	public boolean isAgent(){
		return graph.isAgent();
	}

	@Override
	public Signature<PlaceGraphControl> getSignature() {
		return graph.getSignature();
	}

}
