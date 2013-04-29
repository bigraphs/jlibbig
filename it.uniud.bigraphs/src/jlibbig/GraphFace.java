package jlibbig;

/**
 * Describes the interface (face) of a composable graph structure (e.g. place graphs link graphs and bigraphs)
 */
public interface GraphFace {
	//public boolean isComposable(GraphFace i);
	//public boolean isJuxtaposable(GraphFace i);
	//public void juxtapose(GraphFace i);
	/** Returns {@literal true} if the interface is empty and therefore is a unit for the juxtaposition operation.
	 * @return {@literal true} if the interface is empty.
	 */
	public boolean isEmpty();
}
