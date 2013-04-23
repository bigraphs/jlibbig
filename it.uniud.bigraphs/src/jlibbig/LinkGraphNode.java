package jlibbig;

import java.util.*;

public interface LinkGraphNode extends GraphNode {	
	public List<Port> getPorts();
	public Port getPort(int index);
}
