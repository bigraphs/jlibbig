package jlibbig;

import java.util.*;

public class PlaceGraph{

	private final Signature<PlaceGraphControl> _sig;

	private Set<PlaceGraphNode> _nodes = new HashSet<>();
	private List<Root> _roots = new ArrayList<>();
	private List<Site> _sites = new ArrayList<>();
	@SuppressWarnings("unchecked")
	private final PlaceGraphFace _outer = new PGFace(
			(List<PlaceGraphFacet>) (List<? extends PlaceGraphFacet>) _roots);
	@SuppressWarnings("unchecked")
	private final PlaceGraphFace _inner = new PGFace(
			(List<PlaceGraphFacet>) (List<? extends PlaceGraphFacet>) _sites);

	// there is a lot of redundancy, but helps the generation of CSPs for the
	// matching algorithm
	// parent map
	private Map<Child, Parent> _prnt = new HashMap<>();
	// children (prnt-1)
	private Map<Parent, Set<Child>> _chld = new HashMap<>();
	// ancestors (prnt+)
	private Map<Child, Set<Parent>> _ncst = null; // new HashMap<>();
	// private Set<Parent> _ncst_filter = new HashSet<>(); // used to filter out
	// inconsistencies introduced by composition
	// descendants (prnt-1+)
	private Map<Parent, Set<Child>> _dscn = null; // new HashMap<>();

	// private Set<Child> _dscn_filter = new HashSet<>(); // used to filter out
	// inconsistencies introduced by composition

	protected PlaceGraph(Signature<PlaceGraphControl> signature) {
		this._sig = signature;
	}

	private void buildPrntClosure() {
		if (_dscn != null)
			return;
		_ncst = new HashMap<>();
		_dscn = new HashMap<>();
		// DFS visit _chld from roots
		for (Root r : _roots) {
			Set<Child> dr = new HashSet<>();
			buildPrntClosureSub(r, dr);
			_dscn.put(r, dr);
		}
	}

	private void buildPrntClosureSub(Parent p, Set<Child> dp) {
		Set<Child> cp = _chld.get(p);
		Set<Parent> an = new HashSet<>();
		an.add(p);
		if (p instanceof Child) {
			an.addAll(_ncst.get((Child) p));
		}
		for (Child c : cp) {
			dp.add(c);
			_ncst.put(c, an);
			if (c instanceof Parent) {
				Set<Child> dc = new HashSet<>();
				buildPrntClosureSub((Parent) c, dc);
				_dscn.put((Parent) c, dc);
				dp.addAll(dc);
			}
		}
	}

	private Root addRoot(Root r, int index) {
		if (this._roots.contains(r))
			throw new IllegalArgumentException("Root already present");
		this._roots.add(index, r);
		this._chld.put(r, new HashSet<Child>());
		if (this._dscn != null) {
			this._dscn.put(r, new HashSet<Child>());
		}
		return r;
	}

	private Site addSite(Site s, int index, Parent p) {
		if (_sites.contains(s))
			throw new IllegalArgumentException("Site already present");
		if (!_chld.containsKey(p)) {
			throw new IllegalArgumentException("Unknown parent");
		}
		_sites.add(index, s);
		_prnt.put(s, p);
		_chld.get(p).add(s);
		if (this._ncst != null) {
			Set<Parent> an = new HashSet<>();
			an.addAll(this._ncst.get(p));
			an.add(p);
			this._ncst.put(s, an);
			for (Parent a : an) {
				_dscn.get(a).add(s);
			}
		}
		return s;
	}

	private PlaceGraphNode addNode(PlaceGraphNode n, Parent p) {
		if (!this._sig.contains(n.getControl())) {
			throw new IllegalArgumentException(
					"Control not present in the signature");
		}
		if (this._nodes.contains(n))
			throw new IllegalArgumentException("Node already present");
		if (!this._chld.containsKey(p)) {
			throw new IllegalArgumentException("Unknown parent");
		}
		this._nodes.add(n);
		this._prnt.put(n, p);
		_chld.get(p).add(n);
		this._chld.put(n, new HashSet<Child>());
		if (this._dscn != null) {
			this._dscn.put(n, new HashSet<Child>());
			Set<Parent> an = new HashSet<>();
			an.addAll(this._ncst.get(p));
			an.add(p);
			this._ncst.put(n, an);
			for (Parent a : an) {
				_dscn.get(a).add(n);
			}
		}
		return n;
	}

