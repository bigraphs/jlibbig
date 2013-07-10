package jlibbig.core;

import java.util.*;

/**
 * The class is meant as a helper for bigraph construction and manipulation in
 * presence of series of operations since {@link Bigraph} is immutable.
 * <p>
 * e.g.
 * {@link Bigraph#compose(Bigraph, Bigraph)} or
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object.
 * </p>
 */
final public class BigraphBuilder implements AbstBigraphBuilder {
	private final boolean DEBUG_CONSISTENCY_CHECK = true;

	private Bigraph big;

	public BigraphBuilder(Signature sig) {
		this.big = Bigraph.makeEmpty(sig);
	}

	public BigraphBuilder(Bigraph big) {
		this(big, true);
	}

	BigraphBuilder(Bigraph big, boolean clone) {
		if (!big.isConsistent())
			throw new IllegalArgumentException("Inconsistent bigraph.");
		this.big = (clone) ? big.clone(this) : big.setOwner(this);
	}

	@Override
	public String toString() {
		return big.toString();
	}

	/**
	 * Creates a new bigraph from its inner one.
	 * 
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph() {
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		Bigraph b = big.clone();
		if (!b.isConsistent())
			throw new RuntimeException("Inconsistent bigraph.");
		return b;
	}

	@Override
	public BigraphBuilder clone() {
		BigraphBuilder bb = new BigraphBuilder(this.big.getSignature());
		bb.big = this.big.clone(bb);
		return bb;
	}

	public Signature getSignature() {
		return this.big.getSignature();
	}
	
	@Override
	public boolean isEmpty(){
		return this.big.isEmpty();
	}
	
	@Override
	public boolean isGround(){
		return this.big.isGround();
	}

	/**
	 * Get bigraph's roots.
	 * @return a list carrying bigraph's roots
	 */
	public List<? extends Root> getRoots() {
		return this.big.getRoots();
	}

	/**
	 * Get bigraph's sites.
	 * @return a list carrying bigraph's sites
	 */
	public List<? extends Site> getSites() {
		return this.big.getSites();
	}

	/**
	 * Get bigraph's outer names.
	 * @return a list carrying bigraph's outer names
	 */
	public Set<? extends OuterName> getOuterNames() {
		return this.big.getOuterNames();
	}

	/**
	 * Get bigraph's inner names.
	 * @return a list carrying bigraph's inner names
	 */
	public Set<? extends InnerName> getInnerNames() {
		return this.big.getInnerNames();
	}

	/**
	 * Get bigraph's nodes.
	 * @return a set containing bigraph's nodes.
	 */
	public Set<? extends Node> getNodes() {
		return this.big.getNodes();
	}

