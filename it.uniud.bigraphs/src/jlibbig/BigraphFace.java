package jlibbig;

/**
 * Describes the interface (face) of a bigraph.
 * @see PlaceGraphFace
 * @see LinkGraphFace
 */
public interface BigraphFace extends PlaceGraphFace, LinkGraphFace {
	LinkGraphFace getLinkGraphFace();
	PlaceGraphFace getPlaceGraphFace();
}
