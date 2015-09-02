package it.uniud.mads.jlibbig.core;

/**
 * Outer names are handles accessible through the outer interface (whereas edges
 * are not) identified by their name (cf. {@link Named}). Outer names (like
 * inner ones) are link facets comparison is based on their name.
 * 
 * @see LinkFacet
 * @see Handle
 */
public interface OuterName extends Named, Handle, LinkFacet {
}
