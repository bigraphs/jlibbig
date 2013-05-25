package jlibbig.core;

/**
 * Describes a control assignable to nodes of a bigraph. 
 */
public interface Control extends Named{
	int getArity();
	boolean isActive();
}
