package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;

import java.util.List;

/**
 * Describes nodes of bigraphs with abstract internal names. For this kind of
 * nodes identity is instance based. Nodes are assigned a {@link DirectedControl} which
 * specifies their arity i.e. the number of ports exposed by a node. Ports are
 * end-points for hyper-edges composing the link graph.
 */
public interface Node extends PropertyTarget, Parent, Child,
        it.uniud.mads.jlibbig.core.Node<DirectedControl> {
    public List<? extends Port> getPorts();

    public List<? extends Port> getUpperPorts();

    public List<? extends Port> getLowerPorts();

    @Override
    public abstract Port getPort(int index);

    @Override
    public abstract DirectedControl getControl();

    /*
     * Actually this is a dirty trick prevent implementation of this interface
     * outside of the package.
     */
    @Override
    public abstract EditableNode getEditable();
}