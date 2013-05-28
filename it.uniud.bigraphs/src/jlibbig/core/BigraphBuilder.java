package jlibbig.core;

import java.util.*;

/**
 * The class is meant as a helper for bigraph construction
 * and manipulation in presence of series of operations since {#link Bigraph} 
 * is immutable: e.g. {@link Bigraph#compose(Bigraph, Bigraph)} or 
 * {@link Bigraph#juxtapose(Bigraph, Bigraph)} instantiate a new object. 
 */
public class BigraphBuilder implements AbstBigraph{
	private final Bigraph big;
	private final Signature sig;
	
	public BigraphBuilder(Signature sig){
		this.sig = sig;
		this.big = Bigraph.makeEmpty(this.sig);
	}
	
	public BigraphBuilder(Bigraph big){ 
		this(big,true);
	}
	
	BigraphBuilder(Bigraph big, boolean clone){
		if(!big.isConsistent())
			throw new IllegalArgumentException("Inconsistent bigraph.");
		this.big = (clone) ? big.clone() : big; 
		this.sig = big.getSignature();
		
	}
		
	/** Creates a new bigraph from its inner one.
	 * @return a bigraph.
	 */
	public Bigraph makeBigraph(){
		Bigraph b = big.clone();
		if(!b.isConsistent())
			throw new RuntimeException("Inconsistent bigraph.");
		return b;
	} 

	//TODO common read-only interface with bigraph
		
	public Signature getSignature(){
		return this.sig;
	}
	
	public List<? extends Root> getRoots(){
		return this.big.getRoots();
	}
	
	public List<? extends Site> getSites(){
		return this.big.getSites();
	}
	
	public Set<? extends OuterName> getOuterNames(){
		return this.big.getOuterNames();
	}
	
	public Set<? extends InnerName> getInnerNames(){
		return this.big.getInnerNames();
	}
	
	public Set<? extends Node> getNodes(){
		return this.big.getNodes();
	}
	
