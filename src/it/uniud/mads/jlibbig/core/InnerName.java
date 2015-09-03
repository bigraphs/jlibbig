package it.uniud.mads.jlibbig.core;

/**
 * Inner names are points accessible through the inner interface (whereas ports
 * are not) identified by their name (cf. {@link Named}). Inner names (like
 * outer ones) are link facets comparison is based on their name.
 * 
 * @see LinkFacet
 * @see Point
 */
public interface InnerName extends Point, LinkFacet {
}
