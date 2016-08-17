package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.exceptions.IncompatibleInterfaceException;
import it.uniud.mads.jlibbig.core.exceptions.IncompatibleSignatureException;
import it.uniud.mads.jlibbig.core.exceptions.NameClashException;
import it.uniud.mads.jlibbig.core.util.CachingProxy;
import it.uniud.mads.jlibbig.core.util.Provider;

import java.util.*;

/**
 * Objects created from this class are directed bigraphs with abstract internal names
 * (i.e. {@link Node} equality is reference based) whereas link interfaces still
 * use concrete names. Instances of this class are immutable and can be created
 * by means of the factory methods provided by this class like e.g.
 * {@link #makeEmpty}, {@link #makeId}, {@link #compose}, and {@link #juxtapose}
 * ; or from instances of  BigraphBuilder}.
 */
/*
 * For efficiency reasons immutability can be relaxed by the user (cf. {@link
 * #compose(Bigraph, Bigraph, boolean)}) allowing the reuse of (all or parts) of
 * these objects.
 */

final public class DirectedBigraph implements
        it.uniud.mads.jlibbig.core.Bigraph<DirectedControl>, Cloneable {

    static final Collection<Parent> EMPTY_ANCS_LST = Collections.unmodifiableList(Collections.emptyList());
    private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean.getBoolean("it.uniud.mads.jlibbig.consistency")
            || Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.bigraphops");
    private static final Comparator<DirectedControl> controlComparator = new Comparator<DirectedControl>() {
        @Override
        public int compare(DirectedControl o1, DirectedControl o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    private static final Comparator<Node> nodeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            int c = controlComparator.compare(o1.getControl(), o2.getControl());
            if (c == 0)
                return o1.getEditable().getName()
                        .compareTo(o2.getEditable().getName());
            else
                return c;
        }
    };
    private static final Comparator<InPort> inPortComparator = new Comparator<InPort>() {
        @Override
        public int compare(InPort i1, InPort i2) {
            if (i1 == i2)
                return 0;
            int c = nodeComparator.compare(i1.getNode(), i2.getNode());
            if (c == 0)
                return (i1.getNumber() < i2.getNumber()) ? -1 : 1;
            else
                return c;
        }
    };
    private static final Comparator<OutPort> outPortComparator = new Comparator<OutPort>() {
        @Override
        public int compare(OutPort o1, OutPort o2) {
            if (o1 == o2)
                return 0;
            int c = nodeComparator.compare(o1.getNode(), o2.getNode());
            if (c == 0)
                return (o1.getNumber() < o2.getNumber()) ? -1 : 1;
            else
                return c;
        }
    };
    private static final Comparator<InnerName> innerComparator = new Comparator<InnerName>() {
        @Override
        public int compare(InnerName o1, InnerName o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    private static final Comparator<Point> pointComparator = new Comparator<Point>() {
        @Override
        public int compare(Point o1, Point o2) {
            if (o1.isPort()) {
                if (o2.isPort()) {
                    return outPortComparator.compare((OutPort) o1, (OutPort) o2);
                } else {
                    return -1;
                }
            } else {
                if (o2.isPort()) {
                    return 1;
                } else {
                    return innerComparator.compare((InnerName) o1,
                            (InnerName) o2);
                }
            }
        }
    };
    private static final Comparator<OuterName> outerComparator = new Comparator<OuterName>() {
        @Override
        public int compare(OuterName o1, OuterName o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    private static final Comparator<Edge> edgeComparator = new Comparator<Edge>() {
        @Override
        public int compare(Edge o1, Edge o2) {
            return o1.getEditable().getName()
                    .compareTo(o2.getEditable().getName());
        }
    };
    final DirectedSignature signature;
    final List<EditableRoot> roots = new ArrayList<>();
    final List<EditableSite> sites = new ArrayList<>();
    final Interface<EditableOuterName, EditableInnerName> outers = new Interface<>();
    final Interface<EditableInnerName, EditableOuterName> inners = new Interface<>();
    private final Comparator<Child> childComparator = new Comparator<Child>() {
        @Override
        public int compare(Child o1, Child o2) {
            if (o1.isSite()) {
                if (o2.isSite()) {
                    return (sites.indexOf(o1) < sites.indexOf(o2)) ? -1 : 1;
                } else {
                    return 1;
                }
            } else {
                if (o2.isSite()) {
                    return -1;
                } else {
                    return nodeComparator.compare((Node) o1, (Node) o2);
                }
            }
        }
    };
    private final List<? extends Root> ro_roots = Collections.unmodifiableList(roots);
    private final List<? extends Site> ro_sites = Collections.unmodifiableList(sites);
    /* The set of nodes composing a bigraph is derived by visiting its place graph.
     * On one hand storing this information in some collection introduces
     * redundancies on the other end retrieving it every time it is needed may result
     * too costly since bigraphs are meant to be immutable (up to internal optimisations).
     * To mitigate between the two sides we use a caching proxy to access this information:
     * the proxy takes care of handling the cache accordingly to the machine load.
     * The cache is populated invoking the medthod provideNodes whereas
     * onNodeAdded, onNodeRemoved, and onNodesChanged should be invoked to inform
     * the structure of potentially invalidating changes.
     * */
    CachingProxy<Collection<EditableNode>> nodesProxy = new CachingProxy<>(
            new Provider<Collection<EditableNode>>() {
                @Override
                public Collection<EditableNode> get() {
                    return provideNodes();
                }
            });
    /* Edges are handled like nodes, see getNodes() */
    CachingProxy<Collection<EditableEdge>> edgesProxy = new CachingProxy<>(
            new Provider<Collection<EditableEdge>>() {
                @Override
                public Collection<EditableEdge> get() {
                    return provideEdges();
                }
            });
    private Map<Child, Collection<Parent>> ancestors = new WeakHashMap<>();

    DirectedBigraph(DirectedSignature sig) {
        if (sig == null)
            throw new IllegalArgumentException("Signature can not be null.");
        this.signature = sig;
    }

    /**
     * Juxtaposes two bigraph. The first argument will be the left operand that
     * is its roots and sites will precede those of the second argument in the
     * outcome.
     *
     * @param left  the first bigraph.
     * @param right the second bigraph.
     * @return the juxtaposition of the arguments.
     */
    public static DirectedBigraph juxtapose(DirectedBigraph left, DirectedBigraph right) {
        return juxtapose(left, right, false);
    }

    /**
     * Juxtaposes two bigraph. The first argument will be the left operand that
     * is its roots and sites will precede those of the second argument in the
     * outcome. Optionally, arguments can be reused.
     *
     * @param left  the first bigraph.
     * @param right the second bigraph.
     * @param reuse flag. If true, bigraphs in input will not be copied.
     * @return the juxtaposition of the arguments.
     */
    static DirectedBigraph juxtapose(DirectedBigraph left, DirectedBigraph right, boolean reuse) {
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (left == right)
            throw new IllegalArgumentException("Operand shuld be distinct; a bigraph can not be juxtaposed with itself.");
        if (!left.signature.equals(right.signature)) {
            throw new IncompatibleSignatureException(left.getSignature(), right.getSignature());
        }
        if (!Collections.disjoint(left.inners.getAsc(), right.inners.getAsc())
                || !Collections.disjoint(left.inners.getDesc(), right.inners.getDesc())
                || !Collections.disjoint(left.outers.getAsc(), right.outers.getAsc())
                || !Collections.disjoint(left.outers.getDesc(), right.outers.getDesc())) {
            throw new IncompatibleInterfaceException(new NameClashException(
                    Interface.intersectNames(
                            left.inners.getAsc(),
                            right.inners.getAsc(),
                            Interface.intersectNames(left.outers.getAsc(),
                                    right.outers.getAsc(),
                                    Interface.intersectNames(left.outers.getDesc(),
                                            right.outers.getDesc(),
                                            Interface.intersectNames(left.inners.getDesc(),
                                                    right.inners.getDesc()))))));
        }
        DirectedBigraph l = (reuse) ? left : left.clone();
        DirectedBigraph r = (reuse) ? right : right.clone();

        for (EditableOwned o : r.roots) {
            o.setOwner(l);
        }
        for (EditableOwned o : r.outers.getAsc()) {
            o.setOwner(l);
        }
        for (EditableOwned o : r.inners.getDesc()) {
            o.setOwner(l);
        }
        Collection<EditableEdge> es = r.edgesProxy.get();
        for (EditableEdge e : es) {
            e.setOwner(l);
        }
        l.onEdgeAdded(es);
        l.onNodeAdded(r.nodesProxy.get());
        r.onEdgeSetChanged();
        r.onNodeSetChanged();
        l.roots.addAll(r.roots);
        l.sites.addAll(r.sites);

        Interface.joinInterfaces(l.outers, r.outers);
        Interface.joinInterfaces(l.inners, r.inners);

        if (DEBUG_CONSISTENCY_CHECK && !l.isConsistent()) {
            throw new RuntimeException("Inconsistent bigraph");
        }
        return l;
    }

    /**
     * Composes two bigraphs. The first argument will be the outer bigraph that
     * is the one composed to the outer face of the second argument.
     *
     * @param out the outer bigraph
     * @param in  the inner bigraph
     * @return the composition of the arguments.
     */
    public static DirectedBigraph compose(DirectedBigraph out, DirectedBigraph in) {
        return compose(out, in, false);
    }

    /**
     * Composes two bigraphs. The first argument will be the outer bigraph that
     * is the one composed to the outer face of the second argument. Optionally,
     * arguments can be reused.
     *
     * @param out   the outer bigraph
     * @param in    the inner bigraph
     * @param reuse flag. If true, bigraphs in input will not be copied.
     * @return the composition of the arguments.
     */
    static DirectedBigraph compose(DirectedBigraph out, DirectedBigraph in, boolean reuse) {
        // Arguments are assumed to be consistent (e.g. parent and links are
        // well defined)
        if (out == in)
            throw new IllegalArgumentException("Operand shuld be distinct; a bigraph can not be composed with itself.");
        if (!out.signature.equals(in.signature)) {
            throw new IncompatibleSignatureException(out.getSignature(), in.getSignature());
        }

        Set<String> xs = new HashSet<>(out.inners.keySet());
        Set<String> ys = new HashSet<>(in.outers.keySet());
        Set<String> zs = new HashSet<>(xs);
        xs.removeAll(ys);
        ys.removeAll(zs);

        if (!xs.isEmpty() || !ys.isEmpty() || out.sites.size() != in.roots.size()) {
            throw new IncompatibleInterfaceException("The outer face of the first graph must be equal to inner face of the second");
        }

        DirectedBigraph a = (reuse) ? out : out.clone();
        DirectedBigraph b = (reuse) ? in : in.clone();
        Collection<EditableEdge> es = b.edgesProxy.get();
        Collection<EditableNode> ns = b.nodesProxy.get();
        // iterate over sites and roots of a and b respectively and glue them
        Iterator<EditableRoot> ir = b.roots.iterator();
        Iterator<EditableSite> is = a.sites.iterator();
        while (ir.hasNext()) { // |ir| == |is|
            EditableSite s = is.next();
            EditableParent p = s.getParent();
            p.removeChild(s);
            for (EditableChild c : new ArrayList<>(ir.next().getEditableChildren())) {
                c.setParent(p);
            }
        }
        // iterate over inner and outer names of a and b respectively and glue
        // them
        Map<String, EditableHandle> a_inners = new HashMap<>();
        for (EditableInnerName i : a.inners.values()) {
            a_inners.put(i.getName(), i.getHandle());
            i.setHandle(null);
        }
        for (EditableOuterName o : b.outers.values()) {
            EditableHandle h = a_inners.get(o.getName());
            for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                p.setHandle(h);
            }
        }
        // update inner interfaces
        a.inners.clear();
        a.sites.clear();
        a.inners.putAll(b.inners);
        a.sites.addAll(b.sites);
        a.onNodeAdded(ns);
        b.onNodeSetChanged();
        for (EditableEdge e : es) {
            e.setOwner(a);
        }
        a.onEdgeAdded(es);
        b.onEdgeSetChanged();
        if (DEBUG_CONSISTENCY_CHECK && !a.isConsistent()) {
            throw new RuntimeException("Inconsistent bigraph");
        }
        return a;
    }

    /**
     * Creates an empty bigraph for the given signature.
     *
     * @param signature the signature of the bigraph.
     * @return the empty bigraph.
     */
    public static DirectedBigraph makeEmpty(DirectedSignature signature) {
        return new DirectedBigraph(signature);
    }

    /**
     * Creates an identity bigraph i.e. a bigraph without nodes where every site
     * is the only child of the root at the same index and every inner name is
     * the only point of the outer name with the same concrete name.
     *
     * @param signature the signature of the bigraph.
     * @param width     the number of roots/sites.
     * @param names     the names of its link faces.
     * @return the resulting identity bigraph.
     */
    public static DirectedBigraph makeId(DirectedSignature signature, int width, String... names) {
        DirectedBigraphBuilder bb = new DirectedBigraphBuilder(signature);
        for (int i = 0; i < width; i++) {
            bb.addSite(bb.addRoot());
        }
        for (String name : names)
            bb.addInnerName(name, bb.addOuterName(name));
        return bb.makeBigraph();
    }

    /**
     * Creates an identity bigraph i.e. a bigraph without nodes where every site
     * is the only child of the root at the same index and every inner name is
     * the only point of the outer name with the same concrete name.
     *
     * @param signature the signature of the bigraph.
     * @param width     the number of roots/sites.
     * @param names     the set of names that will appear in resulting bigraph's link
     *                  faces.
     * @return an identity bigraph.
     */
    public static DirectedBigraph makeId(DirectedSignature signature, int width,
                                         Iterable<? extends LinkFacet> names) {
        DirectedBigraphBuilder bb = new DirectedBigraphBuilder(signature);
        for (int i = 0; i < width; i++) {
            bb.addSite(bb.addRoot());
        }
        for (LinkFacet f : names) {
            String name = f.getName();
            bb.addInnerName(name, bb.addOuterName(name));
        }
        return bb.makeBigraph();
    }

    /**
     * Checks the consistency of the bigraph.
     *
     * @return a boolean indicating whether the bigraph is consistent.
     */
    boolean isConsistent() {
        return this.isConsistent(this);
    }

    /**
     * Checks the consistency of the bigraph. Optionally, an owner different
     * from this object can be specified.
     *
     * @param owner the alternative owner.
     * @return a boolean indicating whether the bigraph is consistent.
     */
    boolean isConsistent(Owner owner) {
        Set<Point> seen_points = new HashSet<>();
        Set<Handle> seen_handles = new HashSet<>();
        Set<Site> unseen_sites = new HashSet<>();
        unseen_sites.addAll(this.sites);
        Set<Child> seen_children = new HashSet<>();
        Deque<Parent> q = new ArrayDeque<>();
        for (EditableRoot r : this.roots) {
            if (r.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign root");
                return false;
            }
            q.add(r);
        }
        while (!q.isEmpty()) {
            Parent p = q.poll();
            for (Child c : p.getChildren()) {
                if (!p.equals(c.getParent())) {
                    System.err.println("INCOSISTENCY: parent/child mismatch");
                    return false;
                }
                if (!seen_children.add(c)) {
                    // c was already visited
                    // we have found a cycle in the place structure
                    System.err.println("INCOSISTENCY: cyclic place");
                    return false;
                } else if (c.isNode()) {
                    EditableNode n = (EditableNode) c;
                    if (n.getControl().getArityIn() != n.getInPorts().size()
                            || n.getControl().getArityOut() != n.getOutPorts().size()
                            || !signature.contains(n.getControl())) {
                        System.err.println("INCOSISTENCY: control/arity");
                        return false;
                    }
                    q.add(n);
                    for (Point t : n.getOutPorts()) {
                        EditableHandle h = ((EditablePoint) t).getHandle();
                        if (h == null || h.getOwner() != owner) {
                            // foreign or broken handle
                            System.out.println(this);
                            System.err.println("INCOSISTENCY: broken or foreign handle");
                            return false;
                        }
                        if (!h.getPoints().contains(t)) {
                            // broken link chain
                            System.err.println("INCOSISTENCY: handle/point mismatch");
                            return false;
                        }
                        seen_points.add(t);
                        seen_handles.add(h);
                    }
                    for (Handle h : n.getInPorts()) {
                        EditableHandle eh = h.getEditable();
                        if (eh == null || eh.getOwner() != owner) {
                            // foreign or broken handle
                            System.out.println(this);
                            System.err.println("INCOSISTENCY: broken or foreign handle");
                            return false;
                        }
                        seen_handles.add(eh);

                    }
                } else if (c.isSite()) {
                    Site s = (Site) c;
                    unseen_sites.remove(s);
                    if (!this.sites.contains(s)) {
                        System.err.println("INCOSISTENCY: foreign site");
                        // unknown site
                        return false;
                    }
                } else {
                    System.err.println("INCOSISTENCY: neither a node nor a site");
                    // c is neither a site nor a node
                    return false;
                }
            }
        }
        for (EditableOuterName outAsc : this.outers.getAsc()) {
            if (outAsc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign ascendant name in outer interface");
                return false;
            }
            seen_handles.add(outAsc);
        }
        for (EditableOuterName inDesc : this.inners.getDesc()) {
            if (inDesc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign descendant name in inner interface");
                return false;
            }
            seen_handles.add(inDesc);
        }
        // System.out.println(seen_points);
        for (EditableInnerName inAsc : this.inners.getAsc()) {
            if (inAsc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign ascendant name in inner interface");
                return false;
            }
            seen_handles.add(inAsc.getHandle());
            seen_points.add(inAsc);
        }
        for (EditableInnerName outDesc : this.outers.getDesc()) {
            if (outDesc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign descendant name in outer interface");
                return false;
            }
            seen_handles.add(outDesc.getHandle());
            seen_points.add(outDesc);
        }
        // System.out.println(this);
        // System.out.println(seen_points);
        for (Handle h : seen_handles) {
            // System.out.println(h + ": " + h.getPoints());
            for (Point p : h.getPoints()) {
                // System.out.println(p + ", " + p.getHandle() + ", " + h);
                if (!seen_points.remove(p)) {
                    // foreign point
                    System.err.println("INCOSISTENCY: foreign point");
                    return false;
                }
            }
        }
        // System.out.println(ps);
        if (seen_points.size() > 0) {
            // broken handle chain
            System.err.println("INCOSISTENCY: handle chain broken");
            return false;
        }
        if (unseen_sites.size() > 0) {
            // these sites are unreachable from roots
            System.err.println("INCOSISTENCY: unreachable site");
            return false;
        }
        return true;
    }

    /**
     * Sets the owner of internal structures of this bigraph (but leaves them
     * connected to it). Be careful. This method is meant to be used by builders
     * to avoid leaking references to their internal working bigraph. If the
     * argument is null, the owner is set to this bigraph.
     *
     * @param owner the owner to set
     * @return this bigraph
     */
    DirectedBigraph setOwner(Owner owner) {
        if (owner == null) {
            owner = this;
        }
        for (EditableOwned o : this.roots) {
            o.setOwner(owner);
        }
        for (EditableOwned o : this.inners.getDesc()) {
            o.setOwner(owner);
        }
        for (EditableOwned o : this.outers.getAsc()) {
            o.setOwner(owner);
        }
        for (Edge e : this.getEdges()) {
            ((EditableOwned) e).setOwner(owner);
        }
        return this;
    }

    @Override
    public DirectedBigraph clone() {
        return this.clone(null);
    }

    /**
     * Same as clone, but additionally sets a custom owner for the internal s
     * tructures of the cloned bigraph. It corresponds to
     * <code>someBigraph.clone().setOwner(someOwner)</code>. If the argument is
     * null, the owner is set to the cloned bigraph.
     *
     * @param owner the owner of the new bigraph
     * @return a copy of this bigraph.
     */
    DirectedBigraph clone(Owner owner) {
        /*
         * firstly clone inner and outer names and store handles into a
		 * translation map since ports are not yet cloned. then clones the place
		 * graph structure following the parent map from roots to sites. during
		 * the visit follows outgoing links from ports and clones edges and
		 * outer names if these are not already present into the translation
		 * map. Idle edges are lost during the process since these are not
		 * reachable. The procedure may not terminate or raise exceptions if the
		 * bigraph is inconsistent (e.g. loops into the parent map or foreign
		 * sites/names)
		 */
        DirectedBigraph big = new DirectedBigraph(this.signature);
        // owner == null -> self
        if (owner == null)
            owner = big;
        Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
        // replicate outer names
        for (EditableOuterName o1 : this.outers.values()) {
            EditableOuterName o2 = o1.replicate();
            big.outers.put(o2.getName(), o2);
            o2.setOwner(owner);
            hnd_dic.put(o1, o2);
        }
        // replicate inner interface
        for (EditableInnerName i1 : this.inners.values()) {
            EditableInnerName i2 = i1.replicate();
            EditableHandle h1 = i1.getHandle();
            EditableHandle h2 = hnd_dic.get(h1);
            if (h2 == null) {
                // the bigraph is inconsistent if g is null
                h2 = h1.replicate();
                h2.setOwner(owner);
                hnd_dic.put(h1, h2);
            }
            i2.setHandle(h2);
            big.inners.put(i2.getName(), i2);
        }
        // replicate place structure
        // the queue is used for a breadth first visit
        class Pair {
            final EditableChild c;
            final EditableParent p;

            Pair(EditableParent p, EditableChild c) {
                this.c = c;
                this.p = p;
            }
        }
        Queue<Pair> q = new LinkedList<>();
        for (EditableRoot r1 : this.roots) {
            EditableRoot r2 = r1.replicate();
            big.roots.add(r2);
            r2.setOwner(owner);
            for (EditableChild c : r1.getEditableChildren()) {
                q.add(new Pair(r2, c));
            }
        }
        EditableSite[] sites = new EditableSite[this.sites.size()];
        while (!q.isEmpty()) {
            Pair t = q.poll();
            if (t.c.isNode()) {
                EditableNode n1 = (EditableNode) t.c;
                EditableNode n2 = n1.replicate();
                // set m's parent (which added adds m as its child)
                n2.setParent(t.p);
                for (int i = n1.getControl().getArityOut() - 1; 0 <= i; i--) {
                    EditableNode.EditableOutPort p1 = n1.getOutPort(i);
                    EditableHandle h1 = p1.getHandle();
                    // looks for an existing replica
                    EditableHandle h2 = hnd_dic.get(h1);
                    if (h2 == null) {
                        // the bigraph is inconsistent if g is null
                        h2 = h1.replicate();
                        h2.setOwner(owner);
                        hnd_dic.put(h1, h2);
                    }
                    n2.getOutPort(i).setHandle(h2);
                }
                for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
                    EditableNode.EditableInPort p1 = n1.getInPort(i);
                    EditableHandle h1 = p1.getHandle();
                    // looks for an existing replica
                    EditableHandle h2 = hnd_dic.get(h1);
                    if (h2 == null) {
                        // the bigraph is inconsistent if g is null
                        h2 = h1.replicate();
                        h2.setOwner(owner);
                        hnd_dic.put(h1, h2);
                    }
                }
                // enqueue children for visit
                for (EditableChild c : n1.getEditableChildren()) {
                    q.add(new Pair(n2, c));
                }
            } else {
                // c instanceof EditableSite
                EditableSite s1 = (EditableSite) t.c;
                EditableSite s2 = s1.replicate();
                s2.setParent(t.p);
                sites[this.sites.indexOf(s1)] = s2;
            }
        }
        big.sites.addAll(Arrays.asList(sites));
        return big;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.uniud.mads.jlibbig.core.AbstBigraph#getSignature()
     */
    @Override
    public DirectedSignature getSignature() {
        return this.signature;
    }

    @Override
    public boolean isEmpty() {
        return this.outers.isEmpty() && this.inners.isEmpty()
                && this.roots.isEmpty() && this.sites.isEmpty();
    }

    @Override
    public boolean isGround() {
        return this.inners.isEmpty() && this.sites.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see it.uniud.mads.jlibbig.core.AbstBigraph#getRoots()
     */
    @Override
    public List<? extends Root> getRoots() {
        return this.ro_roots;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.uniud.mads.jlibbig.core.AbstBigraph#getSites()
     */
    @Override
    public List<? extends Site> getSites() {
        return this.ro_sites;
    }

	/* comparators used by toString */

    /**
     * @return the set of the outer names of the bigraph
     */
    @Override
    public Collection<? extends OuterName> getOuterNames() {
        Set<EditableOuterName> ou = this.outers.getAsc();
        ou.addAll(this.inners.getDesc());
        return ou;
    }

    /**
     * @return the set of the inner names of the bigraph
     */
    @Override
    public Collection<? extends InnerName> getInnerNames() {
        Set<EditableInnerName> in = this.inners.getAsc();
        in.addAll(this.outers.getDesc());
        return in;
    }

    /**
     * Adds the new node to the cached collection of all nodes.
     * No coherence controls are enforced and the update is not
     * propagated to the edge collection.
     *
     * @param node
     */
    void onNodeAdded(EditableNode node) {
        Collection<EditableNode> ns = nodesProxy.softGet();
        if (ns != null) {
            ns.add(node);
        }
    }

    /**
     * Adds the new nodes to the cached collection of all nodes.
     * No coherence controls are enforced and the update is not
     * propagated to the edge or ancestor collections.
     *
     * @param nodes
     */
    void onNodeAdded(Collection<EditableNode> nodes) {
        Collection<EditableNode> ns = nodesProxy.softGet();
        if (ns != null) {
            ns.addAll(nodes);
        }
    }

    /**
     * Removes the given node from the cached collection of all nodes.
     * No coherence controls are enforced and the update is not
     * propagated to the edge collection.
     *
     * @param node
     */
    void onNodeRemoved(EditableNode node) {
        ancestors.clear(); // very conservative, could be improved
        Collection<EditableNode> ns = nodesProxy.softGet();
        if (ns != null) {
            ns.remove(node);
        }
    }

    /**
     * Removes the given nodes from the cached collection of all nodes.
     * No coherence controls are enforced and the update is not
     * propagated to the edge collection.
     *
     * @param nodes
     */
    void onNodeRemoved(Collection<EditableNode> nodes) {
        ancestors.clear(); // very conservative, could be improved
        Collection<EditableNode> ns = nodesProxy.softGet();
        if (ns != null) {
            ns.removeAll(nodes);
        }
    }

    void onNodeSetChanged() {
        this.nodesProxy.invalidate();
        this.ancestors.clear();
    }

    private Collection<EditableNode> provideNodes() {
        Set<EditableNode> s = new HashSet<>();
        Queue<EditableNode> q = new LinkedList<>();
        for (Root r : this.roots) {
            for (Child c : r.getChildren()) {
                if (c.isNode()) {
                    EditableNode n = (EditableNode) c;
                    q.add(n);
                }
            }
        }
        while (!q.isEmpty()) {
            EditableNode p = q.poll();
            s.add(p);
            for (Child c : p.getChildren()) {
                if (c.isNode()) {
                    EditableNode n = (EditableNode) c;
                    q.add(n);
                }
            }
        }
        return s;
    }

    //
    // private static final Comparator<Handle> handleComparator = new
    // Comparator<Handle>() {
    // @Override
    // public int compare(Handle o1, Handle o2) {
    // if (o1.isEdge()) {
    // if (o2.isEdge()) {
    // return edgeComparator.compare((Edge) o1, (Edge) o2);
    // } else {
    // return 1;
    // }
    // } else {
    // if (o2.isEdge()) {
    // return -1;
    // } else {
    // return outerComparator.compare((OuterName) o1,
    // (OuterName) o2);
    // }
    // }
    // }
    // };


    @Override
    public Collection<? extends Node> getNodes() {
        return nodesProxy.get();
    }

    void onEdgeAdded(EditableEdge edge) {
        Collection<EditableEdge> ns = edgesProxy.softGet();
        if (ns != null) {
            ns.add(edge);
        }
    }

    void onEdgeAdded(Collection<EditableEdge> edges) {
        Collection<EditableEdge> ns = edgesProxy.softGet();
        if (ns != null) {
            ns.addAll(edges);
        }
    }

    void onEdgeRemoved(EditableEdge edge) {
        Collection<EditableEdge> ns = edgesProxy.softGet();
        if (ns != null) {
            ns.remove(edge);
        }
    }

    void onEdgeRemoved(Collection<EditableEdge> edges) {
        Collection<EditableEdge> ns = edgesProxy.softGet();
        if (ns != null) {
            ns.removeAll(edges);
        }
    }

    void onEdgeSetChanged() {
        this.nodesProxy.invalidate();
    }

    public Collection<EditableEdge> provideEdges() {
        Iterable<? extends Node> nodes = getNodes();
        Set<EditableEdge> s = new HashSet<>();
        for (Node n : nodes) {
            for (OutPort p : n.getOutPorts()) {
                Handle h = p.getHandle();
                if (h.isEdge()) {
                    s.add((EditableEdge) h);
                }
            }
        }
        for (InnerName n : this.inners.getAsc()) {
            Handle h = n.getHandle();
            if (h.isEdge()) {
                s.add((EditableEdge) h);
            }
        }
        for (InnerName n : this.outers.getDesc()) {
            Handle h = n.getHandle();
            if (h.isEdge()) {
                s.add((EditableEdge) h);
            }
        }
        return s;
    }

    @Override
    public Collection<? extends Edge> getEdges() {
        return this.edgesProxy.get();
    }

    Collection<Parent> getAncestors(Child child) {
        if (child == null) {
            throw new IllegalArgumentException("The argument can not be null.");
        }
//		if(child.getOwner() != this){
//			throw new ForeignArgumentException("The argument does not belong to this bigraph.");
//		}
        Collection<Parent> s = ancestors.get(child);
        if (s == null) {
            Parent parent = child.getParent();
            if (parent.isRoot()) {
                s = EMPTY_ANCS_LST;
            } else {
                s = new LinkedList<>(getAncestors((Child) parent));
                s.add(parent);
            }
            ancestors.put(child, s);
        }
        return s;
    }

    @Override
    public String toString() {

        String nl = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        b.append(signature.getUSID());
        b.append(" {");
        Iterator<DirectedControl> is = this.signature.iterator();
        while (is.hasNext()) {
            b.append(is.next().toString());
            if (is.hasNext())
                b.append(", ");
        }
        b.append("} :: ");

        b.append(inners.toString());
        b.append(" -> ");
        b.append(outers.toString());

        b.append("}>");
        for (Handle h : this.inners.getDesc()) {
            b.append(nl).append(h);
            b.append(":o <- {");
            List<? extends Point> ps = new ArrayList<>(h.getPoints());
            Collections.sort(ps, pointComparator);
            Iterator<? extends Point> ip = ps.iterator();
            while (ip.hasNext()) {
                Point p = ip.next();
                b.append(p);
                if (p.isInnerName()) {
                    b.append(":i");
                }
                if (ip.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        for (Handle h : this.outers.getAsc()) {
            b.append(nl).append(h);
            b.append(":o <- {");
            List<? extends Point> ps = new ArrayList<>(h.getPoints());
            Collections.sort(ps, pointComparator);
            Iterator<? extends Point> ip = ps.iterator();
            while (ip.hasNext()) {
                Point p = ip.next();
                b.append(p);
                if (p.isInnerName()) {
                    b.append(":i");
                }
                if (ip.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        List<? extends Edge> es = new ArrayList<>(this.getEdges());
        Collections.sort(es, edgeComparator);
        for (Handle h : es) {
            b.append(nl).append(h);
            b.append(":e <- {");
            List<? extends Point> ps = new ArrayList<>(h.getPoints());
            Collections.sort(ps, pointComparator);
            Iterator<? extends Point> ip = ps.iterator();
            while (ip.hasNext()) {
                Point p = ip.next();
                b.append(p);
                if (p.isInnerName()) {
                    b.append(":i");
                }
                if (ip.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        Queue<Parent> q = new LinkedList<>();
        q.addAll(this.roots);
        while (!q.isEmpty()) {
            Parent p = q.poll();
            b.append(nl);
            if (p.isRoot()) {
                b.append(this.roots.indexOf(p));
            } else {
                b.append(p);
            }
            b.append(" <- {");
            List<? extends Child> cs = new ArrayList<>(p.getChildren());
            Collections.sort(cs, childComparator);
            Iterator<? extends Child> ic = cs.iterator();
            while (ic.hasNext()) {
                Child c = ic.next();
                if (c.isSite()) {
                    b.append(this.sites.indexOf(c));
                } else {
                    b.append(c);
                    q.add((Parent) c);
                }
                if (ic.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        return b.toString();
    }

    /*
     * //ATTACHED PROPERTIES
     * 
     * private final PropertyContainer props = new PropertyContainer();
     * 
     * @Override public Property<?> attachProperty(Property<?> prop) { return
     * props.attachProperty(prop); }
     * 
     * @Override public Property<?> detachProperty(Property<?> prop) { return
     * this.detachProperty(prop.getName()); }
     * 
     * @Override public Property<?> detachProperty(String name) { return
     * props.detachProperty(name); }
     * 
     * @Override public Property<?> getProperty(String name) { return
     * props.getProperty(name); }
     * 
     * @Override public Set<String> getPropertyNames() { return
     * props.getPropertyNames(); }
     */

    private static class Interface<Asc extends EditableLinkFacet, Desc extends EditableLinkFacet> {
        final List<InterfacePair<Asc, Desc>> names = new ArrayList<>();

        public Interface() {
            names.add(0, new InterfacePair<>(new HashSet<>(), new HashSet<>()));
        }

        Interface(InterfacePair<Asc, Desc> interfacePair0) {
            names.add(0, interfacePair0);
        }

        /**
         * Creates the collection containing all the names in the given collections
         * of link facets (i.e. inner and outer names).
         *
         * @param arg0 one of the collections to be intersected.
         * @param arg1 one of the collections to be intersected.
         * @return the intersection.
         */
        private static Collection<String> intersectNames(
                Collection<? extends LinkFacet> arg0,
                Collection<? extends LinkFacet> arg1) {
            return intersectNames(arg0, arg1, new HashSet<>());
        }

        /**
         * Extends the given collection of strings with all the names in the given
         * collections of link facets (i.e. inner and outer names).
         *
         * @param arg0 one of the collections to be intersected.
         * @param arg1 one of the collections to be intersected.
         * @param ns0  the collection to be extended.
         * @return the given string collection extended with the intersection of the
         * other two.
         */
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

        /**
         * join two interfaces
         *
         * @param i1 the first interface
         * @param i2 the second interface
         * @return the joined interface
         */
        public static <Asc extends EditableLinkFacet, Desc extends EditableLinkFacet> Interface<Asc, Desc> joinInterfaces(
                Interface<Asc, Desc> i1,
                Interface<Asc, Desc> i2) {

            Interface<Asc, Desc> i = new Interface<>(InterfacePair.mergePairs(i1.names.get(0), i2.names.get(0)));

            // skip first element because added before
            i.names.addAll(i1.names.subList(1, i1.names.size()));
            i.names.addAll(i2.names.subList(1, i2.names.size()));

            return i;
        }

        public int getWidth() {
            return names.size() - 1;
        }

        boolean isEmpty() {
            return this.names.isEmpty();
        }

        public void addPair(InterfacePair<Asc, Desc> interfacePair) {
            this.names.add(interfacePair);
        }

        public void addAsc(int index, Asc a) {
            this.names.get(index).getLeft().add(a);
        }

        Set<Asc> getAsc() {
            Set<Asc> asc = new HashSet<>();
            for (InterfacePair<Asc, Desc> ip : names) {
                asc.addAll(ip.getLeft());
            }
            return asc;
        }

        public Set<Asc> getAsc(int index) {
            if (index < 0 || index >= names.size()) {
                throw new IndexOutOfBoundsException("Index '" + index + "' not in list");
            }
            return names.get(index).getLeft();
        }

        public void addDesc(int index, Desc d) {
            this.names.get(index).getRight().add(d);
        }

        Set<Desc> getDesc() {
            Set<Desc> desc = new HashSet<>();
            for (InterfacePair<Asc, Desc> ip : names) {
                desc.addAll(ip.getRight());
            }
            return desc;
        }

        public Set<Desc> getDesc(int index) {
            if (index < 0 || index >= names.size()) {
                throw new IndexOutOfBoundsException("Index '" + index + "' not in list");
            }
            return names.get(index).getRight();
        }

        Set<String> keySet() {
            Set<String> ss = new HashSet<>();

            for (InterfacePair ip : names) {
                int id = names.indexOf(ip);

                for (Asc l : (Set<Asc>) ip.getLeft()) {
                    ss.add(id + " l " + l);
                }

                for (Desc r : (Set<Desc>) ip.getRight()) {
                    ss.add(id + " r " + r);
                }
            }
            return ss;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            Iterator<InterfacePair<Asc, Desc>> ii = names.iterator();
            while (ii.hasNext()) {
                sb.append(ii.next().toString());
                if (ii.hasNext())
                    sb.append(", ");
            }
            sb.append(">");
            return sb.toString();
        }
    }
}
