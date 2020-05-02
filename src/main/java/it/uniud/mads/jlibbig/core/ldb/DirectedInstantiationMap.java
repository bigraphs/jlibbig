package it.uniud.mads.jlibbig.core.ldb;

import java.util.*;

import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableInPort;
import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableOutPort;

/**
 * Objects created from this class are instantiation rules for bigraphs which
 * are maps from the reactum sites to the redex ones describing how parameters
 * (for the fixed width) are instantiated.
 * 
 * @see DirectedRewritingRule
 */
public class DirectedInstantiationMap implements 
		it.uniud.mads.jlibbig.core.DirectedInstantiationRule<DirectedBigraph> {

	private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean
			.getBoolean("it.uniud.mads.jlibbig.consistency")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.reactions");

	final private int map[];
	final private int dom;
	final private int cod;

	final private int[] multiplicity;

	public DirectedInstantiationMap(int codomain, int... map) {
		dom = map.length;
		cod = codomain--;
		this.map = new int[dom];
		// reusable = new boolean[dom];
		multiplicity = new int[cod];

		for (int i = 0; i < map.length; i++) {
			if (map[i] < 0 || map[i] > codomain) {
				throw new IllegalArgumentException("Invalid image");
			}
			int j = map[i];
			this.map[i] = j;
			// reusable[i] = (0 == mulParam[j]++);
			multiplicity[j]++;
		}
	}

	/**
	 * Gets the domain of the instantiation map which is the number of rectum
	 * sites or equivalently the width of the instantiated parameter.
	 * 
	 * @return the number of sites in the reactum.
	 */
	public int getPlaceDomain() {
		return dom;
	}

	/**
	 * Gets the codomain of the instantiation map which is the number of redex
	 * sites or equivalently the width of the parameter before it is
	 * instantiated.
	 * 
	 * @return the number of sites in the reactum.
	 */
	public int getPlaceCodomain() {
		return cod;
	}

	/**
	 * Gets, for every site in the reactum, its image under the instantiation map.
	 * 
	 * @param arg the position of the site in the reactum.
	 * @return the position of the site in the redex.
	 */
	public int getPlaceInstance(int arg) {
		if (-1 < arg && arg < dom) {
			return map[arg];
		} else {
			return -1;
		}
	}

	/**
	 * Tells if a site is needed i.e. if it is the image of some site from the reactum
	 * w.r.t. the instantiation map.
	 * 
	 * @param prm the redex site.
	 * @return a boolean indicating whether the given site is the image of something
	 * under the instantiation map.
	 */
	public boolean isNeeded(int prm) {
		return -1 < prm && prm < cod && multiplicity[prm] > 0;
	}

	@Override
	public Iterable<DirectedBigraph> instantiate(DirectedBigraph parameters) {
		return instantiate(parameters, false);
	}

	Iterable<DirectedBigraph> instantiate(DirectedBigraph parameters, boolean reuse) {
		if (parameters.roots.size() < this.cod) {
			throw new IncompatibleInterfaceException();
		}
		List<DirectedBigraph> l = new LinkedList<>();
		DirectedBigraph prm = new DirectedBigraph(parameters.signature);
		EditableRoot[] rs = new EditableRoot[dom];
		EditableSite[][] ss = new EditableSite[parameters.sites.size()][];

		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
		Map<EditablePoint, EditablePoint> pnt_dic = new HashMap<>();

		// replicates ascending outers
		for (EditableOuterName o1 : parameters.outers.getAsc().values()) {
			EditableOuterName o2 = (reuse) ? o1 : o1.replicate();
			prm.outers.addAsc(0, o2);
			o2.setOwner(prm);
			hnd_dic.put(o1, o2);
		}
		// replicates ascending inners
		for (EditableInnerName i1 : parameters.inners.getAsc().values()) {
			EditableInnerName i2 = (reuse) ? i1 : i1.replicate();
			pnt_dic.put(i1, i2);
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				h2 = (reuse) ? h1 : h1.replicate();
				h2.setOwner(prm);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			prm.inners.addAsc(0, i2);
		}
		// replicates descending inners
		for (EditableOuterName o1 : parameters.inners.getDesc().values()) {
			EditableOuterName o2 = (reuse) ? o1 : o1.replicate();
			prm.inners.addDesc(0, o2);
			o2.setOwner(prm);
			hnd_dic.put(o1, o2);
		}
		// replicates descending outers
		for (EditableInnerName i1 : parameters.outers.getDesc().values()) {
			EditableInnerName i2 = (reuse) ? i1 : i1.replicate();
			pnt_dic.put(i1, i2);
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				h2 = (reuse) ? h1 : h1.replicate();
				h2.setOwner(prm);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			prm.outers.addDesc(0, i2);
		}
		// descends and replicate the place graph (@see DirectedBigraph.clone except for
		// multiplicity)
		class VState {
			final EditableChild c;
			final EditableParent[] ps;

			VState(EditableParent[] ps, EditableChild c) {
				this.c = c;
				this.ps = ps;
			}
		}
		Deque<VState> q = new ArrayDeque<>();

		for (int j = 0; j < cod; j++) {
			int m = multiplicity[j];
			if (m > 0) {
				EditableRoot r1 = parameters.roots.get(j);
				EditableRoot[] r2s = new EditableRoot[m];
				int k = 0;
				for (int i = 0; i < dom; i++) {
					if (map[i] == j) {
						r2s[k] = rs[i] = (reuse && k > 0) ? r1 : r1.replicate();
						k++;
					}
				}
				for (EditableChild c : r1.getEditableChildren()) {
					q.add(new VState(r2s, c));
				}
			}
		}
		while (!q.isEmpty()) {
			VState s = q.poll();
			if (s.c.isNode()) {
				EditableNode n1 = (EditableNode) s.c;
				EditableNode[] n2s = new EditableNode[s.ps.length];
				for (int i = 0; i < n2s.length; i++) {
					n2s[i] = (reuse && i > 0) ? n1 : n1.replicate();
					n2s[i].setParent(s.ps[i]);
				}
				for (int j = n1.getControl().getArityOut() - 1; 0 <= j; j--) {
					EditableOutPort p1 = n1.getOutPort(j);
					EditableHandle h1 = p1.getHandle();
					EditableHandle h2 = hnd_dic.get(h1);
					if (h2 == null) {
						h2 = (reuse) ? h1 : h1.replicate();
						h2.setOwner(prm);
						hnd_dic.put(h1, h2);
					}
					for (EditableNode n2 : n2s)
						n2.getOutPort(j).setHandle(h2);
				}
				for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
					EditableNode.EditableInPort ip1 = n1.getInPort(i);
					EditableHandle h2 = hnd_dic.get(ip1);
					if (h2 == null) {
						for (EditableNode n2 : n2s) {
							EditableNode.EditableInPort ip2 = n2.getInPort(i);
							hnd_dic.put(ip1, ip2);
							for (InnerName i1 : parameters.outers.getDesc().values()) {
								EditablePoint pnt = pnt_dic.get(i1);
								if (pnt != null) {
									pnt.setHandle(ip2);
								}
							}
						}
					}
				}

				for (EditableChild c : n1.getEditableChildren()) {
					q.add(new VState(n2s, c));
				}
			} else {
				EditableSite s1 = (EditableSite) s.c;
				EditableSite[] s2s = new EditableSite[s.ps.length];
				ss[parameters.sites.indexOf(s1)] = s2s;
				for (int i = 0; i < s2s.length; i++) {
					s2s[i] = (reuse && i > 0) ? s1 : s1.replicate();
				}
			}
		}
		for (EditableRoot r : rs) {
			prm.roots.add(r);
			r.setOwner(prm);
		}
		for (EditableSite[] s : ss) {
			if (s == null)
				continue;
			prm.sites.addAll(Arrays.asList(s));
		}

		if (reuse) {
			parameters.sites.clear();
			parameters.roots.clear();
			parameters.inners.names.clear();
			parameters.outers.names.clear();
		}
		if (DEBUG_CONSISTENCY_CHECK && !prm.isConsistent()) {
			throw new RuntimeException("Inconsistent bigraph");
		}
		l.add(prm);
		return l;
	}
}

