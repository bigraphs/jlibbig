package jlibbig;

import java.util.*;

public interface LinkGraphAbst {

	public abstract Signature<LinkGraphControl> getSignature();
	public abstract Set<LinkGraphNode> getNodes();
	public abstract Set<Port> getPorts();
	public abstract Set<Edge> getEdges();
	public abstract LinkGraphFace getInnerFace();
	public abstract LinkGraphFace getOuterFace();
	
	public abstract Linker getLink(Linked l);
	public abstract Set<Linked> getLinked(Linker l);
	
	public abstract boolean isEmpty();
	
	
	public interface Linked{}
	public interface Linker{}
}
