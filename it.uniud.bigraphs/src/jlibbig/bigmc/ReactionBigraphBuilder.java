package jlibbig.bigmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jlibbig.core.*;

/**
 * The class is meant as a helper for bigmc's reaction bigraph construction and manipulation in
 * presence of series of operations since {@link ReactionBigraph} is immutable.
 *
 */
public class ReactionBigraphBuilder implements AbstBigraphBuilder{
	final BigraphBuilder rbig;
	private final List<Integer> sites;
	private final List<Integer> ro_sites;
	
	public static final String nameexpr = "[a-zA-Z][a-zA-Z_0-9]*";
	
	/**
	 * Create a new ReactionBigraphBuilder
	 * @param sig
	 * 			The (immutable) signature of the ReactionBigraphBuilder.
	 */
	public ReactionBigraphBuilder( Signature sig ) {
		this.rbig = new BigraphBuilder( sig );
		sites = new LinkedList<>();
		ro_sites = Collections.unmodifiableList( sites );
	}
	
	/**
	 * Create a new ReactionBigraphBuilder from the {@link jlibbig.bigmc.ReactionBigraph} in input.
	 * @param big
	 * 			ReactionBigraph that will be copied.
	 */
	public ReactionBigraphBuilder( ReactionBigraph big ) {
		this.rbig = new BigraphBuilder( big.big );
		sites = new LinkedList<>( big.getSitesIndices() );
		ro_sites = Collections.unmodifiableList( sites );
	}
	
	/**
	 * Create a new ReactionBigraphBuilder from the one in input.
	 * @param big
	 * 			ReactionBigraphBuilder that will be cloned.
	 */
	public ReactionBigraphBuilder( ReactionBigraphBuilder big ){
		this.rbig = big.rbig.clone();
		this.sites = new LinkedList<>( big.sites );
		this.ro_sites = Collections.unmodifiableList( this.sites );
	}
	
	/**
	 * Create a new ReactionBigraphBuilder from the Bigraph in input.
	 * It's sites will be indexed from 0 to Bigraph#getSites().size().
	 * @param big
	 * 			Bigraph that will be used to create the new ReactionBigraphBuilder
	 */
	public ReactionBigraphBuilder( Bigraph big ){
		this( new ReactionBigraph( big ) );
	}
	
	/**
	 * Create a new ReactionBigraphBuilder from the Bigraph in input and an array of sites' indices.
	 * @param big
	 * 			Bigraph that will be used to create the new ReactionBigraphBuilder
	 * @param sitesindices
	 * 			Sites' indices
	 */
	public ReactionBigraphBuilder( Bigraph big , int... sitesindices ){
		if( big.getRoots().size() == 0 )
			throw new IllegalArgumentException("This bigraph can't be converted to a BigMC's ReactionBigraph. The place graph's outerface must be at least 1 (one root).");
		for( Edge edge : big.getEdges() ){
			if( edge.getPoints().size() > 1 )
				throw new IllegalArgumentException( "Redex can't be converted to a BigMC's ReactionBigraph. Every edge must have only one handled point." );
		}
		if( big.getInnerNames().size() > 0 )
			throw new IllegalArgumentException( "Redex can't be converted to a BigMC's ReactionBigraph. Its link graph's innerface must be empty." );
		if( sitesindices.length != big.getSites().size() )
			throw new IllegalArgumentException( "The size of list of sites indices must be equal to the number of sites in the bigraph." );
		
		this.rbig = new BigraphBuilder( big );
		this.sites = new ArrayList<>( sitesindices.length );
		
		for( int i = 0 ; i < sitesindices.length ; ++i )
			sites.add( sitesindices[ i ] );
		
		this.ro_sites = Collections.unmodifiableList( sites );
	}
	
	/**
	 * Make a {@link jlibbig.bigmc.ReactionBigraph} from the current ReactionBigraphBuilder.
	 * @return
	 * 			The generated {@link jlibbig.bigmc.ReactionBigraph}
	 */
	public ReactionBigraph makeReactionBigraph() {
		this.sortSites();
		
		return new ReactionBigraph( this );
	}
	
