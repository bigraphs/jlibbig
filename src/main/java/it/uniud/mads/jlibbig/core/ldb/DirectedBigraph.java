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
        it.uniud.mads.jlibbig.core.DirectedBigraph<DirectedControl>, Cloneable {

    static final Collection<Parent> EMPTY_ANCS_LST = Collections.unmodifiableList(Collections.emptyList());
    private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean.getBoolean("it.uniud.mads.jlibbig.consistency")
            || Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.bigraphps");
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
                return o1.getEditable().getName().compareTo(o2.getEditable().getName());
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
        if (!Collections.disjoint(left.inners.getAsc(0).keySet(), right.inners.getAsc(0).keySet())
                || !Collections.disjoint(left.inners.getDesc(0).keySet(), right.inners.getDesc(0).keySet())
                || !Collections.disjoint(left.outers.getAsc(0).keySet(), right.outers.getAsc(0).keySet())
                || !Collections.disjoint(left.outers.getDesc(0).keySet(), right.outers.getDesc(0).keySet())) {
            throw new IncompatibleInterfaceException(new NameClashException(
                    Interface.intersectNames(
                            left.inners.getAsc(0).values(),
                            right.inners.getAsc(0).values(),
                            Interface.intersectNames(left.outers.getAsc(0).values(),
                                    right.outers.getAsc(0).values(),
                                    Interface.intersectNames(left.outers.getDesc(0).values(),
                                            right.outers.getDesc(0).values(),
                                            Interface.intersectNames(left.inners.getDesc(0).values(),
                                                    right.inners.getDesc(0).values()))))));
        }
        DirectedBigraph l = (reuse) ? left : left.clone();
        DirectedBigraph r = (reuse) ? right : right.clone();

        for (EditableOwned o : r.roots) {
            o.setOwner(l);
        }
        for (EditableOwned o : r.outers.getAsc().values()) {
            o.setOwner(l);
        }
        for (EditableOwned o : r.inners.getDesc().values()) {
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

        l.roots.addAll(0, r.roots);
        l.sites.addAll(0, r.sites);

        l.outers.join(r.outers);
        l.inners.join(r.inners);

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
        // Arguments are assumed to be consistent (e.g. parent and links are well defined)
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
        // iterate over inner and outer names of a and b respectively and glue them
        for (int l = 0; l < a.inners.getWidth(); l++) {
            Map<String, EditableHandle> handleMap = new HashMap<>();
            for (EditableInnerName i : a.inners.getAsc(l).values()) {
                handleMap.put("A" + i.getName(), i.getHandle());
                i.setHandle(null);
            }
            for (EditableOuterName o : b.outers.getAsc(l).values()) {
                EditableHandle h = handleMap.get("A" + o.getName());
                for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                    p.setHandle(h);
                }
            }
            for (EditableInnerName i : b.outers.getDesc(l).values()) {
                handleMap.put("D" + i.getName(), i.getHandle());
                i.setHandle(null);
            }
            for (EditableOuterName o : a.inners.getDesc(l).values()) {
                EditableHandle h = handleMap.get("D" + o.getName());
                for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
                    p.setHandle(h);
                }
            }
        }
        // update inner interfaces
        a.inners.names.clear();
        a.sites.clear();
        a.inners.names.addAll(b.inners.names);
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
     * Composes two sets of bigraphs. The first argument will be the list of outer bigraphs that
     * will be composed to the outer faces of the second argument.
     *
     * @param outs the outer bigraphs
     * @param ins  the inner bigraphs
     * @return the composition of the arguments.
     */
    public static DirectedBigraph compose(Iterable<DirectedBigraph> outs, Iterable<DirectedBigraph> ins) {
        return compose(outs, ins, false);
    }

    /**
     * Composes two sets of bigraphs. The first argument will be the list of outer bigraphs that
     * will be composed to the outer faces of the second argument.
     *
     * @param outs  the outer bigraphs
     * @param ins   the inner bigraphs
     * @param reuse flag. If true, bigraphs in input will not be copied.
     * @return the composition of the arguments.
     */
    static DirectedBigraph compose(Iterable<DirectedBigraph> outs, Iterable<DirectedBigraph> ins, boolean reuse) {
        int sumout = 1;
        int sumin = 1;

        for (DirectedBigraph o : outs) {
            sumout += o.inners.getWidth() - 1;
        }
        for (DirectedBigraph i : ins) {
            sumin += i.outers.getWidth() - 1;
        }
        if (sumout != sumin) {
            throw new RuntimeException("The outer faces of the inner bigraphs together must match the inner faces of the outers.");
        }

        Iterator<DirectedBigraph> outIt = outs.iterator();
        Iterator<DirectedBigraph> inIt = ins.iterator();
        DirectedBigraph out = outIt.next();
        DirectedBigraph in = inIt.next();

        DirectedBigraph a = (reuse) ? out : out.clone();
        DirectedBigraph b = (reuse) ? in : in.clone();

        while (outIt.hasNext()) {
            a = juxtapose(a, outIt.next(), reuse);
        }
        while (inIt.hasNext()) {
            b = juxtapose(b, inIt.next(), reuse);
        }

        return compose(a, b, reuse);
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
     * @param names     the names of its link faces.
     * @return the resulting identity bigraph.
     */
    public static DirectedBigraph makeId(DirectedSignature signature, List<Set<String>> names) {
        DirectedBigraphBuilder bb = new DirectedBigraphBuilder(signature);
        for (int i = 0; i < names.size(); i++) {
            bb.addSite(bb.addRoot());
        }
        for (Set<String> name : names) {
            int ind = names.indexOf(name);
            for (String s : name)
                bb.addDescNameOuterInterface(ind, s, bb.addDescNameInnerInterface(ind, s));
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
        for (EditableOuterName outAsc : this.outers.getAsc().values()) {
            if (outAsc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign ascendant name in outer interface");
                return false;
            }
            seen_handles.add(outAsc);
        }
        for (EditableOuterName inDesc : this.inners.getDesc().values()) {
            if (inDesc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign descendant name in inner interface");
                return false;
            }
            seen_handles.add(inDesc);
        }
        // System.out.println(seen_points);
        for (EditableInnerName inAsc : this.inners.getAsc().values()) {
            if (inAsc.getOwner() != owner) {
                System.err.println("INCOSISTENCY: foreign ascendant name in inner interface");
                return false;
            }
            seen_handles.add(inAsc.getHandle());
            seen_points.add(inAsc);
        }
        for (EditableInnerName outDesc : this.outers.getDesc().values()) {
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
        for (EditableOwned o : this.inners.getDesc().values()) {
            o.setOwner(owner);
        }
        for (EditableOwned o : this.outers.getAsc().values()) {
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
        big.inners.names.clear();
        big.outers.names.clear();
        // owner == null -> self
        if (owner == null)
            owner = big;
        Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
        List<Set<EditableOuterName>> newInnersRight = new ArrayList<>();
        List<Set<EditableInnerName>> newInnersLeft = new ArrayList<>();
        List<Set<EditableInnerName>> newOutersRight = new ArrayList<>();
        List<Set<EditableOuterName>> newOutersLeft = new ArrayList<>();
        // replicate inner interface handles
        for (InterfacePair<EditableInnerName, EditableOuterName> ip1 : this.inners.names) {
            // clone right set
            Set<EditableOuterName> right = new HashSet<>();
            for (EditableOuterName o1 : ip1.getRight()) {
                EditableOuterName o2 = o1.replicate();
                right.add(o2);
                o2.setOwner(owner);
                hnd_dic.put(o1, o2);
            }
            newInnersRight.add(right);
        }
        // replicate outer interface handles
        for (InterfacePair<EditableOuterName, EditableInnerName> ip1 : this.outers.names) {
            // clone left set
            Set<EditableOuterName> left = new HashSet<>();
            for (EditableOuterName o1 : ip1.getLeft()) {
                EditableOuterName o2 = o1.replicate();
                left.add(o2);
                o2.setOwner(owner);
                hnd_dic.put(o1, o2);
            }
            newOutersLeft.add(left);
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
        Queue<EditableNode> nodes = new LinkedList<>();
        Queue<EditableNode> nodeReplicas = new LinkedList<>();
        while (!q.isEmpty()) {
            Pair t = q.poll();
            if (t.c.isNode()) {
                EditableNode n1 = (EditableNode) t.c;
                EditableNode n2 = n1.replicate();
                nodes.add(n1);
                nodeReplicas.add(n2);
                // set m's parent (which added adds m as its child)
                n2.setParent(t.p);
                for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
                    EditableNode.EditableInPort p1 = n1.getInPort(i);
                    EditableHandle h2 = hnd_dic.get(p1);
                    EditableNode.EditableInPort p2 = n2.getInPort(i);
                    hnd_dic.put(p1, p2);
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
        // replicate inner interface
        for (InterfacePair<EditableInnerName, EditableOuterName> ip1 : this.inners.names) {
            // clone left set
            Set<EditableInnerName> left = new HashSet<>();
            for (EditableInnerName i1 : ip1.getLeft()) {
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
                left.add(i2);
            }
            newInnersLeft.add(left);
        }
        // replicate outer interface
        for (InterfacePair<EditableOuterName, EditableInnerName> ip1 : this.outers.names) {
            // clone right set
            Set<EditableInnerName> right = new HashSet<>();
            for (EditableInnerName i1 : ip1.getRight()) {
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
                right.add(i2);
            }
            newOutersRight.add(right);
        }
        for (int j = 0; j < newInnersLeft.size(); j++) {
            InterfacePair<EditableInnerName, EditableOuterName> ip2 = new InterfacePair<>(newInnersLeft.get(j), newInnersRight.get(j));
            big.inners.names.add(ip2);
        }
        for (int j = 0; j < newOutersLeft.size(); j++) {
            InterfacePair<EditableOuterName, EditableInnerName> ip2 = new InterfacePair<>(newOutersLeft.get(j), newOutersRight.get(j));
            big.outers.names.add(ip2);
        }
        // end with out ports
        while (!nodes.isEmpty()) {
            EditableNode n1 = nodes.poll();
            EditableNode n2 = nodeReplicas.poll();
            for (int i = n1.getControl().getArityOut() - 1; 0 <= i; i--) {
                EditableNode.EditableOutPort p1 = n1.getOutPort(i);
                EditableHandle h1 = p1.getHandle();
                // looks for an existing replica
                EditableHandle h2 = hnd_dic.get(h1);
                if (h1 != null && h2 == null) {
                    // the bigraph is inconsistent if g is null
                    h2 = h1.replicate();
                    h2.setOwner(owner);
                    hnd_dic.put(h1, h2);
                }
                n2.getOutPort(i).setHandle(h2);
            }
        }
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

    @Override
    public Interface getOuterInterface() {
        return this.outers;
    }

    @Override
    public Interface getInnerInterface() {
        return this.inners;
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
                if (h != null && h.isEdge()) {
                    s.add((EditableEdge) h);
                }
            }
        }
        for (InnerName n : this.inners.getAsc().values()) {
            Handle h = n.getHandle();
            if (h.isEdge()) {
                s.add((EditableEdge) h);
            }
        }
        for (InnerName n : this.outers.getDesc().values()) {
            Handle h = n.getHandle();
            if (h != null && h.isEdge()) {
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
        Map<String, EditableOuterName> inMap = this.inners.getDesc();
        for (Map.Entry<String, EditableOuterName> entry : inMap.entrySet()) {
            Handle h = entry.getValue();
            b.append(nl).append("I").append(entry.getKey());
            b.append(" <- {");
            List<? extends Point> ps = new ArrayList<>(h.getPoints());
            Collections.sort(ps, pointComparator);
            Iterator<? extends Point> ip = ps.iterator();
            while (ip.hasNext()) {
                Point p = ip.next();
                if (p.isInnerName()) {
                    b.append("O-.");
                }
                b.append(p);
                if (ip.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        Map<String, EditableOuterName> outMap = this.outers.getAsc();
        for (Map.Entry<String, EditableOuterName> entry : outMap.entrySet()) {
            Handle h = entry.getValue();
            b.append(nl).append("O").append(entry.getKey());
            b.append(" <- {");
            List<? extends Point> ps = new ArrayList<>(h.getPoints());
            Collections.sort(ps, pointComparator);
            Iterator<? extends Point> ip = ps.iterator();
            while (ip.hasNext()) {
                Point p = ip.next();
                if (p.isInnerName()) {
                    b.append("I+.");
                }
                b.append(p);
                if (ip.hasNext())
                    b.append(", ");
            }
            b.append('}');
        }
        for (Node n : this.getNodes()) {
            for (Handle h : n.getInPorts()) {
                b.append(nl).append(h);
                b.append(" <- {");
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

    static class Interface<Asc extends EditableLinkFacet, Desc extends EditableLinkFacet> implements it.uniud.mads.jlibbig.core.Interface {
        final List<InterfacePair<Asc, Desc>> names = new ArrayList<>();

        Interface() {
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
        static Collection<String> intersectNames(
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
        static Collection<String> intersectNames(
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
        static <Asc extends EditableLinkFacet, Desc extends EditableLinkFacet> Interface<Asc, Desc> joinInterfaces(
                Interface<Asc, Desc> i1,
                Interface<Asc, Desc> i2) {

            Interface<Asc, Desc> i = new Interface<>(InterfacePair.mergePairs(i1.names.get(0), i2.names.get(0)));

            // skip first element because added before
            i.names.addAll(i1.names.subList(1, i1.names.size()));
            i.names.addAll(i2.names.subList(1, i2.names.size()));

            return i;
        }

        /**
         * join with another interface
         *
         * @param i the interface to join
         */
        void join(Interface<Asc, Desc> i) {
            this.names.set(0, InterfacePair.mergePairs(this.names.get(0), i.names.get(0)));
            this.names.addAll(i.names.subList(1, i.names.size()));
        }

        public int getWidth() {
            return names.size();
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

        Map<String, Asc> getAsc() {
            Map<String, Asc> asc = new HashMap<>();
            for (InterfacePair<Asc, Desc> ip : names) {
                for (Asc a : ip.getLeft()) {
                    asc.put(names.indexOf(ip) + "+." + a.getName(), a);
                }
            }
            return asc;
        }

        public Map<String, Asc> getAsc(int index) {
            if (index < 0 || index >= names.size()) {
                throw new IndexOutOfBoundsException("Index '" + index + "' not in list");
            }
            Map<String, Asc> ascInd = new HashMap<>();
            for (Asc a : names.get(index).getLeft()) {
                ascInd.put(a.getName(), a);
            }
            return ascInd;
        }

        public void addDesc(int index, Desc d) {
            this.names.get(index).getRight().add(d);
        }

        Map<String, Desc> getDesc() {
            Map<String, Desc> desc = new HashMap<>();
            for (InterfacePair<Asc, Desc> ip : names) {
                for (Desc d : ip.getRight()) {
                    desc.put(names.indexOf(ip) + "-." + d.getName(), d);
                }
            }
            return desc;
        }

        public Map<String, Desc> getDesc(int index) {
            if (index < 0 || index >= names.size()) {
                throw new IndexOutOfBoundsException("Index '" + index + "' not in list");
            }
            Map<String, Desc> descInd = new HashMap<>();
            for (Desc d : names.get(index).getRight()) {
                descInd.put(d.getName(), d);
            }
            return descInd;
        }

        void removeAsc(int locality, String name) {
            this.names.get(locality).getLeft().remove(this.getAsc(locality).get(name));
        }

        void removeDesc(int locality, String name) {
            this.names.get(locality).getRight().remove(this.getDesc(locality).get(name));
        }

        public Set<String> keySet() {
            Set<String> ss = new HashSet<>();

            for (InterfacePair<Asc, Desc> ip : names) {
                int id = names.indexOf(ip);

                for (Asc l : ip.getLeft()) {
                    ss.add(id + " l " + l);
                }

                for (Desc r : ip.getRight()) {
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