	@Override
	protected PlaceGraph clone(){
		PlaceGraph pg = new PlaceGraph(_sig);
		pg._roots.addAll(this._roots);
		pg._sites.addAll(this._sites);
		pg._nodes.addAll(this._nodes);
		pg._prnt.putAll(this._prnt);
		for (Parent p : this._chld.keySet()) {
			pg._chld.put(p, new HashSet<>(this._chld.get(p)));
		}
		if (this._dscn != null) {
			for (Parent p : this._dscn.keySet()) {
				pg._dscn.put(p, new HashSet<>(this._dscn.get(p)));
			}
			for (Child c : this._ncst.keySet()) {
				pg._ncst.put(c, new HashSet<>(this._ncst.get(c)));
			}
		}
		return pg;
	}

	
	public Signature<PlaceGraphControl> getSignature() {
		return _sig;
	}

	
	public PlaceGraphFace getOuterFace() {
		return _outer;
	}

	
	public PlaceGraphFace getInnerFace() {
		return _inner;
	}

	
	public Set<PlaceGraphNode> getNodes() {
		return Collections.unmodifiableSet(this._nodes);
	}

	
	public List<Root> getRoots() {
		return Collections.unmodifiableList(this._roots);
	}

	
	public List<Site> getSites() {
		return Collections.unmodifiableList(this._sites);
	}

	
	public Parent getParentOf(Child c) {
		return this._prnt.get(c);
	}

	
	public Set<Child> getChildrenOf(Parent p) {
		return this._chld.get(p);
	}

	public Set<Parent> getAncestorsOf(Child c) {
		if (_ncst == null)
			this.buildPrntClosure();
		return Collections.unmodifiableSet(this._ncst.get(c));
	}

	public Set<Child> getDescendantsOf(Parent p) {
		if (_dscn == null)
			this.buildPrntClosure();
		return Collections.unmodifiableSet(this._dscn.get(p));
	}

	
	public boolean isEmpty() {
		return _nodes.isEmpty() && _roots.isEmpty() && _sites.isEmpty();
	}
	
	public boolean isAgent() {
		return _inner.isEmpty();
	}

	private synchronized void juxtaposeEx(PlaceGraph g,boolean onleft) throws IncompatibleSignatureException, NameClashException {
		// does not perform a deep copy of plc
		if (this._sig != g._sig) {
			throw new IncompatibleSignatureException();
		}
		if (!Collections.disjoint(this._nodes, g._nodes)) {
			throw new NameClashException("Overlapping supports");
		}
	
		// descendants and ancestors are lazy but viral
		// these are present in one of the two graphs the other ones are 
		// computed before any other operations 
		if (this._dscn != null){
			if (g._dscn == null)
				g.buildPrntClosure();
		}else if (g._dscn != null) {
			this.buildPrntClosure();
		}
		
		// some instances of roots and sites may be shared and may introduce aliasing
		// these have to be substituted on the fly
		Map<Parent,Parent> sub_prnts = new HashMap<>();
		Map<Child,Child> sub_chds = new HashMap<>();
		// nodes are safe, but putting them in subs simplify the code below
		for(PlaceGraphNode n : g._nodes){
			sub_prnts.put(n,n);
			sub_chds.put(n,n);
		}
		this._nodes.addAll(g._nodes);
		for(Root r : g._roots){
			Root a = (this._roots.contains(r)) ? new Root() : r;
			sub_prnts.put(r, a);
			if(onleft){
				//perform juxtaposition on the left of this
				this._roots.add(0,a);
			}else{
				//perform juxtaposition on the right of this
				this._roots.add(a);
			}
		}
		for(Site s : g._sites){
			Site a = (this._sites.contains(s)) ? new Site() : s;
			sub_chds.put(s, a);
			if(onleft){
				//perform juxtaposition on the left of this
				this._sites.add(0,a);
			}else{
				//perform juxtaposition on the right of this
				this._sites.add(a);
			}
		}
		// merge parent maps
		for(Child c : g._prnt.keySet()){
			this._prnt.put(sub_chds.get(c),sub_prnts.get(g._prnt.get(c)));	
		}
		for (Parent p : g._chld.keySet()) {
			Set<Child> cs = new HashSet<>(); 
			for(Child c : g._chld.get(p)){
				cs.add(sub_chds.get(c));
			}
			this._chld.put(sub_prnts.get(p), cs);
		}		
		// descendants and ancestors are lazy
		if (this._dscn != null) {
			// both are not null now
			// copy g information applying substitutions of roots and sites
			for (Parent p : g._dscn.keySet()) {
				Set<Child> cs = new HashSet<>(); 
				for(Child c : g._dscn.get(p)){
					cs.add(sub_chds.get(c));
				}
				this._dscn.put(sub_prnts.get(p), cs);
			}
			for (Child c : g._ncst.keySet()) {
				Set<Parent> ps = new HashSet<>(); 
				for(Parent p : g._ncst.get(c)){
					ps.add(sub_prnts.get(p));
				}
				this._ncst.put(sub_chds.get(c), ps);
			}
		}
	}
	
