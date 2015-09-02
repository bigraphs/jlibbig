package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.Named;

/**
 * Link facets are the components of link graph inner and outer interfaces and
 * are names specialised in inner and outer ones (cf. {@link InnerName} and
 * {@link OuterName} respectively). Comparison is based on the name facets.
 * 
 * @see Named
 */
public interface LinkFacet extends LinkEntity,
		it.uniud.mads.jlibbig.core.LinkFacet {

	public abstract EditableLinkFacet getEditable();
}
