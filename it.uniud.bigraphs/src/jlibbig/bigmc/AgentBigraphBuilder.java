package jlibbig.bigmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jlibbig.core.*;

/**
 * The class is meant as a helper for agent (ground bigraph) construction and manipulation in
 * presence of series of operations since {@link AgentBigraph} is immutable.
 *
 */
public class AgentBigraphBuilder implements AbstBigraphBuilder{
	BigraphBuilder bigraph; 
	
	/**
	 * Start a new AgentBigraphBuilder.
	 * @param sig
	 * 			the Signature of the AgentBigraph that will be created.
	 */
	public AgentBigraphBuilder( Signature sig ) {
		this.bigraph = new BigraphBuilder( sig );
	}

	/**
	 * Start a new AgentBigraphBuilder from an AgentBigraph.
	 * @param big
	 * 			the AgentBigraph that will be copied in the new AgentBigraphBuilder
	 */
	public AgentBigraphBuilder( AgentBigraph big ) {
		this.bigraph = new BigraphBuilder( big.bigraph );
	}
	
	/**
	 * Start a new AgentBigraphBuilder from a (ground) Bigraph.
	 * @param big
	 * 			the Ground Bigraph that will be copied in the new AgentBigraphBuilder
	 */
	public AgentBigraphBuilder( Bigraph big ){
		this( new AgentBigraph( big ) );
	}
	
	/**
	 * Create a AgentBigraph from the current AgentBigraphBuilder
	 * @return
	 * 			The generated AgetnBigraph
	 */
	public AgentBigraph makeAgent() {
		return new AgentBigraph( this );
	}
	
	/**
	 * Create a Ground Bigraph from the current AgentBigraphBuilder
	 * @return
	 * 			The generated Ground Bigraph.
	 */
	public Bigraph makeBigraph(){
		return bigraph.makeBigraph();
	}

	@Override
	public AgentBigraphBuilder clone() {
		AgentBigraphBuilder abb = new AgentBigraphBuilder( this.bigraph.getSignature() );
		abb.bigraph = bigraph.clone();
		return abb;
	}
	
	@Override
	public Signature getSignature() {
		return this.bigraph.getSignature();
	}

	@Override
	public boolean isEmpty() {
		return this.bigraph.isEmpty();
	}

	@Override
	public boolean isGround() {
		return this.bigraph.isEmpty();
	}

	@Override
	public List<? extends Root> getRoots() {
		return this.bigraph.getRoots();
	}

	@Override
	public List<? extends Site> getSites() {
		return this.bigraph.getSites();
	}

	@Override
	public Set<? extends OuterName> getOuterNames() {
		return this.bigraph.getOuterNames();
	}

	@Override
	public Set<? extends InnerName> getInnerNames() {
		return this.bigraph.getInnerNames();
	}

	@Override
	public Set<? extends Node> getNodes() {
		return this.bigraph.getNodes();
	}

	@Override
	public Set<? extends Edge> getEdges() {
		return this.bigraph.getEdges();
	}

	
	/**
	 * Add a root to the current AgentBigraphBuilder.
	 * 
	 * @return the reference of the new root
	 */
	public Root addRoot() {
		return this.bigraph.addRoot();
	}

	/**
	 * Add a new node to the current AgentBigraphBuilder.
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
	 * Add a new node to the current AgentBigraphBuilder.
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param outernames
	 *            Outernames that will be linked to the node's ports
	 * @return the reference of the new node
	 */
	public Node addNode( String controlName , Parent parent , OuterName... outernames ) {
		return addNode( controlName , parent , Arrays.asList( outernames ) );
	}

	/**
	 * Add a new node to the current AgentBigraphBuilder.
	 * 
	 * @param controlName
	 *            the control's name of the new node
	 * @param parent
	 *            the father of the new node, in the place graph
	 * @param outernames
	 *            Outernames that will be linked to the node's ports
	 * @return the reference of the new node
	 */
	public Node addNode( String controlName , Parent parent , List<OuterName> outernames ) {
		List<Handle> handles = new LinkedList<>();
		for( Handle outer : outernames )
			handles.add( outer );
		return this.bigraph.addNode( controlName , parent , handles );
	}

