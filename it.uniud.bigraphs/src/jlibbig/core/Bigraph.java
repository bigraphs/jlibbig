package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;

public class Bigraph implements AbstBigraph {

	final Signature signature;
	final List<EditableRoot> roots = new ArrayList<>();
	final List<EditableSite> sites = new ArrayList<>();
	final Set<EditableOuterName> outers = new HashSet<>();
	final Set<EditableInnerName> inners = new HashSet<>();

	private final List<? extends Root> ro_roots = Collections
			.unmodifiableList(this.roots);
	private final List<? extends Site> ro_sites = Collections
			.unmodifiableList(this.sites);
	private final Set<? extends OuterName> ro_outers = Collections
			.unmodifiableSet(this.outers);
	private final Set<? extends InnerName> ro_inners = Collections
			.unmodifiableSet(this.inners);

	Bigraph(Signature sig) {
		this.signature = sig;
	}

	boolean isConsistent() {
		return this.isConsistent(this);
	}

	boolean isConsistent(Owner owner) {
		Set<Point> ps = new HashSet<>();
		Set<Handle> seen_handles = new HashSet<>();
		Set<Site> unseen_sites = new HashSet<>();
		unseen_sites.addAll(this.sites);
		Set<Child> seen_children = new HashSet<>();
		Queue<Parent> q = new LinkedList<>();
		for (EditableRoot r : this.roots) {
			if (r.getOwner() != owner)
				return false;
			q.add(r);
		}
		while (!q.isEmpty()) {
			Parent p = q.poll();
			for (Child c : p.getChildren()) {
				if (!p.equals(c.getParent())) {
					// faux parent/child
					return false;
				}
				if (!seen_children.add(c)) {
					// c was already visited
					// we have found a cycle (or diamond) in the place structure
					return false;
				} else if (c instanceof EditableNode) {
					EditableNode n = (EditableNode) c;
					if (n.getControl().getArity() != n.getPorts().size()
							|| !signature.contains(n.getControl())) {
						return false;
					}
					q.add(n);
					for (Point t : n.getPorts()) {
						EditableHandle h = ((EditablePoint) t).getHandle();
						if (h == null || h.getOwner() != owner)
							// foreign or broken handle
							return false;
						if (!h.getPoints().contains(t))
							// broken link chain
							return false;
						ps.add(t);
						seen_handles.add(h);
					}
				} else if (c instanceof EditableSite) {
					Site s = (Site) c;
					unseen_sites.remove(s);
					if (!this.sites.contains(s)) {
						// unknown site
						return false;
					}
				} else {
					// c is neither a site nor a node
					return false;
				}
			}
		}
		for (EditableOuterName h : this.outers) {
			if (h.getOwner() != owner)
				return false;
			seen_handles.add(h);
		}
		// System.out.println(ps);
		for (EditableInnerName n : this.inners) {
			if (n.getOwner() != owner) // || n.getHandle() == null is implicit
				return false;
			seen_handles.add(n.getHandle());
			ps.add(n);
		}
		// System.out.println(ps);
		for (Handle h : seen_handles) {
			// System.out.println(h);
			for (Point p : h.getPoints()) {
				// System.out.println(p + " " + p.getHandle() + " " + h);
				if (!ps.remove(p))
					// foreign point
					return false;
			}
		}
		// System.out.println(ps);
		if (ps.size() > 0) {
			// broken handle chain
			return false;
		}
		if (unseen_sites.size() > 0) {
			// these sites are unreachable from roots
			return false;
		}
		return true;
	}

