package it.uniud.mads.jlibbig.core.ldb;

import java.util.*;

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableInPort;
import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableOutPort;
import it.uniud.mads.jlibbig.core.attachedProperties.*;

/**
 * A rewriting rule is a reaction rule described by means of rewrites i.e. the
 * reaction is a substitution of an occurrence (in the bigraph to which the rule
 * is applied) of the rule's redex with the rule's reactum. Redex and reactum of
 * a rewriting rule are described by means of two bigraphs ({@link #getRedex()}
 * and {@link #getReactum()}). Occurrences are described by matches (cf.
 * {@link DirectedMatch}) i.e. triples like &lt;C,F,P&gt; where F is the juxtaposition
 * of is the redex occurrence R and some suitable identity. Then R is replaced
 * by the reactum R' and the parameter P is instantiated to P' in order to match
 * R' inner interface. Parameter instantiation is handled by instantiation rule
 * returned by {@link #getInstantiationRule()}.
 * 
 * Matches are computed by means of {@link DirectedMatcher} but the class allows
 * to provide different matchers.
 * 
 * During the rewrite, the reactum is instantiated by standard replication.
 * Non-replicating attached properties such as {@link SimpleProperty} may be
 * lost in the process; consider the use of self-replicating ones such as
 * {@link ReplicatingProperty} or {@link SharedProperty}. Self-replicating
 * properties may suit most scenarios but may be handy to intercept rectum node
 * instantiation e.g. for adding or changing properties. This can be achieved by
 * inheriting the method
 * {@link #instantiateReactumNode}.
 * 
 * //@see AgentRewritingRule
 */
