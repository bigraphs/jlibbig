package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.exceptions.IncompatibleInterfaceException;
import it.uniud.mads.jlibbig.core.exceptions.IncompatibleSignatureException;
import it.uniud.mads.jlibbig.core.exceptions.NameClashException;
import it.uniud.mads.jlibbig.core.exceptions.UnexpectedOwnerException;
import it.uniud.mads.jlibbig.core.std.*;
import it.uniud.mads.jlibbig.core.std.Edge;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.LinkFacet;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Parent;
import it.uniud.mads.jlibbig.core.std.Point;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Site;

import java.util.*;

/**
 * This class provides services for the creation and manipulation of bigraphs
 * since instances of {@link Bigraph} are immutable.
 * <p>
 * For efficiency reasons immutability can be relaxed by the user (cf.
 * {@link #outerCompose(Bigraph, boolean)}) by allowing the reuse of (parts) of
 * the arguments. Notice that, if not handled properly, the reuse of bigraphs
 * can cause inconsistencies e.g. as a consequence of the reuse of a bigraphs
 * held by a rewriting rule as its redex or reactum.
 */
final public class DirectedBigraphBuilder implements
        it.uniud.mads.jlibbig.core.BigraphBuilder<Control>, Cloneable {

    private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean
            .getBoolean("it.uniud.mads.jlibbig.consistency")
            || Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.bigraphops");

    private Bigraph big;
    private boolean closed = false;

    /**
     * Initially the builder describes an empty bigraph for the given signature.
     *
     * @param sig the signature to be used.
     */
    public DirectedBigraphBuilder(Signature sig) {
        this.big = Bigraph.makeEmpty(sig);
    }

    /**
     * Creates a builder from (a copy of) the given bigraph.
     *
     * @param big the bigraph describing the starting state.
     */
    public DirectedBigraphBuilder(Bigraph big) {
        this(big, false);
    }

    /**
     * @param big   the bigraph describing the starting state.
     * @param reuse whether the argument can be reused as it is or should be
     *              cloned.
     */
    DirectedBigraphBuilder(Bigraph big, boolean reuse) {
        if (big == null)
            throw new IllegalArgumentException("Argument can not be null.");
        if (!big.isConsistent())
            throw new IllegalArgumentException("Inconsistent bigraph.");
        this.big = (reuse) ? big.setOwner(this) : big.clone(this);
    }

    private static Collection<String> intersectNames(
            Collection<? extends LinkFacet> arg0,
            Collection<? extends LinkFacet> arg1) {
        return intersectNames(arg0, arg1, new HashSet<String>());
    }

    private static Collection<String> intersectNames(
            Collection<? extends LinkFacet> arg0,
            Collection<? extends LinkFacet> arg1, Collection<String> ns0) {
        Collection<String> ns1 = new HashSet<>();
        for (LinkFacet l : arg0) {
            ns1.add(l.getName());
        }
        for (LinkFacet r : arg1) {
            String s = r.getName();
            if (ns1.contains(s)) {
                ns0.add(s);
                ns1.remove(s);
            }
        }
        return ns0;
    }

    private static void clearOwnedCollection(
            Collection<? extends it.uniud.mads.jlibbig.core.std.EditableOwned> col) {
        for (it.uniud.mads.jlibbig.core.std.EditableOwned i : col) {
            i.setOwner(null);
        }
        col.clear();
    }

    private static void clearChildCollection(
            Collection<? extends it.uniud.mads.jlibbig.core.std.EditableChild> col) {
        for (it.uniud.mads.jlibbig.core.std.EditableChild i : col) {
            i.setParent(null);
        }
        col.clear();
    }

    private static void clearOuterMap(Map<String, it.uniud.mads.jlibbig.core.std.EditableOuterName> map) {
        Iterator<it.uniud.mads.jlibbig.core.std.EditableOuterName> ir = map.values().iterator();
        while (ir.hasNext()) {
            ir.next().setOwner(null);
        }
        map.clear();
    }

    private static void clearInnerMap(Map<String, it.uniud.mads.jlibbig.core.std.EditableInnerName> map) {
        Iterator<it.uniud.mads.jlibbig.core.std.EditableInnerName> ir = map.values().iterator();
        while (ir.hasNext()) {
            ir.next().setHandle(null);
        }
        map.clear();
    }

    @Override
    public String toString() {
        assertOpen();
        return big.toString();
    }

    /**
     * Returns the bigraph build so far. The new bigraph is independent from any
     * other operation done by the builder.
     *
     * @return a bigraph.
     */
    public Bigraph makeBigraph() {
        return makeBigraph(false);
    }

    /**
     * Return the bigraph build so far. The new bigraph is independent from any
     * other operation done by the builder. The new bigraph can be created more
     * efficiently if the builder is closed by the same method call.
     *
     * @param close disables the builder to perform any other operation.
     * @return a bigraph.
     */
    public Bigraph makeBigraph(boolean close) {
        assertOpen();
        assertConsistency();
        Bigraph b;
        if (close) {
            b = big.setOwner(big);
            closed = true;
        } else {
            b = big.clone();
            if (DEBUG_CONSISTENCY_CHECK && !b.isConsistent())
                throw new RuntimeException("Inconsistent bigraph.");
        }
        return b;
    }

    /**
     * A closed builder can not perform any operation.
     *
     * @return a boolean indicating whether the builder is disabled to perform
     * any operation.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Asserts that the builder is not closed and operations on it are allowd.
     * If called on a closed builder, an exception is trown.
     */
    private void assertOpen() throws UnsupportedOperationException {
        if (this.closed)
            throw new UnsupportedOperationException(
                    "The operation is not supported by a closed BigraphBuilder");
    }

    @Override
    public DirectedBigraphBuilder clone() {
        assertOpen();
        DirectedBigraphBuilder bb = new DirectedBigraphBuilder(this.big.getSignature());
        bb.big = this.big.clone(bb);
        return bb;
    }

    @Override
    public Signature getSignature() {
        assertOpen();
        return this.big.getSignature();
    }

    @Override
    public boolean isEmpty() {
        assertOpen();
        return this.big.isEmpty();
    }

    @Override
    public boolean isGround() {
        assertOpen();
        return this.big.isGround();
    }

    @Override
    public List<? extends Root> getRoots() {
        assertOpen();
        return this.big.getRoots();
    }

    @Override
    public List<? extends Site> getSites() {
        assertOpen();
        return this.big.getSites();
    }

    @Override
    public Collection<? extends OuterName> getOuterNames() {
        assertOpen();
        return this.big.getOuterNames();
    }

    public boolean containsOuterName(String name) {
        assertOpen();
        return this.big.outers.containsKey(name);
    }

    @Override
    public Collection<? extends InnerName> getInnerNames() {
        assertOpen();
        return this.big.getInnerNames();
    }

    public boolean containsInnerName(String name) {
        assertOpen();
        return this.big.inners.containsKey(name);
    }

    @Override
    public Collection<? extends it.uniud.mads.jlibbig.core.std.Node> getNodes() {
        assertOpen();
        return this.big.getNodes();
    }

    @Override
    public Collection<? extends Edge> getEdges() {
        assertOpen();
        return this.big.getEdges();
    }

    /**
     * Adds a root to the current bigraph.
     *
     * @return the new root.
     */
    public Root addRoot() {
        assertOpen();
        it.uniud.mads.jlibbig.core.std.EditableRoot r = new it.uniud.mads.jlibbig.core.std.EditableRoot();
        r.setOwner(this);
        this.big.roots.add(r);
        assertConsistency();
        return r;
    }

    /**
     * Adds a root to the current bigraph.
     *
     * @param index the region to be assigned to the root.
     * @return the new root.
     */
    public Root addRoot(int index) {
        assertOpen();
        it.uniud.mads.jlibbig.core.std.EditableRoot r = new it.uniud.mads.jlibbig.core.std.EditableRoot();
        r.setOwner(this);
        this.big.roots.add(index, r);
        assertConsistency();
        return r;
    }

    /**
     * Adds a site to the current bigraph.
     *
     * @param parent the parent of the site.
     * @return the new site.
     */
    public Site addSite(Parent parent) {
        if (parent == null)
            throw new IllegalArgumentException("Argument can not be null.");
        assertOpen();
        assertOwner(parent, "Parent");
        it.uniud.mads.jlibbig.core.std.EditableSite s = new it.uniud.mads.jlibbig.core.std.EditableSite((it.uniud.mads.jlibbig.core.std.EditableParent) parent);
        this.big.sites.add(s);
        assertConsistency();
        return s;
    }

    /**
     * Adds a new node to the bigraph.
     *
     * @param controlName the control's name of the new node.
     * @param parent      the father of the new node, in the place graph.
     * @return the new node.
     */
    public it.uniud.mads.jlibbig.core.std.Node addNode(String controlName, Parent parent) {
        return addNode(controlName, parent, new LinkedList<Handle>());
    }

    /**
     * Adds a new node to the bigraph.
     *
     * @param controlName the control's name of the new node.
     * @param parent      the father of the new node, in the place graph.
     * @param handles     Handles (outernames or edges) that will be linked to new
     *                    node's ports.
     * @return the new node.
     */
    public it.uniud.mads.jlibbig.core.std.Node addNode(String controlName, Parent parent, Handle... handles) {
        if (controlName == null)
            throw new IllegalArgumentException("Control name can not be null.");
        if (parent == null)
            throw new IllegalArgumentException("Parent can not be null.");
        assertOpen();
        Control c = this.big.getSignature().getByName(controlName);
        if (c == null)
            throw new IllegalArgumentException(
                    "Control should be in the signature.");
        assertOwner(parent, "Parent");
        it.uniud.mads.jlibbig.core.std.EditableHandle[] hs = new it.uniud.mads.jlibbig.core.std.EditableHandle[c.getArity()];
        for (int i = 0; i < hs.length; i++) {
            it.uniud.mads.jlibbig.core.std.EditableHandle h = null;
            if (i < handles.length) {
                h = (it.uniud.mads.jlibbig.core.std.EditableHandle) handles[i];
            }
            if (h != null) {
                assertOwner(h, "Handle");
            } else {
                it.uniud.mads.jlibbig.core.std.EditableEdge e = new it.uniud.mads.jlibbig.core.std.EditableEdge(this);
                big.onEdgeAdded(e);
                h = e;
            }
            hs[i] = h;
        }
        it.uniud.mads.jlibbig.core.std.EditableNode n = new it.uniud.mads.jlibbig.core.std.EditableNode(c, (it.uniud.mads.jlibbig.core.std.EditableParent) parent, hs);
        this.big.onNodeAdded(n);
        assertConsistency();
        return n;
    }

    /**
     * Adds a new node to the bigraph.
     *
     * @param controlName the control's name of the new node.
     * @param parent      the father of the new node, in the place graph.
     * @param handles     list of handles (outernames or edges) that will be linked to
     *                    new node's ports.
     * @return the new node.
     */
    public Node addNode(String controlName, Parent parent, List<Handle> handles) {
        if (controlName == null)
            throw new IllegalArgumentException("Control name can not be null.");
        if (parent == null)
            throw new IllegalArgumentException("Parent can not be null.");
        assertOpen();
        Control c = this.big.getSignature().getByName(controlName);
        if (c == null)
            throw new IllegalArgumentException(
                    "Control should be in the signature.");
        assertOwner(parent, "Parent");
        int ar = c.getArity();
        List<it.uniud.mads.jlibbig.core.std.EditableHandle> hs = new ArrayList<>(ar);
        Iterator<Handle> hi = (handles == null) ? null : handles.iterator();
        for (int i = 0; i < ar; i++) {
            it.uniud.mads.jlibbig.core.std.EditableHandle h = null;
            if (hi != null && hi.hasNext()) {
                h = (it.uniud.mads.jlibbig.core.std.EditableHandle) hi.next();
            }
            if (h != null) {
                assertOwner(h, "Handle");
            } else {
                it.uniud.mads.jlibbig.core.std.EditableEdge e = new it.uniud.mads.jlibbig.core.std.EditableEdge(this);
                big.onEdgeAdded(e);
                h = e;
            }
            hs.add(h);
        }
        it.uniud.mads.jlibbig.core.std.EditableNode n = new it.uniud.mads.jlibbig.core.std.EditableNode(c, (it.uniud.mads.jlibbig.core.std.EditableParent) parent, hs);
        this.big.onNodeAdded(n);
        assertConsistency();
        return n;
    }

    /**
     * Adds a fresh outer name to the current bigraph.
     *
     * @return the new outer name.
     */
    public OuterName addOuterName() {
        return addOuterName(new it.uniud.mads.jlibbig.core.std.EditableOuterName());
    }

    /**
     * Add an outer name to the current bigraph.
     *
     * @param name the name of the new outer name.
     * @return the new outer name.
     */
    public OuterName addOuterName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Argument can not be null.");
        return addOuterName(new it.uniud.mads.jlibbig.core.std.EditableOuterName(name));
    }

    /**
     * Adds an outer name to the current bigraph.
     *
     * @param name the outer name that will be added.
     * @return new outer name.
     */
    private OuterName addOuterName(it.uniud.mads.jlibbig.core.std.EditableOuterName name) {
        assertOpen();
        if (big.outers.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName()
                    + "' already present.");
        }
        name.setOwner(this);
        this.big.outers.put(name.getName(), name);
        assertConsistency();
        return name;
    }

    /**
     * Adds a fresh inner name to the current bigraph. The name will be the only
     * point of a fresh edge.
     *
     * @return the new inner name.
     */
    public InnerName addInnerName() {
        it.uniud.mads.jlibbig.core.std.EditableEdge e = new it.uniud.mads.jlibbig.core.std.EditableEdge(this);
        big.onEdgeAdded(e);
        return addInnerName(new it.uniud.mads.jlibbig.core.std.EditableInnerName(), e);
    }

    /**
     * Adds a new inner name to the current bigraph.
     *
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name
     */
    public InnerName addInnerName(Handle handle) {
        Owner o = handle.getOwner();
        assertOrSetOwner(handle, "Handle");
        if (handle.isEdge() && o != null) {
            this.big.onEdgeAdded((it.uniud.mads.jlibbig.core.std.EditableEdge) handle);
        }
        return addInnerName(new it.uniud.mads.jlibbig.core.std.EditableInnerName(), (it.uniud.mads.jlibbig.core.std.EditableHandle) handle);
    }

    /**
     * Adds an inner name to the current bigraph. The name will be the only
     * point of a fresh edge.
     *
     * @param name name of the new inner name.
     * @return the new inner name.
     */
    public InnerName addInnerName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null.");
        it.uniud.mads.jlibbig.core.std.EditableEdge e = new it.uniud.mads.jlibbig.core.std.EditableEdge(this);
        big.onEdgeAdded(e);
        return addInnerName(name, e);
    }

    /**
     * Adds an inner name to the current bigraph.
     *
     * @param name   name of the new inner name.
     * @param handle the outer name or the edge linking the new inner name.
     * @return the new inner name.
     */
    public InnerName addInnerName(String name, Handle handle) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null.");
        Owner o = handle.getOwner();
        assertOrSetOwner(handle, "Handle");
        if (handle.isEdge() && o != null) {
            this.big.onEdgeAdded((it.uniud.mads.jlibbig.core.std.EditableEdge) handle);
        }
        return addInnerName(new it.uniud.mads.jlibbig.core.std.EditableInnerName(name),
                (it.uniud.mads.jlibbig.core.std.EditableHandle) handle);
    }

    /**
     * Add an innername to the current bigraph.
     *
     * @param n innername that will be added.
     * @param h outername or edge that will be linked with the innername in
     *          input.
     * @return the inner name
     */
    private InnerName addInnerName(it.uniud.mads.jlibbig.core.std.EditableInnerName n, it.uniud.mads.jlibbig.core.std.EditableHandle h) {
        assertOpen();
        if (big.inners.containsKey(n.getName())) {
            throw new IllegalArgumentException("Name already present.");
        }
        n.setHandle(h);
        this.big.inners.put(n.getName(), n);
        assertConsistency();
        return n;
    }

    /**
     * Links a set of points with a fresh edge.
     *
     * @param points the points to be linked.
     * @return the new edge connecting the points in input.
     */
    public Edge relink(Point... points) {
        return (Edge) relink(new it.uniud.mads.jlibbig.core.std.EditableEdge(), points);
    }

    public Edge relink(Collection<? extends Point> points) {
        if (points == null)
            throw new IllegalArgumentException("Argument can not be null.");
        return relink(points.toArray(new it.uniud.mads.jlibbig.core.std.EditablePoint[points.size()]));
    }

    /**
     * Links a set of points with the given handle.
     *
     * @param handle the handle to be used.
     * @param points the points to be linked.
     * @return the handle.
     */
    public Handle relink(Handle handle, Point... points) {
        assertOpen();
        assertOrSetOwner(handle, "Handle");
        it.uniud.mads.jlibbig.core.std.EditablePoint[] ps = new it.uniud.mads.jlibbig.core.std.EditablePoint[points.length];
        it.uniud.mads.jlibbig.core.std.EditableHandle h = (it.uniud.mads.jlibbig.core.std.EditableHandle) handle;
        for (int i = 0; i < points.length; i++) {
            ps[i] = (it.uniud.mads.jlibbig.core.std.EditablePoint) points[i];
            assertOwner(ps[i], "Point");
        }
        for (int i = 0; i < points.length; i++) {
            Handle old = ps[i].getHandle();
            ps[i].setHandle(h);
            if (old.isEdge() && old.getPoints().isEmpty()) {
                big.onEdgeRemoved((it.uniud.mads.jlibbig.core.std.EditableEdge) old);
            }
        }
        if (handle.isEdge()) { // checking owner to be null is not enough to find new edges
            big.onEdgeAdded((it.uniud.mads.jlibbig.core.std.EditableEdge) handle);
        }
        assertConsistency();
        return h;
    }

    public Handle relink(Handle handle, Collection<? extends Point> points) {
        if (points == null)
            throw new IllegalArgumentException("Argument can not be null.");
        return relink(handle, points.toArray(new it.uniud.mads.jlibbig.core.std.EditablePoint[points.size()]));
    }

    /**
     * Disconnects a point from its current handle and connect it with a fresh
     * edge.
     *
     * @param point the point that will be unlinked
     * @return the new edge
     */
    public Edge unlink(Point point) {
        return relink(point);
    }

    /**
     * Closes an outer name turning it into an edge.
     *
     * @param name the outer name as string.
     * @return the new edge.
     */
    public Edge closeOuterName(String name) {
        return closeOuterName(big.outers.get(name));
    }

    /**
     * Closes an outer name turning it into an edge.
     *
     * @param name the outer name to close.
     * @return the new edge.
     */
    public Edge closeOuterName(OuterName name) {
        assertOwner(name, "OuterName ");
        if (!big.outers.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName()
                    + "' not present.");
        }
        it.uniud.mads.jlibbig.core.std.EditableOuterName n1 = (it.uniud.mads.jlibbig.core.std.EditableOuterName) name;
        Edge e = relink(n1.getEditablePoints());
        big.outers.remove(n1.getName());
        n1.setOwner(null);
        return e;
    }

    /**
     * Closes an inner name.
     *
     * @param name the inner name as string.
     */
    public void closeInnerName(String name) {
        closeInnerName(big.inners.get(name));
    }

    /**
     * Closes an inner name.
     *
     * @param name the inner name to close.
     */
    public void closeInnerName(InnerName name) {
        assertOwner(name, "InnerName ");
        if (!big.inners.containsKey(name.getName())) {
            throw new IllegalArgumentException("Name '" + name.getName()
                    + "' not present.");
        }
        it.uniud.mads.jlibbig.core.std.EditableInnerName n1 = (it.uniud.mads.jlibbig.core.std.EditableInnerName) name;
        Handle h = n1.getHandle();
        n1.setHandle(null);
        big.inners.remove(n1.getName());
        if (h.isEdge() && h.getPoints().isEmpty()) {
            big.onEdgeRemoved((it.uniud.mads.jlibbig.core.std.EditableEdge) h);
        }
    }

    /**
     * Renames an outer name.
     *
     * @param oldName the outer name to be renamed.
     * @param newName the new name.
     */
    public void renameOuterName(String oldName, String newName) {
        if (newName == null || oldName == null)
            throw new IllegalArgumentException("Arguments can not be null");
        it.uniud.mads.jlibbig.core.std.EditableOuterName n1 = big.outers.get(oldName);
        if (n1 == null) {
            throw new IllegalArgumentException("Name '" + oldName
                    + "' is not present.");
        } else {
            renameOuterName(n1, newName);
        }
    }

    /**
     * Renames an outer name.
     *
     * @param oldName the outer name to be renamed.
     * @param newName the new name.
     */
    public void renameOuterName(OuterName oldName, String newName) {
        if (newName == null || oldName == null)
            throw new IllegalArgumentException("Arguments can not be null");
        assertOwner(oldName, "OuterName ");
        if (newName.equals(oldName.getName()))
            return;
        it.uniud.mads.jlibbig.core.std.EditableOuterName n2 = big.outers.get(newName);
        if (n2 == null) {
            ((it.uniud.mads.jlibbig.core.std.EditableOuterName) oldName).setName(newName);
        } else {
            throw new IllegalArgumentException("Name '" + newName
                    + "' already in use");
        }
    }

    /**
     * Renames an inner name.
     *
     * @param oldName the inner name to be renamed.
     * @param newName the new name.
     */
    public void renameInnerName(String oldName, String newName) {
        if (newName == null || oldName == null)
            throw new IllegalArgumentException("Arguments can not be null");
        it.uniud.mads.jlibbig.core.std.EditableInnerName n1 = big.inners.get(oldName);
        if (n1 == null) {
            throw new IllegalArgumentException("Name '" + oldName
                    + "' is not present.");
        } else {
            renameInnerName(n1, newName);
        }
    }

    /**
     * Renames an inner name.
     *
     * @param oldName the inner name to be renamed.
     * @param newName the new name.
     */
    public void renameInnerName(InnerName oldName, String newName) {
        if (newName == null || oldName == null)
            throw new IllegalArgumentException("Arguments can not be null");
        assertOwner(oldName, "InnerName ");
        if (newName.equals(oldName.getName()))
            return;
        it.uniud.mads.jlibbig.core.std.EditableInnerName n2 = big.inners.get(newName);
        if (n2 == null) {
            ((it.uniud.mads.jlibbig.core.std.EditableInnerName) oldName).setName(newName);
        } else {
            throw new IllegalArgumentException("Name '" + newName
                    + "' is present already.");
        }
    }

    /**
     * Merges every region of the bigraph into one.
     *
     * @return the new root.
     */
    public Root merge() {
        assertOpen();
        it.uniud.mads.jlibbig.core.std.EditableRoot r = new it.uniud.mads.jlibbig.core.std.EditableRoot();
        r.setOwner(this);
        for (it.uniud.mads.jlibbig.core.std.EditableParent p : big.roots) {
            for (it.uniud.mads.jlibbig.core.std.EditableChild c : new HashSet<>(p.getEditableChildren())) {
                c.setParent(r);
            }
        }
        clearOwnedCollection(big.roots);// .clear();
        big.roots.add(r);
        assertConsistency();
        return r;
    }

    /**
     * Merges the given regions of the bigraph into one.
     *
     * @param index the index of the new region.
     * @param roots the index of the regions to be merged.
     * @return the new root.
     */
    public Root merge(int index, int... roots) {
        assertOpen();
        it.uniud.mads.jlibbig.core.std.EditableRoot r = new it.uniud.mads.jlibbig.core.std.EditableRoot();
        r.setOwner(this);
        it.uniud.mads.jlibbig.core.std.EditableRoot[] rs = new it.uniud.mads.jlibbig.core.std.EditableRoot[roots.length];
        for (int i = 0; i < roots.length; i++) {
            rs[i] = big.roots.get(roots[i]);
            Iterator<it.uniud.mads.jlibbig.core.std.EditableChild> ir = rs[i].getEditableChildren().iterator();
            while (ir.hasNext()) {
                ir.next().setParent(r);
            }
        }
        for (it.uniud.mads.jlibbig.core.std.EditableRoot r1 : rs) {
            big.roots.remove(r1);
            r1.setOwner(null);
        }
        big.roots.add(index, r);
        assertConsistency();
        return r;
    }

    public void removeRoot(Root root) {
        assertOwner(root, "Root ");
        if (!root.getChildren().isEmpty())
            throw new IllegalArgumentException("Unempty region.");
        it.uniud.mads.jlibbig.core.std.EditableRoot editableRoot = (it.uniud.mads.jlibbig.core.std.EditableRoot) root;
        editableRoot.setOwner(null);
        big.roots.remove(editableRoot);
        if (!root.getChildren().isEmpty()) {
            big.onNodeSetChanged(); // retrieving its descendants is not cheap and the cache may already be invalid anyway.
        }
        assertConsistency();
    }

    public void removeRoot(int index) {
        if (index < 0 || index >= big.roots.size())
            throw new IndexOutOfBoundsException(
                    "The argument does not refer to a root.");
        removeRoot(big.roots.get(index));
    }

    /**
     * Removes a site from the bigraph.
     *
     * @param site the site to be removed.
     */
    public void closeSite(Site site) {
        assertOwner(site, "Site ");
        it.uniud.mads.jlibbig.core.std.EditableSite editableSite = (it.uniud.mads.jlibbig.core.std.EditableSite) site;
        editableSite.setParent(null);
        big.sites.remove(editableSite);
        assertConsistency();
    }

    /**
     * Removes a site from the bigraph.
     *
     * @param index the index of site to be removed.
     */
    public void closeSite(int index) {
        if (index < 0 || index >= big.sites.size())
            throw new IndexOutOfBoundsException(
                    "The argument does not refer to a site.");
        closeSite(big.sites.get(index));
    }

    /**
     * Close all site an innernames of the current bigraph, generating a ground
     * bigraph.
     */
    public void ground() {
        assertOpen();
        clearChildCollection(big.sites);// .clear();
        clearInnerMap(big.inners);// .clear();
        assertConsistency();
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Roots and sites of the bigraph will precede those of the bigraphbuilder
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void leftJuxtapose(Bigraph graph) {
        leftJuxtapose(graph, false);
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Roots and sites of the bigraph will precede those of the bigraphbuilder
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void leftJuxtapose(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph left = graph;
        Bigraph right = this.big;
        if (left == right)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be juxtaposed with itself.");
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (!left.signature.equals(right.signature)) {
            throw new IncompatibleSignatureException(left.getSignature(),
                    right.getSignature());
        }
        if (!Collections.disjoint(left.inners.keySet(), right.inners.keySet())
                || !Collections.disjoint(left.outers.keySet(),
                right.outers.keySet())) {
            throw new IncompatibleInterfaceException(new NameClashException(
                    intersectNames(
                            left.inners.values(),
                            right.inners.values(),
                            intersectNames(left.outers.values(),
                                    right.outers.values()))));
        }
        Bigraph l = (reuse) ? left : left.clone();
        Bigraph r = right;
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : l.roots) {
            o.setOwner(this);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : l.outers.values()) {
            o.setOwner(this);
        }
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = l.edgesProxy.get();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        r.onEdgeAdded(es);
        r.onNodeAdded(l.nodesProxy.get());
        l.onEdgeSetChanged();
        l.onNodeSetChanged();
        r.roots.addAll(0, l.roots);
        r.sites.addAll(0, l.sites);
        r.outers.putAll(l.outers);
        r.inners.putAll(l.inners);
        assertConsistency();
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Roots and sites of the bigraphbuilder will precede those of the bigraph
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void rightJuxtapose(Bigraph graph) {
        rightJuxtapose(graph, false);
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Roots and sites of the bigraphbuilder will precede those of the bigraph
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void rightJuxtapose(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph left = this.big;
        Bigraph right = graph;
        if (left == right)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be juxtaposed with itself.");
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (!left.signature.equals(right.signature)) {
            throw new IncompatibleSignatureException(left.signature,
                    right.signature);
        }
        if (!Collections.disjoint(left.inners.keySet(), right.inners.keySet())
                || !Collections.disjoint(left.outers.keySet(),
                right.outers.keySet())) {
            throw new IncompatibleInterfaceException(new NameClashException(
                    intersectNames(
                            left.inners.values(),
                            right.inners.values(),
                            intersectNames(left.outers.values(),
                                    right.outers.values()))));
        }
        Bigraph l = left;
        Bigraph r = (reuse) ? right : right.clone();
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : r.roots) {
            o.setOwner(this);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : r.outers.values()) {
            o.setOwner(this);
        }
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = r.edgesProxy.get();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        l.onEdgeAdded(es);
        l.onNodeAdded(r.nodesProxy.get());
        r.onEdgeSetChanged();
        r.onNodeSetChanged();
        l.roots.addAll(r.roots);
        l.sites.addAll(r.sites);
        l.outers.putAll(r.outers);
        l.inners.putAll(r.inners);
        assertConsistency();
    }

    /**
     * Compose the current bigraphbuilder with the bigraph in input.
     *
     * @param graph the "inner" bigraph
     */
    public void innerCompose(Bigraph graph) {
        innerCompose(graph, false);
    }

    /**
     * Compose the current bigraphbuilder with the bigraph in input.
     *
     * @param graph the "inner" bigraph
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void innerCompose(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph in = graph;
        Bigraph out = this.big;
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (out == in)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be composed with itself.");
        if (!out.signature.equals(in.signature)) {
            throw new IncompatibleSignatureException(out.signature,
                    in.signature);
        }
        Set<String> xs = new HashSet<>(out.inners.keySet());
        Set<String> ys = new HashSet<>(in.outers.keySet());
        Set<String> zs = new HashSet<>(xs);
        xs.removeAll(ys);
        ys.removeAll(zs);

        if (!xs.isEmpty() || !ys.isEmpty()
                || out.sites.size() != in.roots.size()) {
            throw new IncompatibleInterfaceException(
                    "The outer face of the first graph must be equal to inner face of the second");
        }
        Bigraph a = out;
        Bigraph b = (reuse) ? in : in.clone();
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = b.edgesProxy.get();
        Collection<it.uniud.mads.jlibbig.core.std.EditableNode> ns = b.nodesProxy.get();
        // iterate over sites and roots of a and b respectively and glue them
        Iterator<it.uniud.mads.jlibbig.core.std.EditableRoot> ir = b.roots.iterator();
        Iterator<it.uniud.mads.jlibbig.core.std.EditableSite> is = a.sites.iterator();
        while (ir.hasNext()) { // |ir| == |is|
            it.uniud.mads.jlibbig.core.std.EditableSite s = is.next();
            it.uniud.mads.jlibbig.core.std.EditableParent p = s.getParent();
            p.removeChild(s);
            for (it.uniud.mads.jlibbig.core.std.EditableChild c : new HashSet<>(ir.next()
                    .getEditableChildren())) {
                c.setParent(p);
            }
        }
        // iterate over inner and outer names of a and b respectively and glue
        // them
        Map<String, it.uniud.mads.jlibbig.core.std.EditableHandle> a_inners = new HashMap<>();
        for (it.uniud.mads.jlibbig.core.std.EditableInnerName i : a.inners.values()) {
            a_inners.put(i.getName(), i.getHandle());
            i.setHandle(null);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : b.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableHandle h = a_inners.get(o.getName());
            for (it.uniud.mads.jlibbig.core.std.EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                p.setHandle(h);
            }
        }
        // update inner interfaces
        clearInnerMap(a.inners);// .clear();
        clearChildCollection(a.sites);// ;.clear();
        a.inners.putAll(b.inners);
        a.sites.addAll(b.sites);
        a.onNodeAdded(ns);
        b.onNodeSetChanged();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        a.onEdgeAdded(es);
        b.onEdgeSetChanged();
        assertConsistency();
    }

    /**
     * Compose bigraph in input with the current bigraphbuilder
     *
     * @param graph the "outer" bigraph
     */
    public void outerCompose(Bigraph graph) {
        outerCompose(graph, false);
    }

    /**
     * Compose the current bigraph in input with the bigraphbuilder.
     *
     * @param graph the "outer" bigraph
     * @param reuse flag. If true, the bigraph in input will not be copied.
     */
    public void outerCompose(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph in = this.big;
        Bigraph out = graph;
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (out == in)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be composed with itself.");
        if (!out.signature.equals(in.signature)) {
            throw new IncompatibleSignatureException(out.signature,
                    in.signature);
        }
        Set<String> xs = new HashSet<>(out.inners.keySet());
        Set<String> ys = new HashSet<>(in.outers.keySet());
        Set<String> zs = new HashSet<>(xs);
        xs.removeAll(ys);
        ys.removeAll(zs);

        if (!xs.isEmpty() || !ys.isEmpty()
                || out.sites.size() != in.roots.size()) {
            // System.err.println(out.inners.keySet() + " " + in.outers.keySet()
            // + " " + out.sites.size() + " " + in.roots.size());
            throw new IncompatibleInterfaceException(
                    "The outer face of the first graph must be equal to inner face of the second");
        }
        Bigraph a = (reuse) ? out : out.clone();
        Bigraph b = in; // this BB
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = a.edgesProxy.get();
        Collection<it.uniud.mads.jlibbig.core.std.EditableNode> ns = a.nodesProxy.get();
        // iterates over sites and roots of a and b respectively and glues them
        Iterator<it.uniud.mads.jlibbig.core.std.EditableRoot> ir = b.roots.iterator();
        Iterator<it.uniud.mads.jlibbig.core.std.EditableSite> is = a.sites.iterator();
        while (ir.hasNext()) { // |ir| == |is|
            it.uniud.mads.jlibbig.core.std.EditableSite s = is.next();
            it.uniud.mads.jlibbig.core.std.EditableParent p = s.getParent();
            p.removeChild(s);
            for (it.uniud.mads.jlibbig.core.std.EditableChild c : new HashSet<>(ir.next()
                    .getEditableChildren())) {
                c.setParent(p);
            }
        }
        // iterates over inner and outer names of a and b respectively and glues
        // them
        Map<String, it.uniud.mads.jlibbig.core.std.EditableHandle> a_inners = new HashMap<>(a.inners.size());
        for (it.uniud.mads.jlibbig.core.std.EditableInnerName i : a.inners.values()) {
            it.uniud.mads.jlibbig.core.std.EditableHandle h = i.getHandle();
            a_inners.put(i.getName(), h);
            i.setHandle(null);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : b.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableHandle h = a_inners.get(o.getName());
            for (it.uniud.mads.jlibbig.core.std.EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                p.setHandle(h);
            }
        }
        // updates inner interfaces
        clearOuterMap(b.outers);// .clear();
        clearOwnedCollection(b.roots);// .clear();
        b.outers.putAll(a.outers);
        b.roots.addAll(a.roots);
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : b.roots) {
            o.setOwner(this);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : b.outers.values()) {
            o.setOwner(this);
        }
        b.onNodeAdded(ns);
        a.onNodeSetChanged();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        b.onEdgeAdded(es);
        a.onEdgeSetChanged();
        assertConsistency();
    }

    /**
     * Nest the current bigraphbuilder with the bigraph in input. <br />
     * Nesting, differently from composition, add bigraph's outernames to
     * bigraphbuilder if they aren't already present.
     *
     * @param graph the "inner" bigraph
     */
    public void innerNest(Bigraph graph) {
        innerNest(graph, false);
    }

    /**
     * Nest the current bigraphbuilder with the bigraph in input. <br />
     * Nesting, differently from composition, add bigraph's outername to
     * bigraphbuilder if they aren't already present.
     *
     * @param graph the "inner" bigraph
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void innerNest(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph in = graph;
        Bigraph out = this.big;
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (out == in)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be composed with itself.");
        if (!out.signature.equals(in.signature)) {
            throw new IncompatibleSignatureException(out.signature,
                    in.signature);
        }
        if (!out.inners.isEmpty() || out.sites.size() != in.roots.size()) {
            throw new IncompatibleInterfaceException();
        }
        Map<String, it.uniud.mads.jlibbig.core.std.EditableOuterName> nmap = new HashMap<>();
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : out.outers.values()) {
            nmap.put(o.getName(), o);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : in.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableOuterName p = nmap.get(o.getName());
            if (p == null) {
                p = (it.uniud.mads.jlibbig.core.std.EditableOuterName) this.addOuterName(o.getName());
            }
            this.addInnerName(o.getName(), p);
        }
        this.innerCompose(in, reuse);
    }

    /**
     * Nest bigraph in input with the current bigraphbuilder. <br />
     * Nesting, differently from composition, add bigraph's outername to
     * bigraphbuilder if they aren't already present. It will then perform the
     * standard composition.
     *
     * @param graph the "inner" bigraph
     */
    public void outerNest(Bigraph graph) {
        outerNest(graph, false);
    }

    /**
     * Nest bigraph in input with the current bigraphbuilder. <br />
     * Nesting, differently from composition, add bigraph's outername to
     * bigraphbuilder if they aren't already present. It will then perform the
     * standard composition.
     *
     * @param graph the "inner" bigraph
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void outerNest(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph in = this.big;
        Bigraph out = graph;
        if (out == in)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be composed with itself.");
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (!out.signature.equals(in.signature)) {
            throw new IncompatibleSignatureException(out.signature,
                    in.signature);
        }
        if (!out.inners.isEmpty() || out.sites.size() != in.roots.size()) {
            throw new IncompatibleInterfaceException();
        }
        if (reuse)
            out = out.clone();
        Map<String, it.uniud.mads.jlibbig.core.std.EditableOuterName> nmap = new HashMap<>();
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : out.outers.values()) {
            nmap.put(o.getName(), o);
        }
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : in.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableOuterName p = nmap.get(o.getName());
            if (p == null) {
                p = new it.uniud.mads.jlibbig.core.std.EditableOuterName(o.getName());
                p.setOwner(out);
                out.outers.put(p.getName(), p);
            }
            it.uniud.mads.jlibbig.core.std.EditableInnerName i = new it.uniud.mads.jlibbig.core.std.EditableInnerName(p.getName());
            i.setHandle(p);
            out.inners.put(i.getName(), i);
        }
        // System.out.println(in.toString() + in.isConsistent(this));
        // System.out.println(out.toString() + out.isConsistent());
        this.outerCompose(out, false);
    }

    /**
     * Juxtapose bigraph in input with the current bigraphbuilder. <br />
     * ParallelProduct, differently from the normal juxtapose, doesn't need
     * disjoint sets of outernames for the two bigraphs. Common outernames will
     * be merged. <br />
     * Roots and sites of the bigraph will precede those of the bigraphbuilder
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void leftParallelProduct(Bigraph graph) {
        leftParallelProduct(graph, false);
    }

    /**
     * Juxtapose bigraph in input with the current bigraphbuilder. <br />
     * ParallelProduct, differently from the normal juxtapose, doesn't need
     * disjoint sets of outernames for the two bigraphs. Common outernames will
     * be merged. <br />
     * Roots and sites of the bigraph will precede those of the bigraphbuilder
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void leftParallelProduct(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph left = graph;
        Bigraph right = this.big;
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (!left.signature.equals(right.signature)) {
            throw new IncompatibleSignatureException(left.signature,
                    right.signature);
        }
        if (!Collections.disjoint(left.inners.keySet(), right.inners.keySet())) {
            throw new IncompatibleInterfaceException(
                    new NameClashException(intersectNames(left.inners.values(),
                            right.inners.values())));
        }
        Bigraph l = (reuse) ? left : left.clone();
        Bigraph r = right;
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : l.roots) {
            o.setOwner(this);
        }
        Map<String, it.uniud.mads.jlibbig.core.std.EditableOuterName> os = new HashMap<>();
        // merge outers
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : l.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableOuterName q = null;
            for (it.uniud.mads.jlibbig.core.std.EditableOuterName p : r.outers.values()) {
                if (p.getName().equals(o.getName())) {
                    q = p;
                    break;
                }
            }
            if (q == null) {
                // o is not part of r.outerface
                os.put(o.getName(), o);
                o.setOwner(this);
            } else {
                // this name apperas also in r, merge points
                for (it.uniud.mads.jlibbig.core.std.EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                    q.linkPoint(p);
                }
            }
        }
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = l.edgesProxy.get();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        r.onEdgeAdded(es);
        r.onNodeAdded(l.nodesProxy.get());
        l.onEdgeSetChanged();
        l.onNodeSetChanged();
        r.roots.addAll(l.roots);
        r.sites.addAll(l.sites);
        r.outers.putAll(os);
        r.inners.putAll(l.inners);
        assertConsistency();
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * ParallelProduct, differently from the normal juxtapose, doesn't need
     * disjoint sets of outernames for the two bigraphs. Common outernames will
     * be merged. <br />
     * Roots and sites of the bigraphbuilder will precede those of the bigraph
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void rightParallelProduct(Bigraph graph) {
        rightParallelProduct(graph, false);
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * ParallelProduct, differently from the normal juxtapose, doesn't need
     * disjoint sets of outernames for the two bigraphs. Common outernames will
     * be merged. <br />
     * Roots and sites of the bigraphbuilder will precede those of the bigraph
     * in the resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void rightParallelProduct(Bigraph graph, boolean reuse) {
        assertOpen();
        Bigraph left = this.big;
        Bigraph right = graph;
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (left == right)
            throw new IllegalArgumentException(
                    "Operand shuld be distinct; a bigraph can not be juxtaposed with itself.");
        if (!left.signature.equals(right.signature)) {
            throw new IncompatibleSignatureException(left.signature,
                    right.signature);
        }
        if (!Collections.disjoint(left.inners.keySet(), right.inners.keySet())) {
            throw new IncompatibleInterfaceException(
                    new NameClashException(intersectNames(left.inners.values(),
                            right.inners.values())));
        }
        Bigraph l = left;
        Bigraph r = (reuse) ? right : right.clone();
        for (it.uniud.mads.jlibbig.core.std.EditableOwned o : r.roots) {
            o.setOwner(this);
        }
        Map<String, it.uniud.mads.jlibbig.core.std.EditableOuterName> os = new HashMap<>();
        // merge outers
        for (it.uniud.mads.jlibbig.core.std.EditableOuterName o : r.outers.values()) {
            it.uniud.mads.jlibbig.core.std.EditableOuterName q = null;
            for (it.uniud.mads.jlibbig.core.std.EditableOuterName p : l.outers.values()) {
                if (p.getName().equals(o.getName())) {
                    q = p;
                    break;
                }
            }
            if (q == null) {
                // o is not part of r.outerface
                os.put(o.getName(), o);
                o.setOwner(this);
            } else {
                // this name apperas also in r, merge points
                for (it.uniud.mads.jlibbig.core.std.EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                    q.linkPoint(p);
                }
            }
        }
        Collection<it.uniud.mads.jlibbig.core.std.EditableEdge> es = r.edgesProxy.get();
        for (it.uniud.mads.jlibbig.core.std.EditableEdge e : es) {
            e.setOwner(this);
        }
        l.onEdgeAdded(es);
        l.onNodeAdded(r.nodesProxy.get());
        r.onEdgeSetChanged();
        r.onNodeSetChanged();
        l.roots.addAll(r.roots);
        l.sites.addAll(r.sites);
        l.outers.putAll(os);
        l.inners.putAll(r.inners);
        assertConsistency();
    }

    /**
     * Juxtapose bigraph in input with the current bigraphbuilder. <br />
     * Perform then {@link DirectedBigraphBuilder#merge()} on the resulting
     * bigraphbuilder. <br />
     * Sites of the bigraph will precede those of the bigraphbuilder in the
     * resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void leftMergeProduct(Bigraph graph) {
        leftMergeProduct(graph, false);
    }

    /**
     * Juxtapose bigraph in input with the current bigraphbuilder. <br />
     * Perform then {@link DirectedBigraphBuilder#merge()} on the resulting
     * bigraphbuilder. <br />
     * Sites of the bigraph will precede those of the bigraphbuilder in the
     * resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void leftMergeProduct(Bigraph graph, boolean reuse) {
        leftJuxtapose(graph, reuse);
        merge();
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Perform then {@link DirectedBigraphBuilder#merge()} on the resulting
     * bigraphbuilder. <br />
     * Sites of the bigraphbuilder will precede those of the bigraph in the
     * resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     */
    public void rightMergeProduct(Bigraph graph) {
        rightMergeProduct(graph, false);
    }

    /**
     * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
     * Perform then {@link DirectedBigraphBuilder#merge()} on the resulting
     * bigraphbuilder. <br />
     * Sites of the bigraphbuilder will precede those of the bigraph in the
     * resulting bigraphbuilder.
     *
     * @param graph bigraph that will be juxtaposed.
     * @param reuse flag. If true, the bigraph in input won't be copied.
     */
    public void rightMergeProduct(Bigraph graph, boolean reuse) {
        rightJuxtapose(graph, reuse);
        merge();
    }

    private void assertConsistency() {
        if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
            throw new RuntimeException("Inconsistent bigraph.");
    }

    private void assertOwner(Owned owned, String obj) {
        if (owned == null)
            throw new IllegalArgumentException(obj + " can not be null.");
        Owner o = owned.getOwner();
        if (o != this)
            throw new UnexpectedOwnerException(obj
                    + " should be owned by this structure.");
    }

    private void assertOrSetOwner(Owned owned, String obj) {
        if (owned == null)
            throw new IllegalArgumentException(obj + " can not be null.");
        Owner o = owned.getOwner();
        if (o == null)
            ((it.uniud.mads.jlibbig.core.std.EditableOwned) owned).setOwner(this);
        else if (o != this)
            throw new UnexpectedOwnerException(obj
                    + " already owned by an other structure.");
    }
}
