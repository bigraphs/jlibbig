package jlibbig.bigmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jlibbig.core.*;

/**
 * The class is meant as a helper for bigmc's reaction bigraph construction and manipulation in
 * presence of series of operations since {@link ReactionBigraph} is immutable.
 *
 */
public class ReactionBigraphBuilder implements AbstBigraphBuilder{
	BigraphBuilder rbig;
	Map<Site , Integer> siteNames;
	
	public static final String nameexpr = "[a-zA-Z][a-zA-Z_0-9]*";
	
	/**
	 * Create a new ReactionBigraphBuilder
	 * @param sig
	 * 			The (immutable) signature of the ReactionBigraphBuilder.
	 */
	public ReactionBigraphBuilder( Signature sig ) {
		this.rbig = new BigraphBuilder( sig );
		siteNames = new HashMap<>();
	}
	
	/**
	 * Create a new ReactionBigraphBuilder from the {@link jlibbig.bigmc.ReactionBigraph} in input.
	 * @param big
	 * 			ReactionBigraph that will be copied.
	 */
	public ReactionBigraphBuilder( ReactionBigraph big ) {
		this.rbig = new BigraphBuilder( big.big );
		this.siteNames = new HashMap<>();
		
		Iterator<? extends Site> site_iter = big.getSites().iterator();
		for( Site site : rbig.getSites() )
			this.siteNames.put( site , big.siteNames.get( site_iter.next() ) );
	}
	
	/**
	 * Make a {@link jlibbig.bigmc.ReactionBigraph} from the current ReactionBigraphBuilder.
	 * @return
	 * 			The generated {@link jlibbig.bigmc.ReactionBigraph}
	 */
	public ReactionBigraph makeReactionBigraph() {
		return new ReactionBigraph( this );
	}
	
	/**
	 * Make a {@link jlibbig.core.Bigraph} from the current ReactionBigraphBuilder.
	 * @return
	 * 			The generated {@link jlibbig.core.Bigraph}
	 */
	public Bigraph makeBigraph(){
		return rbig.makeBigraph();
	}

	@Override
	public ReactionBigraphBuilder clone() {
		ReactionBigraphBuilder rbb = new ReactionBigraphBuilder( this.rbig.getSignature() );
		rbb.rbig = this.rbig.clone();
		
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( Site site : rbb.rbig.getSites() )
			rbb.siteNames.put( site , this.siteNames.get( site_iter.next() ) );
		return rbb;
	}
	
	@Override
	public Signature getSignature() {
		return this.rbig.getSignature();
	}

	@Override
	public boolean isEmpty() {
		return this.rbig.isEmpty();
	}

	@Override
	public boolean isGround() {
		return this.rbig.isGround();
	}

	@Override
	public List<? extends Root> getRoots() {
		return this.rbig.getRoots();
	}

	@Override
	public List<? extends Site> getSites() {
		return this.rbig.getSites();
	}

	@Override
	public Set<? extends OuterName> getOuterNames() {
		return this.rbig.getOuterNames();
	}

	@Override
	public Set<? extends InnerName> getInnerNames() {
		return this.rbig.getInnerNames();
	}

	@Override
	public Set<? extends Node> getNodes() {
		return this.rbig.getNodes();
	}

	@Override
	public Set<? extends Edge> getEdges() {
		return this.rbig.getEdges();
	}

	/**
	 * Get the map ( Site , Integer ) storing, for each Site, its name (Integer).
	 */
	public Map<Site , Integer> getSitesMap(){
		return Collections.unmodifiableMap( this.siteNames );
	}
	
	/**
	 * Add a root to the current ReactionBigraphBuilder.
	 * 
	 * @return the reference of the new root
	 */
	public Root addRoot() {
		return this.rbig.addRoot();
	}

	/**
	 * Add a site to the current ReactionBigraphBuilder.
	 * 
	 * @param parent
	 *            the handler, in the place graph, father of the new site
	 * @return the reference of the new site
	 */
	public Site addSite( Parent parent , int name ) {
		Site site = this.rbig.addSite( parent );
		siteNames.put( site , name );
		return site;
	}

	/**
	 * Add a new node to the ReactionBigraphBuilder.
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @return the reference of the new node
	 */
	public Node addNode( String controlName , Parent parent ) {
		return addNode( controlName , parent , new LinkedList<OuterName>() );
	}

