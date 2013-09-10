package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;
import jlibbig.core.abstractions.Owner;
import jlibbig.core.exceptions.*;

/**
 * The class is used to store immutable bigraphs.
 * <p>
 * e.g. {@link #compose(Bigraph, Bigraph)} or
 * {@link #juxtapose(Bigraph, Bigraph)} instantiate a new object. For a mutable
 * version of bigraphs, users can use {@link BigraphBuilder}.
 * </p>
 */
final public class Bigraph implements jlibbig.core.abstractions.Bigraph<Control> {// , PropertyTarget {

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
		this.signature = sig;
	}

	boolean isConsistent() {
		return this.isConsistent(this);
	}

	boolean isConsistent(Owner owner) {
		Set<Point> seen_points = new HashSet<>();
		Set<Handle> seen_handles = new HashSet<>();
		Set<Site> unseen_sites = new HashSet<>();
		unseen_sites.addAll(this.sites);
		Set<Child> seen_children = new HashSet<>();
		Queue<Parent> q = new LinkedList<>();
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
		//System.out.println(seen_points);
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
	 * Set owner of internal structures of this bigraph (but leaves them
	 * connected to it). Be careful. This method is meant to be used by Builder
	 * to avoid leaking references to their internal working bigraph. If the
	 * argument is null, the owner is set to the cloned bigraph.
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
	 * Same as clone, but set a custom owner for the internal structures of the
	 * cloned bigraph. It correspond to
	 * <code>someBigraph.clone().setOwner(someOwner)</code>. If the argument is
	 * null, the owner is set to the cloned bigraph.
	 * 
	 * @param owner
	 * @return a cloned bigraph
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
	 * @see jlibbig.core.AbstBigraph#getSignature()
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
	 * @see jlibbig.core.AbstBigraph#getRoots()
	 */
	@Override
	public List<? extends Root> getRoots() {
		return this.ro_roots;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getSites()
	 */
	@Override
	public List<? extends Site> getSites() {
		return this.ro_sites;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getOuterNames()
	 */
	@Override
	public Collection<? extends OuterName> getOuterNames() {
		return this.outers.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getInnerNames()
	 */
	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return this.inners.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getNodes()
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
	 * @see jlibbig.core.AbstBigraph#getEdges()
	 */
	@Override
	public Collection<? extends Edge> getEdges() {
		return getEdges(this.getNodes());
	}

	// avoid visit the place graph to compute the set of nodes
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
		Iterator<EditableInnerName> ii = this.inners.values().iterator();
		while (ii.hasNext()) {
			b.append(ii.next().toString());
			if (ii.hasNext())
				b.append(", ");
		}
		b.append("}> -> <").append(this.roots.size()).append(",{");
		Iterator<EditableOuterName> io = this.outers.values().iterator();
		while (io.hasNext()) {
			b.append(io.next().toString());
			if (io.hasNext())
				b.append(", ");
		}
		b.append("}>");
		for (Handle h : this.outers.values()) {
			b.append(nl).append(h);
			b.append(":o <- {");
			Iterator<? extends Point> ip = h.getPoints().iterator();
			while (ip.hasNext()) {
				Point p = ip.next();
				b.append(p);
				if (p instanceof InnerName) {
					b.append(":i");
				}
				if (ip.hasNext())
					b.append(", ");
			}
			b.append('}');
		}
		for (Handle h : this.getEdges()) {
			b.append(nl).append(h);
			b.append(":e <- {");
			Iterator<? extends Point> ip = h.getPoints().iterator();
			while (ip.hasNext()) {
				Point p = ip.next();
				b.append(p);
				if (p instanceof InnerName) {
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
			if (p instanceof Root) {
				b.append(this.roots.indexOf(p));
			} else {
				b.append(p);
			}
			b.append(" <- {");
			Iterator<? extends Child> ic = p.getChildren().iterator();
			while (ic.hasNext()) {
				Child c = ic.next();
				if (c instanceof Site) {
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
	 * Juxtapose two bigraph. In the resulting bigraph, roots and sites of the
	 * first (left) bigraph will precede those of the second (right) bigraph.
	 * 
	 * @param left
	 *            the first bigraph
	 * @param right
	 *            the second bigraph
	 * @return the resulting bigraph
	 */
	public static Bigraph juxtapose(Bigraph left, Bigraph right) {
		return juxtapose(left, right, false);
	}

	/**
	 * Juxtapose two bigraph. In the resulting bigraph, roots and sites of the
	 * first (left) bigraph will precede those of the second (right) bigraph.
	 * 
	 * @param left
	 *            the first bigraph
	 * @param right
	 *            the second bigraph
	 * @param reuse
	 *            flag. If true, bigraphs in input won't be copied.
	 * @return the resulting bigraph
	 */
	static Bigraph juxtapose(Bigraph left, Bigraph right, boolean reuse) {
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!left.signature.equals(right.signature)) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners.keySet(), right.inners.keySet())
				|| !Collections.disjoint(left.outers.keySet(),
						right.outers.keySet())) {
			throw new IncompatibleInterfacesException(left, right,
					new NameClashException(intersectNames(
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
	 * Compose two bigraph. The first bigraph in input will be the "outer" one.
	 * 
	 * @param out
	 *            the outer bigraph
	 * @param in
	 *            the inner bigraph
	 * @return the resulting bigraph
	 */
	public static Bigraph compose(Bigraph out, Bigraph in) {
		return compose(out, in, false);
	}

	/**
	 * Compose two bigraph. The first bigraph in input will be the "outer" one.
	 * 
	 * @param out
	 *            the outer bigraph
	 * @param in
	 *            the inner bigraph
	 * @param reuse
	 *            flag. If true, bigraphs in input won't be copied.
	 * @return the resulting bigraph
	 */
	static Bigraph compose(Bigraph out, Bigraph in, boolean reuse) {
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.keySet().equals(in.outers.keySet())
				|| out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfacesException(in, out,
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
			for (EditableChild c : new ArrayList<>(ir.next().getEditableChildren())) {
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
	 * Make an empty bigraph.
	 * 
	 * @param signature
	 *            the signature of the bigraph
	 * @return the empty bigraph
	 */
	public static Bigraph makeEmpty(Signature signature) {
		return new Bigraph(signature);
	}

	/**
	 * Make an identity bigraph.
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
		for (int i = 0; i < names.length; i++) {
			bb.addInnerName(names[i], bb.addOuterName(names[i]));
		}
		return bb.makeBigraph();
	}

	/**
	 * Make an identity bigraph.
	 * 
	 * @param signature
	 *            the signature of the bigraph.
	 * @param width
	 *            the number of roots/sites.
	 * @param names
	 *            the set of names that will appear in resulting bigraph's link
	 *            faces.
	 * @return the resulting identity bigraph.
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

	// TODO factory methods

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