	/**
	 * Add an outername to the current AgentBigraphBuilder.
	 * Its name will be automatically chosen and can be retrieved with
	 * {@link OuterName#getName() }.
	 * 
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName() {
		return this.bigraph.addOuterName();
	}

	/**
	 * Add an outername to the current AgentBigraphBuilder.
	 * 
	 * @param name
	 *            name of the new outername
	 * @return the reference of the new outername
	 */
	public OuterName addOuterName( String name ) {
		return this.bigraph.addOuterName( name );
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
			if( name != null )
				list.add( this.bigraph.addOuterName( name ) );
			else
				list.add( null );
		}
		return list;
	}

	/**
	 * Add a node to the current AgentBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old AgentBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 */
	public void outerAddNode( String controlName ){
		outerAddNode( controlName , new LinkedList<OuterName>() );
	}
	
	/**
	 * Add a node to the current AgentBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old AgentBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerAddNode( String controlName , OuterName... outernames ){
		outerAddNode( controlName , Arrays.asList( outernames ) );
	}
	
	/**
	 * Add a node to the current AgentBigraphBuilder.
	 * The resulting bigraph will have only one root, connected with a node that contains the old AgentBigraphBuilder.
	 * @param controlName
	 * 			Node's name.
	 * @param outernames
	 * 			Outernames that will be linked to the node's ports
	 */
	public void outerAddNode( String controlName , List<OuterName> outernames ){
		BigraphBuilder bb = new BigraphBuilder( bigraph.getSignature() );
		List<Handle> outers = new ArrayList<>();
		for( OuterName outer : outernames ){
			if( outer != null )
				outers.add( bb.addOuterName( outer.getName() ) );
			else
				outers.add( null );
		}
		bb.addSite( bb.addNode( controlName , bb.addRoot() , outers ) );
		bigraph.outerNest( bb.makeBigraph() );
	}
	
	/**
	 * Set a new OuterName for a node's Port.
	 * 
	 * @param port
	 * @param outername
	 */
	public void relink( Port port, OuterName outername ) {
		this.bigraph.relink( port , outername );
	}

	/**
	 * disconnect a node's port from its current outername.
	 * 
	 * @param p
	 *            the port that will be unlinked
	 */
	public void unlink( Port p ) {
		this.bigraph.unlink( p );
	}

	/**
	 * Merge regions (roots of a place graph)
	 */
	public void merge() {
		this.bigraph.merge();
	}

	/**
	 * Juxtapose the current AgentBigraphBuilder with the AgentBigraph in input. <br />
	 * Roots of the AgentBigraph will precede those of the AgentBigraphBuilder
	 * in the resulting AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftJuxtapose( AgentBigraph graph ) {
		this.bigraph.leftJuxtapose( graph.bigraph );
	}

	/**
	 * Juxtapose the current AgentBigraphBuilder with the AgentBigraph in input. <br />
	 * Roots of the AgentBigraphBuilder will precede those of the AgentBigraph
	 * in the resulting AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            Bigraph that will be juxtaposed.
	 */
	public void rightJuxtapose( AgentBigraph graph ) {
		this.bigraph.rightJuxtapose( graph.bigraph );
	}

	/**
	 * Juxtapose AgentBigraph in input with the current AgentBigraphBuilder.
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged.
	 * Roots of the AgentBigraph will precede those of the AgentBigraphBuilder
	 * in the resulting AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            Bigraph that will be juxtaposed.
	 */
	public void leftParallelProduct( AgentBigraph graph ) {
		this.bigraph.leftParallelProduct( graph.bigraph );
	}

	/**
	 * Juxtapose the current AgentBigraphBuilder with the AgentBigraph in input.
	 * ParallelProduct, differently from the normal juxtapose, doesn't need
	 * disjoint sets of outernames for the two bigraphs. Common outernames will
	 * be merged.
	 * Roots of the AgentBigraphBuilder will precede those of the AgentBigraph
	 * in the resulting AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void rightParallelProduct( AgentBigraph graph ) {
		this.bigraph.rightParallelProduct( graph.bigraph );
	}

	/**
	 * Juxtapose AgentBigraph in input with the current AgentBigraphBuilder. <br />
	 * It will then perform {@link AgentBigraphBuilder#merge()} on the resulting
	 * AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftMergeProduct( AgentBigraph graph ) {
		this.bigraph.leftMergeProduct( graph.bigraph );
	}

	/**
	 * Juxtapose the current AgentBigraphBuilder with the AgentBigraph in input. <br />
	 * It will then perform {@link AgentBigraphBuilder#merge()} on the resulting
	 * AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            AgentBigraph that will be juxtaposed.
	 */
	public void rightMergeProduct( AgentBigraph graph ) {
		this.bigraph.rightMergeProduct(graph.bigraph );
	}

}
