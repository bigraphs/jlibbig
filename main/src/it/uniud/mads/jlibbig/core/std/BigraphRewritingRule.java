package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.InstantiationRule;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.RewritingRule;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.std.EditableNode.EditablePort;

public class BigraphRewritingRule implements RewritingRule<Bigraph, Bigraph> {

	private final static boolean DEBUG = false;
	private final static boolean DEBUG_PRINT_MATCH = DEBUG;
	private final static boolean DEBUG_PRINT_RESULT = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = true;

	final Bigraph redex;
	final Bigraph reactum;
	final BigraphInstantiationMap eta;

	private BigraphMatcher matcher;

	public BigraphRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		this(BigraphMatcher.DEFAULT, redex, reactum,
				new BigraphInstantiationMap(redex.sites.size(), eta));
	}

	public BigraphRewritingRule(BigraphMatcher matcher, Bigraph redex,
			Bigraph reactum, int... eta) {
		this(matcher, redex, reactum, new BigraphInstantiationMap(
				redex.sites.size(), eta));
	}

	public BigraphRewritingRule(Bigraph redex, Bigraph reactum,
			BigraphInstantiationMap eta) {
		this(BigraphMatcher.DEFAULT, redex, reactum, eta);
	}

	public BigraphRewritingRule(BigraphMatcher matcher, Bigraph redex,
			Bigraph reactum, BigraphInstantiationMap eta) {
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(
					"Redex and reactum should have the same singature.",
					reactum.getSignature(), redex.getSignature());
		}
		if (redex.sites.size() < eta.getPlaceCodomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the redex inner interface.");
		}
		if (reactum.sites.size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the reactum inner interface.");
		}
		if (reactum.sites.size() != eta.getPlaceDomain()) {
			throw new InvalidInstantiationRuleException(
					"The instantiation rule does not match the reactum inner interface.");
		}
		if (redex.roots.size() != reactum.roots.size()
				|| !redex.outers.keySet().containsAll(reactum.outers.keySet())
				|| !reactum.outers.keySet().containsAll(redex.outers.keySet())) {
			throw new IncompatibleInterfaceException(
					"Redex and reactum should have the same outer interface.");
		}
		if (!redex.inners.keySet().containsAll(reactum.inners.keySet())
				|| !reactum.inners.keySet().containsAll(redex.inners.keySet())) {
			throw new IncompatibleInterfaceException(
					"Redex and reactum should have the same outer interface.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;

		this.matcher = (matcher == null) ? BigraphMatcher.DEFAULT : matcher;
	}

	/**
	 * This method is called diring the instantiation of rule's reactum. Inherit
	 * this method to customize instantiation of Nodes e.g. attaching properties
	 * taken from nodes in the redex image determined by the given match.
	 * 
	 * @param original
	 *            The original node from the reactum.
	 * @param instance
	 *            The replica to be used.
	 * @param match
	 *            The match referred by the instantiation.
	 */
	protected void instantiateReactumNode(Node original, Node instance,
			BigraphMatch match) {
	}

	/**
	 * Instantiates rule's reactum with respect to the given match.
	 * 
	 * @param match
	 *            the match with respect to the reactum has to be instantiated.
	 * @return the reactum instance.
	 */
	protected final Bigraph instantiateReactum(BigraphMatch match) {
		Bigraph reactum = getReactum();
		Bigraph big = new Bigraph(reactum.signature);
		Owner owner = big;
		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
		// replicate outer names
		for (EditableOuterName o1 : reactum.outers.values()) {
			EditableOuterName o2 = o1.replicate();
			big.outers.put(o2.getName(), o2);
			o2.setOwner(owner);
			hnd_dic.put(o1, o2);
		}
		// replicate inner names
		for (EditableInnerName i1 : reactum.inners.values()) {
			EditableInnerName i2 = i1.replicate();
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				// the bigraph is inconsistent if g is null
				h2 = h1.replicate();
				h2.setOwner(owner);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			big.inners.put(i2.getName(), i2);
		}
		// replicate place structure
		// the queue is used for a breadth first visit
		class Pair {
			final EditableChild c;
			final EditableParent p;

			Pair(EditableParent p, EditableChild c) {
				this.c = c;
				this.p = p;
			}
		}
		Deque<Pair> q = new ArrayDeque<>();
		for (EditableRoot r1 : reactum.roots) {
			EditableRoot r2 = r1.replicate();
			big.roots.add(r2);
			r2.setOwner(owner);
			for (EditableChild c : r1.getEditableChildren()) {
				q.add(new Pair(r2, c));
			}
		}
		EditableSite[] sites = new EditableSite[reactum.sites.size()];
		while (!q.isEmpty()) {
			Pair t = q.poll();
			if (t.c instanceof EditableNode) {
				EditableNode n1 = (EditableNode) t.c;
				EditableNode n2 = n1.replicate();
				instantiateReactumNode(n1, n2, match);
				// set m's parent (which added adds m as its child)
				n2.setParent(t.p);
				for (int i = n1.getControl().getArity() - 1; 0 <= i; i--) {
					EditablePort p1 = n1.getPort(i);
					EditableHandle h1 = p1.getHandle();
					// looks for an existing replica
					EditableHandle h2 = hnd_dic.get(h1);
					if (h2 == null) {
						// the bigraph is inconsistent if g is null
						h2 = h1.replicate();
						h2.setOwner(owner);
						hnd_dic.put(h1, h2);
					}
					n2.getPort(i).setHandle(h2);
				}
				// enqueue children for visit
				for (EditableChild c : n1.getEditableChildren()) {
					q.add(new Pair(n2, c));
				}
			} else {
				// c instanceof EditableSite
				EditableSite s1 = (EditableSite) t.c;
				EditableSite s2 = s1.replicate();
				s2.setParent(t.p);
				sites[reactum.sites.indexOf(s1)] = s2;
			}
		}
		big.sites.addAll(Arrays.asList(sites));
		return big;
	}

	@Override
	public Bigraph getRedex() {
		return this.redex;
	}

	@Override
	public Bigraph getReactum() {
		return this.reactum;
	}

	@Override
	public InstantiationRule<Bigraph> getInstantiationRule() {
		return this.eta;
	}

	@Override
	public Iterable<Bigraph> apply(Bigraph to) {
		return new RewriteIterable(to);
	}

	private class RewriteIterable implements Iterable<Bigraph> {

		private final Bigraph target;

		private Iterable<? extends BigraphMatch> mAble;

		RewriteIterable(Bigraph target) {
			this.target = target;
		}

		@Override
		public Iterator<Bigraph> iterator() {
			if (mAble == null)
				mAble = matcher.match(target, redex);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<? extends BigraphMatch> mTor = null;
			Iterator<Bigraph> args = null;
			// caches context+redex but not args
			Bigraph big = null;

			@Override
			public boolean hasNext() {
				if (mTor == null)
					mTor = mAble.iterator();
				return mTor.hasNext() || ((args != null) && args.hasNext());
			}

			@Override
			public Bigraph next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				if (args == null || !args.hasNext()) {
					// if (mTor.hasNext()) {
					BigraphMatch match = mTor.next();
					if (DEBUG_PRINT_MATCH)
						System.out.println(match);
					BigraphBuilder bb = new BigraphBuilder(
							instantiateReactum(match), true);
					bb.leftJuxtapose(match.getRedexId(), true);
					bb.outerCompose(match.getContext(), true);
					big = bb.makeBigraph(true);
					args = eta.instantiate(match.getParam()).iterator();
					// }
				}
				if (args.hasNext()) {
					Bigraph result;
					Bigraph params = args.next();
					if (args.hasNext())
						result = Bigraph.compose(big.clone(), params, true);
					else
						result = Bigraph.compose(big, params, true);
					if (DEBUG_PRINT_RESULT)
						System.out.println(result);
					if (DEBUG_CONSISTENCY_CHECK && !result.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph");
					}
					return result;
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("");
			}

		}
	}
}