package jlibbig;

public class Edge extends Named implements LinkGraph.Linker {

	/** Creates an edge with a generated name.
	 * The name is in the reserved form <code>"E_%d"</code> ({@link Named#generateName()}).
	 */
	public Edge() {
		super("E_" + generateName());
	}
	
	/** Creates an edge with the specified name.
	 * The name pattern <code>"E_%d"</code> is reserved ({@link Named#generateName()}).
	 * @param name the name to be used
	 */
	public Edge(String name) {
		super(name);
	}

}