	public Set<? extends Edge> getEdges(){
		return this.big.getEdges();
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	public EditableRoot addRoot(){
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		this.big.roots.add(r);
		return r;
	}
	
	public EditableSite addSite(Parent parent){
		EditableSite s = new EditableSite((EditableParent) parent);
		this.big.sites.add(s);
		return s;
	}
	
	public EditableNode addNode(String controlName, Parent parent){
		return addNode(controlName, parent, new LinkedList<Handle>());
	}
	
	public EditableNode addNode(String controlName, Parent parent,Handle... handles){
		return addNode(controlName,parent, Arrays.asList(handles));
	}
	
	public EditableNode addNode(String controlName, Parent parent,List<Handle> handles){
		Control c = this.sig.getByName(controlName);
		if(c == null)
			throw new IllegalArgumentException("Control should be in the signature.");
		if(!this.getRoots().contains(parent) && !this.getNodes().contains(parent))
			throw new IllegalArgumentException("Parent sould be in the bigraph.");
		Set<? extends Edge> es = this.getEdges();
		for(Handle h : handles){
			if(!this.getOuterNames().contains(h) && 
				!(h instanceof Edge && (es.contains(h) || (h.getPoints().size() == 0))))
				throw new IllegalArgumentException("Handles sould be in the bigraph or be idle edges.");
		}
		for(int i = handles.size(); i < c.getArity(); i++){
			handles.add(new EditableEdge()); //add spare edges
		}
		return new EditableNode(c,(EditableParent) parent,handles);
	}
	
	public EditableOuterName addOuterName(String name){
		EditableOuterName n = new EditableOuterName(name);
		n.setOwner(this);
		this.big.outers.add(n);
		return n;
	}
	
	public EditableInnerName addInnerName(String name){
		EditableEdge e = new EditableEdge();
		e.setOwner(this);
		return addInnerName(name, e);
	}
	
	public EditableInnerName addInnerName(String name, Handle handle){
		EditableHandle h = (EditableHandle) handle;
		Set<? extends Edge> es = this.getEdges();
		if(!this.getOuterNames().contains(h) && 
				!(h instanceof Edge && (es.contains(h) || (h.getPoints().size() == 0))))
				throw new IllegalArgumentException("Handles sould be in the bigraph or be idle edges.");
		EditableInnerName n = new EditableInnerName(name);
		n.setHandle(h);
		this.big.inners.add(n);
		return n;
	}
	
	public void relink(Point point, Handle handle){
		EditablePoint p = (EditablePoint) point;
		EditableHandle h = (EditableHandle) handle;
		Owner o1 = p.getOwner();
		Owner o2 = h.getOwner();
		if(o1 == null || o1 != this || o2 == null || o2 != this){
			throw new IllegalArgumentException("Point and handle sould be in the bigraph.");
		}
		p.setHandle(h);
	}
	
	public EditableEdge relink(Point p1, Point p2){
		EditablePoint t1 = (EditablePoint) p1;
		EditablePoint t2 = (EditablePoint) p2;
		Owner o1 = t1.getOwner();
		Owner o2 = t2.getOwner();
		if(o1 == null || o1 != this || o2 == null || o2 != this){
			throw new IllegalArgumentException("Points sould be in the bigraph.");
		}
		EditableEdge e = new EditableEdge();
		e.setOwner(this);
		t1.setHandle(e);
		t2.setHandle(e);
		return e;
	}
	
	/**
	 * Merge regions
	 */
	public void merge(){
		EditableRoot r = new EditableRoot();
		r.setOwner(this);
		for(EditableParent p : big.roots){
			for(EditableChild c : new HashSet<>(p.getEditableChildren())){
				c.setParent(r);
			}
		}
		big.roots.clear();
		big.roots.add(r);
	}
	
	
	public void leftJuxtapose(Bigraph graph){
		leftJuxtapose(graph,false);
	}
	
	public void leftJuxtapose(Bigraph graph, boolean reuse){
		Bigraph left = graph;
		Bigraph right = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(left.signature != right.signature){
			throw new IncompatibleSignatureException(left.signature,right.signature);
		}
		if(!Collections.disjoint(left.inners,right.inners) || 
				!Collections.disjoint(left.outers,right.outers)){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = (reuse) ? left : left.clone();
		Bigraph r = right;
		for(EditableOwned o : l.roots){
			o.setOwner(this);
		}
		for(EditableOwned o : l.outers){
			o.setOwner(this);
		}
		for(Edge e : l.getEdges()){
			((EditableEdge) e).setOwner(this);
		}
		r.roots.addAll(l.roots);
		r.sites.addAll(l.sites);
		r.outers.addAll(l.outers);
		r.inners.addAll(l.inners);
	}
	
	public void rightJuxtapose(Bigraph graph){
		rightJuxtapose(graph,false);
	}
	
	public void rightJuxtapose(Bigraph graph, boolean reuse){
		Bigraph left = this.big;
		Bigraph right = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(left.signature != right.signature){
			throw new IncompatibleSignatureException(left.signature,right.signature);
		}
		if(!Collections.disjoint(left.inners,right.inners) || 
				!Collections.disjoint(left.outers,right.outers)){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = left;
		Bigraph r = (reuse) ? right : right.clone();
		for(EditableOwned o : r.roots){
			o.setOwner(this);
		}
		for(EditableOwned o : r.outers){
			o.setOwner(this);
		}
		for(Edge e : r.getEdges()){
			((EditableEdge) e).setOwner(this);
		}
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(r.outers);
		l.inners.addAll(r.inners);
	}
	
	public void innerCompose(Bigraph graph){
		innerCompose(graph,false);
	}
	
	public void innerCompose(Bigraph graph, boolean reuse){
		Bigraph in = graph;
		Bigraph out = this.big;
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(out.signature != in.signature){
			throw new IncompatibleSignatureException(out.signature,in.signature);
		}
		if(!out.inners.equals(in.outers) || out.sites.size() != in.roots.size()){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph a = out;
		Bigraph b = (reuse) ? in : in.clone();
		Set<? extends Edge> es = this.getEdges();
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableParent p = is.next().getParent();
			for(EditableChild c : ir.next().getEditableChildren()){
				c.setParent(p);
			}
		}
		// iterate over inner and outer names of a and b respectively and glue them
		for(EditableOuterName o : b.outers){
			for(EditableInnerName i : a.inners){
				if(!i.equals(o))
					continue;
				EditableHandle h = i.getHandle();
				for(EditablePoint p : o.getEditablePoints()){
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
		for(Edge e : es){
			((EditableEdge) e).setOwner(this);
		}
	}
	
	public void outerCompose(Bigraph graph){
		outerCompose(graph,false);
	}
	
	public void outerCompose(Bigraph graph, boolean reuse){
		Bigraph in = this.big;
		Bigraph out = graph;
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(out.signature != in.signature){
			throw new IncompatibleSignatureException(out.signature,in.signature);
		}
		if(!out.inners.equals(in.outers) || out.sites.size() != in.roots.size()){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph a = (reuse) ? out : out.clone();
		Bigraph b = in;
		Set<? extends Edge> es = this.getEdges();
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableParent p = is.next().getParent();
			for(EditableChild c : ir.next().getEditableChildren()){
				c.setParent(p);
			}
		}
		// iterate over inner and outer names of a and b respectively and glue them
		for(EditableOuterName o : b.outers){
			for(EditableInnerName i : a.inners){
				if(!i.equals(o))
					continue;
				EditableHandle h = i.getHandle();
				for(EditablePoint p : o.getEditablePoints()){
					p.setHandle(h);
				}
				a.inners.remove(i);				
				break;
			}
		}
		// update inner interfaces
		b.outers.clear();
		b.roots.clear();
		b.outers.addAll(a.outers);
		b.roots.addAll(a.roots);
		for(EditableOwned o : b.roots){
			o.setOwner(this);
		}
		for(EditableOwned o : b.outers){
			o.setOwner(this);
		}
		for(Edge e : es){
			((EditableEdge) e).setOwner(this);
		}
	}
	
	// Derived operations
	public void innerNest(Bigraph graph){
		innerNest(graph,false);
	}
	
	public void innerNest(Bigraph graph,boolean reuse){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void outerNest(Bigraph graph){
		outerNest(graph,false);
	}
	
	public void outerNest(Bigraph graph,boolean reuse){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void leftParallelProduct(Bigraph graph){
		leftParallelProduct(graph,false);
	}
	
	public void leftParallelProduct(Bigraph graph,boolean reuse){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void rightParallelProduct(Bigraph graph){
		rightParallelProduct(graph,false);
	}
	
	public void rightParallelProduct(Bigraph graph,boolean reuse){
		// TODO implement
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void leftMergeProduct(Bigraph graph){
		leftMergeProduct(graph,false);
	}
	
	public void leftMergeProduct(Bigraph graph,boolean reuse){
		leftJuxtapose(graph,reuse);
		merge();
	}
	
	public void rightMergeProduct(Bigraph graph){
		rightMergeProduct(graph,false);
	}
	
	public void rightMergeProduct(Bigraph graph,boolean reuse){
		rightJuxtapose(graph,reuse);
		merge();
	}
}
