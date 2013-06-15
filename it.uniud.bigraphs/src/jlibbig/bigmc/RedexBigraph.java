package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

public class RedexBigraph implements AbstBigraph{
	private Set<String> outerNames;
	private Map<Site , Integer> siteNames;
	private Bigraph big;

	public RedexBigraph( Bigraph big, Set<String> outerNames, Map<Site , Integer> siteNames ){
		this.outerNames = outerNames;
		this.siteNames = siteNames;
		this.big = big;
	}


	public Signature getSignature() {
		return big.getSignature();
	}

	public boolean isEmpty() {
		return big.isEmpty();
	}

	public boolean isGround() {
		return big.isGround();
	}

	public List<? extends Root> getRoots() {
		return big.getRoots();
	}

	public List<? extends Site> getSites() {
		return big.getSites();
	}
	
	public Map<Site , Integer> getSitesMap(){
		return Collections.unmodifiableMap( this.siteNames );
	}

	public Set<? extends OuterName> getOuterNames() {
		return big.getOuterNames();
	}

	public Set<String> getOuters(){
		return Collections.unmodifiableSet( this.outerNames );
	}
	
	public Set<? extends InnerName> getInnerNames() {
		return big.getInnerNames();
	}

	public Set<? extends Node> getNodes() {
		return big.getNodes();
	}

	public Set<? extends Edge> getEdges() {
		return big.getEdges();
	}
}
