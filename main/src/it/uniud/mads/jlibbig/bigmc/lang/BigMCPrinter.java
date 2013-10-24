package it.uniud.mads.jlibbig.bigmc.lang;

import java.util.*;

import it.uniud.mads.jlibbig.core.BigraphHandler;
import it.uniud.mads.jlibbig.core.Child;
import it.uniud.mads.jlibbig.core.Edge;
import it.uniud.mads.jlibbig.core.Handle;
import it.uniud.mads.jlibbig.core.OuterName;
import it.uniud.mads.jlibbig.core.Root;
import it.uniud.mads.jlibbig.core.Site;
import it.uniud.mads.jlibbig.core.Node;
import it.uniud.mads.jlibbig.core.Port;
import it.uniud.mads.jlibbig.core.lang.PrettyPrinter;
import it.uniud.mads.jlibbig.core.std.Control;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.bigmc.*;

/**
 * BigMC's syntax pretty printer. Convert into Strings all the bigraphs or
 * systems that can be represented with the BigMC's Language
 * 
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 * 
 */
public class BigMCPrinter implements PrettyPrinter<BigraphRewritingSystem> {
	public BigMCPrinter() {
	}

	public static final String ln = System.getProperty("line.separator");

	/**
	 * Convert a BigraphSystem into a string in BigMC's syntax
	 * 
	 * @param brs
	 *            the system that will be converted into a string.
	 * @see BigraphRewritingSystem
	 * @return the resulting string
	 */
	@Override
	public String toString(BigraphRewritingSystem brs) {
		StringBuilder s = new StringBuilder();

		s.append(toString(brs.getSignature()));

		s.append(ln);

		for (String str : brs.getOuterNames())
			s.append("%name ").append(str).append(";").append(ln);

		s.append(ln);

		s.append(toString(brs.getReactions()));

		s.append(ln);

		for (AgentBigraph big : brs.getBigraphs())
			s.append(toString(big)).append(" ;").append(ln);

		s.append(ln);

		return s.toString();
	}

	/**
	 * Convert a Signature into a string in BigMC's syntax
	 * 
	 * @param sig
	 *            The Signature that will be converted into a string.
	 * @see Signature
	 * @return The String representing the element in input.
	 */
	public static String toString(Signature sig) {
		StringBuilder s = new StringBuilder();

		for (Control ctrl : sig) {
			s.append(ctrl.isActive() ? "%active " : "%passive ")
					.append(ctrl.getName()).append(" : ")
					.append(ctrl.getArity()).append(";").append(ln);
		}

		return s.toString();
	}

	/**
	 * Convert a Collection of {@link RewritingRule} into a string in BigMC's
	 * syntax.
	 * 
	 * @param reaction_rules
	 *            Collection of RewritingRules that will be converted into a
	 *            string.
	 * @return The String representing the element in input.
	 */
	public static String toString(Collection<RewritingRule> reaction_rules) {
		StringBuilder s = new StringBuilder();
		for (RewritingRule reaction : reaction_rules) {
			s.append(toString(reaction)).append(";").append(ln);
		}
		return s.toString();
	}

	/**
	 * Convert a {@link RewritingRule} into the corresponding String in BigMC's
	 * syntax.
	 * 
	 * @param reaction
	 * @return the resulting string
	 */
	public static String toString(RewritingRule reaction) {
		return toString(reaction.getRedex()) + " -> "
				+ toString(reaction.getReactum());
	}

	/**
	 * Translate a bigraph ( {@link ReactionBigraph} ) to a string with BigMC's
	 * syntax
	 * 
	 * @param big
	 *            the bigraph that will be converted into a string.
	 * @return the resulting string
	 */

	public static String toString(ReactionBigraph big) {
		StringBuilder s = new StringBuilder();
		Iterator<? extends Root> it = big.getRoots().iterator();
		while (it.hasNext()) {

			Collection<? extends Child> childs = it.next().getChildren();
			if (!childs.isEmpty()) {
				Iterator<? extends Child> childIt = childs.iterator();
				while (childIt.hasNext()) {
					s.append(
							toString(childIt.next(), big.getOuterNames(),
									big.getSites(), big.getSitesIndices()))
							.append(childIt.hasNext() ? " | " : "");
				}
			} else
				s.append("nil");
			s.append(it.hasNext() ? " || " : "");
		}
		return s.toString();
	}

