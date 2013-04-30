package jlibbig;

import java.util.*;

public interface LinkGraphAbst {

	public abstract Signature<LinkGraphControl> getSignature();
	public abstract Set<LinkGraphNode> getNodes();
	public abstract Set<Port> getPorts();
	public abstract Set<Edge> getEdges();
	public abstract LinkGraphFace getInnerFace();
	public abstract LinkGraphFace getOuterFace();
	public abstract Set<InnerName> getInnerNames();
	public abstract Set<OuterName> getOuterNames();
	
	public abstract Linker getLink(Linked l);
	public abstract Set<Linked> getLinked(Linker l);
	
	public abstract boolean isEmpty();
	public abstract boolean isAgent();
	
	
	public interface Linked{}
	public interface Linker{}
}
