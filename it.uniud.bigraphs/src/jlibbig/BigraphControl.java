package jlibbig;

/**
 * Describes a control assignable to nodes of a bigraph. 
 */
public interface BigraphControl extends Named{
	int getArity();
	boolean isActive();
}
