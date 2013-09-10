package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;
import jlibbig.core.abstractions.Matcher;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

public class AgentMatcher implements Matcher<Bigraph, Bigraph> {

	private final static boolean DEBUG = false;
	private final static boolean DEBUG_PRINT_CSP_SOLUTIONS = DEBUG;
	private final static boolean DEBUG_PRINT_SOLUTION_FETCH = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = true;

	/**
	 * The default instance of the macher.
	 */
	public final static AgentMatcher DEFAULT = new AgentMatcher();

	@Override
	public Iterable<? extends AgentMatch> match(Bigraph agent, Bigraph redex) {
		return this.match(agent, redex, null);
	}

	Iterable<? extends AgentMatch> match(Bigraph agent, Bigraph redex,
			boolean... neededParams) {
		return new MatchIterable(agent, redex, neededParams);
	}

	protected boolean areMatchable(Bigraph agent, Node fromAgent,
			Bigraph redex, Node fromRedex) {
		return fromAgent.getControl().equals(fromRedex.getControl());
	}

	private class MatchIterable implements Iterable<AgentMatch> {

		final Bigraph agent, redex;

		boolean agent_ancestors_is_empty = true;
		final Map<Node, Collection<Parent>> agent_ancestors;

		// caches some collections of entities (e.g. nodes and edges are
		// computed on the fly)
		final List<? extends Root> agent_roots;
		// final List<? extends Site> agent_sites;
		final Collection<? extends Node> agent_nodes;
		final Collection<Port> agent_ports;
		final Collection<? extends Edge> agent_edges;
		final List<Handle> agent_handles; // simplifies some constraints for
											// f_vars

		final List<? extends Root> redex_roots;
		final List<? extends Site> redex_sites;
		final Collection<? extends Node> redex_nodes;
		final Collection<Point> redex_points;
		final Collection<? extends Edge> redex_edges;
		final List<Handle> redex_handles;

		final boolean[] neededParam;

		final int ars, ans, ahs, rrs, rns, rss, rhs;

		private MatchIterable(Bigraph agent, Bigraph redex,
				boolean[] neededParams) {
			if (!agent.isGround()) {
				throw new UnsupportedOperationException(
						"Agent should be a bigraph with empty inner interface i.e. ground.");
			}
			if (!agent.signature.equals(redex.signature)) {
				throw new UnsupportedOperationException(
						"Agent and redex should have the same singature.");
			}
			this.agent = agent;
			this.redex = redex;

			this.agent_roots = agent.getRoots();
			this.agent_nodes = agent.getNodes();
			this.agent_edges = agent.getEdges(agent_nodes);
			this.agent_handles = new LinkedList<Handle>(agent_edges);
			agent_handles.addAll(agent.getOuterNames());

			ars = agent_roots.size();
			ans = agent_nodes.size();
			ahs = agent_handles.size();

			this.agent_ports = new HashSet<Port>(ans);
			for (Node n : agent_nodes) {
				agent_ports.addAll(n.getPorts());
			}

			this.agent_ancestors = new HashMap<>(ans);

			this.redex_roots = redex.getRoots();
			this.redex_sites = redex.getSites();
			this.redex_nodes = redex.getNodes();
			this.redex_edges = redex.getEdges(redex_nodes);
			this.redex_handles = new LinkedList<Handle>(redex_edges);
			redex_handles.addAll(redex.getOuterNames());

			rrs = redex_roots.size();
			rns = redex_nodes.size();
			rss = redex_sites.size();
			rhs = redex_handles.size();

			this.redex_points = new HashSet<>(rns);
			for (Node n : redex_nodes) {
				redex_points.addAll(n.getPorts());
			}
			redex_points.addAll(redex.getInnerNames());

			this.neededParam = new boolean[rss];
			for (int i = 0; i < this.neededParam.length; i++) {
				this.neededParam[i] = (neededParams == null) || neededParams[i];
			}
		}

		@Override
		public Iterator<AgentMatch> iterator() {
			return new MatchIterator();
		}

		private class MatchIterator implements Iterator<AgentMatch> {

			private boolean mayHaveNext = true;

			private AgentMatch nextMatch = null;

			final CPModel model;
			final CPSolver solver;
			// variables for the place embedding problem
			final Map<PlaceEntity, Map<PlaceEntity, IntegerVariable>> p_vars;
			// variables for the multiflux problem desrcibing the link embedding
			final Map<LinkEntity, Map<LinkEntity, IntegerVariable>> e_vars;
			// variables for flux separation implicitly describing the handles
			// embedding
			final Map<Handle, Map<Handle, IntegerVariable>> f_vars;

