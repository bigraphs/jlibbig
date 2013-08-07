package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

/**
 * Class used to store a bigMC's redex or reactum bigraph
 * @see AbstBigraph
 */
public class ReactionBigraph implements AbstBigraph{
	final Map<Site , Integer> siteNames;
	final Bigraph big;
	private List<Site> sites;
	
	public ReactionBigraph( ReactionBigraphBuilder rbb ){
		this.big = rbb.rbig.makeBigraph();
		this.siteNames = new HashMap<>();
		sites = new ArrayList<>();
		
		Iterator<? extends Site> site_iter = rbb.rbig.getSites().iterator();
		for( Site site : big.getSites() ){
			this.siteNames.put( site , rbb.siteNames.get( site_iter.next() ) );
			sites.add( site );
		}
		Collections.sort( sites , new SiteComparator() );
		sites = Collections.unmodifiableList( sites );
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
		return sites;
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
		return Collections.unmodifiableMap( this.siteNames );
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
	
	private class SiteComparator implements Comparator<Site> {
	    @Override
	    public int compare( Site s1, Site s2 ) {
	        return siteNames.get( s1 ).compareTo( siteNames.get( s2 ) );
	    }
	}
}
