package jlibbig;

import java.util.*;

public class Bigraph {
	
	private final Signature<BigraphControl> _sig;
	
	private final PlaceGraph _pg;
	private final LinkGraph _lg;
	
	private final BigraphFace _inner;
	private final BigraphFace _outer;

	private final Set<BigraphNode> _nodes = new HashSet<>();
	
	public final PlaceGraphView placing;
	public final LinkGraphView linking;
	
	private Bigraph(Signature<BigraphControl> sig, PlaceGraph pg, LinkGraph lg) {
		_sig = sig;
		_pg = pg;
		_lg = lg;
		try{
			Set<BigraphNode> ns1 = new HashSet<>();
			Set<BigraphNode> ns2 = new HashSet<>();
			for(PlaceGraphNode pn : _pg.getNodes()){
				BigraphNode bn = (BigraphNode) pn;
				ns1.add(bn);
				ns2.add(bn);
			}
			for(LinkGraphNode ln : _lg.getNodes()){
				BigraphNode bn = (BigraphNode) ln;
				if(!ns1.remove(bn)){
					throw new IllegalArgumentException("Incompatible place and link graph");
				}
			}
			if(ns1.size() > 0){
				throw new IllegalArgumentException("Incompatible place and link graph");
			}
			_nodes.addAll(ns2);
		}catch(ClassCastException e){
			throw new IllegalArgumentException("Incompatible place and link graph");
		}
		_inner = new BGFace(_pg.getInnerFace(),_lg.getInnerFace());
		_outer = new BGFace(_pg.getOuterFace(),_lg.getOuterFace());
		placing = new PlaceGraphView(_pg);
		linking = new LinkGraphView(_lg);
	}
	
	public PlaceGraph getPlaceGraph(){
		try {
			return (PlaceGraph)_pg.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public LinkGraph getLinkGraph(){
		try {
			return (LinkGraph)_lg.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Signature<BigraphControl> getSignature(){
		return _sig;
	}
	
	public BigraphFace getInnerFace(){
		return _inner;
	}
	
	public BigraphFace getOuterFace(){
		return _outer;
	}
	
	public Set<BigraphNode> getNodes(){
		return Collections.unmodifiableSet(this._nodes);
	} 
	
	@SuppressWarnings("unchecked")
	private static Signature<PlaceGraphControl> asPlaceSignature(Signature<BigraphControl> s){
		return (Signature<PlaceGraphControl>) (Signature<?>) s;
	}
	
	@SuppressWarnings("unchecked")
	private static Signature<LinkGraphControl> asLinkSignature(Signature<BigraphControl> s){
		return (Signature<LinkGraphControl>) (Signature<?>) s;
	}
	
	public Bigraph juxtapose(Bigraph g){
		this._pg.juxtapose(g._pg);
		this._lg.juxtapose(g._lg);
		this._nodes.addAll(g._nodes);
		return this;
	}
	
	public Bigraph compose(Bigraph g){
		this._pg.compose(g._pg);
		this._lg.compose(g._lg);
		this._nodes.addAll(g._nodes);
		return this;
	}
	
	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphControl c){
		return makeIon(s,new BGNode(c));
	}

	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphControl c, String name){
		return makeIon(s,new BGNode(c,name));
	}

	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphControl c, String name, String... names){
		return makeIon(s,new BGNode(c,name),names);
	}
	
	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphNode n){
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(s), n);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(s), n);
		return new Bigraph(s,pg,lg);
	}

	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphNode n, String... names){
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(s), n);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(s), n, Arrays.asList(names));
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeIon(Signature<BigraphControl> s,BigraphNode n, List<String> names){
		PlaceGraph pg = PlaceGraph.makeIon(asPlaceSignature(s), n);
		LinkGraph lg = LinkGraph.makeIon(asLinkSignature(s), n,names);
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeId(Signature<BigraphControl> s, int width){
		return makeId(s,width,new HashSet<String>());
	}
	
	public static Bigraph makeId(Signature<BigraphControl> s, Set<String> names){
		return makeId(s,0,names);
	}
	
	public static Bigraph makeId(Signature<BigraphControl> s, int width, Set<String> names){
		PlaceGraph pg = PlaceGraph.makeId(asPlaceSignature(s), width);
		LinkGraph lg = LinkGraph.makeId(asLinkSignature(s), names);
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeId(Signature<BigraphControl> s, BigraphFace f){
		PlaceGraph pg = PlaceGraph.makeId(asPlaceSignature(s), f);
		LinkGraph lg = LinkGraph.makeId(asLinkSignature(s), f);
		return new Bigraph(s,pg,lg);
	}
		
	public static Bigraph makeEmpty(Signature<BigraphControl> s){
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(s));
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(s));
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeMerge(Signature<BigraphControl> s, BigraphFace f){
		return makeMerge(s,f.getWidth());
	}
	
	public static Bigraph makeMerge(Signature<BigraphControl> s, int width){
		PlaceGraph pg = PlaceGraph.makeMerge(asPlaceSignature(s), width);
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(s));
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeSwap(Signature<BigraphControl> s){
		PlaceGraph pg = PlaceGraph.makeSwap(asPlaceSignature(s));
		LinkGraph lg = LinkGraph.makeEmpty(asLinkSignature(s));
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeSubstitution(Signature<BigraphControl> s, Map<InnerName,OuterName> map){
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(s));
		LinkGraph lg = LinkGraph.makeSubstitution(asLinkSignature(s),map);
		return new Bigraph(s,pg,lg);
	}
	public static Bigraph makeTaps(Signature<BigraphControl> s, LinkGraphFace inner, LinkGraphFace outer){
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(s));
		LinkGraph lg = LinkGraph.makeTaps(asLinkSignature(s),inner,outer);
		return new Bigraph(s,pg,lg);
	}
	
	public static Bigraph makeTaps(Signature<BigraphControl> s, Set<String> innerNames, Set<String> outerNames){
		PlaceGraph pg = PlaceGraph.makeEmpty(asPlaceSignature(s));
		LinkGraph lg = LinkGraph.makeTaps(asLinkSignature(s),innerNames,outerNames);
		return new Bigraph(s,pg,lg);
	}
		
	protected static class BGNode extends Named implements BigraphNode{
		private final LinkGraph.LGNode lgn;
		
		protected BGNode(BigraphControl  ctrl){
			super();
			lgn = new LinkGraph.LGNode(ctrl);
		}
		
		protected BGNode(BigraphControl ctrl,String name){
			super(name);
			lgn = new LinkGraph.LGNode(ctrl,name);
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
	
	protected static class BGFace implements BigraphFace{

		private PlaceGraphFace p;
		private LinkGraphFace l;
		
		protected BGFace(PlaceGraphFace p, LinkGraphFace l){
			if(p==null || l == null)
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
			return "<" + p.getWidth() + ", {" + s.substring(1,s.length()-1) + "}>";
		}

		@Override
		public int getWidth() {
			return p.getWidth();
		}

		@Override
		public Set<LinkGraphFacet> getNames() {
			return l.getNames();
		}
		
	}

}