	/**
	 * Set owner of internal structures of this bigraph (but leaves them
	 * connected to it). Be careful. This method is meant to be used by Builder
	 * to avoid leaking references to their internal working bigraph.
	 * If the argument is null, the owner is set to the cloned bigraph.
	 * 
	 * @param owner
	 * @return this bigraph
	 */
	Bigraph setOwner(Owner owner) {
		if(owner == null)
			owner = this;
		for (EditableOwned o : this.roots) {
			o.setOwner(owner);
		}
		for (EditableOwned o : this.outers) {
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
	 * cloned bigraph. It correspond to <code>someBigraph.clone().setOwner(someOwner)</code>.
	 * If the argument is null, the owner is set to the cloned bigraph.
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
		Map<Handle, EditableHandle> trs = new HashMap<>();
		// replicate outer names
		for (EditableOuterName o : this.outers) {
			EditableOuterName p = o.replicate();
			big.outers.add(p);
			p.setOwner(owner);
			trs.put(o, p);
		}
		// replicate inner names
		for (EditableInnerName i : this.inners) {
			EditableInnerName j = i.replicate();
			// set replicated handle for j
			EditableHandle g = i.getHandle();
			EditableHandle h = trs.get(g);
			if (h == null) {
				// the bigraph is inconsistent if g is null
				h = g.replicate();
				h.setOwner(owner);
				trs.put(g, h);
			}
			j.setHandle(h);
			big.inners.add(j);
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
		for (EditableRoot r : this.roots) {
			EditableRoot s = r.replicate();
			big.roots.add(s);
			s.setOwner(owner);
			for (EditableChild c : r.getEditableChildren()) {
				q.add(new Pair(s, c));
			}
		}
		EditableSite[] sites = new EditableSite[this.sites.size()];
		while (!q.isEmpty()) {
			Pair p = q.poll();
			if (p.c instanceof EditableNode) {
				EditableNode n = (EditableNode) p.c;
				EditableNode m = n.replicate();
				// set m's parent (which added adds m as its child)
				m.setParent(p.p);
				for (int i = n.getControl().getArity() - 1; 0 <= i; i--) {
					EditablePort o = n.getPort(i);
					EditableHandle g = o.getHandle();
					// looks for an existing replica
					EditableHandle h = trs.get(g);
					if (h == null) {
						// the bigraph is inconsistent if g is null
						h = g.replicate();
						h.setOwner(owner);
						trs.put(g, h);
					}
					m.getPort(i).setHandle(h);
				}
				// enqueue children for visit
				for (EditableChild c : n.getEditableChildren()) {
					q.add(new Pair(m, c));
				}
			} else {
				// c instanceof EditableSite
				EditableSite s = (EditableSite) p.c;
				EditableSite t = s.replicate();
				t.setParent(p.p);
				sites[this.sites.indexOf(s)] = t;
			}
		}
		for (int i = 0; i < sites.length; i++) {
			big.sites.add(sites[i]);
		}
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
	public boolean isEmpty(){
		return this.outers.isEmpty() && this.inners.isEmpty() && this.roots.isEmpty() && this.sites.isEmpty();
	}
	
	@Override
	public boolean isAgent(){
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
	public Set<? extends OuterName> getOuterNames() {
		return this.ro_outers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getInnerNames()
	 */
	@Override
	public Set<? extends InnerName> getInnerNames() {
		return this.ro_inners;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jlibbig.core.AbstBigraph#getNodes()
	 */
	@Override
	public Set<? extends Node> getNodes() {
		Set<EditableNode> s = new HashSet<>();
		Queue<EditableNode> q = new LinkedList<>();
		for (Root r : this.roots) {
			for (Child c : r.getChildren()) {
				if (c instanceof EditableNode) {
					EditableNode n = (EditableNode) c;
					q.add(n);
				}
			}
		}
		while (!q.isEmpty()) {
			EditableNode p = q.poll();
			s.add(p);
			for (Child c : p.getChildren()) {
				if (c instanceof EditableNode) {
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
	public Set<? extends Edge> getEdges() {
		Set<Edge> s = new HashSet<>();
		for (Node n : this.getNodes()) {
			for (Port p : n.getPorts()) {
				Handle h = p.getHandle();
				if (h instanceof Edge) {
					s.add((Edge) h);
				}
			}
		}
		for (InnerName n : this.inners) {
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
		b.append('{');
		Iterator<Control> is = this.signature.iterator();
		while (is.hasNext()) {
			b.append(is.next().toString());
			if (is.hasNext())
				b.append(", ");
		}
		b.append("} :: <").append(this.sites.size()).append(",{");
		for (EditableLinkFacet n : this.inners) {
			b.append(n.getName());
		}
		b.append("}> -> <").append(this.roots.size()).append(",{");
		for (EditableLinkFacet n : this.outers) {
			b.append(n.getName());
		}
		b.append("}>");
		for (Handle h : this.outers) {
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

	public static Bigraph juxtapose(Bigraph left, Bigraph right) {
		return juxtapose(left, right, false);
	}

	static Bigraph juxtapose(Bigraph left, Bigraph right, boolean reuse) {
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (left.signature != right.signature) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)
				|| !Collections.disjoint(left.outers, right.outers)) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = (reuse) ? left : left.clone();
		Bigraph r = (reuse) ? right : right.clone();
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(r.outers);
		l.inners.addAll(r.inners);
		return l;
	}

	public static Bigraph compose(Bigraph out, Bigraph in) {
		return compose(out, in, false);
	}

	public static Bigraph compose(Bigraph out, Bigraph in, boolean reuse) {
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (out.signature != in.signature) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.equals(in.outers)
				|| out.sites.size() != in.roots.size()) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph a = (reuse) ? out : out.clone();
		Bigraph b = (reuse) ? in : in.clone();
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableSite s = is.next();
			EditableParent p = s.getParent();
			for (EditableChild c : ir.next().getEditableChildren()) {
				c.setParent(p);
			}
			p.removeChild(s);
		}
		// iterate over inner and outer names of a and b respectively and glue
		// them
		for (EditableOuterName o : b.outers) {
			for (EditableInnerName i : a.inners) {
				if (!i.equals(o))
					continue;
				EditableHandle h = i.getHandle();
				for (EditablePoint p : o.getEditablePoints()) {
					p.setHandle(h);
				}
				a.inners.remove(i);
				break;
			}
		}
		// update inner interfaces
		a.inners.clear();
		a.sites.clear();
		a.inners.addAll(b.inners);
		a.sites.addAll(b.sites);
		return a;
	}

	public static Bigraph makeEmpty(Signature signature) {
		return new Bigraph(signature);
	}

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

	public static Bigraph makeId(Signature signature, int width,
			Set<LinkFacet> names) {
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

}
