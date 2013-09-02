package jlibbig.core;

import java.util.*;

import jlibbig.core.exceptions.*;

/**
 * The class is meant as a helper for bigraph construction and manipulation in
 * presence of series of operations since {@link Bigraph} is immutable.
 * <p>
 * e.g. {@link Bigraph#compose(Bigraph, Bigraph)} or
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object.
 * </p>
 */
final public class BigraphBuilder implements AbstractBigraphBuilder {
	private final boolean DEBUG_CONSISTENCY_CHECK = true;

	private Bigraph big;
	private boolean closed = false;

	public BigraphBuilder(Signature sig) {
		this.big = Bigraph.makeEmpty(sig);
	}

	public BigraphBuilder(Bigraph big) {
		this(big, false);
	}

	BigraphBuilder(Bigraph big, boolean reuse) {
		if (!big.isConsistent())
			throw new IllegalArgumentException("Inconsistent bigraph.");
		this.big = (reuse) ? big.setOwner(this) : big.clone(this);
	}

	@Override
	public String toString() {
		assertOpen();
		return big.toString();
	}

	/**
	 * Return the bigraph build so far.
	 * 
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph() {
		return makeBigraph(false);
	}

	/**
	 * Return the bigraph build so far.
	 * 
	 * @param close
	 *            disables the builder to perform any other operation.
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph(boolean close) {
		assertOpen();
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
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

	public boolean isClosed() {
		return closed;
	}

	private void assertOpen() {
		if (this.closed)
			throw new UnsupportedOperationException(
					"The operation is not supported by a closed BigraphBuilder");
	}

	/*
	 * private void assertOpen(String operation){ if(this.closed) throw new
	 * UnsupportedOperationException("The operation <" + operation
	 * +"> is not supported by a closed BigraphBuilder"); }
	 */

