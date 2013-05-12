package jlibbig;

import java.util.*;

public class LinkGraph{

	private final Signature<LinkGraphControl> _sig;
	private final Set<OuterName> _outerNames = new HashSet<>();
	private final Set<InnerName> _innerNames = new HashSet<>();
	private final LinkGraphFace _outer = new LGFace((Set<LinkGraphFacet>) (Set<?>)_outerNames);
	private final LinkGraphFace _inner = new LGFace((Set<LinkGraphFacet>) (Set<?>)_innerNames);
	private final Set<LinkGraphNode> _nodes = new HashSet<>();
	private final Set<Port> _ports = new HashSet<>();
	private final Set<Edge> _edges = new HashSet<>();

	private final Map<Linked, Linker> _lnk = new HashMap<>();
	private final Map<Linker, Set<Linked>> _rev = new HashMap<>();

	public LinkGraph(Signature<LinkGraphControl> s) {
		this._sig = s;
	}

	/**
	 * 
	 */
	private void removeIdleEdges() {
		Collection<Edge> r = new LinkedList<>();
		for (Edge e : _edges) {
			if (_rev.get(e).isEmpty()) {
				r.add(e);
			}
		}
		_edges.removeAll(r);
	}

	/*
	 * helper methods for linkgraph construction, can introduce inconsistencies
	 * things (ports/inner names) linked to null. this is intentional since
	 * addNode addInnerName are meant to be used with addLink.
	 * g.addLink(g.addInnerName("x"),g.addEdge("e")); of course it is possible
	 * to generate fresh edges for every added port and inner name but this is
	 * indeed safer but also less concise (temporary edges are generated and a
	 * lot of extra lookup in _lnk are required).
	 */

	/**
	 * Adds a new edge.
	 * 
	 * @return the edge just added
	 */
	private Edge addEdge() {
		return addEdge(new Edge());
	}

	/**
	 * Adds the edge provided.
	 * 
	 * @throws IllegalArgumentException
	 * @param e
	 *            the edge to be added
	 * @return the edge just added
	 */
	private Edge addEdge(Edge e) {
		if (_edges.contains(e))
			throw new IllegalArgumentException("Edge already present");
		_edges.add(e);
		_rev.put(e, new HashSet<Linked>());
		return e;
	}

	/**
	 * @param c
	 * @param n
	 * @return
	 */
	@SuppressWarnings("unused")
	private LinkGraphNode addNode(LinkGraphControl c, String n) {
		return addNode(new LGNode(c, n));
	}

	/**
	 * @param n
	 * @return
	 */
	private LinkGraphNode addNode(LinkGraphNode n) {
		if (!this._sig.contains(n.getControl())) {
			throw new IllegalArgumentException(
					"Control not present in the signature");
		}
		if (_nodes.contains(n))
			throw new IllegalArgumentException("Node already present");
		_nodes.add(n);
		for (Port p : n.getPorts()) {
			_ports.add(p);
			_lnk.put(p, null); // ports are not linked, the linkgraph is not
								// well-formed
		}
		return n;
	}