	synchronized PlaceGraph rightJuxtapose(PlaceGraph g) throws IncompatibleSignatureException, NameClashException {
		juxtaposeEx(g,false);
		return this;
	}
	
	synchronized PlaceGraph leftJuxtapose(PlaceGraph g) throws IncompatibleSignatureException, NameClashException {
		juxtaposeEx(g,true);
		return this;
	}
	
	synchronized PlaceGraph outerCompose(PlaceGraph g) throws IncompatibleSignatureException, NameClashException, IncompatibleInterfaces {
		// does not perform a deep copy of plc
		if (this._sig != g._sig) {
			throw new IncompatibleSignatureException();
		}
		if (!this.getOuterFace().equals(g.getInnerFace())){
			// interfaces does not match
			throw new IncompatibleInterfaces(this.getOuterFace(),g.getInnerFace());
		}
		if (!Collections.disjoint(this._nodes, g._nodes)) {
			throw new NameClashException("Overlapping supports");
		}
		
		// descendants and ancestors are lazy but viral, update these structures
		// before making any change to this graph
		/*
		 * if(this._dscn != null && pg._dscn == null){ pg.buildPrntClosure();
		 * }else if(pg._dscn != null && this._dscn == null){
		 * this.buildPrntClosure(); }
		 */
		// However lazily rebuild them from scratch is much simpler
		this._dscn = null;
		this._ncst = null;

		// add plc's nodes
		this._nodes.addAll(g._nodes);
		// bulk copy the parent of pg, this introduces some inconsistencies, but
		// these will be addressed below
		this._prnt.putAll(g._prnt);
		for (Parent p : g._chld.keySet()) {
			this._chld.put(p, new HashSet<Child>(g._chld.get(p)));
		}
		// iterate over roots and sites to be composed and glue parent maps
		// accordingly
		Iterator<Root> ir = this._roots.iterator();
		Iterator<Site> is = g._sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			Root r = ir.next();
			Site s = is.next();
			Parent ps = this._prnt.get(s); // parent of s			
			Set<Child> cps = this._chld.get(ps); // siblings of s
			Set<Child> cr = this._chld.get(r); // children of r
			// discard s
			cps.remove(s);
			this._prnt.remove(s);
			//discard r
			this._chld.remove(r);
			// put the subtree rooted in r under the parent of s
			cps.addAll(cr); // update the children of the parent of s
			for (Child c : cr) {
				this._prnt.put(c, ps); // point each child of r to ps
			}
		}
		// update interfaces
		// maintain current inner face
		// use plc's outer interface
		this._roots.clear();
		this._roots.addAll(g._roots);
		return this;
	}

	synchronized PlaceGraph innerCompose(PlaceGraph g) throws IncompatibleSignatureException, NameClashException, IncompatibleInterfaces {
		// does not perform a deep copy of plc
		if (this._sig != g._sig) {
			throw new IncompatibleSignatureException();
		}
		if (!this.getInnerFace().equals(g.getOuterFace())){
			// interfaces does not match
			throw new IncompatibleInterfaces(this.getInnerFace(),g.getOuterFace());
		}
		if (!Collections.disjoint(this._nodes, g._nodes)) {
			throw new NameClashException("Overlapping supports");
		}
		
		// descendants and ancestors are lazy but viral, update these structures
		// before making any change to this graph
		/*
		 * if(this._dscn != null && pg._dscn == null){ pg.buildPrntClosure();
		 * }else if(pg._dscn != null && this._dscn == null){
		 * this.buildPrntClosure(); }
		 */
		// However lazily rebuild them from scratch is much simpler
		this._dscn = null;
		this._ncst = null;

		// add plc's nodes
		this._nodes.addAll(g._nodes);
		// bulk copy the parent of pg, this introduces some inconsistencies, but
		// these will be addressed below
		this._prnt.putAll(g._prnt);
		for (PlaceGraphNode n : g._nodes) {
			this._chld.put(n, new HashSet<Child>(g._chld.get(n)));
		}
		// iterate over roots and sites to be composed and glue parent maps
		// accordingly
		Iterator<Root> ir = g._roots.iterator();
		Iterator<Site> is = this._sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			Root r = ir.next();
			Site s = is.next();
			Parent ps = this._prnt.get(s); // parent of s
			Set<Child> cps = this._chld.get(ps); // siblings of s
			Set<Child> cr = g._chld.get(r); // children of r
			// discard s
			cps.remove(s);
			this._prnt.remove(s);
			// put the subtree rooted in r under the parent of s
			cps.addAll(cr); // update the children of the parent of s
			for (Child c : cr) {
				this._prnt.put(c, ps); // point each child of r to ps
			}
		}
		// update interfaces
		// maintain current outer face
		// use plc's inner interface
		this._sites.clear();
		this._sites.addAll(g._sites);
		return this;
	}

	public static PlaceGraph makeIon(Signature<PlaceGraphControl> sig,
			PlaceGraphNode n) {
		if (!sig.contains(n.getControl()))
			throw new IllegalArgumentException(
					"Control must be an element of the givent signature");
		PlaceGraph p = new PlaceGraph(sig);
		Root r = new Root();
		p.addRoot(r, 0);
		p.addNode(n, r);
		p.addSite(new Site(), 0, n);
		return p;
	}

	public static PlaceGraph makeIon(Signature<PlaceGraphControl> sig,
			PlaceGraphControl control) {
		return makeIon(sig, new PGNode(control));
	}

	public static PlaceGraph makeIon(Signature<PlaceGraphControl> sig,
			PlaceGraphControl control, String name) {
		return makeIon(sig, new PGNode(control, name));
	}

	public static PlaceGraph makeId(Signature<PlaceGraphControl> sig,
			PlaceGraphFace f) {
		return makeId(sig, f.getWidth());
	}

	public static PlaceGraph makeId(Signature<PlaceGraphControl> sig, int width) {
		PlaceGraph p = new PlaceGraph(sig);
		for (int i = 0; i < width; i++) {
			Root r = new Root();
			Site s = new Site();
			p.addRoot(r, i);
			p.addSite(s, i, r);
		}
		return p;
	}

	public static PlaceGraph makeEmpty(Signature<PlaceGraphControl> s) {
		return new PlaceGraph(s);
	}

	public static PlaceGraph makeMerge(Signature<PlaceGraphControl> sig,
			PlaceGraphFace inner) {
		return makeMerge(sig, inner.getWidth());
	}

	public static PlaceGraph makeMerge(Signature<PlaceGraphControl> sig,
			int inner) {
		PlaceGraph p = new PlaceGraph(sig);
		Root r = new Root();
		p.addRoot(r, 0);
		for (int i = 0; i < inner; i++) {
			p.addSite(new Site(), i, r);
		}
		return p;
	}

	public static PlaceGraph makeSwap(Signature<PlaceGraphControl> sig) {
		PlaceGraph p = new PlaceGraph(sig);
		Root r1 = new Root();
		Root r2 = new Root();
		p.addRoot(r1, 0);
		p.addRoot(r2, 1);
		p.addSite(new Site(), 0, r2);
		p.addSite(new Site(), 1, r1);
		return p;
	}
	

	public static interface Parent{}	
	public static interface Child{}

	protected static class PGNode extends Named implements PlaceGraphNode {
		private final PlaceGraphControl _ctrl;

		protected PGNode(PlaceGraphControl ctrl) {
			super("N_" + generateName());
			this._ctrl = ctrl;
		}

		protected PGNode(PlaceGraphControl ctrl, String name) {
			super(name);
			this._ctrl = ctrl;
		}

		@Override
		public PlaceGraphControl getControl() {
			return this._ctrl;
		}
	}

	private static class PGFace implements PlaceGraphFace {

		private final List<PlaceGraphFacet> _facets;

		public PGFace(List<PlaceGraphFacet> facets) {
			if (facets == null)
				throw new IllegalArgumentException("Argument can not be null");
			this._facets = facets;
		}

		@Override
		public int getWidth() {
			return _facets.size();
		}

		@Override
		public boolean isEmpty() {
			return _facets.isEmpty();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((Integer) _facets.size()).hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			PlaceGraphFace other;
			try {
				other = (PlaceGraphFace) obj;
			} catch (ClassCastException e) {
				return false;
			}
			return this.getWidth() == other.getWidth();
		}

		@Override
		public String toString() {
			return "<" + this.getWidth() + ">";
		}

	}
}
