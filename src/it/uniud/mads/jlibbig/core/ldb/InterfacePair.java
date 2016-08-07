package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class InterfacePair implements Owner {
    private final Map<String, EditableOuterName> outers = new IdentityHashMap<>();
    private final Map<String, EditableInnerName> inners = new IdentityHashMap<>();

    public InterfacePair() {

    }

    private InterfacePair(Map<String, EditableOuterName> outers, Map<String, EditableInnerName> inners) {
        this.outers.putAll(outers);
        this.inners.putAll(inners);
    }

    static InterfacePair merge(InterfacePair a, InterfacePair b) {
        Map<String, EditableOuterName> os = new IdentityHashMap<>();
        Map<String, EditableInnerName> is = new IdentityHashMap<>();

        os.putAll(a.outers);
        os.putAll(b.outers);
        is.putAll(a.inners);
        is.putAll(b.inners);

        return new InterfacePair(os, is);
    }

    /**
     * Adds a fresh outer name to the current interface pair.
     *
     * @return the new outer name.
     */
    public OuterName addOuterName() {
        return addOuterName(new EditableOuterName());
    }

    /**
     * Add an outer name to the current interface pair.
     *
     * @param name the name of the new outer name.
     * @return the new outer name.
     */
    public OuterName addOuterName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Argument can not be null.");
        return addOuterName(new EditableOuterName(name));
    }

    /**
     * Adds an outer name to the current interface pair.
     *
     * @param name the outer name that will be added.
     * @return new outer name.
     */
    private OuterName addOuterName(EditableOuterName name) {
        if (outers.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName() + "' already present.");
        }
        name.setOwner(this);
        outers.put(name.getName(), name);
        return name;
    }

    /**
     * Adds a fresh inner name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @return the new inner name.
     */
    public InnerName addInnerName() {
        EditableEdge e = new EditableEdge(this);
        return addInnerName(new EditableInnerName(), e);
    }

    /**
     * Adds a new inner name to the current interface pair.
     *
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name
     */
    public InnerName addInnerName(Handle handle) {
        return addInnerName(new EditableInnerName(), (EditableHandle) handle);
    }

    /**
     * Adds an inner name to the current interface pair. The name will be the only
     * point of a fresh edge.
     *
     * @param name name of the new inner name.
     * @return the new inner name.
     */
    public InnerName addInnerName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null.");
        EditableEdge e = new EditableEdge(this);
        return addInnerName(name, e);
    }

    /**
     * Adds an inner name to the current interface pair.
     *
     * @param name   name of the new inner name.
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name.
     */
    public InnerName addInnerName(String name, Handle handle) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null.");
        return addInnerName(new EditableInnerName(name), (EditableHandle) handle);
    }

    /**
     * Add an innername to the current interface pair.
     *
     * @param n innername that will be added.
     * @param h outername or edge that will be linked with the innername in
     *          input.
     * @return the inner name
     */
    private InnerName addInnerName(EditableInnerName n, EditableHandle h) {
        if (inners.containsKey(n.getName())) {
            throw new IllegalArgumentException("Name already present.");
        }
        n.setHandle(h);
        inners.put(n.getName(), n);
        return n;
    }

    public Collection<? extends OuterName> getOuterNames() {
        return this.outers.values();
    }

    public Collection<? extends InnerName> getInnerNames() {
        return this.inners.values();
    }

    public boolean containsOuterName(String name) {
        return this.outers.containsKey(name);
    }

    public boolean containsInnerName(String name) {
        return this.inners.containsKey(name);
    }
}