	@Override
	public BigraphBuilder clone() {
		assertOpen();
		BigraphBuilder bb = new BigraphBuilder(this.big.getSignature());
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

	/**
	 * Get bigraph's roots.
	 * 
	 * @return a list carrying bigraph's roots
	 */
	@Override
	public List<? extends Root> getRoots() {
		assertOpen();
		return this.big.getRoots();
	}

	/**
	 * Get bigraph's sites.
	 * 
	 * @return a list carrying bigraph's sites
	 */
	@Override
	public List<? extends Site> getSites() {
		assertOpen();
		return this.big.getSites();
	}

	/**
	 * Get bigraph's outer names.
	 * 
	 * @return a list carrying bigraph's outer names
	 */
	@Override
	public Set<? extends OuterName> getOuterNames() {
		assertOpen();
		return this.big.getOuterNames();
	}

	/**
	 * Get bigraph's inner names.
	 * 
	 * @return a list carrying bigraph's inner names
	 */
	@Override
	public Set<? extends InnerName> getInnerNames() {
		assertOpen();
		return this.big.getInnerNames();
	}

	/**
	 * Get bigraph's nodes.
	 * 
	 * @return a set containing bigraph's nodes.
	 */
	@Override
	public Set<? extends Node> getNodes() {
		assertOpen();
		return this.big.getNodes();
	}

	/**
	 * Get bigraph's edges.
	 * 
	 * @return a set containing bigraph's edges.
	 */
	@Override
	public Set<? extends Edge> getEdges() {
		assertOpen();
		return this.big.getEdges();
	}

	// /////////////////////////////////////////////////////////////////////////

	private void assertOwner(Owned owned, String obj) {
		if (owned == null)
			throw new IllegalArgumentException(obj + " can not be null.");
		Owner o = owned.getOwner();
		if (o != this)
			throw new IllegalArgumentException(obj
					+ " should be owned by this structure.");
	}

	private void assertOrSetOwner(Owned owned, String obj) {
		if (owned == null)
			throw new IllegalArgumentException(obj + " can not be null.");
		Owner o = owned.getOwner();
		if (o == null)
			((EditableOwned) owned).setOwner(this);
		else if (o != this)
			throw new IllegalArgumentException(obj
					+ " already owned by an other structure.");
	}

	/**
	 * Add a root to the current bigraph
	 * 
	 * @return the reference of the new root
	 */
	public Root addRoot() {
		assertOpen();
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
	 * 
	 * @param parent
	 *            the handler, in the place graph, father of the new site
	 * @return the reference of the new site
	 */
	public Site addSite(Parent parent) {
		assertOpen();
		assertOwner(parent, "Parent");
		EditableSite s = new EditableSite((EditableParent) parent);
		this.big.sites.add(s);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return s;
	}

	/**
	 * Add a new node to the bigraph
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent) {
		return addNode(controlName, parent, new LinkedList<Handle>());
	}

	/**
	 * Add a new node to the bigraph
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param handles
	 *            Handles (outernames or edges) that will be linked to new
	 *            node's ports
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent, Handle... handles) {
		assertOpen();
		Control c = this.big.getSignature().getByName(controlName);
		if (c == null)
			throw new IllegalArgumentException(
					"Control should be in the signature.");
		assertOwner(parent, "Parent");
		EditableHandle[] hs = new EditableHandle[c.getArity()];
		for (int i = 0; i < hs.length; i++) {
			if (i < handles.length) {
				hs[i] = (EditableHandle) handles[i];
			}
			if (hs[i] == null)
				hs[i] = new EditableEdge();
			assertOrSetOwner(hs[i], "Handle");
		}
		EditableNode n = new EditableNode(c, (EditableParent) parent, hs);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Add a new node to the bigraph
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param handles
	 *            list of handles (outernames or edges) that will be linked to
	 *            new node's ports
	 * @return the reference of the new node
	 */
	public Node addNode(String controlName, Parent parent, List<Handle> handles) {
		assertOpen();
		Control c = this.big.getSignature().getByName(controlName);
		if (c == null)
			throw new IllegalArgumentException(
					"Control should be in the signature.");
		assertOwner(parent, "Parent");
		EditableHandle[] hs = new EditableHandle[c.getArity()];
		for (int i = 0; i < hs.length; i++) {
			if (i < handles.size()) {
				hs[i] = (EditableHandle) handles.get(i);
			}
			if (hs[i] == null)
				hs[i] = new EditableEdge();
			assertOrSetOwner(hs[i], "Handle");
		}
		EditableNode n = new EditableNode(c, (EditableParent) parent, hs);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Add an outername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with
	 * {@link OuterName#getName() }.
	 * 
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName() {
		return addOuterName(new EditableOuterName());
	}

	/**
	 * Add an outername to the current bigraph.
	 * 
	 * @param name
	 *            name of the new outername
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName(String name) {
		return addOuterName(new EditableOuterName(name));
	}

	/**
	 * Add an outername to the current bigraph.
	 * 
	 * @param n
	 *            outername that will be added
	 * @return the reference to the new outername
	 * @see EditableOuterName
	 */
	private OuterName addOuterName(EditableOuterName n) {
		assertOpen();
		n.setOwner(this);
		this.big.outers.add(n);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with
	 * {@link InnerName#getName() }. <br />
	 * This innername will be linked to a new edge that can be retrieved with
	 * {@link InnerName#getHandle() }.
	 * 
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName() {
		return addInnerName(new EditableInnerName(), new EditableEdge(this));
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * Its name will be automatically chosen and can be retrieved with
	 * {@link InnerName#getName() }.
	 * 
	 * @param handle
	 *            outername or edge that will be linked with the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(Handle handle) {
		assertOrSetOwner(handle, "Handle");
		return addInnerName(new EditableInnerName(), (EditableHandle) handle);
	}

	/**
	 * Add a new innername to the current bigraph. <br />
	 * It will be linked to a new edge that can be retrieved with
	 * {@link InnerName#getHandle()}.
	 * 
	 * @param name
	 *            name of the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(String name) {
		return addInnerName(name, new EditableEdge(this));
	}

	/**
	 * 0 Add a new innername to the current bigraph.
	 * 
	 * @param name
	 *            name of the new innername
	 * @param handle
	 *            outername or edge that will be linked with the new innername
	 * @return the reference of the new innername
	 */
	public InnerName addInnerName(String name, Handle handle) {
		assertOrSetOwner(handle, "Handle");
		return addInnerName(new EditableInnerName(name),
				(EditableHandle) handle);
	}

	/**
	 * Add an innername to the current bigraph. <br />
	 * 
	 * @param n
	 *            innername that will be added
	 * @param h
	 *            outername or edge that will be linked with the innername in
	 *            input
	 * @return the reference of the innername
	 */
	private InnerName addInnerName(EditableInnerName n, EditableHandle h) {
		assertOpen();
		n.setHandle(h);
		this.big.inners.add(n);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return n;
	}

	/**
	 * Set a new handle (outername or edge) for a point (innername or node's
	 * port).
	 * 
	 * @param point
	 * @param handle
	 */
	public void relink(Point point, Handle handle) {
		assertOpen();
		assertOrSetOwner(handle, "Handle");
		assertOrSetOwner(point, "Point");
		EditablePoint p = (EditablePoint) point;
		EditableHandle h = (EditableHandle) handle;
		p.setHandle(h);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Set a new edge for two points (innername or node's port), linking them.
	 * 
	 * @param p1
	 *            first point
	 * @param p2
	 *            second point
	 * @return the new edge connecting the points in input
	 */
	public Edge relink(Point p1, Point p2) {
		assertOpen();
		assertOwner(p1, "Point");
		assertOwner(p2, "Point");
		EditablePoint t1 = (EditablePoint) p1;
		EditablePoint t2 = (EditablePoint) p2;
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
	 * Set a new edge for an arbitrary number of points (innername or node's
	 * port), linking them.
	 * 
	 * @param points
	 *            series of points
	 * @return the new edge connecting the points in input
	 */
	public Edge relink(Point... points) {
		assertOpen();
		EditablePoint[] ps = new EditablePoint[points.length];
		for (int i = 0; i < points.length; i++) {
			ps[i] = (EditablePoint) points[i];
			assertOwner(ps[i], "Point");
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
	 * Set a new edge for an arbitrary number of points (innername or node's
	 * port), linking them.
	 * 
	 * @param points
	 *            series of points
	 * @return the new edge connecting the points in input
	 */
	public Edge relink(Collection<? extends Point> points) {
		return relink(points.toArray(new EditablePoint[points.size()]));
	}

	/**
	 * disconnect a point from its current handle and connect it with a new
	 * edge.
	 * 
	 * @param point
	 *            the point that will be unlinked
	 * @return the new edge
	 */
	public Edge unlink(Point point) {
		return relink(point);
	}

	public Edge closeOuterName(String name) {
		EditableOuterName n1 = null;
		Iterator<EditableOuterName> in = big.outers.iterator();
		while ((n1 = in.next()) != null && !n1.getName().equals(name)) {
		}
		Edge e = null;
		if (n1 != null) {
			e = relink(n1.getEditablePoints());
			big.outers.remove(n1);
			n1.setOwner(null);
		}
		return e;
	}

	public Edge closeOuterName(OuterName name) {
		assertOwner(name, "OuterName ");
		EditableOuterName n1 = (EditableOuterName) name;
		Edge e = relink(n1.getEditablePoints());
		big.outers.remove(n1);
		n1.setOwner(null);
		return e;
	}

	public void closeInnerName(String name) {
		EditableInnerName n1 = null;
		Iterator<EditableInnerName> in = big.inners.iterator();
		while ((n1 = in.next()) != null && !n1.getName().equals(name)) {
		}
		if (n1 != null) {
			n1.setHandle(null);
			big.inners.remove(n1);
		}
	}

	public void closeInnerName(InnerName name) {
		assertOwner(name, "InnerName ");
		EditableInnerName n1 = (EditableInnerName) name;
		n1.setHandle(null);
		big.inners.remove(n1);
	}

	public void renameOuterName(String oldName, String newName) {
		if (newName == null || oldName == null)
			throw new IllegalArgumentException("Arguments can not be null");
		EditableOuterName n1 = null, n2 = null;
		Iterator<EditableOuterName> in = big.outers.iterator();
		while (in.hasNext() && (n1 == null || n2 == null)) {
			EditableOuterName n0 = in.next();
			if (n1 == null && n0.getName().equals(oldName))
				n1 = n0;
			if (n2 == null && n0.getName().equals(newName))
				n2 = n0;
		}
		if (n1 != null) {
			if (n2 == null) {
				if (!newName.equals(oldName))
					n1.setName(newName);
			} else
				throw new NameClashException(oldName, newName);
		} else {
			throw new IllegalArgumentException("The '" + oldName
					+ "' is not present.");
		}
	}

	public void renameOuterName(OuterName oldName, String newName) {
		if (newName == null || oldName == null)
			throw new IllegalArgumentException("Arguments can not be null");
		assertOwner(oldName, "OuterName ");
		if (newName.equals(oldName.getName()))
			return;
		EditableOuterName n2 = null;
		Iterator<EditableOuterName> in = big.outers.iterator();
		while (in.hasNext()) {
			EditableOuterName n0 = in.next();
			if (n0.getName().equals(newName)){
				n2 = n0;
				break;
			}
		}
		if (n2 == null) {
			((EditableOuterName) oldName).setName(newName);
		} else {
			throw new IllegalArgumentException("Name '" + newName
					+ "' already in use");
		}
	}

	public void renameInnerName(String oldName, String newName) {
		if (newName == null || oldName == null)
			throw new IllegalArgumentException("Arguments can not be null");
		EditableInnerName n1 = null, n2 = null;
		Iterator<EditableInnerName> in = big.inners.iterator();
		while (in.hasNext() && (n1 == null || n2 == null)) {
			EditableInnerName n0 = in.next();
			if (n1 == null && n0.getName().equals(oldName))
				n1 = n0;
			if (n2 == null && n0.getName().equals(newName))
				n2 = n0;
		}
		if (n1 != null) {
			if (n2 == null) {
				if (!newName.equals(oldName))
					n1.setName(newName);
			} else
				throw new IllegalArgumentException("Name '" + newName
						+ "' already in use");
		} else {
			throw new IllegalArgumentException("The '" + oldName
					+ "' is not present.");
		}
	}

	public void renameInnerName(InnerName oldName, String newName) {
		if (newName == null || oldName == null)
			throw new IllegalArgumentException("Arguments can not be null");
		assertOwner(oldName, "InnerName ");
		if (newName.equals(oldName.getName()))
			return;
		EditableInnerName n2 = null;
		Iterator<EditableInnerName> in = big.inners.iterator();
		while (in.hasNext()) {
			EditableInnerName n0 = in.next();
			if (n0.getName().equals(newName)){
				n2 = n0;
				break;
			}
		}
		if (n2 == null) {
			((EditableInnerName) oldName).setName(newName);
		} else {
			throw new NameClashException(oldName.getName(), newName);
		}
	}

	/**
	 * Merge regions (roots of a place graph)
	 */
	public Root merge() {
		assertOpen();
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		for (EditableParent p : big.roots) {
			for (EditableChild c : new HashSet<>(p.getEditableChildren())) {
				c.setParent(r);
			}
		}
		clearOwnedCollection(big.roots);// .clear();
		big.roots.add(r);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return r;
	}
	
	public Root merge(int index, int... roots) {
		assertOpen();
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		EditableRoot[] rs = new EditableRoot[roots.length];
		for(int i = 0; i < roots.length; i++){
			rs[i] = big.roots.get(roots[i]);
			Iterator<EditableChild> ir = rs[i].getEditableChildren().iterator();
			while(ir.hasNext()){
				ir.next().setParent(r);
			}
		}
		for(int i = 0; i < rs.length; i++){
			big.roots.remove(rs[i]);	
			rs[i].setOwner(null);
		}
		big.roots.add(index,r);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
		return r;
	}
	
	public void removeRoot(Root root) {
		assertOwner(root,"Root ");
		if(!root.getChildren().isEmpty())
			throw new IllegalArgumentException("Unempty region.");
		((EditableRoot) root).setOwner(null);
		big.roots.remove(root);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}
	
	public void removeRoot(int index) {
		if(index < 0 || index >= big.roots.size())
			throw new IndexOutOfBoundsException("The argument does not refer to a root.");
		removeRoot(big.roots.get(index));
	}
	
	public void closeSite(Site site) {
		assertOwner(site,"Site ");
		((EditableSite) site).setParent(null);
		big.sites.remove(site);
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}
	
	public void closeSite(int index) {
		if(index < 0 || index >= big.sites.size())
			throw new IndexOutOfBoundsException("The argument does not refer to a site.");
		closeSite(big.sites.get(index));
	}

	/**
	 * Close all site an innernames of the current bigraph, generating a ground
	 * bigraph.
	 */
	public void ground() {
		assertOpen();
		for (EditableChild s : big.sites)
			s.setParent(null);
		for (EditablePoint i : big.inners)
			i.setHandle(null);
		clearChildCollection(big.sites);// .clear();
		clearPointCollection(big.inners);// .clear();
		// TODO skip check on internal data
		if (DEBUG_CONSISTENCY_CHECK && !big.isConsistent(this))
			throw new RuntimeException("Inconsistent bigraph.");
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftJuxtapose(Bigraph graph) {
		leftJuxtapose(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void leftJuxtapose(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph left = graph;
		Bigraph right = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!left.signature.equals(right.signature)) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)
				|| !Collections.disjoint(left.outers, right.outers)) {
			throw new IncompatibleInterfacesException(left, right,
					new NameClashException(intersectNames(left.inners,
							right.inners,
							intersectNames(left.outers, right.outers))));
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
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightJuxtapose(Bigraph graph) {
		rightJuxtapose(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void rightJuxtapose(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph left = this.big;
		Bigraph right = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!left.signature.equals(right.signature)) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)
				|| !Collections.disjoint(left.outers, right.outers)) {
			throw new IncompatibleInterfacesException(left, right,
					new NameClashException(intersectNames(left.inners,
							right.inners,
							intersectNames(left.outers, right.outers))));
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
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 */
	public void innerCompose(Bigraph graph) {
		innerCompose(graph, false);
	}

	/**
	 * Compose the current bigraphbuilder with the bigraph in input.
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void innerCompose(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph in = graph;
		Bigraph out = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.equals(in.outers)
				|| out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfacesException(in, out,
					"The outer face of the first graph must be equal to inner face of the second");
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
		Map<String, EditableHandle> a_inners = new HashMap<>();
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
		clearPointCollection(a.inners);// .clear();
		clearChildCollection(a.sites);// ;.clear();
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
	 * 
	 * @param graph
	 *            the "outer" bigraph
	 */
	public void outerCompose(Bigraph graph) {
		outerCompose(graph, false);
	}

	/**
	 * Compose the current bigraph in input with the bigraphbuilder.
	 * 
	 * @param graph
	 *            the "outer" bigraph
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void outerCompose(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph in = this.big;
		Bigraph out = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.equals(in.outers)
				|| out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfacesException(in, out,
					"The outer face of the first graph must be equal to inner face of the second");
		}
		Bigraph a = (reuse) ? out : out.clone();
		Bigraph b = in; // this BB
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
		Map<String, EditableHandle> a_inners = new HashMap<>();
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
		clearOwnedCollection(b.outers);// .clear();
		clearOwnedCollection(b.roots);// .clear();
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
	 * Nesting, differently from composition, add bigraph's outernames to
	 * bigraphbuilder if they aren't already present.
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 */
	public void innerNest(Bigraph graph) {
		innerNest(graph, false);
	}

	/**
	 * Nest the current bigraphbuilder with the bigraph in input. <br />
	 * Nesting, differently from composition, add bigraph's outername to
	 * bigraphbuilder if they aren't already present.
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void innerNest(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph in = graph;
		Bigraph out = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.isEmpty() || out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfacesException(in, out);
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
	 * Nesting, differently from composition, add bigraph's outername to
	 * bigraphbuilder if they aren't already present. It will then perform the
	 * standard composition.
	 * 
	 * @param graph
	 *            the "inner" bigraph
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
	 * @param graph
	 *            the "inner" bigraph
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void outerNest(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph in = this.big;
		Bigraph out = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!out.signature.equals(in.signature)) {
			throw new IncompatibleSignatureException(out.signature,
					in.signature);
		}
		if (!out.inners.isEmpty() || out.sites.size() != in.roots.size()) {
			throw new IncompatibleInterfacesException(in, out);
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
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged. <br />
	 * Roots and sites of the bigraph will precede those of the bigraphbuilder
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
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
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
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
		if (!Collections.disjoint(left.inners, right.inners)) {
			throw new IncompatibleInterfacesException(left, right,
					new NameClashException(intersectNames(left.inners,
							right.inners)));
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
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged. <br />
	 * Roots and sites of the bigraphbuilder will precede those of the bigraph
	 * in the resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
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
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void rightParallelProduct(Bigraph graph, boolean reuse) {
		assertOpen();
		Bigraph left = this.big;
		Bigraph right = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are
		// well defined)
		if (!left.signature.equals(right.signature)) {
			throw new IncompatibleSignatureException(left.signature,
					right.signature);
		}
		if (!Collections.disjoint(left.inners, right.inners)) {
			throw new IncompatibleInterfacesException(left, right,
					new NameClashException(intersectNames(left.inners,
							right.inners)));
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
	 * Perform then {@link BigraphBuilder#merge()} on the resulting
	 * bigraphbuilder. <br />
	 * Sites of the bigraph will precede those of the bigraphbuilder in the
	 * resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftMergeProduct(Bigraph graph) {
		leftMergeProduct(graph, false);
	}

	/**
	 * Juxtapose bigraph in input with the current bigraphbuilder. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting
	 * bigraphbuilder. <br />
	 * Sites of the bigraph will precede those of the bigraphbuilder in the
	 * resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void leftMergeProduct(Bigraph graph, boolean reuse) {
		leftJuxtapose(graph, reuse);
		merge();
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting
	 * bigraphbuilder. <br />
	 * Sites of the bigraphbuilder will precede those of the bigraph in the
	 * resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightMergeProduct(Bigraph graph) {
		rightMergeProduct(graph, false);
	}

	/**
	 * Juxtapose the current bigraphbuilder with the bigraph in input. <br />
	 * Perform then {@link BigraphBuilder#merge()} on the resulting
	 * bigraphbuilder. <br />
	 * Sites of the bigraphbuilder will precede those of the bigraph in the
	 * resulting bigraphbuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 * @param reuse
	 *            flag. If true, the bigraph in input won't be copied.
	 */
	public void rightMergeProduct(Bigraph graph, boolean reuse) {
		rightJuxtapose(graph, reuse);
		merge();
	}

	private static Set<String> intersectNames(Set<? extends LinkFacet> arg0,
			Set<? extends LinkFacet> arg1) {
		return intersectNames(arg0, arg1, new HashSet<String>());
	}

	private static Set<String> intersectNames(Set<? extends LinkFacet> arg0,
			Set<? extends LinkFacet> arg1, Set<String> ns0) {
		Set<String> ns1 = new HashSet<>();
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
			Collection<? extends EditableOwned> col) {
		for (EditableOwned i : col) {
			i.setOwner(null);
		}
		col.clear();
	}

	private static void clearChildCollection(
			Collection<? extends EditableChild> col) {
		for (EditableChild i : col) {
			i.setParent(null);
		}
		col.clear();
	}

	private static void clearPointCollection(
			Collection<? extends EditablePoint> col) {
		for (EditablePoint i : col) {
			i.setHandle(null);
		}
		col.clear();
	}
}
