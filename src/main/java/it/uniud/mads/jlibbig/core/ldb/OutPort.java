package it.uniud.mads.jlibbig.core.ldb;

/**
 * Out ports are end-points for hyper-edges composing the link graphs and are
 * exposed by nodes structured in the place graph composing the bigraph together
 * with the aforementioned link graph. Despite a link graph {@link Point}, out ports
 * belong to a node and are identified by their number or position w.r.t. that
 * node.
 */
public interface OutPort extends Point, it.uniud.mads.jlibbig.core.Port<DirectedControl> {

    @Override
    public abstract Node getNode();

    @Override
    public abstract EditableNode.EditableOutPort getEditable();
}