	/**
	 * Add a new node to the ReactionBigraphBuilder.
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param outernames
	 *            Outernames that will be linked to the new node's ports
	 * @return the reference of the new node
	 */
	public Node addNode( String controlName , Parent parent , OuterName... outernames ) {
		return addNode( controlName , parent , Arrays.asList( outernames ) );
	}

	/**
	 * Add a new node to the ReactionBigraphBuilder.
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param outernames
	 *            Outernames that will be linked to the new node's ports
	 * @return the reference of the new node
	 */
	public Node addNode( String controlName , Parent parent , List<OuterName> outernames ) {
		if( !controlName.matches(nameexpr) )
			throw new IllegalArgumentException( "Control's name: " + controlName + " - Controls' names must match the following regular expression: " + nameexpr );
		List<Handle> handles = new LinkedList<>();
		for( Handle outer : outernames )
			handles.add( outer );
		return this.rbig.addNode( controlName , parent , handles );
	}

	/**
	 * Add an outername to the current ReactionBigraphBuilder.
	 * Its name will be automatically generated and can be retrieved with {@link OuterName#getName() }.
	 * 
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName() {
		return this.rbig.addOuterName();
	}

	/**
	 * Add an outername to the current ReactionBigraphBuilder.
	 * 
	 * @param name
	 *            name of the new outername
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName( String name ) {
		if( !name.matches(nameexpr) )
			throw new IllegalArgumentException( "OuterName: " + name + " - OuterNames must match the following regular expression: " + nameexpr );
		
		return this.rbig.addOuterName( name );
	}
	
	/**
	 * Add a list of outernames to the current ReactionBigraphBuilder.
	 * @param names
	 * 			List of outernames' names (String).
	 * @return
	 * 			List of added OuterNames. If the list in input present some null values, then a null value will be present in the same position in the returned list.
	 */
	public List<OuterName> addOuterNames( List<String> names ){
		List<OuterName> list = new LinkedList<>();
		for( String name : names ){
			if( name != null ){
				if( !name.matches(nameexpr) )
					throw new IllegalArgumentException( "OuterName: " + name + " - OuterNames must match the following regular expression: " + nameexpr );
				list.add( this.rbig.addOuterName( name ) );
			}else
				list.add( null );
		}
		return list;
	}

	/**
	 * Set a new OuterName for a node's Port.
	 * 
	 * @param port
	 * @param outername
	 */
	public void relink( Port port, OuterName outername ) {
		this.rbig.relink( port , outername );
	}

	/**
	 * Disconnect a node's port from its current OuterName.
	 * 
	 * @param p
	 *            the port that will be unlinked
	 */
	public void unlink( Port p ) {
		this.rbig.unlink( p );
	}

	/**
	 * Merge regions (roots of a place graph)
	 */
	public void merge() {
		this.rbig.merge();
	}

	/**
	 * Add a node to the current ReactionBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old ReactionBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 */
	public void outerAddNode( String controlName ){
		outerAddNode( controlName , new LinkedList<OuterName>() );
	}
	
	/**
	 * Add a node to the current ReactionBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old ReactionBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerAddNode( String controlName , OuterName... outernames ){
		outerAddNode( controlName , Arrays.asList( outernames ) );
	}
	
	/**
	 * Add a node to the current ReactionBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old ReactionBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerAddNode( String controlName , List<OuterName> outernames ){
		if( !controlName.matches(nameexpr) )
			throw new IllegalArgumentException( "Control's name: " + controlName + " - Controls' names must match the following regular expression: " + nameexpr );
		
		BigraphBuilder bb = new BigraphBuilder( rbig.getSignature() );
		List<Handle> outers = new ArrayList<>();
		for( OuterName outer : outernames ){
			if( outer != null )
				outers.add( bb.addOuterName( outer.getName() ) );
			else
				outers.add( null );
		}
		bb.addSite( bb.addNode( controlName , bb.addRoot() , outers ) );
		rbig.outerNest( bb.makeBigraph() );
	}
	
	/**
	 * Juxtapose the current ReactionBigraphBuilder with the ReactionBigraph in input.
	 * Roots and sites of the ReactionBigraph will precede those of the ReactionBigraphBuilder
	 * in the resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftJuxtapose( ReactionBigraph graph ) {
		this.rbig.leftJuxtapose( graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}

	/**
	 * Juxtapose the current ReactionBigraphBuilder with the ReactionBigraph in input.
	 * Roots and sites of the ReactionbBigraphBuilder will precede those of the ReactionBigraph
	 * in the resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightJuxtapose( ReactionBigraph graph ) {
		int sitenum = this.rbig.getSites().size();
		this.rbig.rightJuxtapose( graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( int i = 0; i < sitenum; ++i ) site_iter.next();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}

	/**
	 * Nest the current ReactionBigraphBuilder with the ReactionBigraph in input.
	 * Nesting, differently from composition, add ReactionBigraph's outernames to
	 * the ReactionBigraphBuilder if they aren't already present.
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 */
	public void innerNest( ReactionBigraph graph ) {
		//TODO
		throw new UnsupportedOperationException( "innerNest: Not implemented yet." );
	}