	/**
	 * Make a {@link jlibbig.core.Bigraph} from the current ReactionBigraphBuilder.
	 * @return
	 * 			The generated {@link jlibbig.core.Bigraph}
	 */
	public Bigraph makeBigraph(){
		this.sortSites();
		
		return rbig.makeBigraph();
	}

	@Override
	public ReactionBigraphBuilder clone() {
		return new ReactionBigraphBuilder( this );
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
	public List<Integer> getSitesIndices(){
		return ro_sites;
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
	 * 			the handler, in the place graph, father of the new site
	 * @param index
	 * 			the index of the new site ($i).
	 * @return the reference of the new site
	 */
	public Site addSite( Parent parent , int index ) {
		if( index < 0 )
			throw new IllegalArgumentException( "Sites' indices cant be less than zero." );
		Site site = this.rbig.addSite( parent );
		this.sites.add( index );
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
	public void outerNestNode( String controlName ){
		outerNestNode( controlName , new LinkedList<OuterName>() );
	}
	
	/**
	 * Add a node to the current ReactionBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old ReactionBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerNestNode( String controlName , OuterName... outernames ){
		outerNestNode( controlName , Arrays.asList( outernames ) );
	}
	
	/**
	 * Add a node to the current ReactionBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old ReactionBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerNestNode( String controlName , List<OuterName> outernames ){
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
		this.sites.addAll( 0 , graph.getSitesIndices() );
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
		this.rbig.rightParallelProduct( graph.big );
		this.sites.addAll( graph.getSitesIndices() );
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
		if( graph.getRoots().size() > 1 || this.getRoots().size() > 1 )
			throw new RuntimeException( "Cannot apply the merge product '|' between to Bigraphs with more than one root. Parallel Product '||' can only appear at top level" );
		this.rbig.leftParallelProduct( graph.big );
		this.rbig.merge();	
		this.sites.addAll( 0 , graph.getSitesIndices() );
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
		if( graph.getRoots().size() > 1 || this.getRoots().size() > 1 )
			throw new RuntimeException( "Cannot apply the merge product '|' between to Bigraphs with more than one root. Parallel Product '||' can only appear at top level" );
		this.rbig.rightParallelProduct( graph.big );
		this.rbig.merge();
		this.sites.addAll( graph.getSitesIndices() );
	}
	
	/**
	 * Sort the bigraph's sites (according to the values in the Map {@code siteNames}).
	 */
	private void sortSites(){
		
		List<? extends Site> big_sites = new ArrayList<>( rbig.getSites() );
		Collections.sort( big_sites , new SiteComparator() );
		BigraphBuilder bb = new BigraphBuilder( rbig.getSignature() );
		Root[] roots = new Root[ rbig.getSites().size() ];
		for( int i = 0; i < rbig.getSites().size(); ++i )
			roots[i] = bb.addRoot();
		for( Site site : big_sites )
			bb.addSite( roots[ rbig.getSites().indexOf( site ) ] );
		rbig.innerCompose( bb.makeBigraph() );
		Collections.sort( sites );
	}
	
	/**
	 * Used to compare two sites (using their value in the Map {@code siteNames}
	 * @see ReactionBigraphBuilder#sortSites()
	 *
	 */
	private class SiteComparator implements Comparator<Site> {
	    @Override
	    public int compare( Site s1, Site s2 ) {
	    	Integer i1 = null;
	    	Integer i2 = null;
	    	Iterator<Integer> intIter = sites.iterator();
	    	for( Site s : rbig.getSites() ){
	    		if( s == s1 ) 
	    			i1 = intIter.next();
	    		else if( s == s2 )
	    			i2 = intIter.next();
	    		else
	    			intIter.next();
	    	}
	        return i1.compareTo( i2 );
	    }
	}
	
}
