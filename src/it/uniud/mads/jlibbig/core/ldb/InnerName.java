package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Named;

/**
 * Inner names are points accessible through the inner interface (whereas ports
 * are not) identified by their name (cf. {@link Named}). Inner names (like
 * outer ones) are link facets comparison is based on their name.
 *
 * @see LinkFacet
 * @see Point
 */
public interface InnerName extends Point, LinkFacet, it.uniud.mads.jlibbig.core.InnerName {
    @Override
    public abstract EditableInnerName getEditable();
}