	/**
	 * Get bigraph's edges.
	 * @return a set containing bigraph's edges.
	 */
	public Set<? extends Edge> getEdges() {
		return this.big.getEdges();
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Add a root to the current bigraph
	 * @return the reference of the new root
	 */
	public Root addRoot() {
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		this.big.roots.add(r);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return r;
	}

	/**
	 * Add a site to the current bigraph
	 * @param parent the handler, in the place graph, father of the new site
	 * @return the reference of the new site
	 */
	public Site addSite(Parent parent) {
		EditableSite s = new EditableSite((EditableParent) parent);
		this.big.sites.add(s);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return s;
	}

	/**
	 * Add a new node to the bigraph
	 * @param controlName the control's name of the new node
	 * @param parent the father of the new node, in the place graph
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent) {
		return addNode(controlName, parent, new LinkedList<Handle>());
	}

	/**
	 * Add a new node to the bigraph
	 * @param controlName the control's name of the new node
	 * @param parent the father of the new node, in the place graph
	 * @param handles Handles (outernames or edges) that will be linked to new node's ports
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent, Handle... handles) {
		return addNode(controlName, parent, Arrays.asList(handles));
	}

	/**
	 * Add a new node to the bigraph
	 * @param controlName the control's name of the new node
	 * @param parent the father of the new node, in the place graph
	 * @param handles list of handles (outernames or edges) that will be linked to new node's ports
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent, List<Handle> handles) {
		Control c = this.big.getSignature().getByName(controlName);
		if (c == null)
			throw new IllegalArgumentException(
					"Control should be in the signature.");
		if (!this.getRoots().contains(parent)
				&& !this.getNodes().contains(parent))
			throw new IllegalArgumentException(
					"Parent sould be in the bigraph.");
		handles = new LinkedList<>(handles);
		for (int i = handles.size(); i < c.getArity(); i++) {
			handles.add(new EditableEdge()); // add spare edges
		}
		for (Handle h : handles) {
			Owner o = ((EditableHandle) h).getOwner();
			if (o == null)
				((EditableHandle) h).setOwner(this);
			else if (o != this)
				throw new IllegalArgumentException(
						"Handles sould be in the bigraph or be idle edges.");
		}
		EditableNode n = new EditableNode(c, (EditableParent) parent, handles);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Add an outername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with {@link OuterName#getName() }.
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName() {
		return addOuterName(new EditableOuterName());
	}

	/**
	 * Add an outername to the current bigraph.
	 * @param name name of the new outername
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName(String name) {
		return addOuterName(new EditableOuterName(name));
	}

	/**
	 * Add an outername to the current bigraph.
	 * @param n outername that will be added
	 * @return the reference to the new outername
	 * @see EditableOuterName
	 */
	private OuterName addOuterName(EditableOuterName n) {
		n.setOwner(this);
		this.big.outers.add(n);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with {@link InnerName#getName() }. <br />
	 * This innername will be linked to a new edge that can be retrieved with {@link InnerName#getHandle() }.
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName() {
		return addInnerName(new EditableInnerName(), new EditableEdge(this));
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with {@link InnerName#getName() }.
	 * @param handle outername or edge that will be linked with the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(Handle handle) {
		return addInnerName(new EditableInnerName(), (EditableHandle) handle);
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * It will be linked to a new edge that can be retrieved with {@link InnerName#getHandle()}.
	 * @param name name of the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(String name) {
		return addInnerName(name, new EditableEdge(this));
	}

	/**
	 * Add a new innername to the current bigraph.
	 * @param name name of the new innername
	 * @param handle outername or edge that will be linked with the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(String name, Handle handle) {
		return addInnerName(new EditableInnerName(name),
				(EditableHandle) handle);
	}

	/**
	 * Add an innername to the current bigraph. <br />
	 * @param n innername that will be added
	 * @param h outername or edge that will be linked with the innername in input
	 * @return the reference of the innername
	 */
	private InnerName addInnerName(EditableInnerName n, EditableHandle h) {
		Set<? extends Edge> es = this.getEdges();
		if (!this.getOuterNames().contains(h)
				&& !(h instanceof Edge && (es.contains(h) || (h.getPoints()
						.size() == 0))))
			throw new IllegalArgumentException(
					"Handles sould be in the bigraph or be idle edges.");
		n.setHandle(h);
		this.big.inners.add(n);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Set a new handle (outername or edge) for a point (innername or node's port).
	 * @param point 
	 * @param handle
	 */
	public void relink(Point point, Handle handle) {
		EditablePoint p = (EditablePoint) point;
		EditableHandle h = (EditableHandle) handle;
		Owner o1 = p.getOwner();
		Owner o2 = h.getOwner();
		if (o1 == null || o1 != this || o2 == null || o2 != this) {
			throw new IllegalArgumentException(
					"Point and handle sould be in the bigraph.");
		}
		p.setHandle(h);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Set a new edge for two points (innername or node's port), linking them.
	 * @param p1 first point 
	 * @param p2 second point
	 * @return the new edge connecting the points in input
	 */
	public Edge relink(Point p1, Point p2) {
		EditablePoint t1 = (EditablePoint) p1;
		EditablePoint t2 = (EditablePoint) p2;
		Owner o1 = t1.getOwner();
		Owner o2 = t2.getOwner();
		if (o1 == null || o1 != this || o2 == null || o2 != this) {
			throw new IllegalArgumentException(
					"Points sould be in the bigraph.");
		}
		EditableEdge e = new EditableEdge();
		e.setOwner(this);
		t1.setHandle(e);
		t2.setHandle(e);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return e;
	}

	/**
	 * Set a new edge for an arbitrary number of points (innername or node's port), linking them.
	 * @param points series of points
	 * @return the new edge connecting the points in input
	 */
	public Edge relink(Point... points) {
		EditablePoint[] ps = new EditablePoint[points.length];
		for (int i = 0; i < points.length; i++) {
			ps[i] = (EditablePoint) points[i];
			Owner o = ps[i].getOwner();
			if (o == null || o != this) {
				throw new IllegalArgumentException(
						"Points sould be in the bigraph.");
			}
		}
		EditableEdge e = new EditableEdge();
		e.setOwner(this);
		for (int i = 0; i < points.length; i++) {
			ps[i].setHandle(e);
		}
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return e;
	}

	/**
	 * disconnect a point from its current handle and connect it with a new edge.
	 * @param p the point that will be unlinked
	 * @return the new edge
	 */
	public Edge unlink(Point p) {
		return relink(p);
	}

	/**
	 * Merge regions (roots of a place graph)
	 */
	public void merge() {
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		for (EditableParent p : big.roots) {
			for (EditableChild c : new HashSet<>(p.getEditableChildren())) {
				c.setParent(r);
			}
		}
		big.roots.clear();
		big.roots.add(r);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Close all site an innernames of the current bigraph, generating a ground bigraph.
	 */
	public void ground(){
		for(EditableChild s : big.sites)
			s.setParent(null);
		for(EditablePoint i : big.inners)
			i.setHandle(null);
		big.sites.clear();
		big.inners.clear();
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}
	
	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void leftJuxtapose(Bigraph graph) {
		leftJuxtapose(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void leftJuxtapose(Bigraph graph, boolean reuse) {
		Bigraph left = graph;
		Bigraph right = this.big;
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
		Bigraph r = right;
		for (EditableOwned o : l.roots) {
			o.setOwner(this);
		}
		for (EditableOwned o : l.outers) {
			o.setOwner(this);
		}
		for (Edge e : l.getEdges()) {
			((EditableEdge) e).setOwner(this);
		}
		r.roots.addAll(l.roots);
		r.sites.addAll(l.sites);
		r.outers.addAll(l.outers);
		r.inners.addAll(l.inners);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void rightJuxtapose(Bigraph graph) {
		rightJuxtapose(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void rightJuxtapose(Bigraph graph, boolean reuse) {
		Bigraph left = this.big;
		Bigraph right = graph;
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
		Bigraph l = left;
		Bigraph r = (reuse) ? right : right.clone();
		for (EditableOwned o : r.roots) {
			o.setOwner(this);
		}
		for (EditableOwned o : r.outers) {
			o.setOwner(this);
		}
		for (Edge e : r.getEdges()) {
			((EditableEdge) e).setOwner(this);
		}
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(r.outers);
		l.inners.addAll(r.inners);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Compose the current bigraphbuilder with the bigraph in input.
	 * @param graph the "inner" bigraph
	 */
	public void innerCompose(Bigraph graph) {
		innerCompose(graph, false);
	}

	/**
	 * Compose the current bigraphbuilder with the bigraph in input.
	 * @param graph the "inner" bigraph
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void innerCompose(Bigraph graph, boolean reuse) {
		Bigraph in = graph;
		Bigraph out = this.big;
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
		Bigraph a = out;
		Bigraph b = (reuse) ? in : in.clone();
		Set<? extends Edge> es = b.getEdges();
		// iterate over sites and roots of a and b respectively and glue them
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableSite s = is.next();
			EditableParent p = s.getParent();
			p.removeChild(s);
			for (EditableChild c : new HashSet<>(ir.next()
					.getEditableChildren())) {
				c.setParent(p);
			}
		}
		// iterate over inner and outer names of a and b respectively and glue
		// them
		Map<String,EditableHandle> a_inners = new HashMap<>();
		for (EditableInnerName i : a.inners) {
			a_inners.put(i.getName(), i.getHandle());
			i.setHandle(null);
		}
		for (EditableOuterName o : b.outers) {
			EditableHandle h = a_inners.get(o.getName());
			for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
				p.setHandle(h);
			}
		}
		// update inner interfaces
		a.inners.clear();
		a.sites.clear();
		a.inners.addAll(b.inners);
		a.sites.addAll(b.sites);
		for (Edge e : es) {
			((EditableEdge) e).setOwner(this);
		}
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Compose bigraph in input with the current bigraphbuilder
	 * @param graph the "outer" bigraph
	 */
	public void outerCompose(Bigraph graph) {
		outerCompose(graph, false);
	}

	/**
	 * Compose the current bigraph in input with the bigraphbuilder.
	 * @param graph the "outer" bigraph
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void outerCompose(Bigraph graph, boolean reuse) {
		Bigraph in = this.big;
		Bigraph out = graph;
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
		Bigraph b = in; //this BB		
		Set<? extends Edge> es = a.getEdges();
		// iterates over sites and roots of a and b respectively and glues them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableSite s = is.next();
			EditableParent p = s.getParent();
			p.removeChild(s);
			for (EditableChild c : new HashSet<>(ir.next()
					.getEditableChildren())) {
				c.setParent(p);
			}
		}
		// iterates over inner and outer names of a and b respectively and glues
		// them
		Map<String,EditableHandle> a_inners = new HashMap<>();
		for (EditableInnerName i : a.inners) {
			a_inners.put(i.getName(), i.getHandle());
			i.setHandle(null);
		}
		for (EditableOuterName o : b.outers) {
			EditableHandle h = a_inners.get(o.getName());
			for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
				p.setHandle(h);
			}
		}
		// updates inner interfaces
		b.outers.clear();
		b.roots.clear();
		b.outers.addAll(a.outers);
		b.roots.addAll(a.roots);
		for (EditableOwned o : b.roots) {
			o.setOwner(this);
		}
		for (EditableOwned o : b.outers) {
			o.setOwner(this);
		}
		for (Edge e : es) {
			((EditableEdge) e).setOwner(this);
		}
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Nest the current bigraphbuilder with the bigraph in input. <br />
	 * Nesting, differently from composition, add bigraph's outernames to bigraphbuilder if they aren't already present.
	 * @param graph the "inner" bigraph
	 */
	public void innerNest(Bigraph graph) {
		innerNest(graph, false);
	}

	/**
	 * Nest the current bigraphbuilder with the bigraph in input. <br />
	 * Nesting, differently from composition, add bigraph's outername to bigraphbuilder if they aren't already present.
	 * @param graph the "inner" bigraph
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void innerNest(Bigraph graph, boolean reuse) {
		Bigraph in = graph;
		Bigraph out = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (out.signature != in.signature) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (out.inners.size() != 0 || out.sites.size() != in.roots.size()) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Map<String, EditableOuterName> nmap = new HashMap<>();
		for (EditableOuterName o : out.outers) {
			nmap.put(o.getName(), o);
		}
		for (EditableOuterName o : in.outers) {
			EditableOuterName p = nmap.get(o.getName());
			if (p == null) {
				p = (EditableOuterName) this.addOuterName(o.getName());
			}
			this.addInnerName(o.getName(), p);
		}
		this.innerCompose(in, reuse);
	}

	/**
	 * Nest bigraph in input with the current bigraphbuilder. <br />
	 * Nesting, differently from composition, add bigraph's outername to bigraphbuilder if they aren't already present. It will then perform the standard composition.
	 * @param graph the "inner" bigraph
	 */
	public void outerNest(Bigraph graph) {
		outerNest(graph, false);
	}

	/**
	 * Nest bigraph in input with the current bigraphbuilder. <br />
	 * Nesting, differently from composition, add bigraph's outername to bigraphbuilder if they aren't already present. It will then perform the standard composition.
	 * @param graph the "inner" bigraph
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void outerNest(Bigraph graph, boolean reuse) {
		Bigraph in = this.big;
		Bigraph out = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (out.signature != in.signature) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (out.inners.size() != 0 || out.sites.size() != in.roots.size()) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		if (reuse)
			out = out.clone();
		Map<String, EditableOuterName> nmap = new HashMap<>();
		for (EditableOuterName o : out.outers) {
			nmap.put(o.getName(), o);
		}
		for (EditableOuterName o : in.outers) {
			EditableOuterName p = nmap.get(o.getName());
			if (p == null) {
				p = new EditableOuterName(o.getName());
				p.setOwner(out);
				out.outers.add(p);
			}
			EditableInnerName i = new EditableInnerName(p.getName());
			i.setHandle(p);
			out.inners.add(i);
		}
		// System.out.println(in.toString() + in.isConsistent(this));
		// System.out.println(out.toString() + out.isConsistent());
		this.outerCompose(out, false);
	}

	/**
	 * Juxtapose bigraph in input with the current bigraphbuilder. <br />
	 * ParallelProduct, differently from the normal juxtapose, doesn't need disjoint sets of outernames for the two bigraphs. Common outernames will be merged. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void leftParallelProduct(Bigraph graph) {
		leftParallelProduct(graph, false);
	}

	/**
	 * Juxtapose bigraph in input with the current bigraphbuilder. <br />
	 * ParallelProduct, differently from the normal juxtapose, doesn't need disjoint sets of outernames for the two bigraphs. Common outernames will be merged. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void leftParallelProduct(Bigraph graph, boolean reuse) {
		Bigraph left = graph;
		Bigraph right = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (left.signature != right.signature) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = (reuse) ? left : left.clone();
		Bigraph r = right;
		for (EditableOwned o : l.roots) {
			o.setOwner(this);
		}
		List<EditableOuterName> os = new LinkedList<>();
		// merge outers
		for (EditableOuterName o : l.outers) {
			EditableOuterName q = null;
			for (EditableOuterName p : r.outers) {
				if (p.getName().equals(o.getName())) {
					q = p;
					break;
				}
			}
			if (q == null) {
				// o is not part of r.outerface
				os.add(o);
				o.setOwner(this);
			} else {
				// this name apperas also in r, merge points
				for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
					q.linkPoint(p);
				}
			}
		}
		for (Edge e : l.getEdges()) {
			((EditableEdge) e).setOwner(this);
		}
		r.roots.addAll(l.roots);
		r.sites.addAll(l.sites);
		r.outers.addAll(os);
		r.inners.addAll(l.inners);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * ParallelProduct, differently from the normal juxtapose, doesn't need disjoint sets of outernames for the two bigraphs. Common outernames will be merged. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void rightParallelProduct(Bigraph graph) {
		rightParallelProduct(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * ParallelProduct, differently from the normal juxtapose, doesn't need disjoint sets of outernames for the two bigraphs. Common outernames will be merged. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void rightParallelProduct(Bigraph graph, boolean reuse) {
		Bigraph left = this.big;
		Bigraph right = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (left.signature != right.signature) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)) {
			// TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = left;
		Bigraph r = (reuse) ? right : right.clone();
		for (EditableOwned o : r.roots) {
			o.setOwner(this);
		}
		List<EditableOuterName> os = new LinkedList<>();
		// merge outers
		for (EditableOuterName o : r.outers) {
			EditableOuterName q = null;
			for (EditableOuterName p : l.outers) {
				if (p.getName().equals(o.getName())) {
					q = p;
					break;
				}
			}
			if (q == null) {
				// o is not part of r.outerface
				os.add(o);
				o.setOwner(this);
			} else {
				// this name apperas also in r, merge points
				for (EditablePoint p : new HashSet<>(o.getEditablePoints())) {
					q.linkPoint(p);
				}
			}
		}
		for (Edge e : r.getEdges()) {
			((EditableEdge) e).setOwner(this);
		}
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(os);
		l.inners.addAll(r.inners);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Juxtapose bigraph in input with the current bigraphbuilder. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting bigraphbuilder. <br />
	 * Sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void leftMergeProduct(Bigraph graph) {
		leftMergeProduct(graph, false);
	}

	/**
	 * Juxtapose bigraph in input with the current bigraphbuilder. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting bigraphbuilder. <br />
	 * Sites of the bigraph will precede those of the bigraphbuilder in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void leftMergeProduct(Bigraph graph, boolean reuse) {
		leftJuxtapose(graph, reuse);
		merge();
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting bigraphbuilder. <br />
	 * Sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 */
	public void rightMergeProduct(Bigraph graph) {
		rightMergeProduct(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting bigraphbuilder. <br />
	 * Sites of the bigraphbuilder will precede those of the bigraph in the resulting bigraphbuilder.
	 * @param graph bigraph that will be juxtaposed.
	 * @param reuse flag. If true, the bigraph in input won't be copied.
	 */
	public void rightMergeProduct(Bigraph graph, boolean reuse) {
		rightJuxtapose(graph, reuse);
		merge();
	}
}
