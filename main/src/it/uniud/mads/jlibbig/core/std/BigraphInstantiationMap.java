package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.InstantiationRule;
import it.uniud.mads.jlibbig.core.exceptions.*;
import it.uniud.mads.jlibbig.core.std.EditableNode.EditablePort;

/**
 * Objects created from this class are instantiation rules for bigraphs which
 * are maps from the reactum sites to the redex ones describing how parameters
 * (for the fixed width) are instantiated.
 * 
 * @see BigraphRewritingRule
 */
public class BigraphInstantiationMap implements InstantiationRule<Bigraph> {

	// private final static boolean DEBUG = true;
	private final static boolean DEBUG_CONSISTENCY_CHECK = true;

	final private int map[];
	final private int dom;
	final private int cod;

	final private int[] multiplicity;

	public BigraphInstantiationMap(int codomain, int... map) {
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
	public Iterable<Bigraph> instantiate(Bigraph parameters) {
		return instantiate(parameters, false);
	}

	Iterable<Bigraph> instantiate(Bigraph parameters, boolean reuse) {
		if (parameters.roots.size() < this.cod) {
			throw new IncompatibleInterfaceException();
		}
		List<Bigraph> l = new LinkedList<>();
		Bigraph prm = new Bigraph(parameters.signature);
		EditableRoot[] rs = new EditableRoot[dom];
		EditableSite[][] ss = new EditableSite[parameters.sites.size()][];

		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();

		// replicates outers
		for (EditableOuterName o1 : parameters.outers.values()) {
			EditableOuterName o2 = (reuse) ? o1 : o1.replicate();
			prm.outers.put(o2.getName(), o2);
			o2.setOwner(prm);
			hnd_dic.put(o1, o2);
		}
		// replicates inners
		for (EditableInnerName i1 : parameters.inners.values()) {
			EditableInnerName i2 = (reuse) ? i1 : i1.replicate();
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				h2 = (reuse) ? h1 : h1.replicate();
				h2.setOwner(prm);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			prm.inners.put(i2.getName(), i2);
		}
		// descends and replicate the place graph (@see Bigraph.clone except for
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
				EditableRoot r1 = parameters.roots.get(map[j]);
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
				for (int j = n1.getControl().getArity() - 1; 0 <= j; j--) {
					EditablePort p1 = n1.getPort(j);
					EditableHandle h1 = p1.getHandle();
					EditableHandle h2 = hnd_dic.get(h1);
					if (h2 == null) {
						h2 = (reuse) ? h1 : h1.replicate();
						h2.setOwner(prm);
						hnd_dic.put(h1, h2);
					}
					for (EditableNode n2 : n2s)
						n2.getPort(j).setHandle(h2);
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
			parameters.inners.clear();
			parameters.outers.clear();
		}
		if (DEBUG_CONSISTENCY_CHECK && !prm.isConsistent()) {
			throw new RuntimeException("Inconsistent bigraph");
		}
		l.add(prm);
		return l;
	}
}