	/**
	 * Translate a bigraph ( {@link ReactionBigraphBuilder} ) to a string with
	 * BigMC's syntax
	 * 
	 * @param big
	 *            the bigraph that will be converted into a string.
	 * @return the resulting string
	 */
	public static String toString(ReactionBigraphBuilder big) {
		StringBuilder s = new StringBuilder();
		Iterator<? extends Root> it = big.getRoots().iterator();
		while (it.hasNext()) {

			Collection<? extends Child> childs = it.next().getChildren();
			if (!childs.isEmpty()) {
				Iterator<? extends Child> childIt = childs.iterator();
				while (childIt.hasNext()) {
					s.append(
							toString(childIt.next(), big.getOuterNames(),
									big.getSites(), big.getSitesIndices()))
							.append(childIt.hasNext() ? " | " : "");
				}
			} else
				s.append("nil");
			s.append(it.hasNext() ? " || " : "");
		}
		return s.toString();
	}

	/**
	 * Translate a bigraph ( {@link AgentBigraph} ) to a string with BigMC's
	 * syntax
	 * 
	 * @param big
	 *            the bigraph that will be converted into a string.
	 * @return the resulting string
	 */
	public static String toString(AgentBigraph big) {
		StringBuilder s = new StringBuilder();

		Collection<? extends Child> childs = big.getRoots().get(0)
				.getChildren();
		if (!childs.isEmpty()) {
			Iterator<? extends Child> childIt = childs.iterator();
			while (childIt.hasNext()) {
				s.append(toString(childIt.next(), big.getOuterNames())).append(
						childIt.hasNext() ? " | " : "");
			}
		} else
			s.append("nil");

		return s.toString();
	}

	/**
	 * Translate a bigraph ( {@link AgentBigraphBuilder} ) to a string with
	 * BigMC's syntax
	 * 
	 * @param big
	 *            the bigraph that will be converted into a string.
	 * @return the resulting string
	 */
	public static String toString(AgentBigraphBuilder big) {
		StringBuilder s = new StringBuilder();

		Collection<? extends Child> childs = big.getRoots().get(0)
				.getChildren();
		if (!childs.isEmpty()) {
			Iterator<? extends Child> childIt = childs.iterator();
			while (childIt.hasNext()) {
				s.append(toString(childIt.next(), big.getOuterNames())).append(
						childIt.hasNext() ? " | " : "");
			}
		} else
			s.append("nil");

		return s.toString();
	}

	/**
	 * Translate a bigraph ( {@link BigraphHandler} ) to a string with BigMC's
	 * syntax (if the bigraph can be converted into a ReactionBigraph or an
	 * AgentBigraph).
	 * 
	 * @param big
	 *            the bigraph that will be converted into a string.
	 * @return the resulting string
	 */
	public static String toString(BigraphHandler<Control> big) {
		if (big.getRoots().isEmpty())
			throw new IllegalArgumentException(
					"The place graph's outerface must be at least 1 (one root). Cannot be converted into a string with BigMC's syntax.");
		for (Edge edge : big.getEdges()) {
			if (edge.getPoints().size() > 1)
				throw new IllegalArgumentException(
						"Every edge must have only one handled point. Cannot be converted into a string with BigMC's syntax.");
		}
		if (big.getInnerNames().size() > 0)
			throw new IllegalArgumentException(
					"Link graph's innerface must be empty. Cannot be converted into a string with BigMC's syntax.");

		StringBuilder s = new StringBuilder();

		if (big.isGround()) {
			if (big.getRoots().size() != 1)
				throw new IllegalArgumentException(
						"Bigraph isn't an Agent - The place graph's outerface must be 1 (exactly one root). Cannot be converted into a string with BigMC's syntax.");

			Collection<? extends Child> childs = big.getRoots().get(0)
					.getChildren();
			if (!childs.isEmpty()) {
				Iterator<? extends Child> childIt = childs.iterator();
				while (childIt.hasNext()) {
					s.append(toString(childIt.next(), big.getOuterNames()))
							.append(childIt.hasNext() ? " | " : "");
				}
			} else
				s.append("nil");

		} else {

			List<Integer> sitesindices = new ArrayList<>(big.getSites().size());
			for (int i = 0; i < big.getSites().size(); ++i)
				sitesindices.add(i);

			Iterator<? extends Root> it = big.getRoots().iterator();
			while (it.hasNext()) {
				Collection<? extends Child> childs = it.next().getChildren();
				if (!childs.isEmpty()) {
					Iterator<? extends Child> childIt = childs.iterator();
					while (childIt.hasNext()) {
						s.append(
								toString(childIt.next(), big.getOuterNames(),
										big.getSites(), sitesindices)).append(
								childIt.hasNext() ? " | " : "");
					}
				} else
					s.append("nil");
				s.append(it.hasNext() ? " || " : "");
			}

		}

		return s.toString();
	}

