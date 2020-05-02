package it.uniud.mads.jlibbig.core.ldb;

/**
 * In ports are
 */
public interface InPort extends Handle, it.uniud.mads.jlibbig.core.Port<DirectedControl> {

    @Override
    public abstract Node getNode();

    @Override
    public abstract EditableNode.EditableInPort getEditable();
}