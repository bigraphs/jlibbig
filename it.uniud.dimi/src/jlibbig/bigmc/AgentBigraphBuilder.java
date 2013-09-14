package jlibbig.bigmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jlibbig.core.std.Bigraph;
import jlibbig.core.std.BigraphBuilder;
import jlibbig.core.std.Control;
import jlibbig.core.std.Edge;
import jlibbig.core.std.Handle;
import jlibbig.core.std.InnerName;
import jlibbig.core.std.Node;
import jlibbig.core.std.OuterName;
import jlibbig.core.std.Parent;
import jlibbig.core.std.Port;
import jlibbig.core.std.Root;
import jlibbig.core.std.Signature;
import jlibbig.core.std.Site;

/**
 * The class is meant as a helper for agent (ground bigraph) construction and
 * manipulation in presence of series of operations since {@link AgentBigraph}
 * is immutable.
 * 
 */
public class AgentBigraphBuilder implements
		jlibbig.core.BigraphBuilder<Control> {
	BigraphBuilder bigraph;

	public static final String nameexpr = "[a-zA-Z][a-zA-Z_0-9]*";

	/**
	 * Construct a new AgentBigraphBuilder.
	 * 
	 * @param sig
	 *            the Signature of the AgentBigraph that will be created.
	 */
	public AgentBigraphBuilder(Signature sig) {
		this.bigraph = new BigraphBuilder(sig);
		this.bigraph.addRoot();
	}

	/**
	 * Construct a new AgentBigraphBuilder from an AgentBigraph.
	 * 
	 * @param big
	 *            the AgentBigraph that will be copied in the new
	 *            AgentBigraphBuilder
	 */
	public AgentBigraphBuilder(AgentBigraph big) {
		this.bigraph = new BigraphBuilder(big.bigraph);
	}

	/**
	 * Construct a new AgentBigraphBuilder from the one in input.
	 * 
	 * @param big
	 *            AgentBigraphBuilder that will be used to construct the new
	 *            AgentBigraphBuilder
	 */
	public AgentBigraphBuilder(AgentBigraphBuilder big) {
		this.bigraph = big.bigraph.clone();
	}

	/**
	 * Construct a new AgentBigraphBuilder from a (ground) Bigraph.
	 * 
	 * @param big
	 *            the Ground Bigraph that will be copied in the new
	 *            AgentBigraphBuilder
	 */
	public AgentBigraphBuilder(Bigraph big) {
		this(new AgentBigraph(big));
	}

	/**
	 * Create a AgentBigraph from the current AgentBigraphBuilder
	 * 
	 * @return The generated AgetnBigraph
	 */
	public AgentBigraph makeAgent() {
		return new AgentBigraph(this);
	}

	/**
	 * Create a Ground Bigraph from the current AgentBigraphBuilder
	 * 
	 * @return The generated Ground Bigraph.
	 */
	public Bigraph makeBigraph() {
		return bigraph.makeBigraph();
	}

	@Override
	public AgentBigraphBuilder clone() {
		return new AgentBigraphBuilder(this);
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
		return this.bigraph.isGround();
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
	public Collection<? extends OuterName> getOuterNames() {
		return this.bigraph.getOuterNames();
	}

	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return this.bigraph.getInnerNames();
	}

	@Override
	public Collection<? extends Node> getNodes() {
		return this.bigraph.getNodes();
	}

	@Override
	public Collection<? extends Edge> getEdges() {
		return this.bigraph.getEdges();
	}

	/**
	 * Get the root of the current AgentBigraphBuilder. bigMC's bigraph (non
	 * reaction-rules) have exactly one root.
	 * 
	 * @return the reference of the new root
	 */
	public Root getRoot() {
		return getRoots().get(0);
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
	public Node addNode(String controlName, Parent parent) {
		return addNode(controlName, parent, new LinkedList<OuterName>());
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
	public Node addNode(String controlName, Parent parent,
			OuterName... outernames) {
		return addNode(controlName, parent, Arrays.asList(outernames));
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
	public Node addNode(String controlName, Parent parent,
			List<OuterName> outernames) {
		if (!controlName.matches(nameexpr))
			throw new IllegalArgumentException(
					"Control's name: "
							+ controlName
							+ " - Controls' names must match the following regular expression: "
							+ nameexpr);
		List<Handle> handles = new LinkedList<>();
		for (Handle outer : outernames)
			handles.add(outer);
		return this.bigraph.addNode(controlName, parent, handles);
	}

	/**
	 * Add an outername to the current AgentBigraphBuilder. Its name will be
	 * automatically chosen and can be retrieved with
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
	public OuterName addOuterName(String name) {
		if (!name.matches(nameexpr))
			throw new IllegalArgumentException(
					"OuterName: "
							+ name
							+ " - OuterNames must match the following regular expression: "
							+ nameexpr);
		return this.bigraph.addOuterName(name);
	}

	/**
	 * Add a list of outernames to the current ReactionBigraphBuilder.
	 * 
	 * @param names
	 *            List of outernames' names (String).
	 * @return List of added OuterNames. If the list in input present some null
	 *         values, then a null value will be present in the same position in
	 *         the returned list.
	 */
	public List<OuterName> addOuterNames(List<String> names) {
		List<OuterName> list = new LinkedList<>();
		for (String name : names) {
			if (name != null) {
				if (!name.matches(nameexpr))
					throw new IllegalArgumentException(
							"OuterName: "
									+ name
									+ " - OuterNames must match the following regular expression: "
									+ nameexpr);
				list.add(this.bigraph.addOuterName(name));
			} else
				list.add(null);
		}
		return list;
	}

	/**
	 * Add a node to the current AgentBigraphBuilder. The resulting bigraph will
	 * have only one root, connected with a node that contains the old
	 * AgentBigraphBuilder.
	 * 
	 * @param controlName
	 *            Node's name.
	 */
	public void outerAddNode(String controlName) {
		outerAddNode(controlName, new LinkedList<OuterName>());
	}

	/**
	 * Add a node to the current AgentBigraphBuilder. The resulting bigraph will
	 * have one root, connected with a node that contains the old
	 * AgentBigraphBuilder.
	 * 
	 * @param controlName
	 *            Node's name.
	 * @param outernames
	 *            Outernames that will be linked to the node's ports
	 */
	public void outerAddNode(String controlName, OuterName... outernames) {
		outerAddNode(controlName, Arrays.asList(outernames));
	}

	/**
	 * Add a node to the current AgentBigraphBuilder. The resulting bigraph will
	 * have one root, connected with a node that contains the old
	 * AgentBigraphBuilder.
	 * 
	 * @param controlName
	 *            Node's name.
	 * @param outernames
	 *            Outernames that will be linked to the node's ports
	 */
	public void outerAddNode(String controlName, List<OuterName> outernames) {
		if (!controlName.matches(nameexpr))
			throw new IllegalArgumentException(
					"Control's name: "
							+ controlName
							+ " - Controls' names must match the following regular expression: "
							+ nameexpr);
		BigraphBuilder bb = new BigraphBuilder(bigraph.getSignature());
		List<Handle> outers = new ArrayList<>();
		for (OuterName outer : outernames) {
			if (outer != null)
				outers.add(bb.addOuterName(outer.getName()));
			else
				outers.add(null);
		}
		bb.addSite(bb.addNode(controlName, bb.addRoot(), outers));
		bigraph.outerNest(bb.makeBigraph());
	}

	/**
	 * Set a new OuterName for a node's Port.
	 * 
	 * @param port
	 * @param outername
	 */
	public void relink(Port port, OuterName outername) {
		this.bigraph.relink(port, outername);
	}

	/**
	 * disconnect a node's port from its current outername.
	 * 
	 * @param p
	 *            the port that will be unlinked
	 */
	public void unlink(Port p) {
		this.bigraph.unlink(p);
	}

	/**
	 * Juxtapose AgentBigraph in input with the current AgentBigraphBuilder. <br />
	 * It will then perform {@link BigraphBuilder#merge()} on the resulting
	 * AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            bigraph that will be juxtaposed.
	 */
	public void leftMergeProduct(AgentBigraph graph) {
		this.bigraph.leftParallelProduct(graph.bigraph);
		this.bigraph.merge();
	}

	/**
	 * Juxtapose the current AgentBigraphBuilder with the AgentBigraph in input. <br />
	 * It will then perform {@link BigraphBuilder#merge()} on the resulting
	 * AgentBigraphBuilder.
	 * 
	 * @param graph
	 *            AgentBigraph that will be juxtaposed.
	 */
	public void rightMergeProduct(AgentBigraph graph) {
		this.bigraph.rightParallelProduct(graph.bigraph);
		this.bigraph.merge();
	}

}