	/**
	 * Auxiliary procedure, used by toString( ReactionBigraph )
	 * 
	 * @param d
	 *            control or site handler
	 * @param collection
	 *            set of outernames
	 * @param sitenum
	 *            sites' enumeration, used to retrieve the right number of the
	 *            site
	 * @see ReactionBigraph
	 * @return the resulting string
	 */
	private static String toString(Child d,
			Collection<? extends OuterName> collection,
			List<? extends Site> sitelist, List<Integer> sitenum) {
		StringBuilder s = new StringBuilder();

		if (d instanceof Site) {
			s.append("$").append(sitenum.get(sitelist.indexOf(d)));
		} else {
			s.append(((Node) d).getControl().getName());

			StringBuilder ns = new StringBuilder();
			int unlinked = 0;

			Iterator<? extends Port> portIt = ((Node) d).getPorts().iterator();
			while (portIt.hasNext()) {
				Handle handle = portIt.next().getHandle();
				if (collection.contains(handle)) {
					for (int i = 0; i < unlinked; ++i)
						ns.append(" - , ");
					ns.append(" ").append(((OuterName) handle).getName())
							.append(" , ");
				} else
					++unlinked;
			}

			if (ns.length() > 0) // note: if length > 0 --> at least 1 append
									// has been done --> length > 3
				s.append("[").append(ns.substring(0, ns.length() - 2))
						.append("]");

			Collection<? extends Child> childs = ((Node) d).getChildren();
			if (!childs.isEmpty()) {
				s.append(".");
				if (childs.size() > 1)
					s.append("( ");

				Iterator<? extends Child> childIt = childs.iterator();
				while (childIt.hasNext())
					s.append(
							toString(childIt.next(), collection, sitelist,
									sitenum)).append(
							childIt.hasNext() ? " | " : "");

				if (childs.size() > 1)
					s.append(" )");
			}
		}

		return s.toString();
	}

	/**
	 * Auxiliary procedure, used by toString( AgentBigraph )
	 * 
	 * @param c
	 *            control or site handler
	 * @param collection
	 *            set of outer names
	 * @return the resulting string
	 */
	private static String toString(Child c,
			Collection<? extends OuterName> collection) {
		StringBuilder s = new StringBuilder();

		if (c instanceof Site) {
			throw new RuntimeException(
					"Unexpected error while printing: AgentBigraph with Sites.");
		} else {
			s.append(((Node) c).getControl().getName());

			StringBuilder ns = new StringBuilder();
			int unlinked = 0;

			Iterator<? extends Port> portIt = ((Node) c).getPorts().iterator();
			while (portIt.hasNext()) {
				Handle handle = portIt.next().getHandle();
				if (collection.contains(handle)) {
					for (int i = 0; i < unlinked; ++i)
						ns.append(" - , ");
					ns.append(" ").append(((OuterName) handle).getName())
							.append(" , ");
				} else
					++unlinked;
			}

			if (ns.length() > 0) // note: if length > 0 --> at least 1 append
									// has been done --> length > 3
				s.append("[").append(ns.substring(0, ns.length() - 2))
						.append("]");

			Collection<? extends Child> childs = ((Node) c).getChildren();
			if (!childs.isEmpty()) {
				s.append(".");
				if (childs.size() > 1)
					s.append("( ");

				Iterator<? extends Child> childIt = childs.iterator();
				while (childIt.hasNext())
					s.append(toString(childIt.next(), collection)).append(
							childIt.hasNext() ? " | " : "");

				if (childs.size() > 1)
					s.append(" )");
			}
		}

		return s.toString();
	}

}
