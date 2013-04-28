package jlibbig;

import java.util.*;

/**
 * Represents a bigraph over a fixed signature
 */
public class Bigraph {

	private final Signature<BigraphControl> _sig;

	private final PlaceGraph _pg;
	private final LinkGraph _lg;

	private final BigraphFace _inner;
	private final BigraphFace _outer;

	private final Set<BigraphNode> _nodes = new HashSet<>();

	/**
	 * read only access to place and link graph composing the bigraph
	 */
	public final PlaceGraphView placing;
	public final LinkGraphView linking;

	/** Place graph and link graph must share nodes and signatures.
	 * @param sig the signature
	 * @param pg the place graph
	 * @param lg the link graph
	 */
	private Bigraph(Signature<BigraphControl> sig, PlaceGraph pg, LinkGraph lg) {
		_sig = sig;
		_pg = pg;
		_lg = lg;

		/* TODO simpler bigraph constructor
		 * Signature and nodes checks can be skipped in production (since the
		 * constructor is meant to be internal).
		 * @param sig can be inferred as the meet of the signatures of pg and lg
		 * if these shares the nodes.
		 */
		// signature must share controls
		try{
			// actually both are made of BGControls
			Signature<PlaceGraphControl> pgs = _pg.getSignature();
			Signature<LinkGraphControl> lgs = _lg.getSignature();
			// however, check if every control in sig has a counterpart in pgs and lgs
			for(BigraphControl bc : _sig){
				BigraphControl pc = (BigraphControl) pgs.getByName(bc.getName());
				BigraphControl lc = (BigraphControl) lgs.getByName(bc.getName()); 
				if(pc == null || lc == null || bc.getArity() != lc.getArity()){
					throw new IllegalArgumentException("Incompatible signatures");
				}
			}
			// check if there are controls other than those in sig
			for(GraphControl c : pgs){
				if(_sig.getByName(c.getName()) == null)
					throw new IllegalArgumentException("Incompatible signatures");
			}
			for(GraphControl c : lgs){
				if(_sig.getByName(c.getName()) == null)
					throw new IllegalArgumentException("Incompatible signatures");
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Incompatible signatures");
		}
		// place and link must share their nodes
		try {
			// all nodes found (these go into _nodes)
			Set<BigraphNode> ns1 = new HashSet<>();
			// initially is a replica of n2, but then every node found in _lg
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
		// interfaces
		_inner = new BGFace(_pg.getInnerFace(), _lg.getInnerFace());
		_outer = new BGFace(_pg.getOuterFace(), _lg.getOuterFace());
		// read-only views
		placing = new PlaceGraphView(_pg);
		linking = new LinkGraphView(_lg);
	}

	/**
	 * @return {@value true} if the bigraph has empty interfaces and support.
	 */
	public boolean isEmpty() {
		return _pg.isEmpty() && _lg.isEmpty();
	}

	/**
	 * @return {@value true} if the bigraph has empty inner interface
	 */
	public boolean isAgent() {
		return _inner.isEmpty();
	}
	
	/** Returns a copy of the place graph composing the bigraph.
	 * For read-only access use @see jlibbig.Bigraph#placing.
	 * @return a copy of the place graph composing the bigraph
	 */
	public PlaceGraph getPlaceGraph() {
		try {
			return (PlaceGraph) _pg.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Returns a copy of the link graph composing the bigraph.
	 * For read-only access use @see jlibbig.Bigraph#linking.
	 * @return a copy of the link graph composing the bigraph
	 */
	public LinkGraph getLinkGraph() {
		try {
			return (LinkGraph) _lg.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Returns the signature on which the bigraph is defined.
	 * @return the signature
	 */
	public Signature<BigraphControl> getSignature() {
		return _sig;
	}

	/** Returns the inner interface of the bigraph
	 * @return the inner interface
	 */
	public BigraphFace getInnerFace() {
		return _inner;
	}


	/** Returns the outer interface of the bigraph
	 * @return the outer interface
	 */
	public BigraphFace getOuterFace() {
		return _outer;
	}

	
	/** Return the set of nodes composing the bigraph.
	 * @return the set of nodes
	 */
	public Set<BigraphNode> getNodes() {
		return Collections.unmodifiableSet(this._nodes);
	}

	/** Cast a bigraphical signature to a signature of place graph controls
	 * @param sig a bigraphical signature
	 * @return the same signature
	 */
	@SuppressWarnings("unchecked")
	private static Signature<PlaceGraphControl> asPlaceSignature(
			Signature<BigraphControl> sig) {
		return (Signature<PlaceGraphControl>) (Signature<?>) sig;
	}

	/** Cast a bigraphical signature to a signature of link graph controls
	 * @param sig a bigraphical signature
	 * @return the same signature
	 */
	@SuppressWarnings("unchecked")
	private static Signature<LinkGraphControl> asLinkSignature(
			Signature<BigraphControl> sig) {
		return (Signature<LinkGraphControl>) (Signature<?>) sig;
	}

	
	/** Juxtapose (on the right) the argument to the current bigraph (which is modified accordingly).
	 * @param graph
	 * @return this bigraph
	 */
	public Bigraph juxtapose(Bigraph graph) {
		this._pg.juxtapose(graph._pg);
		this._lg.juxtapose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}
	
	/** Compose the argument to this bigraph.
	 * @param graph
	 * @return this bigraph
	 */
	public Bigraph compose(Bigraph graph) {
		this._pg.compose(graph._pg);
		this._lg.compose(graph._lg);
		this._nodes.addAll(graph._nodes);
		return this;
	}

	/** Creates a ion as for the given control; names are automatically generated.
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode, String...)
	 * @param sig
	 * @param ctrl
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig, BigraphControl ctrl) {
		return makeIon(sig, new BGNode(ctrl));
	}


	/** Creates a ion as for the given control and node name; outer names are automatically generated.
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode, String...)
	 * @param sig
	 * @param ctrl
	 * @param name
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig,
			BigraphControl ctrl, String name) {
		return makeIon(sig, new BGNode(ctrl, name));
	}

	/** Creates a ion as for the given control, node name and list of outer names.
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode, String...)
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

	/** Creates a ion as {@link jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode, String...)}
	 * except that names are automatically generated.
	 * @see jlibbig.Bigraph#makeIon(Signature<PlaceGraphControl>, BigraphNode, String...)
	 * @param sig
	 * @param node
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig, BigraphNode node) {
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(sig), node);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(sig), node);
		return new Bigraph(sig, pg, lg);
	}

	/** Creates the ion composed by the given node. Ports are linked to the outer names following the order in which they are provided.
	 * @see jlibbig.PlaceGraph#makeIon(Signature<PlaceGraphControl>, PlaceGraphNode)
	 * @see jlibbig.LinkGraph#makeIon(Signature<LinkGraphControl>, LinkGraphNode, String...)
	 * @param sig
	 * @param node
	 * @param names 
	 * @return
	 */
	public static Bigraph makeIon(Signature<BigraphControl> sig, BigraphNode node,
			String... names) {
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(sig), node);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(sig), node, names);
		return new Bigraph(sig, pg, lg);
	}

	/*
	public static Bigraph makeId(Signature<BigraphControl> sig, int width) {
		return makeId(sig, width, new HashSet<String>());
	}
	
	public static Bigraph makeId(Signature<BigraphControl> sig, Set<String> names) {
		return makeId(sig, 0, names);
	}
	*/

	/** Creates an identity bigraph for the given signature, width and set of names 
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

	/** Creates an identity bigraph for the given signature and interface 
	 * @see jlibbig.PlaceGraph#makeId(Signature<PlaceGraphControl>, PlaceGraphFace)
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

	/** Creates a empty bigraph over the given signature
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

	/** Created a bigraph composed by a merge place graph and an identity link graph with the given interface.
	 * @see jlibbig.PlaceGraph#makeMerge
	 * @param sig
	 * @param face the inner interface
	 * @return
	 */
	public static Bigraph makeMerge(Signature<BigraphControl> sig, BigraphFace face) {
		return makeMerge(sig, face.getWidth()).juxtapose(makeId(sig,face)) ;
	}

	/** Creates a merge bigraph with inner face of the width specified.
	 * @see jlibbig.PlaceGraph#makeMerge
	 * @param sig
	 * @param width the width of the inner face
	 * @return
	 */
	public static Bigraph makeMerge(Signature<BigraphControl> sig, int width) {
		PlaceGraph pg = PlaceGraph.makeMerge(asPlaceSignature(sig), width);
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(sig));
		return new Bigraph(sig, pg, lg);
	}

	/** Creates a bigraph composed by a swap place graph and an empty link graph.
	 * @see jlibbig.PlaceGraph#makeSwap
	 * @param sig
	 * @return
	 */
	public static Bigraph makeSwap(Signature<BigraphControl> sig) {
		PlaceGraph pg = PlaceGraph.makeSwap(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(sig));
		return new Bigraph(sig, pg, lg);
	}
	
	/** Creates a bigraph whose link graph is a substitution from the inner names to the outer ones. 
	 * @see jlibbig.LinkGraph#makeSubstitution
	 * @param sig
	 * @param subst the map describing the substitution
	 * @return
	 */
	public static Bigraph makeSubstitution(Signature<BigraphControl> sig, Map<InnerName, OuterName> subst) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeSubstitution(asLinkSignature(sig), subst);
		return new Bigraph(sig, pg, lg);
	}

	/** Creates a bigraph whose link graph has no nodes and no link (every name is dangling)
	 * @see jlibbig.LinkGraph#makeTaps
	 * @param sig
	 * @param inner the inner interface of the link graph
	 * @param outer the puter interface of the link graph
	 * @return
	 */
	public static Bigraph makeTaps(Signature<BigraphControl> sig, LinkGraphFace inner, LinkGraphFace outer) {
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(sig));
		LinkGraph lg = LinkGraph.makeTaps(asLinkSignature(sig), inner, outer);
		return new Bigraph(sig, pg, lg);
	}

	/** Creates a bigraph whose link graph has no nodes and no link (every name is dangling)
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

	protected static class BGNode extends Named implements BigraphNode {
		private final LinkGraph.LGNode lgn;

		protected BGNode(BigraphControl ctrl) {
			super();
			lgn = new LinkGraph.LGNode(ctrl);
		}

		protected BGNode(BigraphControl ctrl, String name) {
			super(name);
			lgn = new LinkGraph.LGNode(ctrl, name);
		}

		@Override
		public GraphControl getControl() {
			return lgn.getControl();
		}

		@Override
		public List<Port> getPorts() {
			return lgn.getPorts();
		}

		@Override
		public Port getPort(int index) {
			return lgn.getPort(index);
		}

	}

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

		@Override
		public int getWidth() {
			return p.getWidth();
		}

		@Override
		public Set<LinkGraphFacet> getNames() {
			return l.getNames();
		}

		@Override
		public boolean isEmpty() {
			return p.isEmpty() && l.isEmpty();
		}
	}

}
