package it.uniud.mads.jlibbig.core.ldb;


/**
 * Place graph entities are organised in tree-like structures.
 *
 * @see Parent
 */
public interface Child extends PlaceEntity, it.uniud.mads.jlibbig.core.Child {
    @Override
    public abstract Parent getParent();

    public abstract EditableChild getEditable();
}
