package it.uniud.mads.jlibbig.core.ldb;

import java.util.Collection;

/**
 * Place graph entities are organised in tree-like structures.
 *
 * @see Child
 */
public interface Parent extends PlaceEntity, it.uniud.mads.jlibbig.core.Parent {
    @Override
    public abstract Collection<? extends Child> getChildren();

    public abstract EditableParent getEditable();
}
