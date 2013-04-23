package jlibbig;

import java.util.List;
import java.util.Set;

public interface PlaceGraphAbst {

	
	public abstract Signature<PlaceGraphControl> getSignature();
	public abstract PlaceGraphFace getOuterFace();
	public abstract PlaceGraphFace getInnerFace();
	public abstract Set<PlaceGraphNode> getNodes();
	public abstract List<Root> getRoots();
	public abstract List<Site> getSites();
	public abstract Parent getParentOf(Child c);
	public abstract Set<Child> getChildrenOf(Parent p);
	public abstract boolean isEmpty();

	public static interface Parent{}	
	public static interface Child{}
}