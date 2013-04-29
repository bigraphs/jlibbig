package jlibbig;

import java.util.Set;

/**
 * Describes the interface of a link graph i.e. a set of names ({@link LinkGraphFacet}s)
 */
public interface LinkGraphFace extends GraphFace{
	/**
	 * @return the set of names
	 */
	Set<LinkGraphFacet> getNames();
}
