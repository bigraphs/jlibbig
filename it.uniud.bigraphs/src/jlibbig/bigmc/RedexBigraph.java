package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

/**
 * Class used to store a bigMC's redex or reactum bigraph
 * @see AbstBigraph
 */
public class RedexBigraph implements AbstBigraph{
	private Set<String> outerNames;
	private Map<Site , Integer> siteNames;
	private Bigraph big;

	/**
	 * @param big the Bigraph representing the redex (or reactum)
	 * @param outerNames set of outernames that represent non-free names in the redex/reactum
	 * @param siteNames map used to retrieve the right number of the sites (note: in bigmc, two or more sites can have the same name)
	 * @see Bigraph
	 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
	 */
	public RedexBigraph( Bigraph big, Set<String> outerNames, Map<Site , Integer> siteNames ){
		this.outerNames = outerNames;
		this.siteNames = siteNames;
		this.big = big;
	}

	/**
	 * Get the signature of the bigraph
	 * @return the signature of the bigraph
	 * @see Signature
	 * @see AbstBigraphHandler#getSignature()
	 */
	public Signature getSignature() {
		return big.getSignature();
	}

	@Override
	public boolean isEmpty() {
		return big.isEmpty();
	}

	@Override
	public boolean isGround() {
		return big.isGround();
	}

	@Override
	public List<? extends Root> getRoots() {
		return big.getRoots();
	}

	@Override
	public List<? extends Site> getSites() {
		return big.getSites();
	}
	
	/**
	 * Get the map ( Site , Integer ) storing, for each Site, the right number.
	 */
	public Map<Site , Integer> getSitesMap(){
		return Collections.unmodifiableMap( this.siteNames );
	}

	@Override
	public Set<? extends OuterName> getOuterNames() {
		return big.getOuterNames();
	}

	/**
	 * Get the set of non-free outernames in the redex/reactum.
	 */
	public Set<String> getOuters(){
		return Collections.unmodifiableSet( this.outerNames );
	}
	
	@Override
	public Set<? extends InnerName> getInnerNames() {
		return big.getInnerNames();
	}

	@Override
	public Set<? extends Node> getNodes() {
		return big.getNodes();
	}

	@Override
	public Set<? extends Edge> getEdges() {
		return big.getEdges();
	}
}
