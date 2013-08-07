package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

/**
 * Class used to store a bigMC's redex or reactum bigraph
 * @see AbstBigraph
 */
public class ReactionBigraph implements AbstBigraph{
	Map<Site , Integer> siteNames;
	final Bigraph big;
	
	public ReactionBigraph( ReactionBigraphBuilder rbb ){
		this.big = rbb.rbig.makeBigraph();
		this.siteNames = new HashMap<>();
		
		Iterator<? extends Site> site_iter = rbb.rbig.getSites().iterator();
		for( Site site : this.big.getSites() )
			this.siteNames.put( site , rbb.siteNames.get( site_iter.next() ) );
		this.siteNames = Collections.unmodifiableMap( this.siteNames );
	}
	
	public ReactionBigraph( ReactionBigraph rb ){
		this.big = rb.big.clone();
		this.siteNames = new HashMap<>();
		
		Iterator<? extends Site> site_iter = rb.big.getSites().iterator();
		for( Site site : this.big.getSites() )
			this.siteNames.put( site , rb.siteNames.get( site_iter.next() ) );
		this.siteNames = Collections.unmodifiableMap( this.siteNames );
	}
	
	public ReactionBigraph clone(){
		return new ReactionBigraph( this );
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
	 * Get the name of a site, if it belongs to this ReactionBigraph
	 * @param site
	 * @return
	 * 			The name of the site in input.
	 */
	public Integer getSiteName( Site site ){
		return siteNames.get( site );
	}
	
	/**
	 * Get the map ( Site , Integer ) storing, for each Site, its name (Integer).
	 */
	public Map<Site , Integer> getSitesMap(){
		return this.siteNames;
	}

	@Override
	public Set<? extends OuterName> getOuterNames() {
		return big.getOuterNames();
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
