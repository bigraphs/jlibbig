package jlibbig.core.abstractions;

import jlibbig.core.abstractions.Named;

/**
 * Describes a name of an internal link graph interface.
 * Differently from inner names, outer ones can link but not be linked (i.e. they
 * appear in the codomain of a link map nut not in its domain).
 */
public interface OuterName extends Named, Handle, LinkFacet {
}
