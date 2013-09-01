package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;
import jlibbig.core.exceptions.*;

public class BigraphRewritingRule implements RewritingRule<Bigraph> {
	final Bigraph redex;
	final Bigraph reactum;
	final BigraphInstantiationMap eta;
		
	public BigraphRewritingRule(Bigraph redex, Bigraph reactum, int... eta) {
		this(redex, reactum, new BigraphInstantiationMap(redex.sites.size(),
				eta));
	}

	public BigraphRewritingRule(Bigraph redex, Bigraph reactum,
			BigraphInstantiationMap eta) {
		if (reactum.getSignature() != redex.getSignature()) {
			throw new IncompatibleSignatureException(reactum.getSignature(),
					redex.getSignature(),
					"Redex and reactum should have the same singature.");
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
				|| !redex.outers.containsAll(reactum.outers)
				|| !reactum.outers.containsAll(redex.outers)) {
			throw new IncompatibleInterfacesException(redex, reactum,
					"Redex and reactum should have the same outer interface.");
		}
		if (!redex.inners.containsAll(reactum.inners)
				|| !reactum.inners.containsAll(redex.inners)) {
			throw new IncompatibleInterfacesException(redex, reactum,
					"Redex and reactum should have the same outer interface.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;
		
	}
	
	/**
	 * This method is called diring the instantiation of rule's reactum. Inherit
	 * this method to customize instantiation of Nodes e.g. attaching properties
	 * taken from nodes in the redex image determined by the given match.
	 * 
	 * @param original The original node from the reactum.
	 * @param instance The replica to be used.
	 * @param match The match referred by the instantiation.
	 */
	protected void instantiateReactumNode(Node original, Node instance, BigraphMatch match){}

	/**
	 * Instantiates rule's reactum with respect to the given match.
	 * @param match
	 * @return
	 */
	protected final Bigraph instantiateReactum(BigraphMatch match){
		Bigraph reactum = getReactum();
		Bigraph big = new Bigraph(reactum.signature);
		Owner owner = big;
		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
		// replicate outer names
		for (EditableOuterName o1 : reactum.outers) {
			EditableOuterName o2 = o1.replicate();
			big.outers.add(o2);
			o2.setOwner( owner);
			hnd_dic.put(o1, o2);
		}
		// replicate inner names
		for (EditableInnerName i1 : reactum.inners) {
			EditableInnerName i2 = i1.replicate();
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				// the bigraph is inconsistent if g is null
				h2 = h1.replicate();
				h2.setOwner( owner);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			big.inners.add(i2);
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
		Queue<Pair> q = new LinkedList<>();
		for (EditableRoot r1 : reactum.roots) {
			EditableRoot r2 = r1.replicate();
			big.roots.add(r2);
			r2.setOwner( owner);
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
				instantiateReactumNode(n1,n2,match);
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
						h2.setOwner( owner);
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
		for (int i = 0; i < sites.length; i++) {
			big.sites.add(sites[i]);
		}
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
				mAble = BigraphMatcher.DEFAULT.match(target, redex);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<Bigraph> {

			Iterator<? extends BigraphMatch> matches = null;
			Iterator<Bigraph> args = null;
			Bigraph big = null;

			@Override
			public boolean hasNext() {
				if (matches == null)
					matches = mAble.iterator();
				return matches.hasNext() || ((args != null) && args.hasNext());
			}

			@Override
			public Bigraph next() {
				if (hasNext()) {
					if (args == null) {
						BigraphMatch match = matches.next();
						big = Bigraph.compose(match.getContext(),
								instantiateReactum(match), true);
						args = eta.instantiate(match.getParam()).iterator();
					}
					if (args.hasNext()) {
						Bigraph params = args.next();
						if (args.hasNext())
							return Bigraph.compose(big.clone(), params, true);
						else
							return Bigraph.compose(big, params, true);
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("");
			}

		}
	}
}