package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;
import jlibbig.core.std.Bigraph;
import jlibbig.core.std.Control;
import jlibbig.core.std.Edge;
import jlibbig.core.std.InnerName;
import jlibbig.core.std.Node;
import jlibbig.core.std.OuterName;
import jlibbig.core.std.Root;
import jlibbig.core.std.Signature;
import jlibbig.core.std.Site;

/**
 * Class used to store a bigMC's redex or reactum bigraph
 * 
 * @see Bigraph
 */
public class ReactionBigraph implements
		jlibbig.core.Bigraph<Control> {
	final int[] sites;
	private final List<Integer> ro_sites;
	final Bigraph big;
	private boolean asRedex;

	/**
	 * Create a ReactionBigraph from the ReactionBigraphBuilder in input.
	 * 
	 * @param rbb
	 *            ReactionBigraphBuilder used to build the new ReactionBigraph
	 */
	public ReactionBigraph(ReactionBigraphBuilder rbb) {
		if (rbb.getRoots().isEmpty())
			throw new IllegalArgumentException(
					"This bigraph can't be converted to a BigMC's ReactionBigraph. The place graph's outerface must be at least 1 (one root).");
		this.big = rbb.rbig.makeBigraph();
		this.asRedex = true;

		ro_sites = Collections.unmodifiableList(new ArrayList<>(rbb
				.getSitesIndices()));
		this.sites = new int[ro_sites.size()];

		if (ro_sites.size() > 0) {
			Iterator<Integer> indicIter = ro_sites.iterator();
			this.sites[0] = indicIter.next();
			for (int j = 1; j < ro_sites.size(); ++j) {
				this.sites[j] = indicIter.next();
				if (sites[j] == sites[j - 1])
					this.asRedex = false;
			}
		}
	}

	/**
	 * Create a ReactionBigraph from the Bigraph in input (if the Bigraph can be
	 * converted into a BigMC's ReactionBigraph).
	 * 
	 * @param big
	 *            Bigraph used to build the new ReactionBigraph
	 * 
	 */
	public ReactionBigraph(Bigraph big) {
		if (big.getRoots().isEmpty())
			throw new IllegalArgumentException(
					"This bigraph can't be converted to a BigMC's ReactionBigraph. The place graph's outerface must be at least 1 (one root).");
		for (Edge edge : big.getEdges()) {
			if (edge.getPoints().size() > 1)
				throw new IllegalArgumentException(
						"Redex can't be converted to a BigMC's ReactionBigraph. Every edge must have only one handled point.");
		}
		if (big.getInnerNames().size() > 0)
			throw new IllegalArgumentException(
					"Redex can't be converted to a BigMC's ReactionBigraph. Its link graph's innerface must be empty.");
		this.big = big;
		this.asRedex = true;
		this.sites = new int[big.getSites().size()];
		List<Integer> siteslist = new ArrayList<>(big.getSites().size());
		for (int i = 0; i < big.getSites().size(); ++i)
			siteslist.add(this.sites[i] = i);
		this.ro_sites = Collections.unmodifiableList(siteslist);
	}

	/**
	 * Create a ReactionBigraph from the Bigraph in input (if the Bigraph can be
	 * converted into a BigMC's ReactionBigraph) and an array of sites' indices.
	 * 
	 * @param big
	 *            Bigraph used to build the new ReactionBigraph
	 * @param sitesindices
	 *            Sites' indices
	 * 
	 */
	public ReactionBigraph(Bigraph big, int... sitesindices) {
		this(new ReactionBigraphBuilder(big, sitesindices));
	}

	/**
	 * Get the signature of the bigraph
	 * 
	 * @return the signature of the bigraph
	 * @see Signature
	 * @see BigraphHandler#getSignature()
	 */
	@Override
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
	 * 
	 * @param site
	 * @return The name of the site in input.
	 */
	public int getSiteIndex(Site site) {
		int i = 0;
		for (Site s : big.getSites()) {
			if (s == site) {
				return sites[i];
			}
			++i;
		}
		return -1;
	}

	/**
	 * Get the map ( Site , Integer ) storing, for each Site, its name
	 * (Integer).
	 */
	public List<Integer> getSitesIndices() {
		return this.ro_sites;
	}

	public boolean isRedex() {
		return asRedex;
	}

	@Override
	public Collection<? extends OuterName> getOuterNames() {
		return big.getOuterNames();
	}

	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return big.getInnerNames();
	}

	@Override
	public Collection<? extends Node> getNodes() {
		return big.getNodes();
	}

	@Override
	public Collection<? extends Edge> getEdges() {
		return big.getEdges();
	}

	public Bigraph asBigraph() {
		return big;
	}
}
