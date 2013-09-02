package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;
import jlibbig.core.exceptions.*;

public class BigraphInstantiationMap implements InstantiationRule<Bigraph> {
	final private int map[];
	final private int dom;
	final private int cod;

	//final private boolean[] reusable;
	final private int[] multiplicity;

	public BigraphInstantiationMap(int codomain, int... map) {
		dom = map.length;
		cod = codomain--;
		this.map = new int[dom];
		//reusable = new boolean[dom];
		multiplicity = new int[cod];

		for (int i = 0; i < map.length; i++) {
			if (map[i] < 0 || map[i] > codomain) {
				throw new IllegalArgumentException("Invalid image");
			}
			int j = map[i];
			this.map[i] = j;
			//reusable[i] = (0 == mulParam[j]++);
			multiplicity[j]++;
		}
	}

	public int getPlaceDomain() {
		return dom;
	}

	public int getPlaceCodomain() {
		return cod;
	}

	public int getPlaceInstance(int arg) {
		if (-1 < arg && arg < dom) {
			return map[arg];
		} else {
			return -1;
		}
	}

	public boolean isNeeded(int prm) {
		return -1 < prm && prm < cod && multiplicity[prm] > 0;
	}

	@Override
	public Iterable<Bigraph> instantiate(Bigraph parameters) {
		return instantiate(parameters, false);
	}

	Iterable<Bigraph> instantiate(Bigraph parameters, boolean reuse) {
		if (parameters.roots.size() != this.dom) {
			throw new IncompatibleInterfaceException(parameters);
		}
		List<Bigraph> l = new LinkedList<>();
		Bigraph prm = new Bigraph(parameters.signature);
		EditableRoot[] rs = new EditableRoot[dom];
		EditableSite[][] ss = new EditableSite[parameters.sites.size()][];

		Map<Handle, EditableHandle> hnd_dic = new HashMap<>();

		// replicates outers
		for (EditableOuterName o1 : parameters.outers) {
			EditableOuterName o2 = (reuse) ? o1 : o1.replicate();
			prm.outers.add(o2);
			o2.setOwner(prm);
			hnd_dic.put(o1, o2);
		}
		// replicates inners
		for (EditableInnerName i1 : parameters.inners) {
			EditableInnerName i2 = (reuse) ? i1 : i1.replicate();
			EditableHandle h1 = i1.getHandle();
			EditableHandle h2 = hnd_dic.get(h1);
			if (h2 == null) {
				h2 = (reuse) ? h1 : h1.replicate();
				h2.setOwner(prm);
				hnd_dic.put(h1, h2);
			}
			i2.setHandle(h2);
			prm.inners.add(i2);
		}
		// descends and replicate the place graph (@see Bigraph.clone except for
		// multiplicity)
		class VState {
			final EditableChild c;
			final EditableParent[] ps;
			final int r;

			VState(int r, EditableParent[] ps, EditableChild c) {
				this.c = c;
				this.ps = ps;
				this.r = r;
			}
		}
		Queue<VState> q = new LinkedList<>();
		for (int j = 0; j < map.length; j++) {
			EditableRoot r1 = parameters.roots.get(map[j]);
			EditableRoot[] r2s = new EditableRoot[multiplicity[j]];
			int k = 0;
			for (int i = 0; i < rs.length; i++) {
				r2s[k] = rs[i] = (reuse && k > 0) ? r1 : r1.replicate();
				k++;
			}
			for (EditableChild c : r1.getEditableChildren()) {
				q.add(new VState(j, r2s, c));
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
					for (int i = 0; i < n2s.length; i++) {
						n2s[i].getPort(j).setHandle(h2);
					}
				}
				for (EditableChild c : n1.getEditableChildren()) {
					q.add(new VState(s.r, n2s, c));
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
		// set place interfaces
		for (int i = 0; i < rs.length; i++) {
			prm.roots.add(rs[i]);
			rs[i].setOwner(prm);
		}
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] == null)
				continue;
            prm.sites.addAll(Arrays.asList(ss[i]));
		}

		if (reuse) {
			parameters.sites.clear();
			parameters.roots.clear();
			parameters.inners.clear();
			parameters.outers.clear();
		}

		l.add(prm);
		return l;
	}
}