			MatchIterator() {

				// MODEL
				// ///////////////////////////////////////////////////////////
				this.model = new CPModel();

				// the following variables are indexed over pairs where the
				// first entity is from the agent and the second from the redex
				p_vars = new IdentityHashMap<>(ars + ans);
				// these are indexed by redex handles and then by agent handles
				f_vars = new IdentityHashMap<>(rhs);
				// these are indexed from the source to target of the flux
				e_vars = new IdentityHashMap<>(ahs * rhs + agent_ports.size()
						* (1 + redex_points.size()));

				// int ki = 0, kj = 0, kk = 0;
				// IntegerExpressionVariable[] vars1, vars2;

				{
					int ki = 0;
					for (Root i : agent_roots) {
						int kj = 0;
						Map<PlaceEntity, IntegerVariable> row = new HashMap<>(
								rrs + rns + rss);
						for (Root j : redex_roots) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							row.put(j, var);
						}
						// 1 // these will always be zero
						for (Node j : redex_nodes) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							model.addConstraint(Choco.eq(0, var));
							row.put(j, var);
						}
						for (Site j : redex_sites) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							model.addConstraint(Choco.eq(0, var));
							row.put(j, var);
						}
						p_vars.put(i, row);
						ki++;
					}
					for (Node i : agent_nodes) {
						int kj = 0;
						Map<PlaceEntity, IntegerVariable> row = new HashMap<>(
								rrs + rns + rss);
						for (Root j : redex_roots) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							row.put(j, var);
						}
						for (Node j : redex_nodes) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							row.put(j, var);
						}
						for (Site j : redex_sites) {
							IntegerVariable var = Choco.makeBooleanVar(ki + ","
									+ kj++);
							model.addVariable(var);
							row.put(j, var);
						}
						p_vars.put(i, row);
						ki++;
					}
				}

				{
					int ki = 0;
					for (Handle hr : redex_handles) {
						int kj = 0;
						Map<Handle, IntegerVariable> row = new IdentityHashMap<>(
								ahs);
						for (Handle ha : agent_handles) {
							IntegerVariable var = Choco.makeBooleanVar("F_"
									+ ki + "_" + kj++);
							model.addVariable(var);
							row.put(ha, var);
						}
						f_vars.put(hr, row);
						ki++;
					}

					// vars for agent port flux
					ki = 0;
					for (Port pi : agent_ports) {
						int kj = 0;
						Map<LinkEntity, IntegerVariable> row = new IdentityHashMap<>(
								redex_points.size() + 1);
						Handle hi = pi.getHandle();
						IntegerVariable var = Choco.makeBooleanVar("PH_" + ki);
						row.put(hi, var);
						for (Point pj : redex_points) {
							var = Choco.makeBooleanVar("PP_" + ki + "_" + kj++);
							model.addVariable(var);
							row.put(pj, var);
						}
						e_vars.put(pi, row);
						ki++;
					}

					// vars for redex handles flux
					ki = 0;
					for (Handle hj : redex_handles) {
						int kj = 0;
						Map<LinkEntity, IntegerVariable> row = new IdentityHashMap<>(
								ahs);
						for (Handle hi : agent_handles) {
							IntegerVariable var = new IntegerVariable("HH_"
									+ ki + "_" + kj++, 0, hi.getPoints().size());
							model.addVariable(var);
							row.put(hi, var);
						}
						e_vars.put(hj, row);
						ki++;
					}
				}

				// PLACE CONSTRAINTS //////////////////////////////////////////

				// 2 // M_ij = 0 if nodes are different in the sense of this.eq
				{
					for (Node i : agent_nodes) {
						Map<PlaceEntity, IntegerVariable> row = p_vars.get(i);
						for (Node j : redex_nodes) {
							IntegerVariable var = row.get(j);
							if (!areMatchable(agent, i, redex, j)) {
								model.addConstraint(Choco.eq(0, var));
							}
						}
					}
				}
				// ////////////////////////////////////////////////////////////

				// 3 // M_ij <= M_fg if f = prnt(i) and g = prnt(j)
				{
					for (Node i : agent_nodes) {
						Parent f = i.getParent();
						Map<PlaceEntity, IntegerVariable> i_row = p_vars.get(i);
						Map<PlaceEntity, IntegerVariable> f_row = p_vars.get(f);
						for (Child j : redex_nodes) {
							Parent g = j.getParent();
							model.addConstraint(Choco.leq(i_row.get(j),
									f_row.get(g)));
						}
						for (Child j : redex_sites) {
							Parent g = j.getParent();
							model.addConstraint(Choco.leq(i_row.get(j),
									f_row.get(g)));
						}
					}
				}
				// ////////////////////////////////////////////////////////////

				// 4 // M_ij = 0 if j is a root and i is not in an active
				// context //

				{
					/*
					 * Descends the agent parent map deactivating matching (with
					 * redex roots) below every passive node. Nodes in qa were
					 * found in an active context whereas qp are in a passive
					 * one or passive.
					 */
					Queue<Node> qa = new LinkedList<>();
					Queue<Node> qp = new LinkedList<>();
					for (Root r : agent_roots) {
						for (Child c : r.getChildren()) {
							if (c.isNode()) {
								qa.add((Node) c);
							}
						}
					}
					while (!qa.isEmpty()) {
						Node n = qa.poll();
						if (n.getControl().isActive()) {
							for (Child c : n.getChildren()) {
								if (c.isNode()) {
									qa.add((Node) c);
								}
							}
						} else {
							qp.add(n);
						}
					}
					qa.clear();
					Constraint[] cs = new Constraint[rrs];
					while (!qp.isEmpty()) {
						Node i = qp.poll();
						for (Child c : i.getChildren()) {
							if (c.isNode()) {
								qp.add((Node) c);
							}
						}
						Map<PlaceEntity, IntegerVariable> row = p_vars.get(i);
						int k = 0;
						for (Root j : redex_roots) {
							cs[k++] = Choco.eq(0, row.get(j));
						}
						model.addConstraints(cs);
					}
				}

				// /////////////////////////////////////////////////////////////////

				// 5 // sum M_ij = 1 if j not in sites
				{
					IntegerVariable[] vars = new IntegerVariable[ars + ans];
					for (Root j : redex_roots) {
						int k = 0;
						for (PlaceEntity i : p_vars.keySet()) {
							vars[k++] = p_vars.get(i).get(j);
						}
						model.addConstraint(Choco.eq(1, Choco.sum(vars)));
					}
					for (Node j : redex_nodes) {
						int k = 0;
						// TODO roots can be skipped since these are always zero
						for (Parent i : agent_roots) {
							vars[k++] = p_vars.get(i).get(j);
						}
						for (Parent i : agent_nodes) {
							vars[k++] = p_vars.get(i).get(j);
						}
						model.addConstraint(Choco.eq(1, Choco.sum(vars)));
					}
				}
				// //////////////////////////////////////////////////////////////////

				// 6 // n sum(j not root) M_ij + sum(j root) M_ij <= n if i in
				// nodes
				{
					for (Node i : agent_nodes) {
						Map<PlaceEntity, IntegerVariable> row = p_vars.get(i);
						IntegerVariable[] vars = new IntegerVariable[rns + rss];
						int k = 0;
						for (PlaceEntity j : redex_nodes) {
							vars[k++] = row.get(j);
						}
						for (PlaceEntity j : redex_sites) {
							vars[k++] = row.get(j);
						}
						IntegerExpressionVariable c = Choco.mult(rrs,
								Choco.sum(vars));

						vars = new IntegerVariable[rrs];
						k = 0;
						for (Root j : redex_roots) {
							vars[k++] = row.get(j);
						}
						model.addConstraint(Choco.geq(rrs,
								Choco.sum(c, Choco.sum(vars))));
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 7 // |chld(f)| M_fg <= sum(i chld(f), j in chld(g)) M_ij if
				// f,g in nodes
				{
					for (Parent f : agent_nodes) {
						Collection<? extends Child> cf = f.getChildren();
						for (Parent g : redex_nodes) {
							Collection<? extends Child> cg = g.getChildren();
							IntegerVariable[] vars = new IntegerVariable[cf
									.size() * cg.size()];
							int k = 0;
							for (PlaceEntity i : cf) {
								for (PlaceEntity j : cg) {
									vars[k++] = p_vars.get(i).get(j);
								}
							}
							model.addConstraint(Choco.leq(
									Choco.mult(cf.size(), p_vars.get(f).get(g)),
									Choco.sum(vars)));
						}
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 8 // |chld(g) not sites| M_fg <= sum(i chld(f), j chld(g) not
				// sites) if g in roots
				{
					Map<Root, Collection<? extends Child>> cgs = new HashMap<>(
							rrs);
					for (Root g : redex_roots) {
						Collection<? extends Child> cg = new HashSet<>(
								g.getChildren());
						cg.removeAll(redex_sites);
						cgs.put(g, cg);
					}
					for (PlaceEntity f : p_vars.keySet()) {
						Collection<? extends Child> cf = ((Parent) f)
								.getChildren();
						for (Root g : redex_roots) {
							Collection<? extends Child> cg = cgs.get(g);
							IntegerVariable[] vars = new IntegerVariable[cf
									.size() * cg.size()];
							int k = 0;
							for (Child i : cf) {
								for (Child j : cg) {
									vars[k++] = p_vars.get(i).get(j);
								}
							}
							model.addConstraint(Choco.leq(
									Choco.mult(cg.size(), p_vars.get(f).get(g)),
									Choco.sum(vars)));
						}
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 9 // sum(f ancs(i)\{i}, g in m) M_fg + M_ij <= 1 if j in
				// roots
				if (agent_ancestors_is_empty) {
					/*
					 * acquire agent_ancestors and re-check if it still is empty
					 * if this is the case, descends the agent parent map and
					 * builds agent_ancestors
					 */
					synchronized (agent_ancestors) {
						if (agent_ancestors_is_empty) {
							Stack<Parent> ancs = new Stack<>();
							Stack<Parent> visit = new Stack<>();
							for (Root r : agent_roots) {
								ancs.clear();
								visit.add(r);
								while (!visit.isEmpty()) {
									Parent p = visit.pop();
									if (p.isNode()) {
										Node n = (Node) p;
										if (!ancs.isEmpty()
												&& ancs.peek() != n.getParent())
											ancs.pop();
										// store ancestors for later
										agent_ancestors.put(n, new HashSet<>(
												ancs));
									}
									// put itself as an ancestor and
									// process each of its children
									ancs.push(p);
									for (Child c : p.getChildren()) {
										if (c.isParent()) {
											visit.add((Parent) c);
										}
									}
								}
							}
							agent_ancestors_is_empty = false;
						}
					}
				}
				{
					for (Root j : redex_roots) {
						for (PlaceEntity i : agent_ancestors.keySet()) {
							Collection<Parent> ancs = agent_ancestors.get(i);
							IntegerExpressionVariable[] vars = new IntegerExpressionVariable[1
									+ (ancs.size() - 1) * rss];
							int k = 0;
							for (Parent f : ancs) {
								if (f.isNode()) {
									// skip the only root in ancs
									Map<PlaceEntity, IntegerVariable> row = p_vars
											.get(f);
									for (Site g : redex_sites) {
										vars[k++] = row.get(g);
									}
								}
							}
							vars[k] = p_vars.get(i).get(j);
							model.addConstraint(Choco.geq(1, Choco.sum(vars)));
						}
					}
				}
				// LINK CONSTRAINTS ///////////////////////////////////////////

				// 1 // source constraints
				{
					IntegerVariable[] vars = new IntegerVariable[redex_points
							.size() + 1];
					for (Port p : agent_ports) {
						vars = e_vars.get(p).values().toArray(vars);
						model.addConstraint(Choco.eq(1, Choco.sum(vars)));
					}
				}
				// 2 // sink constraints
				{
					IntegerVariable[] vars1 = new IntegerVariable[rhs];
					for (Handle ha : agent_handles) {
						Collection<? extends Point> ps = ha.getPoints();
						IntegerVariable[] vars2 = new IntegerVariable[ps.size()];
						int k = 0;
						for (Point p : ps) {
							vars2[k++] = e_vars.get(p).get(ha);
						}
						k = 0;
						for (Handle hr : redex_handles) {
							vars1[k++] = e_vars.get(hr).get(ha);
						}
						model.addConstraint(Choco.eq(ps.size(),
								Choco.sum(Choco.sum(vars1), Choco.sum(vars2))));
					}
				}

				// 3 // flux preservation
				{
					IntegerVariable[] vars1 = new IntegerVariable[ahs];
					for (Handle hr : redex_handles) {
						Collection<? extends Point> ps = hr.getPoints();
						int k = 0;
						IntegerVariable[] vars2 = new IntegerVariable[agent_ports
								.size() * ps.size()];
						for (Port pa : agent_ports) {
							Map<LinkEntity, IntegerVariable> row = e_vars
									.get(pa);
							for (Point pr : ps) {
								vars2[k++] = row.get(pr);
							}
						}
						model.addConstraint(Choco.eq(
								Choco.sum(vars2),
								Choco.sum(e_vars.get(hr).values()
										.toArray(vars1))));
					}
				}

				// 4 // redex ports as "sources"
				{
					IntegerVariable[] vars = new IntegerVariable[agent_ports
							.size()];
					for (Point pr : redex_points) {
						if (pr.isPort()) {
							int k = 0;
							for (Port pa : agent_ports) {
								vars[k++] = e_vars.get(pa).get(pr);
							}
							model.addConstraint(Choco.eq(1, Choco.sum(vars)));
						}
					}
				}

				// 5 // relation between f_vars and e_vars for handles
				{
					for (Handle hr : redex_handles) {
						Map<Handle, IntegerVariable> f_row = f_vars.get(hr);
						Map<LinkEntity, IntegerVariable> e_row = e_vars.get(hr);
						if (!hr.getPoints().isEmpty()) {
							for (Handle ha : agent_handles) {
								if (!ha.getPoints().isEmpty()) {
									IntegerVariable vf = f_row.get(ha);
									IntegerVariable ve = e_row.get(ha);
									model.addConstraint(Choco.leq(ve, Choco
											.mult(vf, ha.getPoints().size())));
									model.addConstraint(Choco.leq(vf, ve));
								}
							}
						}
					}
				}
				// 6 // relation between f_vars and e_vars for points
				{
					for (Handle hr : redex_handles) {
						Map<Handle, IntegerVariable> f_row = f_vars.get(hr);
						Collection<? extends Point> ps = hr.getPoints();
						for (Handle ha : agent_handles) {
							IntegerVariable vf = f_row.get(ha);
							int k = 0;
							IntegerVariable[] vars = new IntegerVariable[ps
									.size() * ha.getPoints().size()];
							for (Point pa : ha.getPoints()) {
								Map<LinkEntity, IntegerVariable> e_row = e_vars
										.get(pa);
								for (Point pr : ps) {
									IntegerVariable ve = e_row.get(pr);
									vars[k++] = ve;
									model.addConstraint(Choco.leq(ve, vf));
								}
							}
							if (!ps.isEmpty() || !ha.getPoints().isEmpty())
								model.addConstraint(Choco.leq(vf,
										Choco.sum(vars)));
						}
					}
				}
				// 7 // flux separation
				{
					IntegerVariable[] vars = new IntegerVariable[ahs];
					for (Handle hr : redex_handles) {
						Map<Handle, IntegerVariable> f_row = f_vars.get(hr);
						model.addConstraint(Choco.geq(1,
								Choco.sum(f_row.values().toArray(vars))));
					}
				}

				// 8 // handles type
				{
					ListIterator<Handle> ir1 = redex_handles.listIterator(0);
					while (ir1.hasNext()) {
						Handle hr1 = ir1.next();
						Map<Handle, IntegerVariable> f_row1 = f_vars.get(hr1);
						if (hr1.isEdge()) {
							for (Handle ha : agent_handles) {
								if (ha.isOuterName())
									model.addConstraint(Choco.eq(0,
											f_row1.get(ha)));
							}
						}
						ListIterator<Handle> ir2 = redex_handles
								.listIterator(ir1.nextIndex());
						while (ir2.hasNext()) {
							Handle hr2 = ir2.next();
							Map<Handle, IntegerVariable> f_row2 = f_vars
									.get(hr2);
							if (hr1.isEdge() != hr2.isEdge()) {
								for (Handle ha : agent_handles) {
									model.addConstraint(Choco.geq(
											1,
											Choco.sum(f_row1.get(ha),
													f_row2.get(ha))));
								}
							}
						}
					}
				}

				// INTERPLAY CONSTRAINTS //////////////////////////////////////
				{
					for (Node ni : agent_nodes) {
						Map<PlaceEntity, IntegerVariable> p_row = p_vars
								.get(ni);
						for (Node nj : redex_nodes) {
							IntegerVariable m = p_row.get(nj);
							boolean comp = areMatchable(agent, ni, redex, nj);
							for (int i = ni.getControl().getArity() - 1; 0 <= i; i--) {
								Map<LinkEntity, IntegerVariable> e_row = e_vars
										.get(ni.getPort(i));
								for (int j = nj.getControl().getArity() - 1; 0 <= j; j--) {
									if (comp && i == j) {
										model.addConstraint(Choco.eq(m,
												e_row.get(nj.getPort(j))));
									} else {
										// this is not necessarily an interplay
										// constraint
										model.addConstraint(Choco.eq(0,
												e_row.get(nj.getPort(j))));
									}
								}
							}
						}
					}
				}
				{
					for (Node ni : agent_nodes) {
						Collection<Parent> ancs = agent_ancestors.get(ni);
						IntegerVariable[] vars1 = new IntegerVariable[ancs
								.size() * rrs];
						int k = 0;
						for (Parent f : ancs) {
							Map<PlaceEntity, IntegerVariable> row = p_vars
									.get(f);
							for (Root g : redex_roots) {
								vars1[k++] = row.get(g);
							}
						}
						for (Port pi : ni.getPorts()) {
							Map<LinkEntity, IntegerVariable> row = e_vars
									.get(pi);
							IntegerVariable[] vars2 = new IntegerVariable[redex_points
									.size()];
							k = 0;
							for (Point pj : redex_points) {
								vars2[k++] = row.get(pj);
							}
							model.addConstraint(Choco.leq(Choco.sum(vars2),
									Choco.sum(vars1)));
						}
					}
				}
				// END OF CONSTRAINTS /////////////////////////////////////////

				this.solver = new CPSolver();
				solver.read(model);
				solver.generateSearchStrategy();

				if (DEBUG) {
					System.out.println("- MODEL CREATED ---------------------");
					System.out.println("- AGENT -----------------------------");
					System.out.println(agent);
					System.out.println("- REDEX -----------------------------");
					System.out.println(redex);
					System.out.println("-------------------------------------");
				}
			}

			@Override
			public boolean hasNext() {
				if (mayHaveNext && nextMatch == null) {
					fetchSolution(true);
				}
				return mayHaveNext && nextMatch != null;
			}

			@Override
			public AgentMatch next() {
				AgentMatch res = nextMatch;
				if (mayHaveNext) {
					fetchSolution(nextMatch == null);
				} else if (nextMatch != null) {
					nextMatch = null;
				}
				return res;
			}

			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("");
			}

			private void cleanup() {
				this.mayHaveNext = false;
				this.solver.clear();
			}

			private void fetchSolution(boolean first) {
				if (DEBUG_PRINT_SOLUTION_FETCH)
					System.out.println("fetch solution has been invoked...");
				// look for a solution for the CSP
				if ((first && !solver.solve())
						|| (!first && !solver.nextSolution())) {
					if (DEBUG_PRINT_SOLUTION_FETCH)
						System.out
								.println("...but no more solutions where found.");
					cleanup();
					return;
				}
				if (DEBUG_PRINT_CSP_SOLUTIONS) {
					System.out.println("Solution: #"
							+ solver.getSolutionCount());
					System.out.print('\n');
					int p_cell_width[] = new int[1 + rrs + rns + rss];
					int c = 0;
					p_cell_width[0] = 6;
					for (Node n : agent_nodes) {
						p_cell_width[0] = Math.max(p_cell_width[0], n
								.toString().length());
					}
					System.out.printf("%-" + p_cell_width[0] + "s|", "P_VARS");
					c = 1;
					for (int k = 0; k < redex_roots.size(); k++, c++) {
						String s = "R_" + k;
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c] + "s|", s);
					}
					for (Node n : redex_nodes) {
						String s = n.toString();
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c++] + "s|", s);
					}
					for (int k = 0; k < redex_sites.size(); k++) {
						String s = "S_" + k;
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c] + "s|", s);
					}
					for (int i = 0; i < agent_roots.size(); i++) {
						System.out.printf("\nR_%-" + (p_cell_width[0] - 2)
								+ "d|", i);
						c = 1;
						Root ri = agent_roots.get(i);
						Map<PlaceEntity, IntegerVariable> row = p_vars.get(ri);
						for (int j = 0; j < redex_roots.size(); j++) {
							Root rj = redex_roots.get(j);
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(rj)).getVal());
						}
						for (Node nj : redex_nodes) {
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(nj)).getVal());
						}
						for (int j = 0; j < redex_sites.size(); j++) {
							Site sj = redex_sites.get(j);
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(sj)).getVal());
						}
					}
					for (Node ni : agent_nodes) {
						System.out.printf("\n%-" + p_cell_width[0] + "s|", ni);
						c = 1;
						Map<PlaceEntity, IntegerVariable> row = p_vars.get(ni);
						for (int j = 0; j < redex_roots.size(); j++) {
							Root rj = redex_roots.get(j);
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(rj)).getVal());
						}
						for (Node nj : redex_nodes) {
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(nj)).getVal());
						}
						for (int j = 0; j < redex_sites.size(); j++) {
							Site sj = redex_sites.get(j);
							System.out.printf("%" + p_cell_width[c++] + "d|",
									solver.getVar(row.get(sj)).getVal());
						}
					}
					System.out.println('\n');

					int f_cell_width[] = new int[1 + ahs];
					int e_cell_width[] = new int[1 + redex_points.size() + ahs];
					f_cell_width[0] = 6;
					for (Handle n : redex_handles) {
						f_cell_width[0] = Math.max(f_cell_width[0], n
								.toString().length());
					}
					e_cell_width[0] = f_cell_width[0];
					for (Point n : agent_ports) {
						e_cell_width[0] = Math.max(e_cell_width[0], n
								.toString().length());
					}
					System.out.printf("%-" + e_cell_width[0] + "s|", "E_VARS");
					c = 1;
					for (Point p : redex_points) {
						String s = p.toString();
						e_cell_width[c] = s.length();
						System.out.printf("%-" + e_cell_width[c++] + "s|", s);
					}
					for (Handle h : agent_handles) {
						String s = h.toString();
						e_cell_width[c] = s.length();
						System.out.printf("%-" + e_cell_width[c++] + "s|", s);
					}
					for (Point pi : agent_ports) {
						System.out.printf("\n%-" + e_cell_width[0] + "s|", pi);
						c = 1;
						Map<LinkEntity, IntegerVariable> row = e_vars.get(pi);
						for (Point pj : redex_points) {
							System.out.printf("%" + e_cell_width[c++] + "d|",
									solver.getVar(row.get(pj)).getVal());
						}
						for (Handle hj : agent_handles) {
							if (row.containsKey(hj))
								System.out.printf("%" + e_cell_width[c++]
										+ "d|", solver.getVar(row.get(hj))
										.getVal());
							else
								System.out.printf("%" + e_cell_width[c++]
										+ "c|", ' ');
						}
					}
					for (Handle hi : redex_handles) {
						System.out.printf("\n%-" + e_cell_width[0] + "s|", hi);
						c = 1;
						Map<LinkEntity, IntegerVariable> row = e_vars.get(hi);
						for (int j = redex_points.size(); 0 < j; j--) {
							System.out.printf("%" + e_cell_width[c++] + "c|",
									' ');
						}
						for (Handle hj : agent_handles) {
							System.out.printf("%" + e_cell_width[c++] + "d|",
									solver.getVar(row.get(hj)).getVal());
						}
					}

					System.out.println('\n');

					System.out.printf("%" + f_cell_width[0] + "s|", "F_VARS");
					c=1;
					for (Handle h : agent_handles) {
						String s = h.toString();
						f_cell_width[c] = s.length();
						System.out.printf("%-" + f_cell_width[c++] + "s|", s);
					}
					for (Handle hi : redex_handles) {
						System.out.printf("\n%-" + f_cell_width[0] + "s|", hi);
						c = 1;
						Map<Handle, IntegerVariable> row = f_vars.get(hi);
						for (Handle hj : agent_handles) {
							System.out.printf("%" + f_cell_width[c++] + "d|",
									solver.getVar(row.get(hj)).getVal());
						}
					}
					System.out.println('\n');
				}

				/*
				 * Visit the agent and clones it adding replicas to context,
				 * redex or params bigraphs depending on the seolution of the
				 * CSP above
				 */

				// context
				Bigraph ctx = new Bigraph(agent.signature);
				// redex
				Bigraph rdx = new Bigraph(agent.signature);
				// parameters
				Bigraph[] prms = new Bigraph[rss];
				// linking medianting between parameters and redex+ID
				Bigraph lmb = Bigraph.makeId(redex.signature, rss);
				Bigraph id = Bigraph.makeEmpty(redex.signature);
				// an injective map from redex's nodes to rdx's ones
				Map<Node, EditableNode> nEmb = new HashMap<>(rns);

				// replicated sites
				EditableSite ctx_sites_dic[] = new EditableSite[rrs];
				EditableSite rdx_sites_dic[] = new EditableSite[rss];
				EditableRoot rdx_roots_dic[] = new EditableRoot[rrs];

				// replicated handles lookup tables
				Map<Handle, EditableHandle> ctx_hnd_dic = new IdentityHashMap<>();
				Map<Handle, EditableHandle> rdx_hnd_dic = new IdentityHashMap<>();
				Map<Handle, EditableHandle> lmb_hnd_dic = new IdentityHashMap<>();
				Map<Bigraph, Map<Handle, EditableHandle>> prms_hnd_dic = new IdentityHashMap<>();

				// Collection<Node> unseen_agt_nodes = new
				// HashSet<>(agent_nodes);

				Map<Handle, EditableHandle> handle_img = new IdentityHashMap<>(
						rhs);
				// Map<Root, EditableParent> root_img = new
				// IdentityHashMap<>(rs);
				// Map<Node, EditableNode> node_img = new
				// IdentityHashMap<>(rns);
				// Map<Site, Collection<EditableNode>> site_img = new
				// IdentityHashMap<>(rns);

				/*
				 * { Collection<Node> ns = new LinkedList<>(redex_nodes);
				 * Iterator<Node> in1 = unseen_agt_nodes.iterator();
				 * while(in1.hasNext()){ Node n1 = in1.next(); Map<PlaceEntity,
				 * IntegerVariable> p_row = p_vars.get(n1); Iterator<Node> in0 =
				 * ns.iterator(); while(in0.hasNext()){ Node n0 = in0.next(); if
				 * (solver.getVar(p_row.get(n0)).getVal() == 1) {
				 * node_img.put(n0, (EditableNode) n1); in0.remove();
				 * in1.remove(); break; } } if(ns.isEmpty()) break; } }
				 */

				class VState {
					final PlaceEntity c; // the agent root/node to be visited
					final PlaceEntity i; // if present, is the image of c in the
											// redex
					final EditableParent p; // the replicated parent
					final Bigraph b;

					VState(Bigraph b, EditableParent p, PlaceEntity c) {
						this(b, p, c, null);
					}

					VState(Bigraph b, EditableParent p, PlaceEntity c,
							PlaceEntity i) {
						this.i = i;
						this.c = c;
						this.p = p;
						this.b = b;
					}
				}
				Queue<VState> q = new LinkedList<>();

				for (EditableOuterName o1 : agent.outers.values()) {
					EditableOuterName o2 = o1.replicate();
					ctx.outers.put(o2.getName(), o2);
					o2.setOwner(ctx);
					ctx_hnd_dic.put(o1, o2);
				}
				for (EditableOuterName o0 : redex.outers.values()) {
					// replicate the handle
					EditableOuterName o2 = new EditableOuterName(o0.getName());
					rdx.outers.put(o2.getName(), o2);
					o2.setOwner(rdx);
					rdx_hnd_dic.put(o0, o2);
					// update ctx inner face
					EditableInnerName i1 = new EditableInnerName(o0.getName());
					ctx.inners.put(i1.getName(), i1);
					// find the handle for i1
					EditableHandle h1 = handle_img.get(o0);
					if (h1 == null) {
						// cache miss
						Map<Handle, IntegerVariable> f_row = f_vars.get(o0);
						for (Handle h : agent_handles) {
							if (solver.getVar(f_row.get(h)).getVal() == 1) {
								h1 = h.getEditable();
								break;
							}
						}
						if (h1 == null) {
							h1 = new EditableEdge();
						}
						handle_img.put(o0, h1);
					}
					EditableHandle h2 = ctx_hnd_dic.get(h1);
					if (h2 == null) {
						h2 = h1.replicate();
						h2.setOwner(ctx);
						ctx_hnd_dic.put(h1, h2);
					}
					i1.setHandle(h2);
				}
				for (EditableInnerName i0 : redex.inners.values()) {
					String name = i0.getName();
					EditableInnerName i2 = new EditableInnerName(name);
					// set replicated handle for i2
					EditableHandle h0 = i0.getHandle();
					// looks for an existing replica
					EditableHandle h2 = rdx_hnd_dic.get(h0);
					if (h2 == null) {
						EditableHandle h1 = handle_img.get(h0);
						if (h1 == null) {
							// cache miss
							Map<Handle, IntegerVariable> f_row = f_vars.get(h0);
							for (Handle h : agent_handles) {
								if (solver.getVar(f_row.get(h)).getVal() == 1) {
									h1 = h.getEditable();
									break;
								}
							}
							if (h1 == null) {
								h1 = new EditableEdge();
							}
							handle_img.put(h0, h1);
						}
						h2 = h1.replicate();
						h2.setOwner(rdx);
						rdx_hnd_dic.put(h0, h2);
					}
					i2.setHandle(h2);
					rdx.inners.put(name, i2);

					EditableOuterName o2 = new EditableOuterName(name);
					o2.setOwner(lmb);
					lmb.outers.put(name, o2);
				}
				for (EditableRoot r0 : agent.roots) {
					q.add(new VState(ctx, null, r0));
				}
				Collection<Root> unseen_rdx_roots = new LinkedList<>(
						redex_roots);
				while (!q.isEmpty()) {
					VState v = q.poll();
					if (v.b == ctx) {
						EditableParent p1 = (EditableParent) v.c;
						EditableParent p2 = p1.replicate();
						if (p1.isRoot()) {
							// ordering is ensured by the queue
							EditableRoot r2 = (EditableRoot) p2;
							ctx.roots.add(r2);
							r2.setOwner(ctx);
						} else { // isNode()
							EditableNode n1 = (EditableNode) p1;
							// unseen_agt_nodes.remove(n1);
							EditableNode n2 = (EditableNode) p2;
							n2.setParent(v.p);
							// replicate links from node ports
							for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
								EditablePort o = n1.getPort(i);
								EditableHandle h1 = o.getHandle();
								// looks for an existing replica
								EditableHandle h2 = ctx_hnd_dic.get(h1);
								if (h2 == null) {
									h2 = h1.replicate();
									h2.setOwner(ctx);
									ctx_hnd_dic.put(h1, h2);
								}
								n2.getPort(i).setHandle(h2);
							}
						}
						// enqueue children, if necessary
						Collection<Child> cs = new HashSet<>(p1.getChildren());
						Map<PlaceEntity, IntegerVariable> p_row = p_vars
								.get(p1);
						Iterator<Root> ir = unseen_rdx_roots.iterator();
						while (ir.hasNext()) {
							Root r0 = ir.next();
							// make a site for each root whose image is p1
							if (solver.getVar(p_row.get(r0)).getVal() == 1) {
								// root_img.put(r0, p1);
								ir.remove();
								int k = redex_roots.indexOf(r0);
								EditableSite s = new EditableSite();
								s.setParent(p2);
								ctx_sites_dic[k] = s;
								EditableRoot r2 = new EditableRoot();
								r2.setOwner(rdx);
								rdx_roots_dic[k] = r2;
								Iterator<Child> ic = cs.iterator();
								while (ic.hasNext()) {
									Child c1 = ic.next();
									Map<PlaceEntity, IntegerVariable> c_row = p_vars
											.get(c1);
									for (Child c0 : r0.getChildren()) {
										if (solver.getVar(c_row.get(c0))
												.getVal() == 1) {
											q.add(new VState(rdx, r2, c1, c0));
											ic.remove();
											if (c0.isNode()) {
												// unseen_agt_nodes.remove(c1);
												break;
											}
										} else if (c0.isSite()) {
											// closed site
											q.add(new VState(rdx, r2, null, c0));
										}
									}
								}
							}
						}
						for (Child c1 : cs) {
							q.add(new VState(ctx, p2, c1));
						}
					} else if (v.b == rdx) {
						if (v.i.isNode()) {
							EditableNode n0 = (EditableNode) v.i;
							EditableNode n1 = (EditableNode) v.c;
							EditableNode n2 = n1.replicate();
							nEmb.put(n1, n0);
							n2.setParent(v.p);
							// replicate links from node ports
							for (int i = n0.getControl().getArity() - 1; -1 < i; i--) {
								EditablePort o0 = n0.getPort(i);
								EditableHandle h0 = o0.getHandle();
								// looks for an existing replica
								EditableHandle h2 = rdx_hnd_dic.get(h0);
								if (h2 == null) {
									h2 = n1.getPort(i).getHandle().replicate();
									h2.setOwner(rdx);
									rdx_hnd_dic.put(h0, h2);
								}
								n2.getPort(i).setHandle(h2);
							}
							Collection<Child> cs = new HashSet<>(
									n0.getChildren());
							for (Child c1 : n1.getChildren()) {
								Map<PlaceEntity, IntegerVariable> p_row = p_vars
										.get(c1);
								Iterator<Child> ic = cs.iterator();
								while (ic.hasNext()) {
									Child c0 = ic.next();
									if (solver.getVar(p_row.get(c0)).getVal() == 1) {
										q.add(new VState(rdx, n2, c1, c0));
										ic.remove();
										if (c0.isNode()) {
											// unseen_agt_nodes.remove(c1);
											break;
										}
									} else if (c0.isSite()) {
										q.add(new VState(rdx, n2, null, c0));
									}
								}
							}
						} else {
							EditableSite s0 = (EditableSite) v.i;
							int k = redex_sites.indexOf(s0);
							if (rdx_sites_dic[k] == null) {
								EditableSite s2 = new EditableSite();
								s2.setParent(v.p);
								rdx_sites_dic[k] = s2;
							}
							if (neededParam[k]) {
								Bigraph prm = prms[k];
								if (prm == null) {
									prm = new Bigraph(agent.getSignature());
									prm.roots.add(new EditableRoot(prm));
									prms[k] = prm;
									prms_hnd_dic
											.put(prm,
													new IdentityHashMap<Handle, EditableHandle>());
								}
								if (v.c != null)
									q.add(new VState(prm, prm.roots.get(0), v.c));
							}
						}
					} else {
						EditableNode n1 = (EditableNode) v.c;
						EditableNode n2 = n1.replicate();
						n2.setParent(v.p);
						for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
							/*
							 * every handle with a point in the param is
							 * translated into an outer and the necessary wiring
							 * is delegated to the bigraph lambda.
							 */
							EditablePort p1 = n1.getPort(i);
							EditablePort p2 = n2.getPort(i);

							EditableHandle h2 = null;
							Map<Handle, EditableHandle> hnd_dic = prms_hnd_dic
									.get(v.b);
							Map<LinkEntity, IntegerVariable> row = e_vars
									.get(p1);
							EditableHandle h1 = p1.getHandle();
							if (solver.getVar(row.get(h1)).getVal() == 1) {
								/*
								 * this port bypasses the redex. Checks if the
								 * handle already has an image in this parameter
								 * otherwise creates a suitable name in the
								 * parameter and in the wiring lambda. This may
								 * require some additional step if the handle
								 * already has an image in the context.
								 */
								h2 = hnd_dic.get(h1);
								if (h2 == null) {
									EditableHandle h3 = lmb_hnd_dic.get(h1);
									if (h3 == null) {
										EditableHandle h4 = ctx_hnd_dic.get(h1);
										if (h4 != null) {
											/*
											 * h1 has an image in the context,
											 * add an inner to it and link it
											 * down to the parameter passing
											 * through id e lmb.
											 */
											EditableInnerName i4 = new EditableInnerName();
											i4.setHandle(h4);
											String name = i4.getName();
											// add it also to id
											EditableOuterName o5 = new EditableOuterName(
													name);
											o5.setOwner(id);
											id.outers.put(name, o5);
											EditableInnerName i5 = new EditableInnerName(
													name);
											i5.setHandle(o5);
											id.inners.put(name, i5);
											// and finally to lambda
											EditableOuterName o3 = new EditableOuterName(
													name);
											lmb.outers.put(name, o3);
											h3 = o3;
										} else {
											/*
											 * this handle is not required by
											 * the context, use an edge to
											 * reduce the interface of id
											 */
											h3 = new EditableEdge();
										}
										h3.setOwner(lmb);
										lmb_hnd_dic.put(h1, h3);
									}
									EditableInnerName i3 = new EditableInnerName(
											h3);
									String name = i3.getName();
									lmb.inners.put(name, i3);
									EditableOuterName o2 = new EditableOuterName(
											name);
									o2.setOwner(v.b);
									v.b.outers.put(name, o2);
									h2 = o2;
									hnd_dic.put(h1, h2);
								}
							} else {
								for (InnerName i0 : redex.inners.values()) {
									if (solver.getVar(row.get(i0)).getVal() == 1) {
										/*
										 * this port is attached to the redex
										 * inner i0. Add a fresh name to lambda
										 * and this param and link it to i0 e p2
										 * resp.
										 */
										EditableHandle h3 = lmb.outers.get(i0
												.getName());
										h2 = hnd_dic.get(h3);
										if (h2 == null) {
											EditableInnerName i3 = new EditableInnerName(
													h3);
											String name = i3.getName();
											lmb.inners.put(name, i3);
											EditableOuterName o2 = new EditableOuterName(
													name);
											o2.setOwner(v.b);
											v.b.outers.put(name, o2);
											h2 = o2;
											hnd_dic.put(h3, h2);
										}
										break;
									}
								}
							}
							p2.setHandle(h2);
						}
						for (Child c1 : n1.getChildren()) {
							q.add(new VState(ctx, n2, c1));
						}
					}
				}
				ctx.sites.addAll(Arrays.asList(ctx_sites_dic));
				rdx.sites.addAll(Arrays.asList(rdx_sites_dic));
				rdx.roots.addAll(Arrays.asList(rdx_roots_dic));

				if (DEBUG_CONSISTENCY_CHECK) {
					if (!ctx.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (ctx)");
					}
					if (!rdx.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (rdx)");
					}
					if (!lmb.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (lmb)");
					}
					if (!id.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (id)");
					}
					for (int i = 0; i < rss; i++) {
						if (neededParam[i] && !prms[i].isConsistent()) {
							throw new RuntimeException(
									"Inconsistent bigraph (prm " + i + ")");
						}
					}
				}

				this.nextMatch = new AgentMatch(ctx, rdx, id, lmb, prms, nEmb);
			}
		}
	}
}
