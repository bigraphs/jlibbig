package jlibbig.core;

/**
 * Describes a name of an internal link graph interface.
 * Differently from outer names, inners can be linked but not link (i.e. they 
 * appear in the domain of a link map but not in its codomain).
 */
public interface InnerName extends  Named, Point{}
