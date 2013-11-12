package it.uniud.mads.jlibbig.core.std;


/**
 * Roots describe regions of the bigraph. These are the roots of the place 
 * graph structure and compose its outer interface.
 * Roots (like sites) are identified within a bigraph by their index (position).
 * 
 * @see Site
 */
public interface Root extends Parent, PlaceEntity,
		it.uniud.mads.jlibbig.core.Root {
	@Override
	public abstract EditableRoot getEditable();
}
