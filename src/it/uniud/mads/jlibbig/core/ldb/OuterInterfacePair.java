package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class OuterInterfacePair implements Owner {
    private final Map<String, EditableOuterName> ascendants = new IdentityHashMap<>();
    private final Map<String, EditableInnerName> descendants = new IdentityHashMap<>();

    public OuterInterfacePair() {

    }

    private OuterInterfacePair(Map<String, EditableOuterName> ascendants, Map<String, EditableInnerName> descendants) {
        this.ascendants.putAll(ascendants);
        this.descendants.putAll(descendants);
    }

    static OuterInterfacePair merge(OuterInterfacePair a, OuterInterfacePair b) {
        Map<String, EditableOuterName> os = new IdentityHashMap<>();
        Map<String, EditableInnerName> is = new IdentityHashMap<>();

        os.putAll(a.ascendants);
        os.putAll(b.ascendants);
        is.putAll(a.descendants);
        is.putAll(b.descendants);

        return new OuterInterfacePair(os, is);
    }

    /**
     * Adds a fresh outer name to the current interface pair.
     *
     * @return the new outer name.
     */
    public OuterName addAscendant() {
        return addAscendant(new EditableOuterName());
    }

    /**
     * Add an outer name to the current interface pair.
     *
     * @param name the name of the new outer name.
     * @return the new outer name.
     */
    public OuterName addAscendant(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Argument can not be null.");
        return addAscendant(new EditableOuterName(name));
    }

    /**
     * Adds an outer name to the current interface pair.
     *
     * @param name the outer name that will be added.
     * @return new outer name.
     */
    private OuterName addAscendant(EditableOuterName name) {
        if (ascendants.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' already present.");
        }
        name.setOwner(this);
        ascendants.put(name.getName(), name);
        return name;
    }

    /**
     * Adds a fresh inner name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @return the new inner name.
     */
    public InnerName addDescendant() {
        EditableEdge e = new EditableEdge(this);
        return addDescendant(new EditableInnerName(), e);
    }

    /**
     * Adds a new inner name to the current interface pair.
     *
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name
     */
    public InnerName addDescendant(Handle handle) {
        return addDescendant(new EditableInnerName(), (EditableHandle) handle);
    }

    /**
     * Adds an inner name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @param name name of the new inner name.
     * @return the new inner name.
     */
    public InnerName addDescendant(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null.");
        EditableEdge e = new EditableEdge(this);
        return addDescendant(name, e);
    }

    /**
     * Adds an inner name to the current interface pair.
     *
     * @param name   name of the new inner name.
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name.
     */
    public InnerName addDescendant(String name, Handle handle) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null.");
        return addDescendant(new EditableInnerName(name), (EditableHandle) handle);
    }

    /**
     * Add an innername to the current interface pair.
     *
     * @param n innername that will be added.
     * @param h outername or edge that will be linked with the innername in
     *          input.
     * @return the inner name
     */
    private InnerName addDescendant(EditableInnerName n, EditableHandle h) {
        if (descendants.containsKey(n.getName())) {
            throw new IllegalArgumentException("Name already present.");
        }
        n.setHandle(h);
        descendants.put(n.getName(), n);
        return n;
    }

    public Collection<? extends OuterName> getAscendants() {
        return this.ascendants.values();
    }

    public Collection<? extends InnerName> getDescendants() {
        return this.descendants.values();
    }

    public boolean containsAscendant(String name) {
        return this.ascendants.containsKey(name);
    }

    public boolean containsDescendant(String name) {
        return this.descendants.containsKey(name);
    }
}
