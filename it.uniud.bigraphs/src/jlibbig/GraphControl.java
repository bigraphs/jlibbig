package jlibbig;

/**
 * Describes controls assignable to nodes of a composable graph structure (e.g. place graphs link graphs and bigraphs)
 */
public interface GraphControl {
	/** A control is identified uniquely by its name.
	 * @return the name of the control
	 */
	String getName();
}
