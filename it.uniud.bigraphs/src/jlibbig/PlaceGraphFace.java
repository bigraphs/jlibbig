package jlibbig;

/**
 * Describes the interface of a place graph.
 * Nonetheless a place graph interface is a list of facets (i.e. {@link Site}s and {@link Root}s)
 * it is characterized by the length (width) of this list.
 * Place graph interfaces should be considered equal iff they have the same width.
 */
public interface PlaceGraphFace extends GraphFace{
	/** A place graph interface is characterized by its width (i.e. the number of its {@link PlaceGraphFacet}).
	 * @return the width of the interface
	 */
	int getWidth();
	//List<PlaceGraphFacet> getFacets();
}
