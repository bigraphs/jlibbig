package jlibbig;
/*
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ComposableGraph<F extends GraphFace,C extends GraphControl, N extends GraphNode> {
	private final Signature<C> signature;
	
	private Set<N> _nodes = new HashSet<>();
	
	private final F _inner;
	private final F _outer;
	
	protected ComposableGraph(Signature<C> signature, F inner, F outer){
		this.signature = signature;
		this._inner = inner;
		this._outer = outer;
	}
	
	protected ComposableGraph(Signature<C> signature, F inner, F outer, Set<N> nodes){
		this(signature,inner,outer);
		this._nodes.addAll(nodes);
	}
	
	public Signature<C> getSignature(){
		return signature;
	}
	
	public Set<N> getNodes(){
		return Collections.unmodifiableSet(this._nodes);
	}
	
	public F getInnerInterface(){
		return _inner;
	}

	public F getOuterInterface(){
		return _outer;
	}
	
	public abstract ComposableGraph<F,C,N> compose(ComposableGraph<F,C,N> graph);
	
	public boolean isComposable(ComposableGraph<F,C,N> graph){
		return graph != null && this.signature.equals(graph.signature) && this._inner.equals(graph._outer) && Collections.disjoint(this._nodes, graph._nodes);
	}
	
	public abstract ComposableGraph<F,C,N> juxtapose(ComposableGraph<F,C,N> graph);
	
	public boolean isJuxtaposable(ComposableGraph<F,C,N> graph){
		return graph != null && this.signature.equals(graph.signature) && this._inner.isJuxtaposable(graph._inner) && 
				this._outer.isJuxtaposable(graph._outer) && Collections.disjoint(this._nodes, graph._nodes);
	}
}
*/