	/**
	 * @param n
	 * @return
	 */
	private InnerName addInnerName(String n) {
		return addInnerName(new InnerName(n));
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	private InnerName addInnerName() {
		return addInnerName(new InnerName());
	}

	/**
	 * @param n
	 * @return
	 */
	private InnerName addInnerName(InnerName n) {
		if (_innerNames.contains(n))
			throw new IllegalArgumentException("Name already present");
		_innerNames.add(n);
		_lnk.put(n, null);
		return n;
	}

	/**
	 * @param n
	 * @return
	 */
	private OuterName addOuterName(String n) {
		return addOuterName(new OuterName(n));
	}

	/**
	 * @return
	 */
	private OuterName addOuterName() {
		return addOuterName(new OuterName());
	}

	/**
	 * @param n
	 * @return
	 */
	private OuterName addOuterName(OuterName n) {
		if (_outerNames.contains(n))
			throw new IllegalArgumentException("Name already present");
		_outerNames.add(n);
		_rev.put(n, new HashSet<Linked>());
		return n;
	}

	/**
	 * @param d
	 * @param r
	 */
	private void addLink(Linked d, Linker r) {
		if (!_lnk.containsKey(d) || !_rev.containsKey(r))
			throw new IllegalArgumentException(
					"Arguments to be linked are not present in the graph");
		if (_lnk.get(d) != null)
			throw new IllegalArgumentException("Item already linked to "
					+ _lnk.get(d));
		_lnk.put(d, r);
		_rev.get(r).add(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected LinkGraph clone(){
		LinkGraph g = new LinkGraph(_sig);
		g._nodes.addAll(this._nodes);
		g._ports.addAll(this._ports);
		g._edges.addAll(this._edges);
		g._innerNames.addAll(this._innerNames);
		g._outerNames.addAll(this._outerNames);
		g._lnk.putAll(this._lnk);
		for (Linker r : this._rev.keySet()) {
			g._rev.put(r, new HashSet<Linked>(this._rev.get(r)));
		}
		return g;
	}

	public Signature<LinkGraphControl> getSignature() {
		return this._sig;
	}


	public Set<LinkGraphNode> getNodes() {
		return Collections.unmodifiableSet(this._nodes);
	}

	public Set<Port> getPorts() {
		return Collections.unmodifiableSet(this._ports);
	}

	public Set<Edge> getEdges() {
		return Collections.unmodifiableSet(this._edges);
	}

	public LinkGraphFace getInnerFace() {
		return this._inner;
	}
	
	public Set<InnerName> getInnerNames() {
		return Collections.unmodifiableSet(this._innerNames);
	}

	public LinkGraphFace getOuterFace() {
		return this._outer;
	}
	
	public Set<OuterName> getOuterNames() {
		return Collections.unmodifiableSet(this._outerNames);
	}

	public Linker getLink(Linked l) {
		return _lnk.get(l);
	}

	public Set<Linked> getLinked(Linker l) {
		return Collections.unmodifiableSet(_rev.get(l));
	}

	public boolean isEmpty() {
		return _nodes.isEmpty() && _outerNames.isEmpty()
				&& _innerNames.isEmpty();
	}
	
	public boolean isAgent() {
		return _inner.isEmpty();
	}
		
	synchronized LinkGraph leftJuxtapose(LinkGraph g) {
		// juxtapose is commutative on link graphs
		return rightJuxtapose(g);
	}
	
	synchronized LinkGraph rightJuxtapose(LinkGraph g) {
		if (!Collections.disjoint(this._innerNames, g._innerNames)
				|| !Collections.disjoint(this._outerNames, g._outerNames)) {
			throw new IllegalArgumentException("Overlapping interfaces");
		}
		if (!Collections.disjoint(this._nodes, g._nodes)
				|| !Collections.disjoint(this._edges, g._edges)) {
			throw new IllegalArgumentException("Overlapping supports");
		}
		this._nodes.addAll(g._nodes);
		this._ports.addAll(g._ports);
		this._edges.addAll(g._edges);
		this._outerNames.addAll(g._outerNames);
		this._innerNames.addAll(g._innerNames);
		this._lnk.putAll(g._lnk);
		for (Linker l : g._rev.keySet()) {
			this._rev.put(l, new HashSet<>(g._rev.get(l)));
		}
		return this;
	}

	synchronized LinkGraph outerCompose(LinkGraph g) {
		if (!this.getOuterFace().equals(g.getInnerFace())) {
			throw new IllegalArgumentException("Mismatching interfaces "
					+ this.getOuterFace() + " " + g.getInnerFace());
		}
		if (!Collections.disjoint(this._nodes, g._nodes)
				|| !Collections.disjoint(this._edges, g._edges)) {
			throw new IllegalArgumentException("Overlapping supports");
		}
		this._nodes.addAll(g._nodes);
		this._edges.addAll(g._edges);
		// bulk copy the link map (inconsistencies will be addressed below)
		this._lnk.putAll(g._lnk);
		// copy the reverse map
		for (Linker r : _rev.keySet()) {
			this._rev.put(r, new HashSet<>(g._rev.get(r)));
		}
		// iterate over names to be composed and glue link maps accordingly
		for (LinkGraphFacet on : this._outerNames) {
			for (LinkGraphFacet in : g._innerNames) {
				if (in.equals(on)) {
					Linker r1 = this._lnk.get(in); // follow links from inner names 	
					Set<Linked> l1 = this._rev.get(r1); // whatever is attached
														// to e1 in g
					Set<Linked> l2 = g._rev.get(on); // whatever is attached to
													 // on in this graph
					// attach items of l2 to e1
					l1.addAll(l2);
					// points in l2 have to be linked to e1
					for (Linked l : l2) {
						this._lnk.put(l, r1);
					}
					break;
				}
			}
		}
		this._outerNames.clear();
		this._outerNames.addAll(g._outerNames);
		removeIdleEdges();
		return this;
	}
	
	/**
	 * @param g
	 * @return
	 */
	synchronized LinkGraph innerCompose(LinkGraph g) {
		if (!this.getInnerFace().equals(g.getOuterFace())) {
			throw new IllegalArgumentException("Mismatching interfaces "
					+ this.getInnerFace() + " " + g.getOuterFace());
		}
		if (!Collections.disjoint(this._nodes, g._nodes)
				|| !Collections.disjoint(this._edges, g._edges)) {
			throw new IllegalArgumentException("Overlapping supports");
		}
		this._nodes.addAll(g._nodes);
		this._edges.addAll(g._edges);
		// bulk copy the link map (inconsistencies will be addressed below)
		this._lnk.putAll(g._lnk);
		// copy the reverse map only for edges (outer names will be discarded
		// anyway)
		for (Edge e : g._edges) {
			this._rev.put(e, new HashSet<>(g._rev.get(e)));
		}
		// iterate over names to be composed and glue link maps accordingly
		for (LinkGraphFacet on : g._outerNames) {
			for (LinkGraphFacet in : this._innerNames) {
				if (in.equals(on)) {
					Linker e1 = this._lnk.get(in); // an edge or an outer name
													// of this graph
					// Linker e2 = lg._lnk.get(on); // should always be on
					// itself by definition of link graphs
					Set<Linked> l1 = this._rev.get(e1); // whatever is attached
														// to e1 in this graph
					Set<Linked> l2 = g._rev.get(on); // whatever is attached to
														// on in g
					// attach items of l2 to e1
					l1.addAll(l2);
					// points in l2 have to be linked to e1
					for (Linked l : l2) {
						this._lnk.put(l, e1);
						// if(l instanceof Edge) something bad has happened
						// somewere
					}
					break;
				}
			}
		}
		this._innerNames.clear();
		this._innerNames.addAll(g._innerNames);
		removeIdleEdges();
		return this;
	}
	
	/**
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return {@literal true} if the bigraphs can be juxtaposed
	 */
	public static boolean areJuxtaposable(LinkGraph g1, LinkGraph g2) {
		if (!g1._sig.equals(g2._sig))
			return false;
		if (!Collections.disjoint(g1._inner.getNames(), g2._inner.getNames()))
			return false;
		if (!Collections.disjoint(g1._outer.getNames(), g2._outer.getNames()))
			return false;
		if (!Collections.disjoint(g1._nodes, g2._nodes))
			return false;
		if (!Collections.disjoint(g1.getEdges(), g2.getEdges()))
			return false;
		return true;
	}

	/**
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return {@literal true} if the the first can be composed after the latter
	 */
	public static boolean areComposable(LinkGraph g1, LinkGraph g2) {
		if (!g1._sig.equals(g2._sig))
			return false;
		if (!g1._inner.equals(g2._outer))
			return false;
		if (!Collections.disjoint(g1._nodes, g2._nodes))
			return false;
		if (!Collections.disjoint(g1.getEdges(), g2.getEdges()))
			return false;
		return true;
	}

	/**
	 * Juxtapose two link graphs into a new one.
	 * 
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return g1 juxtaposed to g2
	 */
	public static LinkGraph juxtapose(LinkGraph g1, LinkGraph g2) {
		return g1.clone().rightJuxtapose(g2);
	}

	/**
	 * Compose two bigraphs into a new one. For repeated operations use
	 * {@link BigraphBuilder}.
	 * 
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return g1 composed to g2
	 */
	public static LinkGraph compose(LinkGraph g1, LinkGraph g2) {
		return g1.clone().outerCompose(g2);
	}
	

	/**
	 * @param sig
	 *            The signature to be used The signature to be used
	 * @param node
	 * @return
	 */
	public static LinkGraph makeIon(Signature<LinkGraphControl> sig,
			LinkGraphNode node) {
		if (!sig.contains(node.getControl()))
			throw new IllegalArgumentException(
					"Control must be an element of the givent signature");
		LinkGraph g = new LinkGraph(sig);
		g.addNode(node);
		for (Port p : node.getPorts()) {
			g.addLink(p, g.addOuterName());
		}
		return g;
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param node
	 * @param names
	 * @return
	 */
	public static LinkGraph makeIon(Signature<LinkGraphControl> sig,
			LinkGraphNode node, String... names) {
		if (!sig.contains(node.getControl()))
			throw new IllegalArgumentException(
					"Control must be an element of the givent signature");
		List<Port> ps = node.getPorts();
		if (names.length < ps.size())
			throw new IllegalArgumentException("Not enough names");
		LinkGraph g = new LinkGraph(sig);
		g.addNode(node);
		for (int i = 0; i < ps.size(); i++) {
			g.addLink(ps.get(i), g.addOuterName(names[i]));
		}
		return g;
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param ctrl
	 * @return
	 */
	public static LinkGraph makeIon(Signature<LinkGraphControl> sig,
			LinkGraphControl ctrl) {
		return makeIon(sig, new LGNode(ctrl));
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param ctrl
	 * @param name
	 * @param names
	 * @return
	 */
	public static LinkGraph makeIon(Signature<LinkGraphControl> sig,
			LinkGraphControl ctrl, String name, String... names) {
		return makeIon(sig, new LGNode(ctrl, name), names);
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param names
	 * @return
	 */
	public static LinkGraph makeId(Signature<LinkGraphControl> sig,
			Set<String> names) {
		LinkGraph g = new LinkGraph(sig);
		for (String n : names) {
			g.addLink(g.addInnerName(n), g.addOuterName(n));
		}
		return g;
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param face
	 * @return
	 */
	public static LinkGraph makeId(Signature<LinkGraphControl> sig,
			LinkGraphFace face) {
		LinkGraph g = new LinkGraph(sig);
		for (LinkGraphFacet t : face.getNames()) {
			String n = t.getName();
			g.addLink(g.addInnerName(n), g.addOuterName(n));
		}
		return g;
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @return
	 */
	public static LinkGraph makeEmpty(Signature<LinkGraphControl> sig) {
		return new LinkGraph(sig);
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param subst
	 * @return
	 */
	public static LinkGraph makeSubstitution(Signature<LinkGraphControl> sig,
			Map<InnerName, OuterName> subst) {
		LinkGraph g = new LinkGraph(sig);
		for (InnerName i : subst.keySet()) {
			g.addLink(g.addInnerName(i), g.addOuterName(subst.get(i)));
		}
		return g;
	}

	/**
	 * Return a linkgraph with no actual link (every inner name is connected to
	 * its own edge and nothing is linked to any outer name)
	 * 
	 * @param sig
	 *            The signature to be used
	 * @param inners
	 * @param outers
	 * @return
	 */
	public static LinkGraph makeTaps(Signature<LinkGraphControl> sig,
			Set<String> inners, Set<String> outers) {
		LinkGraph g = new LinkGraph(sig);
		for (String n : inners) {
			g.addInnerName(n);
		}
		for (String n : outers) {
			g.addLink(g.addInnerName(n), g.addEdge());
		}
		return g;
	}

	/**
	 * @param sig
	 *            The signature to be used
	 * @param inner
	 * @param outer
	 * @return
	 */
	public static LinkGraph makeTaps(Signature<LinkGraphControl> sig,
			LinkGraphFace inner, LinkGraphFace outer) {
		LinkGraph g = new LinkGraph(sig);
		for (LinkGraphFacet t : inner.getNames()) {
			String n = t.getName();
			g.addInnerName(n);
		}
		for (LinkGraphFacet t : outer.getNames()) {
			String n = t.getName();
			g.addLink(g.addInnerName(n), g.addEdge());
		}
		return g;
	}
	

	public interface Linked{}
	public interface Linker{}

	/**
	 * package-private implementation of LinkGraphNode
	 * 
	 * @see jlibbig.LinkGraphNode
	 */
	protected static class LGNode extends Named implements LinkGraphNode {
		private final LinkGraphControl ctrl;
		private final int ar;
		private final List<Port> ports = new ArrayList<>();

		protected LGNode(LinkGraphControl ctrl) {
			super("N_" + generateName());
			this.ctrl = ctrl;
			this.ar = ctrl.getArity();
			for (int i = 0; i < ar; i++) {
				ports.add(new LGPort(this, i));
			}
		}

		protected LGNode(LinkGraphControl ctrl, String name) {
			super(name);
			this.ctrl = ctrl;
			this.ar = ctrl.getArity();
			for (int i = 0; i < ar; i++) {
				ports.add(new LGPort(this, i));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jlibbig.LinkGraphNode#getControl()
		 */
		@Override
		public LinkGraphControl getControl() {
			return this.ctrl;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jlibbig.LinkGraphNode#getPorts()
		 */
		@Override
		public List<Port> getPorts() {
			return Collections.unmodifiableList(ports);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jlibbig.LinkGraphNode#getPort(int)
		 */
		@Override
		public Port getPort(int index) {
			return ports.get(index);
		}

		private static class LGPort implements Port {
			private final LinkGraphNode node;
			private final int index;

			LGPort(LinkGraphNode node, int index) {
				this.node = node;
				this.index = index;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see jlibbig.Port#getNode()
			 */
			@Override
			public LinkGraphNode getNode() {
				return node;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see jlibbig.Port#getNumber()
			 */
			@Override
			public int getNumber() {
				return index;
			}

		}
	}

	/**
	 * package-private implementation of LinkGraphFace
	 * 
	 * @see jlibbig.LinkGraphFace
	 */
	private static class LGFace implements LinkGraphFace {

		private final Set<LinkGraphFacet> _facets;
		
		private LGFace(Set<LinkGraphFacet> facets) {
			if (facets == null)
				throw new IllegalArgumentException("Argument can not be null");
			this._facets = facets;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jlibbig.LinkGraphFace#getNames()
		 */
		@Override
		public Set<LinkGraphFacet> getNames() {
			return Collections.unmodifiableSet(this._facets);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jlibbig.GraphFace#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return _facets.isEmpty();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((_facets == null) ? 0 : _facets.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			LinkGraphFace other;
			try {
				other = (LinkGraphFace) obj;
			} catch (ClassCastException e) {
				return false;
			}
			return this.getNames().equals(other.getNames());
		}

		@Override
		public String toString() {
			String s = _facets.toString();
			return "<{" + s.substring(1, s.length() - 1) + "}>";
		}
	}
}