public class DirectedRewritingRule implements 
		it.uniud.mads.jlibbig.core.DirectedRewritingRule<DirectedBigraph, DirectedBigraph> {

	private final static boolean DEBUG = Boolean
			.getBoolean("it.uniud.mads.jlibbig.debug")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.debug.reactions");
	private final static boolean DEBUG_PRINT_MATCH = DEBUG;
	private final static boolean DEBUG_PRINT_RESULT = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean
			.getBoolean("it.uniud.mads.jlibbig.consistency")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.reactions");

	final DirectedBigraph redex;
	final DirectedBigraph reactum;
	final DirectedInstantiationMap eta;

	private DirectedMatcher matcher;

	public DirectedRewritingRule(DirectedBigraph redex, DirectedBigraph reactum, int... eta) {
		this(DirectedMatcher.DEFAULT, redex, reactum,
				new DirectedInstantiationMap(redex.sites.size(), eta));
	}

	public DirectedRewritingRule(DirectedMatcher matcher, DirectedBigraph redex,
			DirectedBigraph reactum, int... eta) {
		this(matcher, redex, reactum, new DirectedInstantiationMap(
				redex.sites.size(), eta));
	}

	public DirectedRewritingRule(DirectedBigraph redex, DirectedBigraph reactum,
			DirectedInstantiationMap eta) {
		this(DirectedMatcher.DEFAULT, redex, reactum, eta);
	}

	public DirectedRewritingRule(DirectedMatcher matcher, DirectedBigraph redex,
			DirectedBigraph reactum, DirectedInstantiationMap eta) {
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
		
		Set<String> xs = new HashSet<>(reactum.outers.keySet());
		Set<String> ys = new HashSet<>(redex.outers.keySet());
		Set<String> zs = new HashSet<>(xs);
		xs.removeAll(ys);
		ys.removeAll(zs);
						
		if (redex.roots.size() != reactum.roots.size()
				|| !xs.isEmpty() || !ys.isEmpty()) {
			throw new IncompatibleInterfaceException(
					"Redex and reactum should have the same outer interface.");
		}
		
		xs = new HashSet<>(reactum.inners.keySet());
		ys = new HashSet<>(redex.inners.keySet());
		zs = new HashSet<>(xs);
		xs.removeAll(ys);
		ys.removeAll(zs);
		
		if (!xs.isEmpty() || !ys.isEmpty()) {
			throw new IncompatibleInterfaceException(
					"Redex and reactum should have the same set inner names.");
		}
		this.redex = redex;
		this.reactum = reactum;
		this.eta = eta;

		this.matcher = (matcher == null) ? DirectedMatcher.DEFAULT : matcher;
	}

	/**
	 * This method is called during the instantiation of rule's reactum. Inherit
	 * this method to customise instantiation of Nodes e.g. attaching properties
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
			DirectedMatch match) {
	}

	/**
	 * Instantiates rule's reactum with respect to the given match.
	 * 
	 * @param match
	 *            the match with respect to the reactum has to be instantiated.
	 * @return the reactum instance.
	 */
	protected final DirectedBigraph instantiateReactum(DirectedMatch match) {
		DirectedBigraph reactum = getReactum();
		DirectedBigraph big = new DirectedBigraph(reactum.signature);
		Owner owner = big;
		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
		Map<EditablePoint, EditablePoint> pnt_dic = new HashMap<>();
		// TODO: account for locality when cloning
		// replicate upward outer names
		for (EditableOuterName o1 : reactum.outers.getAsc().values()) {
			EditableOuterName o2 = o1.replicate();
			big.outers.addAsc(0, o2);
			o2.setOwner(owner);
			hnd_dic.put(o1, o2);
		}
		// replicate upward inner names
		for (EditableInnerName i1 : reactum.inners.getAsc().values()) {
			EditableInnerName i2 = i1.replicate();
			pnt_dic.put(i1, i2);
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				// the bigraph is inconsistent if g is null
				h2 = h1.replicate();
				h2.setOwner(owner);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			big.inners.addAsc(0, i2);
		}
		// replicate downward inner names
		for (EditableOuterName o1 : reactum.inners.getDesc().values()) {
			EditableOuterName o2 = o1.replicate();
			big.inners.addDesc(0, o2);
			o2.setOwner(owner);
			hnd_dic.put(o1, o2);
		}
		// replicate downward outer names
		for (EditableInnerName i1 : reactum.outers.getDesc().values()) {
			EditableInnerName i2 = i1.replicate();
			pnt_dic.put(i1, i2);
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				// the bigraph is inconsistent if g is null
				h2 = h1.replicate();
				h2.setOwner(owner);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			big.outers.addDesc(0, i2);
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
				for (int i = n1.getControl().getArityOut() - 1; 0 <= i; i--) {
					EditableOutPort p1 = n1.getOutPort(i);
					EditableHandle h1 = p1.getHandle();
					// looks for an existing replica
					EditableHandle h2 = hnd_dic.get(h1);
					if (h2 == null) {
						// the bigraph is inconsistent if g is null
						h2 = h1.replicate();
						h2.setOwner(owner);
						hnd_dic.put(h1, h2);
					}
					n2.getOutPort(i).setHandle(h2);
				}
				for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
					EditableNode.EditableInPort ip1 = n1.getInPort(i);
					EditableHandle h2 = hnd_dic.get(ip1);
					EditableNode.EditableInPort ip2 = n2.getInPort(i);
					ip2.setOwner(owner);
					for (InnerName i1 : reactum.outers.getDesc().values()) {
						EditablePoint pnt = pnt_dic.get(i1);
						if (pnt != null && ip1.getEditablePoints().contains(i1)) {
							pnt.setHandle(ip2);
						}
					}
					for (InnerName i1 : reactum.inners.getAsc().values()) {
						EditablePoint pnt = pnt_dic.get(i1);
						if (pnt != null && ip1.getEditablePoints().contains(i1)) {
							pnt.setHandle(ip2);
						}
					}
					hnd_dic.put(ip1, ip2);
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
	public DirectedBigraph getRedex() {
		return this.redex;
	}

	@Override
	public DirectedBigraph getReactum() {
		return this.reactum;
	}

	@Override
	public DirectedInstantiationMap getInstantiationRule() {
		return this.eta;
	}
	
	@Override
	public Iterable<DirectedBigraph> apply(DirectedBigraph to) {
		return this.apply(this.getMatcher(),to);
	}

	public Iterable<DirectedBigraph> apply(DirectedMatcher m, DirectedBigraph to) {
		return new RewriteIterable(m,to);
	}
	
	public DirectedMatcher getMatcher(){
		return this.matcher;
	}

	public void setMatcher(DirectedMatcher m){
		this.matcher = m;
	}
	
	private class RewriteIterable implements Iterable<DirectedBigraph> {

		private final DirectedMatcher matcher;
		
		private final DirectedBigraph target;

		private Iterable<? extends DirectedMatch> mAble;

		RewriteIterable(DirectedMatcher m, DirectedBigraph target) {
			this.target = target;
			this.matcher = m;
		}

		@Override
		public Iterator<DirectedBigraph> iterator() {
			if (mAble == null)
				mAble = this.matcher.match(target, redex);
			return new RewriteIterator();
		}

		private class RewriteIterator implements Iterator<DirectedBigraph> {

			Iterator<? extends DirectedMatch> mTor = null;
			Iterator<DirectedBigraph> args = null;
			// caches context+redex but not args
			DirectedBigraph big = null;

			@Override
			public boolean hasNext() {
				if (mTor == null)
					mTor = mAble.iterator();
				return mTor.hasNext() || ((args != null) && args.hasNext());
			}

			@Override
			public DirectedBigraph next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				if (args == null || !args.hasNext()) {
					// if (mTor.hasNext()) {
					DirectedMatch match = mTor.next();
					if (DEBUG_PRINT_MATCH)
						System.out.println(match);
					DirectedBigraphBuilder bb = new DirectedBigraphBuilder(
							instantiateReactum(match), true);
					bb.leftJuxtapose(match.getRedexId(), true);
					bb.outerCompose(match.getContext(), true);
					big = bb.makeBigraph(true);
					args = eta.instantiate(match.getParam()).iterator();
					// }
				}
				if (args.hasNext()) {
					DirectedBigraph result;
					DirectedBigraph params = args.next();
					if (args.hasNext())
						result = DirectedBigraph.compose(big.clone(), params, true);
					else
						result = DirectedBigraph.compose(big, params, true);
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
