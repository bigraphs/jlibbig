package jlibbig;

import java.util.*;

/**
 * Represents a bigraph over a fixed signature together with basic operations
 * from bigraph creation and manipulation (see Milner's algebra for bigraphs).
 */
public class Bigraph {

	private final Signature<BigraphControl> _sig;

	private final PlaceGraph _pg;
	private final LinkGraph _lg;

	private final BigraphFace _inner;
	private final BigraphFace _outer;

	private final Set<BigraphNode> _nodes = new HashSet<>();

	/**
	 * Place graph and link graph must share nodes and signatures.
	 * 
	 * @param sig
	 *            the signature
	 * @param pg
	 *            the place graph
	 * @param lg
	 *            the link graph
	 */
	private Bigraph(Signature<BigraphControl> sig, PlaceGraph pg, LinkGraph lg) {
		/*
		 * Signature and nodes checks can be skipped in production (since the
		 * constructor is meant to be internal).
		 * 
		 * @param sig can be inferred as the meet of the signatures of pg and lg
		 * if these shares the nodes.
		 */
		this(sig, pg, lg, false);
	}

	private Bigraph(Signature<BigraphControl> sig, PlaceGraph pg, LinkGraph lg,
			boolean skipChecks) {
		_sig = sig;
		_pg = pg;
		_lg = lg;
		if (skipChecks) {
			try {
				// actually both are made of BGControls
				Signature<PlaceGraphControl> pgs = _pg.getSignature();
				Signature<LinkGraphControl> lgs = _lg.getSignature();
				// however, check if every control in sig has a counterpart in
				// pgs
				// and lgs
				for (BigraphControl bc : _sig) {
					BigraphControl pc = (BigraphControl) pgs.getByName(bc
							.getName());
					BigraphControl lc = (BigraphControl) lgs.getByName(bc
							.getName());
					if (pc == null || lc == null
							|| bc.getArity() != lc.getArity()) {
						throw new IllegalArgumentException(
								"Incompatible signatures");
					}
				}
				// check if there are controls other than those in sig
				for (GraphControl c : pgs) {
					if (_sig.getByName(c.getName()) == null)
						throw new IllegalArgumentException(
								"Incompatible signatures");
				}
				for (GraphControl c : lgs) {
					if (_sig.getByName(c.getName()) == null)
						throw new IllegalArgumentException(
								"Incompatible signatures");
				}
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Incompatible signatures");
			}
			// place and link must share their nodes
			try {
				// all nodes found (these go into _nodes)
				Set<BigraphNode> ns1 = new HashSet<>();
				// initially is a replica of n2, but then every node found in
				// _lg
				// will be removed.
				// _pg and _lg are compatible iff ns2 is empty.
				Set<BigraphNode> ns2 = new HashSet<>();
				for (PlaceGraphNode pn : _pg.getNodes()) {
					BigraphNode bn = (BigraphNode) pn;
					ns1.add(bn);
					ns2.add(bn);
				}
				for (LinkGraphNode ln : _lg.getNodes()) {
					BigraphNode bn = (BigraphNode) ln;
					if (!ns2.remove(bn)) {
						throw new IllegalArgumentException(
								"Incompatible place and link graph");
					}
				}
				if (ns2.size() > 0) {
					throw new IllegalArgumentException(
							"Incompatible place and link graph");
				}
				_nodes.addAll(ns1);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(
						"Incompatible place and link graph");
			}
		} else {
			for (PlaceGraphNode pn : _pg.getNodes()) {
				_nodes.add((BigraphNode) pn);
			}
		}

		// interfaces
		_inner = new BGFace(_pg.getInnerFace(), _lg.getInnerFace());
		_outer = new BGFace(_pg.getOuterFace(), _lg.getOuterFace());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected synchronized Bigraph clone() {
		return new Bigraph(_sig.clone(), _pg.clone(), _lg.clone(), true);
	}
	
	// TODO equals and hashCode

	
	public boolean isEmpty() {
		return _pg.isEmpty() && _lg.isEmpty();
	}

	
	public boolean isAgent() {
		return _inner.isEmpty();
	}

	public PlaceGraph getPlaceGraph() {
		return _pg;
	}

	
	public LinkGraph getLinkGraph() {
		return  _lg;
	}

	
	public Signature<BigraphControl> getSignature() {
		return _sig;
	}

	
	public BigraphFace getInnerFace() {
		return _inner;
	}

	
	public BigraphFace getOuterFace() {
		return _outer;
	}

	
	public Set<BigraphNode> getNodes() {
		return Collections.unmodifiableSet(this._nodes);
	}

	
	public Set<Edge> getEdges() {
		return this._lg.getEdges();
	}

	/**
	 * Cast a bigraphical signature to a signature of place graph controls
	 * 
	 * @param sig
	 *            a bigraphical signature
	 * @return the same signature
	 */
	@SuppressWarnings("unchecked")
	private static Signature<PlaceGraphControl> asPlaceSignature(
			Signature<BigraphControl> sig) {
		return (Signature<PlaceGraphControl>) (Signature<?>) sig;
	}

	/**
	 * Cast a bigraphical signature to a signature of link graph controls
	 * 
	 * @param sig
	 *            a bigraphical signature
	 * @return the same signature
	 */
	@SuppressWarnings("unchecked")
	private static Signature<LinkGraphControl> asLinkSignature(
			Signature<BigraphControl> sig) {
		return (Signature<LinkGraphControl>) (Signature<?>) sig;
	}

	/**
	 * Juxtapose the argument on the right of this bigraph (which is modified
	 * accordingly while the argument is left untouched). For non desctructive
	 * juxtaposition use {@link Bigraph#juxtapose(Bigraph, Bigraph)}.
	 * Destructive operations are exposed by {@link BigraphBuilder}.
	 * 
	 * @param graph
	 * @return this bigraph
	 */
	synchronized Bigraph rightJuxtapose(Bigraph graph) {
		this._pg.rightJuxtapose(graph._pg);
		this._lg.rightJuxtapose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}
	
	/**
	 * Juxtapose the argument on the left of this bigraph (which is modified
	 * accordingly while the argument is left untouched). For non desctructive
	 * juxtaposition use {@link Bigraph#juxtapose(Bigraph, Bigraph)}.
	 * Destructive operations are exposed by {@link BigraphBuilder}.
	 * 
	 * @param graph
	 * @return this bigraph
	 */
	synchronized Bigraph leftJuxtapose(Bigraph graph) {
		this._pg.leftJuxtapose(graph._pg);
		this._lg.leftJuxtapose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}


	/**
	 * "this before that".
	 * Compose the argument to the inner face of this bigraph (which is modified accordingly while
	 * the argument is left untouched). For non desctructive composition use
	 * {@link Bigraph#compose(Bigraph, Bigraph)}. Destructive operations are
	 * exposed by {@link BigraphBuilder}.
	 * 
	 * @param graph
	 * @return this bigraph
	 */
	synchronized Bigraph innerCompose(Bigraph graph) {
		this._pg.innerCompose(graph._pg);
		this._lg.innerCompose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}
	
	/**
	 * "this after that".
	 * Compose the argument to the inner face of  this bigraph (which is modified accordingly while
	 * the argument is left untouched). For non desctructive composition use
	 * {@link Bigraph#compose(Bigraph, Bigraph)}. Destructive operations are
	 * exposed by {@link BigraphBuilder}.
	 * 
	 * @param graph
	 * @return this bigraph
	 */
	synchronized Bigraph outerCompose(Bigraph graph) {
		this._pg.outerCompose(graph._pg);
		this._lg.outerCompose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}

	
	/**
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return {@literal true} if the bigraphs can be juxtaposed
	 */
	public static boolean areJuxtaposable(Bigraph g1, Bigraph g2) {
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
	public static boolean areComposable(Bigraph g1, Bigraph g2) {
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
	 * Juxtapose thwo bigraphs into a new one. For repeated operations use
	 * {@link BigraphBuilder}.
	 * 
	 * @param g1
	 *            left operand
	 * @param g2
	 *            right operand
	 * @return g1 juxtaposed to g2
	 */
	public static Bigraph juxtapose(Bigraph g1, Bigraph g2) {
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
	 * @return g1 after g2
	 */
	public static Bigraph compose(Bigraph g1, Bigraph g2) {
		return g1.clone().innerCompose(g2);
	}

	/**
	 * Creates a ion as for the given control; names are automatically
	 * generated.
	 * 
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode,
	 *      String...)
	 * @param sig
	 * @param ctrl
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphControl ctrl) {
		return makeIon(sig, new BGNode(ctrl));
	}

	/**
	 * Creates a ion as for the given control and node name; outer names are
	 * automatically generated.
	 * 
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode,
	 *      String...)
	 * @param sig
	 * @param ctrl
	 * @param name
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphControl ctrl, String name) {
		return makeIon(sig, new BGNode(ctrl, name));
	}

	/**
	 * Creates a ion as for the given control, node name and list of outer
	 * names.
	 * 
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode,
	 *      String...)
	 * @param sig
	 * @param ctrl
	 * @param name
	 * @param names
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphControl ctrl, String name, String... names) {
		return makeIon(sig, new BGNode(ctrl, name), names);
	}

	/**
	 * Creates a ion as {@link
	 * jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode,
	 * String...)} except that names are automatically generated.
	 * 
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode,
	 *      String...)
	 * @param sig
	 * @param node
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphNode node) {
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(sig), node);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(sig), node);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates the ion composed by the given node. Ports are linked to the outer
	 * names following the order in which they are provided.
	 * 
	 * @see jlibbig.PlaceGraph#makeIon(Signature<PlaceGraphControl>,
	 *      PlaceGraphNode)
	 * @see jlibbig.LinkGraph#makeIon(Signature<LinkGraphControl>,
	 *      LinkGraphNode, String...)
	 * @param sig
	 * @param node
	 * @param names
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphNode node, String... names) {
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(sig), node);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(sig), node, names);
		return new Bigraph(sig, pg, lg);
	}

	/*
	 * public static Bigraph makeId(Signature<BigraphControl> sig, int width) {
	 * return makeId(sig, width, new HashSet<String>()); }
	 * 
	 * public static Bigraph makeId(Signature<BigraphControl> sig, Set<String>
	 * names) { return makeId(sig, 0, names); }
	 */

	/**
	 * Creates an identity bigraph for the given signature, width and set of
	 * names
	 * 
	 * @see jlibbig.PlaceGraph#makeId(Signature<PlaceGraphControl>, int)
	 * @see jlibbig.LinkGraph#makeId(Signature<LinkGraphControl>, Set<String>)
	 * @param sig
	 * @param width
	 * @param names
	 * @return an identity bigraph
	 */
	public static Bigraph makeId(Signature<BigraphControl> sig, int width,
			Set<String> names) {
		PlaceGraph pg = PlaceGraph.makeId(asPlaceSignature(sig), width);
		LinkGraph lg = LinkGraph.makeId(asLinkSignature(sig), names);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates an identity bigraph for the given signature and interface
	 * 
	 * @see jlibbig.PlaceGraph#makeId(Signature<PlaceGraphControl>,
	 *      PlaceGraphFace)
	 * @see jlibbig.LinkGraph#makeId(Signature<LinkGraphControl>, LinkGraphFace)
	 * @param sig
	 * @param face
	 * @return an identity bigraph
	 */
	public static Bigraph makeId(Signature<BigraphControl> sig, BigraphFace face) {
		PlaceGraph pg = PlaceGraph.makeId(asPlaceSignature(sig), face);
		LinkGraph lg = LinkGraph.makeId(asLinkSignature(sig), face);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates an empty bigraph over the given signature
	 * 
	 * @see jlibbig.PlaceGraph#makeEmpty(Signature<PlaceGraphControl>)
	 * @see jlibbig.LinkGraph#makeEmpty(Signature<LinkGraphControl>)
	 * @param sig
	 * @return an empty bigraph
	 */
	public static Bigraph makeEmpty(Signature<BigraphControl> sig) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(sig));
		return new Bigraph(sig, pg, lg);
	}
	
	/**
	 * Created a bigraph composed by a merge place graph and an identity link
	 * graph with the given interface.
	 * 
	 * @see jlibbig.PlaceGraph#makeMerge
	 * @param sig
	 * @param face
	 *            the inner interface
	 * @return
	 */
	public static Bigraph makeMerge(Signature<BigraphControl> sig,
			BigraphFace face) {
		return new Bigraph(sig, PlaceGraph.makeMerge(asPlaceSignature(sig), face.getWidth()),
				LinkGraph.makeId(asLinkSignature(sig), (LinkGraphFace) face));
	}

	/**
	 * Creates a merge bigraph with inner face of the width specified.
	 * 
	 * @see jlibbig.PlaceGraph#makeMerge
	 * @param sig
	 * @param width
	 *            the width of the inner face
	 * @return
	 */
	public static Bigraph makeMerge(Signature<BigraphControl> sig, int width) {
		PlaceGraph pg = PlaceGraph.makeMerge(asPlaceSignature(sig), width);
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(sig));
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates a bigraph composed by a swap place graph and an empty link graph.
	 * 
	 * @see jlibbig.PlaceGraph#makeSwap
	 * @param sig
	 * @return
	 */
	public static Bigraph makeSwap(Signature<BigraphControl> sig) {
		PlaceGraph pg = PlaceGraph.makeSwap(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(sig));
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates a bigraph whose link graph is a substitution from the inner names
	 * to the outer ones.
	 * 
	 * @see jlibbig.LinkGraph#makeSubstitution
	 * @param sig
	 * @param subst
	 *            the map describing the substitution
	 * @return
	 */
	public static Bigraph makeSubstitution(Signature<BigraphControl> sig,
			Map<InnerName, OuterName> subst) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeSubstitution(asLinkSignature(sig), subst);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates a bigraph whose link graph has no nodes and no link (every name
	 * is dangling)
	 * 
	 * @see jlibbig.LinkGraph#makeTaps
	 * @param sig
	 * @param inner
	 *            the inner interface of the link graph
	 * @param outer
	 *            the puter interface of the link graph
	 * @return
	 */
	public static Bigraph makeTaps(Signature<BigraphControl> sig,
			LinkGraphFace inner, LinkGraphFace outer) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeTaps(asLinkSignature(sig), inner, outer);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Creates a bigraph whose link graph has no nodes and no link (every name
	 * is dangling)
	 * 
	 * @see jlibbig.LinkGraph#makeTaps
	 * @param sig
	 * @param innerNames
	 * @param outerNames
	 * @return
	 */
	public static Bigraph makeTaps(Signature<BigraphControl> sig,
			Set<String> innerNames, Set<String> outerNames) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeTaps(asLinkSignature(sig), innerNames,
				outerNames);
		return new Bigraph(sig, pg, lg);
	}

	/**
	 * Internal implementation of {@link BigraphNode}
	 */
	protected static class BGNode extends Named implements BigraphNode {
		// more or less a bigraphical node is very likelly a link graph node
		private final LinkGraph.LGNode _lgn;
		private final BigraphControl _ctrl;

		/**
		 * Creates a node with a generated name. The name is in the reserved
		 * form <code>"N_%d"</code> ({@link Named#generateName()}).
		 * 
		 * @param ctrl
		 *            the control to be used
		 */
		protected BGNode(BigraphControl ctrl) {
			super("N_" + generateName());
			_ctrl = ctrl;
			_lgn = new LinkGraph.LGNode(ctrl);
		}

		/**
		 * Creates a node with name and control specified. The name pattern
		 * <code>"N_%d"</code> is reserved ({@link Named#generateName()}).
		 * 
		 * @param ctrl
		 *            the control to be used
		 * @param name
		 *            the name to be used
		 */
		protected BGNode(BigraphControl ctrl, String name) {
			super(name);
			_ctrl = ctrl;
			_lgn = new LinkGraph.LGNode(ctrl, name);
		}

		/**
		 * A bigraphical node is decorated with an immutable control
		 * 
		 * @see jlibbig.BigraphNode#getControl()
		 */
		@Override
		public BigraphControl getControl() {
			return this._ctrl;
		}

		/**
		 * A bigraphical node presents an immutable list of ports consistently
		 * with the arity specified by its control
		 * 
		 * @see jlibbig.LinkGraphNode#getPorts()
		 */
		@Override
		public List<Port> getPorts() {
			return _lgn.getPorts();
		}

		/**
		 * @see jlibbig.LinkGraphNode#getPort(int)
		 */
		@Override
		public Port getPort(int index) {
			return _lgn.getPort(index);
		}

	}

	/**
	 * Internal implementation of {@link BigraphFace}. Wraps a
	 * {@link PlaceGraphFace} and a {@link LinkGraphFace}.
	 */
	protected static class BGFace implements BigraphFace {

		private PlaceGraphFace p;
		private LinkGraphFace l;

		protected BGFace(PlaceGraphFace p, LinkGraphFace l) {
			if (p == null || l == null)
				throw new IllegalArgumentException("Arguments can not be null");
			this.p = p;
			this.l = l;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BGFace other = (BGFace) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (p == null) {
				if (other.p != null)
					return false;
			} else if (!p.equals(other.p))
				return false;
			return true;
		}

		@Override
		public String toString() {
			String s = l.getNames().toString();
			return "<" + p.getWidth() + ", {" + s.substring(1, s.length() - 1)
					+ "}>";
		}

		/**
		 * @see jlibbig.PlaceGraphFace#getWidth()
		 */
		@Override
		public int getWidth() {
			return p.getWidth();
		}

		/**
		 * @see jlibbig.LinkGraphFace#getNames()
		 */
		@Override
		public Set<LinkGraphFacet> getNames() {
			return l.getNames();
		}

		/**
		 * @see jlibbig.GraphFace#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return p.isEmpty() && l.isEmpty();
		}
	}

}
