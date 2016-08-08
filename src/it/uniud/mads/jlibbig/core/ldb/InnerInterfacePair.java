package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class InnerInterfacePair implements Owner {
    private final Map<String, EditableInnerName> ascendants = new IdentityHashMap<>();
    private final Map<String, EditableOuterName> descendants = new IdentityHashMap<>();

    public InnerInterfacePair() {

    }

    private InnerInterfacePair(Map<String, EditableInnerName> ascendants, Map<String, EditableOuterName> descendants) {
        this.ascendants.putAll(ascendants);
        this.descendants.putAll(descendants);
    }

    /**
     * Merges two pairs into a single one.
     *
     * @param a the first pair.
     * @param b the second pair.
     * @return the merged pair.
     */
    static InnerInterfacePair merge(InnerInterfacePair a, InnerInterfacePair b) {
        Map<String, EditableInnerName> os = new IdentityHashMap<>();
        Map<String, EditableOuterName> is = new IdentityHashMap<>();

        os.putAll(a.ascendants);
        os.putAll(b.ascendants);
        is.putAll(a.descendants);
        is.putAll(b.descendants);

        return new InnerInterfacePair(os, is);
    }

    /**
     * Adds a fresh descendant name to the current interface pair.
     *
     * @return the new descendant name.
     */
    public OuterName addDescendant() {
        return addDescendant(new EditableOuterName());
    }

    /**
     * Add a descendant name to the current interface pair.
     *
     * @param name the name of the new descendant name.
     * @return the new descendant name.
     */
    public OuterName addDescendant(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Argument can not be null.");
        return addDescendant(new EditableOuterName(name));
    }

    /**
     * Adds a descendant name to the current interface pair.
     *
     * @param name the descendant name that will be added.
     * @return new descendant name.
     */
    private OuterName addDescendant(EditableOuterName name) {
        if (ascendants.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' already present.");
        }
        name.setOwner(this);
        descendants.put(name.getName(), name);
        return name;
    }

    /**
     * Adds a fresh ascendant name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @return the new ascendant name.
     */
    public InnerName addAscendant() {
        EditableEdge e = new EditableEdge(this);
        return addAscendant(new EditableInnerName(), e);
    }

    /**
     * Adds a new ascendant name to the current interface pair.
     *
     * @param handle the outer name or the edge linking the new ascendant name.
     * @return the new ascendant name
     */
    public InnerName addAscendant(Handle handle) {
        return addAscendant(new EditableInnerName(), (EditableHandle) handle);
    }

    /**
     * Adds a ascendant name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @param name name of the new ascendant name.
     * @return the new ascendant name.
     */
    public InnerName addAscendant(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null.");
        EditableEdge e = new EditableEdge(this);
        return addAscendant(name, e);
    }

    /**
     * Adds a ascendant name to the current interface pair.
     *
     * @param name   name of the new ascendant name.
     * @param handle the outer name or the edge linking the new ascendant name.
     * @return the new ascendant name.
     */
    public InnerName addAscendant(String name, Handle handle) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null.");
        return addAscendant(new EditableInnerName(name), (EditableHandle) handle);
    }

    /**
     * Add a ascendant to the current interface pair.
     *
     * @param n ascendant name that will be added.
     * @param h outer name or edge that will be linked with the ascendant name in
     *          input.
     * @return the ascendant name
     */
    private InnerName addAscendant(EditableInnerName n, EditableHandle h) {
        if (ascendants.containsKey(n.getName())) {
            throw new IllegalArgumentException("Name already present.");
        }
        n.setHandle(h);
        ascendants.put(n.getName(), n);
        return n;
    }

    public Collection<? extends InnerName> getAscendants() {
        return this.ascendants.values();
    }

    public Collection<? extends OuterName> getDescendants() {
        return this.descendants.values();
    }

    public boolean containsAscendant(String name) {
        return this.ascendants.containsKey(name);
    }

    public boolean containsDescendant(String name) {
        return this.descendants.containsKey(name);
    }
}
