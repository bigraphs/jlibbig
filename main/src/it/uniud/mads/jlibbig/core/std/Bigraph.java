package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.std.EditableNode.EditablePort;

/**
 * Objects created from this class are bigraphs with abstract internal names
 * (i.e. {@link Node} equality is reference based) whereas link interfaces still
 * use concrete names. Instances of this class are immutable and can be created
 * by means of the factory methods provided by this class like e.g.
 * {@link #makeEmpty}, {@link #makeId}, {@link #compose}, and {@link #juxtapose}
 * ; or from instances of {@link BigraphBuilder}.
 */
/*
 * For efficiency reasons immutability can be relaxed by the user (cf. {@link
 * #compose(Bigraph, Bigraph, boolean)}) allowing the reuse of (all or parts) of
 * these objects.
 */
final public class Bigraph implements
		it.uniud.mads.jlibbig.core.Bigraph<Control>, Cloneable/* , PropertyTarget */{

	final Signature signature;
	final List<EditableRoot> roots = new ArrayList<>();
	final List<EditableSite> sites = new ArrayList<>();
	final Map<String, EditableOuterName> outers = new IdentityHashMap<>();
	final Map<String, EditableInnerName> inners = new IdentityHashMap<>();
	
	private final List<? extends Root> ro_roots = Collections
			.unmodifiableList(roots);
	private final List<? extends Site> ro_sites = Collections
			.unmodifiableList(sites);

	Bigraph(Signature sig) {
		if (sig == null)
			throw new IllegalArgumentException("Signature can not be null.");
		this.signature = sig;
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
	 * @param owner
	 *            the alternative owner.
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
					if (n.getControl().getArity() != n.getPorts().size()
							|| !signature.contains(n.getControl())) {
						System.err.println("INCOSISTENCY: control/arity");
						return false;
					}
					q.add(n);
					for (Point t : n.getPorts()) {
						EditableHandle h = ((EditablePoint) t).getHandle();
						if (h == null || h.getOwner() != owner) {
							// foreign or broken handle
							System.err
									.println("INCOSISTENCY: broken or foreign handle");
							return false;
						}
						if (!h.getPoints().contains(t)) {
							// broken link chain
							System.err
									.println("INCOSISTENCY: handle/point mismatch");
							return false;
						}
						seen_points.add(t);
						seen_handles.add(h);
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
					System.err
							.println("INCOSISTENCY: neither a node nor a site");
					// c is neither a site nor a node
					return false;
				}
			}
		}
		for (EditableOuterName h : this.outers.values()) {
			if (h.getOwner() != owner) {
				System.err.println("INCOSISTENCY: foreign outer name");
				return false;
			}
			seen_handles.add(h);
		}
		// System.out.println(seen_points);
		for (EditableInnerName n : this.inners.values()) {
			if (n.getOwner() != owner) {
				System.err.println("INCOSISTENCY: foreign inner name");
				return false;
			}
			seen_handles.add(n.getHandle());
			seen_points.add(n);
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
	 * @param owner
	 * @return this bigraph
	 */
	Bigraph setOwner(Owner owner) {
		if (owner == null)
			owner = this;
		for (EditableOwned o : this.roots) {
			o.setOwner(owner);
		}
		for (EditableOwned o : this.outers.values()) {
			o.setOwner(owner);
		}
		for (Edge e : this.getEdges()) {
			((EditableOwned) e).setOwner(owner);
		}
		return this;
	}

	@Override
	public Bigraph clone() {
		return this.clone(null);
	}

	/**
	 * Same as clone, but additionally sets a custom owner for the internal s
	 * tructures of the cloned bigraph. It corresponds to
	 * <code>someBigraph.clone().setOwner(someOwner)</code>. If the argument is
	 * null, the owner is set to the cloned bigraph.
	 * 
	 * @param owner
	 * @return a copy of this bigraph.
	 */
	Bigraph clone(Owner owner) {
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
		Bigraph big = new Bigraph(this.signature);
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
		// replicate inner names
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
				for (int i = n1.getControl().getArity() - 1; 0 <= i; i--) {
					EditablePort p1 = n1.getPort(i);
					EditableHandle h1 = p1.getHandle();
					// looks for an existing replica
					EditableHandle h2 = hnd_dic.get(h1);
					if (h2 == null) {
						// the bigraph is inconsistent if g is null
						h2 = h1.replicate();
						h2.setOwner(owner);
						hnd_dic.put(h1, h2);
					}
					n2.getPort(i).setHandle(h2);
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
	public Signature getSignature() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AbstBigraph#getOuterNames()
	 */
	@Override
	public Collection<? extends OuterName> getOuterNames() {
		return this.outers.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AbstBigraph#getInnerNames()
	 */
	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return this.inners.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AbstBigraph#getNodes()
	 */
	@Override
	public Collection<? extends Node> getNodes() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniud.mads.jlibbig.core.AbstBigraph#getEdges()
	 */
	@Override
	public Collection<? extends Edge> getEdges() {
		return getEdges(this.getNodes());
	}

	// avoids the visit of the place graph to compute the set of nodes
	Collection<? extends Edge> getEdges(Iterable<? extends Node> nodes) {
		Set<Edge> s = new HashSet<>();
		for (Node n : nodes) {
			for (Port p : n.getPorts()) {
				Handle h = p.getHandle();
				if (h instanceof Edge) {
					s.add((Edge) h);
				}
			}
		}
		for (InnerName n : this.inners.values()) {
			Handle h = n.getHandle();
			if (h instanceof Edge) {
				s.add((Edge) h);
			}
		}
		return s;
	}

	/* comparators used by toString */

	private static final Comparator<Control> controlComparator = new Comparator<Control>() {
		@Override
		public int compare(Control o1, Control o2) {
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

	private static final Comparator<Port> portComparator = new Comparator<Port>() {
		@Override
		public int compare(Port o1, Port o2) {
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
					return portComparator.compare((Port) o1, (Port) o2);
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
	public String toString() {

		String nl = System.getProperty("line.separator");
		StringBuilder b = new StringBuilder();
		b.append(signature.getUSID());
		b.append(" {");
		Iterator<Control> is = this.signature.iterator();
		while (is.hasNext()) {
			b.append(is.next().toString());
			if (is.hasNext())
				b.append(", ");
		}
		b.append("} :: <").append(this.sites.size()).append(",{");

		List<EditableInnerName> ins = new ArrayList<>(this.inners.values());
		Collections.sort(ins, innerComparator);
		Iterator<EditableInnerName> ii = ins.iterator();
		while (ii.hasNext()) {
			b.append(ii.next().toString());
			if (ii.hasNext())
				b.append(", ");
		}
		b.append("}> -> <").append(this.roots.size()).append(",{");
		List<EditableOuterName> ons = new ArrayList<>(this.outers.values());
		Collections.sort(ons, outerComparator);
		Iterator<EditableOuterName> io = ons.iterator();
		while (io.hasNext()) {
			b.append(io.next().toString());
			if (io.hasNext())
				b.append(", ");
		}
		b.append("}>");
		for (Handle h : this.outers.values()) {
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

	/**
	 * Juxtaposes two bigraph. The first argument will be the left operand that
	 * is its roots and sites will precede those of the second argument in the
	 * outcome.
	 * 
	 * @param left
	 *            the first bigraph.
	 * @param right
	 *            the second bigraph.
	 * @return the juxtaposition of the arguments.
	 */
	public static Bigraph juxtapose(Bigraph left, Bigraph right) {
		return juxtapose(left, right, false);
	}

	/**
	 * Juxtaposes two bigraph. The first argument will be the left operand that
	 * is its roots and sites will precede those of the second argument in the
	 * outcome. Optionally, arguments can be reused.
	 * 
	 * @param left
	 *            the first bigraph.
	 * @param right
	 *            the second bigraph.
	 * @param reuse
	 *            flag. If true, bigraphs in input will not be copied.
	 * @return the juxtaposition of the arguments.
	 */
	static Bigraph juxtapose(Bigraph left, Bigraph right, boolean reuse) {
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
		Bigraph r = (reuse) ? right : right.clone();
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.putAll(r.outers);
		l.inners.putAll(r.inners);
		return l;
	}

	/**
	 * Composes two bigraphs. The first argument will be the outer bigraph that
	 * is the one composed to the outer face of the second argument.
	 * 
	 * @param out
	 *            the outer bigraph
	 * @param in
	 *            the inner bigraph
	 * @return the composition of the arguments.
	 */
	public static Bigraph compose(Bigraph out, Bigraph in) {
		return compose(out, in, false);
	}

	/**
	 * Composes two bigraphs. The first argument will be the outer bigraph that
	 * is the one composed to the outer face of the second argument. Optionally,
	 * arguments can be reused.
	 * 
	 * @param out
	 *            the outer bigraph
	 * @param in
	 *            the inner bigraph
	 * @param reuse
	 *            flag. If true, bigraphs in input will not be copied.
	 * @return the composition of the arguments.
	 */
	static Bigraph compose(Bigraph out, Bigraph in, boolean reuse) {
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.getSignature(),
					in.getSignature());
		}
		if (!out.inners.keySet().equals(in.outers.keySet())
				|| out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfaceException(
					"The outer face of the first graph must be equal to inner face of the second");
		}
		Bigraph a = (reuse) ? out : out.clone();
		Bigraph b = (reuse) ? in : in.clone();
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableSite s = is.next();
			EditableParent p = s.getParent();
			p.removeChild(s);
			for (EditableChild c : new ArrayList<>(ir.next()
					.getEditableChildren())) {
				c.setParent(p);
			}
		}
		// iterate over inner and outer names of a and b respectively and glue
		// them
		// for (EditableOuterName o : b.outers) {
		// for (EditableInnerName i : a.inners) {
		// if (!i.equals(o))
		// continue;
		// EditableHandle h = i.getHandle();
		// for (EditablePoint p : o.getEditablePoints()) {
		// p.setHandle(h);
		// }
		// a.inners.remove(i);
		// break;
		// }
		// }
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
		return a;
	}

	/**
	 * Creates an empty bigraph for the given signature.
	 * 
	 * @param signature
	 *            the signature of the bigraph.
	 * @return the empty bigraph.
	 */
	public static Bigraph makeEmpty(Signature signature) {
		return new Bigraph(signature);
	}

	/**
	 * Creates an identity bigraph i.e. a bigraph without nodes where every site
	 * is the only child of the root at the same index and every inner name is
	 * the only point of the outer name with the same concrete name.
	 * 
	 * @param signature
	 *            the signature of the bigraph.
	 * @param width
	 *            the number of roots/sites.
	 * @param names
	 *            the names of its link faces.
	 * @return the resulting identity bigraph.
	 */
	public static Bigraph makeId(Signature signature, int width,
			String... names) {
		BigraphBuilder bb = new BigraphBuilder(signature);
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
	 * @param signature
	 *            the signature of the bigraph.
	 * @param width
	 *            the number of roots/sites.
	 * @param names
	 *            the set of names that will appear in resulting bigraph's link
	 *            faces.
	 * @return an identity bigraph.
	 */
	public static Bigraph makeId(Signature signature, int width,
			Iterable<? extends LinkFacet> names) {
		BigraphBuilder bb = new BigraphBuilder(signature);
		for (int i = 0; i < width; i++) {
			bb.addSite(bb.addRoot());
		}
		for (LinkFacet f : names) {
			String name = f.getName();
			bb.addInnerName(name, bb.addOuterName(name));
		}
		return bb.makeBigraph();
	}

	// TODO more factory methods

	/**
	 * Creates the collection containing all the names in the given collections
	 * of link facets (i.e. inner and outer names).
	 * 
	 * @param arg0
	 *            one of the collections to be intersected.
	 * @param arg1
	 *            one of the collections to be intersected.
	 * @return the intersection.
	 */
	private static Collection<String> intersectNames(
			Collection<? extends LinkFacet> arg0,
			Collection<? extends LinkFacet> arg1) {
		return intersectNames(arg0, arg1, new HashSet<String>());
	}

	/**
	 * Extends the given collection of strings with all the names in the given
	 * collections of link facets (i.e. inner and outer names).
	 * 
	 * @param arg0
	 *            one of the collections to be intersected.
	 * @param arg1
	 *            one of the collections to be intersected.
	 * @param ns0
	 *            the collection to be extended.
	 * @return the given string collection extended with the intersection of the
	 *         other two.
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
}