	/**
	 * Nest a ReactionBigraph in input with the current ReactionBigraphBuilder.
	 * Nesting, differently from composition, add ReactionBigraph's outernames to
	 * the ReactionBigraphBuilder if they aren't already present.
	 * 
	 * @param graph
	 *            the "inner" bigraph
	 */
	public void outerNest( ReactionBigraph graph ) {
		//TODO
		throw new UnsupportedOperationException( "outerNest: Not implemented yet." );
	}

	/**
	 * Juxtapose ReactionBigraph in input with the current ReactionBigraphBuilder.
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged. <br />
	 * Roots and sites of the ReactionBigraph will precede those of the ReactionBigraphBuilder
	 * in the resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftParallelProduct( ReactionBigraph graph ) {
		this.rbig.leftParallelProduct( graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}

	/**
	 * Juxtapose the current ReactionBigraphBuilder with the ReactionBigraph in input.
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged. <br />
	 * Roots and sites of the ReactionBigraphBuilder will precede those of the ReactionBigraph
	 * in the resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightParallelProduct( ReactionBigraph graph ) {
		int sitenum = this.rbig.getSites().size();
		this.rbig.rightParallelProduct( graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( int i = 0; i < sitenum; ++i ) site_iter.next();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}

	/**
	 * Juxtapose the ReactionBigraph in input with the current ReactionBigraphBuilder.
	 * It will then perform {@link BigraphBuilder#merge()} on the resulting
	 * ReactionBigraphBuilder. <br />
	 * Sites of the ReactionBigraph will precede those of the ReactionBigraphBuilder in the
	 * resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftMergeProduct( ReactionBigraph graph ) {
		this.rbig.leftMergeProduct( graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}

	/**
	 * Juxtapose the current ReactionBigraphBuilder with the ReactionBigraph in input. <br />
	 * It will then perform {@link BigraphBuilder#merge()} on the resulting
	 * ReactionBigraphBuilder. <br />
	 * Sites of the ReactionBigraphBuilder will precede those of the ReactionBigraph in the
	 * resulting ReactionBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightMergeProduct( ReactionBigraph graph ) {
		int sitenum = this.rbig.getSites().size();
		this.rbig.rightMergeProduct(graph.big );
		Iterator<? extends Site> site_iter = this.rbig.getSites().iterator();
		for( int i = 0; i < sitenum; ++i ) site_iter.next();
		for( Site site : graph.big.getSites() )
			this.siteNames.put( site_iter.next() , graph.siteNames.get( site ) );
		sortSites();
	}
	
	/**
	 * Sort the bigraph's sites (according to the values in the Map {@code siteNames}).
	 */
	private void sortSites(){
		//TODO: migliorare computazionalmente, (stile mergesort)
		List<? extends Site> sites = new ArrayList<>( rbig.getSites() );
		Collections.sort( sites , new SiteComparator() );
		BigraphBuilder bb = new BigraphBuilder( rbig.getSignature() );
		Root[] roots = new Root[ rbig.getSites().size() ];
		for( int i = 0; i < rbig.getSites().size(); ++i )
			roots[i] = bb.addRoot();
		for( Site site : sites )
			bb.addSite( roots[ rbig.getSites().indexOf( site ) ] );
		rbig.innerCompose( bb.makeBigraph() );
		HashMap<Site, Integer> newSiteNames = new HashMap<>();
		
		Iterator<? extends Site> site_iter = sites.iterator();
		for( Site site : rbig.getSites() )
			newSiteNames.put( site , siteNames.get( site_iter.next() ) );
		siteNames = newSiteNames;
	}
	
	/**
	 * Used to compare two sites (using their value in the Map {@code siteNames}
	 * @see ReactionBigraphBuilder#sortSites()
	 *
	 */
	private class SiteComparator implements Comparator<Site> {
	    @Override
	    public int compare( Site s1, Site s2 ) {
	        return siteNames.get( s1 ).compareTo( siteNames.get( s2 ) );
	    }
	}
	
}
