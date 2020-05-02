package it.uniud.mads.jlibbig.core.std;

import java.util.*;

import it.uniud.mads.jlibbig.core.std.EditableNode.EditablePort;
import it.uniud.mads.jlibbig.core.util.BidMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
/*
 * TODO: refactor matchers (Matcher, AgentMatcher ...)
 * 
 */


/**
 * Provides services for computing optimal matches of bigraphs with abstract
 * internal names; matches are described by {@link Match}. A weight is assigned
 * to every matchable pair of nodes (one from the agent and the other from the
 * redex) and the weight of a match is defined as the sum of weights assigned to
 * the pairs used in the match. The matcher computes optimal matches only i.e.
 * those with maximal (resp. minimal) weight. The default behaviour looks for
 * maximal solutions but the behaviour can be specified. Weights are defined by
 * the method {@link #matchingWeight}.
 * 
 * The field {@link #DEFAULT} refers to a default instance of the matcher.
 * 
 * The standard matching of nodes can be changed by re-implementing the
 * protected method {@link #areMatchable}.
 * 
 * WARNING: Implementation still is in an early stage.
 */
public class WeightedMatcher extends Matcher {

	private final static boolean DEBUG = Boolean
			.getBoolean("it.uniud.mads.jlibbig.debug")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.debug.matchers");
	private final static boolean DEBUG_PRINT_CSP_SOLUTIONS = DEBUG;
	private final static boolean DEBUG_PRINT_SOLUTION_FETCH = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean
			.getBoolean("it.uniud.mads.jlibbig.consistency")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.matchers");

	/**
	 * The default instance of the matcher.
	 */
	public final static WeightedMatcher DEFAULT = new WeightedMatcher();

	private final boolean maximizing;

	public WeightedMatcher() {
		this(true);
	}

	public WeightedMatcher(boolean maximizing) {
		super();
		this.maximizing = maximizing;
	}

	@Override
	public Iterable<? extends WeightedMatch> match(Bigraph agent, Bigraph redex) {
		return new MatchIterable(agent, redex);
	}

	protected int matchingWeight(Bigraph agent, Node agentNode, Bigraph redex,
			Node redexNode) {
		return 0;
	}

	private class MatchIterable implements Iterable<WeightedMatch> {

		final Bigraph agent, redex;

		boolean agent_ancestors_is_empty = true;
		final Map<Child, Collection<Parent>> agent_ancestors;

		// caches some collections of entities (e.g. nodes and edges are
		// computed on the fly)
		final List<? extends Root> agent_roots;
		final List<? extends Site> agent_sites;
		final Collection<? extends Node> agent_nodes;
		// final Collection<Port> agent_ports;
		final Collection<Point> agent_points;
		final Collection<? extends Edge> agent_edges;
		/*
		 * Handles are not ordered, but the use of a list simplifies some
		 * constraints for f_vars
		 */
		final List<Handle> agent_handles;

		final List<? extends Root> redex_roots;
		final List<? extends Site> redex_sites;
		final Collection<? extends Node> redex_nodes;
		final Collection<Point> redex_points;
		final Collection<? extends Edge> redex_edges;
		final List<Handle> redex_handles;

		// final boolean[] neededParam;

		/*
		 * naming policy for sizes: a- agent r- redex -rs roots -ns nodes -ss
		 * sites -hs handles -ps points -prs ports -ins inners -ots outers
		 */
		final int ars, ans, ass, ahs, aps, rrs, rns, rss, rhs, rps, rprs, rins;

		private MatchIterable(Bigraph agent, Bigraph redex) {
			// boolean[] neededParams) {
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
			this.agent_sites = agent.getSites();
			this.agent_edges = agent.getEdges();//agent_nodes);
			this.agent_handles = new LinkedList<Handle>(agent_edges);
			agent_handles.addAll(agent.getOuterNames());

			ars = agent_roots.size();
			ans = agent_nodes.size();
			ass = agent_sites.size();
			ahs = agent_handles.size();

			this.agent_points = new HashSet<>(ans);
			for (Node n : agent_nodes) {
				agent_points.addAll(n.getPorts());
			}
			agent_points.addAll(agent.getInnerNames());
			aps = agent_points.size();

			this.agent_ancestors = new HashMap<>(ans);

			this.redex_roots = redex.getRoots();
			this.redex_sites = redex.getSites();
			this.redex_nodes = redex.getNodes();
			this.redex_edges = redex.getEdges();//redex_nodes);
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
			rprs = redex_points.size(); // only ports
			redex_points.addAll(redex.getInnerNames());
			rps = redex_points.size();
			rins = rps - rprs;

			// this.neededParam = new boolean[rss];
			// for (int i = 0; i < this.neededParam.length; i++) {
			// this.neededParam[i] = (neededParams == null) || neededParams[i];
			// }
		}

		@Override
		public Iterator<WeightedMatch> iterator() {
			return new MatchIterator();
		}

		private class MatchIterator implements Iterator<WeightedMatch> {

			private boolean mayHaveNext = true;
			private boolean firstRun = true;

			private WeightedMatch nextMatch = null;

			final private Model model;
			final private Solver solver;
			/*
			 * variables for the place embedding problem the following variables
			 * are indexed over pairs where the first entity is from the agent
			 * and the second from the redex
			 */
			final Map<PlaceEntity, Map<PlaceEntity, IntVar>> p_vars = new IdentityHashMap<>(
					ars + ans + ass);
			/*
			 * variables for the multiflux problem desrcibing the link embedding
			 * these are indexed by redex handles and then by agent handles
			 */
			final Map<LinkEntity, Map<LinkEntity, IntVar>> e_vars = new IdentityHashMap<>(
					ahs * rhs + aps * (1 + rps));
			/*
			 * variables for flux separation implicitly describing the handles
			 * embedding these are indexed from the source to target of the flux
			 */
			final Map<Handle, Map<Handle, IntVar>> f_vars = new IdentityHashMap<>(
					rhs);

			/* the cost expression */
			private IntVar weight;

			MatchIterator() {
				this.model = new Model();

				solver = instantiateModel();

				if (DEBUG) {
					System.out.println("- MODEL CREATED ---------------------");
					System.out.println("- AGENT -----------------------------");
					System.out.println(agent);
					System.out.println("- REDEX -----------------------------");
					System.out.println(redex);
					System.out.println("-------------------------------------");
				}
			}

			private Variable findVariable(String name, Variable[] vars) {
				for (Variable v : vars) {
					if (name.equals(v.getName())) {
						return v;
					}
				}
				// We should't encounter this case.
				return null;
			}

			private Solver instantiateModel() {
				// MODEL
				// ///////////////////////////////////////////////////////////

				// int ki = 0, kj = 0, kk = 0;
				// IntegerExpressionVariable[] vars1, vars2;

				{
					int ki = 0;
					for (Root i : agent_roots) {
						int kj = 0;
						Map<PlaceEntity, IntVar> row = new HashMap<>(
								rrs + rns + rss);
						for (Root j : redex_roots) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							row.put(j, var);
						}
						// 1 // these will always be zero
						for (Node j : redex_nodes) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							model.arithm(var, "=", 0).post();
							row.put(j, var);
						}
						for (Site j : redex_sites) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							model.arithm(var, "=", 0).post();
							row.put(j, var);
						}
						p_vars.put(i, row);
						ki++;
					}
					for (Node i : agent_nodes) {
						int kj = 0;
						Map<PlaceEntity, IntVar> row = new HashMap<>(
								rrs + rns + rss);
						for (Root j : redex_roots) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							row.put(j, var);
						}
						for (Node j : redex_nodes) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							row.put(j, var);
						}
						for (Site j : redex_sites) {
							IntVar var = model.boolVar(ki + "_" + kj++);
							row.put(j, var);
						}
						p_vars.put(i, row);
						ki++;
					}

					for (Site i : agent_sites) {
						int kj = 0;
						Map<PlaceEntity, IntVar> row = new HashMap<>(
								rss); // rrs + rns + rss);
						/*
						 * for (Root j : redex_roots) { IntegerVariable var =
						 * model.boolVar(ki + "_" + kj++);
						 * model.addVariable(var); row.put(j, var); } /*for
						 * (Node j : redex_nodes) { IntegerVariable var =
						 * model.boolVar(ki + "_" + kj++);
						 * model.addVariable(var); row.put(j, var); }
						 */
						for (Site j : redex_sites) {
							IntVar var = model.boolVar(ki + "_" + kj++);
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
						Map<Handle, IntVar> row = new IdentityHashMap<>(
								ahs);
						for (Handle ha : agent_handles) {
							IntVar var = model.boolVar("F_" + ki + "_" + kj++);
							row.put(ha, var);
						}
						f_vars.put(hr, row);
						ki++;
					}

					// vars for agent point flux
					ki = 0;
					for (Point pi : agent_points) {
						int kj = 0;
						Map<LinkEntity, IntVar> row = new IdentityHashMap<>(
								rps + 1);
						Handle hi = pi.getHandle();
						IntVar var = model.boolVar("PH_" + ki);
						row.put(hi, var);
						for (Point pj : redex_points) {
							var = model.boolVar("PP_" + ki + "_" + kj++);
							row.put(pj, var);
						}
						e_vars.put(pi, row);
						ki++;
					}

					// vars for redex handles flux
					ki = 0;
					for (Handle hj : redex_handles) {
						int kj = 0;
						Map<LinkEntity, IntVar> row = new IdentityHashMap<>(
								ahs);
						for (Handle hi : agent_handles) {
							IntVar var = model.intVar("HH_"
									+ ki + "_" + kj++, 0, hi.getPoints().size());
							row.put(hi, var);
						}
						e_vars.put(hj, row);
						ki++;
					}
				}
				weight = model.intVar("OPT", Short.MIN_VALUE, 
						Short.MAX_VALUE, true);
				List<IntVar> weights = new LinkedList<>();

				// PLACE CONSTRAINTS //////////////////////////////////////////

				// 2 // M_ij = 0 if nodes are different in the sense of this.eq
				// merged with interplay constraints
				/*
				 * { for (Node i : agent_nodes) { Map<PlaceEntity,
				 * IntegerVariable> row = p_vars.get(i); for (Node j :
				 * redex_nodes) { IntegerVariable var = row.get(j); if
				 * (!areMatchable(agent, i, redex, j)) {
				 * model.addConstraint(Choco.eq(0, var)); } } }}
				 */
				// ////////////////////////////////////////////////////////////

				// 3 // M_ij <= M_fg if f = prnt(i) and g = prnt(j)
				{
					for (Node i : agent_nodes) {
						Parent f = i.getParent();
						Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
						Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
						for (Child j : redex_nodes) {
							Parent g = j.getParent();
							model.arithm(i_row.get(j), "<=", f_row.get(g))
								.post();
						}
						for (Child j : redex_sites) {
							Parent g = j.getParent();
							model.arithm(i_row.get(j), "<=", f_row.get(g))
								.post();
						}
					}
					for (Site i : agent_sites) {
						Parent f = i.getParent();
						Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
						Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
						for (Child j : redex_sites) {
							Parent g = j.getParent();
							model.arithm(i_row.get(j), "<=", f_row.get(g))
								.post();
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
					 * found in an active context whereas children in qp are in
					 * passive contexts or passive nodes.
					 */
					Deque<Node> qa = new ArrayDeque<>();
					Deque<Child> qp = new ArrayDeque<>();
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
					while (!qp.isEmpty()) {
						Child i = qp.poll();
						Map<PlaceEntity, IntVar> row = p_vars.get(i);
						for (Root j : redex_roots) {
							model.arithm(row.get(j), "=", 0).post();
						}

						if (i.isNode()) {
							for (Child c : ((Node) i).getChildren()) {
								if (c.isNode()) {
									qp.add((Node) c);
								}
							}
						}
					}
				}

				// /////////////////////////////////////////////////////////////////

				// 5 // sum M_ij = 1 if j not in sites
				{
					IntVar[] vars = new IntVar[ars + ans];
					for (Root j : redex_roots) {
						int k = 0;
						for (PlaceEntity i : p_vars.keySet()) {
							if (i.isSite())
								continue;
							vars[k++] = p_vars.get(i).get(j);
						}
						model.sum(vars, "=", 1).post();
					}
					vars = new IntVar[ans + ass];
					for (Node j : redex_nodes) {
						int k = 0;
						for (PlaceEntity i : p_vars.keySet()) {
							if (i.isRoot())
								continue;
							vars[k++] = p_vars.get(i).get(j);
						}
						model.sum(vars, "=", 1).post();
					}
				}
				// //////////////////////////////////////////////////////////////////

				// 6 // n sum(j not root) M_ij + sum(j root) M_ij <= n if i in
				// nodes
				{
					for (Node i : agent_nodes) {
						Map<PlaceEntity, IntVar> row = p_vars.get(i);
						IntVar[] vars = new IntVar[rns + rss];
						int k = 0;
						for (PlaceEntity j : redex_nodes) {
							vars[k++] = row.get(j);
						}
						for (PlaceEntity j : redex_sites) {
							vars[k++] = row.get(j);
						}
						IntVar t1 = model.intVar(rrs);
						IntVar c = model.intVar(0);
						for (IntVar v : vars) {
							c = c.add(v).intVar();
						}
						c = c.mul(t1).intVar();

						vars = new IntVar[rrs];
						k = 0;
						for (Root j : redex_roots) {
							vars[k++] = row.get(j);
						}

						t1 = model.intVar(0);
						for (IntVar v : vars) {
							t1 = t1.add(v).intVar();
						}
						t1 = t1.add(c).intVar();
						model.arithm(t1, "<=", rrs).post();
					}
					/*
					 * for (Site i : agent_sites) { Map<PlaceEntity,
					 * IntegerVariable> row = p_vars.get(i); IntegerVariable[]
					 * vars = new IntegerVariable[rss]; int k = 0; for
					 * (PlaceEntity j : redex_sites) { vars[k++] = row.get(j); }
					 * IntegerExpressionVariable c = Choco.mult(rrs,
					 * Choco.sum(vars));
					 * 
					 * vars = new IntegerVariable[rrs]; k = 0; for (Root j :
					 * redex_roots) { vars[k++] = row.get(j); }
					 * model.addConstraint(Choco.geq(rrs, Choco.sum(c,
					 * Choco.sum(vars)))); }
					 */
				}
				// /////////////////////////////////////////////////////////////////

				// 7 // |chld(f)| M_fg <= sum(i chld(f), j in chld(g)) M_ij if
				// f,g in nodes
				{
					for (Parent f : agent_nodes) {
						Collection<? extends Child> cf = f.getChildren();
						for (Parent g : redex_nodes) {
							Collection<? extends Child> cg = g.getChildren();
							IntVar[] vars = new IntVar[cf.size() * cg.size()];
							int k = 0;
							for (PlaceEntity i : cf) {
								for (PlaceEntity j : cg) {
									vars[k++] = p_vars.get(i).get(j);
								}
							}
							IntVar chld = p_vars.get(f).get(g);
							model.sum(vars, ">=", chld.mul(cf.size()).intVar()).post();
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
						if (f.isSite())
							continue;
						Collection<? extends Child> cf = ((Parent) f)
								.getChildren();
						for (Root g : redex_roots) {
							Collection<? extends Child> cg = cgs.get(g);
							IntVar[] vars = new IntVar[cf.size() * cg.size()];
							int k = 0;
							for (Child i : cf) {
								for (Child j : cg) {
									vars[k++] = p_vars.get(i).get(j);
								}
							}
							IntVar chld = p_vars.get(f).get(g);
							model.sum(vars, ">=", chld.mul(cg.size()).intVar()).post();
						}
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 9 // sum(f in ancs(i)\{i}, g in m) M_fg + M_ij <= 1 if j in
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
										while (!ancs.isEmpty()
												&& ancs.peek() != n.getParent())
											ancs.pop();
									}
									// put itself as an ancestor and
									// process each of its children
									ancs.push(p);
									for (Child c : p.getChildren()) {
										if (c.isParent()) {
											visit.add((Parent) c);
										}
										agent_ancestors.put(c, new ArrayList<>(
												ancs));
									}
								}
							}
							agent_ancestors_is_empty = false;
						}
					}
				}
				{
					for (Node i : agent_nodes) {
						Collection<Parent> ancs = agent_ancestors.get(i);
						IntVar[] vars = new IntVar[(ancs.size() - 1) * rss];
						int k = 0;
						for (Parent f : ancs) {
							if (f.isNode()) {
								Map<PlaceEntity, IntVar> f_row = p_vars
										.get(f);
								for (Site g : redex_sites) {
									vars[k++] = f_row.get(g);
								}
							}
						}
						IntVar sum = model.intVar(0);
						for (IntVar v : vars) {
							sum = sum.add(v).intVar();
						}
						Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
						for (Root j : redex_roots) {
							model.arithm(sum.add(i_row.get(j)).intVar(), 
									"<=", 1).post();
						}
					}
				}

				// 10 //
				{
					IntVar[] vars = new IntVar[rrs + rns + rss];
					for (Site i : agent_sites) {
						vars = p_vars.get(i).values().toArray(vars);
						model.sum(vars, "<=", 1).post();
					}
				}
				// LINK CONSTRAINTS ///////////////////////////////////////////

				// 1 // source constraints
				{
					IntVar[] vars = new IntVar[rps + 1];
					for (Point p : agent_points) {
						vars = e_vars.get(p).values().toArray(vars);
						model.sum(vars, "=", 1).post();
					}
				}
				// 2 // sink constraints
				{
					for (Handle ha : agent_handles) {
						Collection<? extends Point> ps = ha.getPoints();
						IntVar[] vars1 = new IntVar[rhs + ps.size()];
						int k = 0;
						for (Point p : ps) {
							vars1[k++] = e_vars.get(p).get(ha);
						}
						for (Handle hr : redex_handles) {
							vars1[k++] = e_vars.get(hr).get(ha);
						}
						model.sum(vars1, "=", ps.size()).post();
					}
				}

				// 3 // flux preservation
				{
					IntVar[] vars1 = new IntVar[ahs];
					for (Handle hr : redex_handles) {
						Collection<? extends Point> ps = hr.getPoints();
						int k = 0;
						IntVar[] vars2 = new IntVar[aps * ps.size()];
						for (Point pa : agent_points) {
							Map<LinkEntity, IntVar> row = e_vars
									.get(pa);
							for (Point pr : ps) {
								vars2[k++] = row.get(pr);
							}
						}
						vars1 = e_vars.get(hr).values().toArray(vars1);
						IntVar sum1 = model.intVar(0);
						for (IntVar v : vars1) {
							sum1 = sum1.add(v).intVar();
						}
						model.sum(vars2, "=", sum1).post();
					}
				}

				// 4 // redex ports as "sources"
				{
					IntVar[] vars = new IntVar[aps];
					for (Point pr : redex_points) {
						if (pr.isPort()) {
							int k = 0;
							for (Point pa : agent_points) {
								vars[k++] = e_vars.get(pa).get(pr);
							}
							model.sum(vars, "=", 1).post();
						}
					}
				}

				// 5 // relation between f_vars and e_vars for handles
				{
					for (Handle hr : redex_handles) {
						Map<Handle, IntVar> f_row = f_vars.get(hr);
						Map<LinkEntity, IntVar> e_row = e_vars.get(hr);
						if (!hr.getPoints().isEmpty()) {
							for (Handle ha : agent_handles) {
								if (!ha.getPoints().isEmpty()) {
									IntVar vf = f_row.get(ha);
									IntVar ve = e_row.get(ha);
									model.arithm(ve, "<=", 
											vf.mul(ha.getPoints().size())
											.intVar()).post();
									model.arithm(vf, "<=", ve).post();
								}
							}
						}
					}
				}
				// 6 // relation between f_vars and e_vars for points
				{
					for (Handle hr : redex_handles) {
						Map<Handle, IntVar> f_row = f_vars.get(hr);
						Collection<? extends Point> ps = hr.getPoints();
						for (Handle ha : agent_handles) {
							IntVar vf = f_row.get(ha);
							int k = 0;
							IntVar[] vars = new IntVar[ps
									.size() * ha.getPoints().size()];
							for (Point pa : ha.getPoints()) {
								Map<LinkEntity, IntVar> e_row = e_vars
										.get(pa);
								for (Point pr : ps) {
									IntVar ve = e_row.get(pr);
									vars[k++] = ve;
									model.arithm(ve, "<=", vf).post();
								}
								// // constraint 10
								if (hr.isEdge()) {
									model.arithm(
											vf.add(e_vars.get(pa).get(ha)).intVar(), 
											"<=", 1).post();
								}
							}
							if (!ps.isEmpty() || !ha.getPoints().isEmpty())
								model.sum(vars, ">=", vf).post();
						}
					}
				}
				// 7 // flux separation
				{
					/*
					 * Redex handles can be matched to at most one handle of the
					 * redex
					 */
					IntVar[] vars = new IntVar[ahs];
					for (Handle hr : redex_handles) {
						Map<Handle, IntVar> f_row = f_vars.get(hr);
						model.sum(f_row.values().toArray(vars), "<=", 1)
							.post();
					}
				}

				// 8 // handles type
				{
					/*
					 * Redex handles can not be matched to agent outers.
					 */
					ListIterator<Handle> ir1 = redex_handles.listIterator(0);
					while (ir1.hasNext()) {
						Handle hr1 = ir1.next();
						Map<Handle, IntVar> f_row1 = f_vars.get(hr1);
						if (hr1.isEdge()) {
							for (Handle ha : agent_handles) {
								// edges belongs to edges
								if (ha.isOuterName())
									model.arithm(f_row1.get(ha), "=", 0)
											.post();
							}
						}
						ListIterator<Handle> ir2 = redex_handles
								.listIterator(ir1.nextIndex());
						while (ir2.hasNext()) {
							Handle hr2 = ir2.next();
							Map<Handle, IntVar> f_row2 = f_vars
									.get(hr2);
							if (hr1.isEdge() != hr2.isEdge()) {
								for (Handle ha : agent_handles) {
									model.arithm(f_row1.get(ha)
											.add(f_row2.get(ha)).intVar(), 
											"<=", 1).post();
								}
							}
						}
					}
				}
				// 9 // embeddings are injective w.r.t edges
				{
					IntVar[] vars = new IntVar[redex_edges
							.size()];
					for (Handle ha : agent_handles) {
						int k = 0;
						for (Handle hr : redex_edges) {
							vars[k++] = f_vars.get(hr).get(ha);
						}
						model.sum(vars, "<=", 1).post();
					}
				}
				// 10 // points of handles mapped to redex edges can not bypass
				// it
				// ! merged with constraint 6 //
				/*
				 * { for (Handle hr : redex_edges) { Map<Handle,
				 * IntegerVariable> f_row = f_vars.get(hr); for (Handle ha :
				 * agent_handles) { IntegerVariable vf = f_row.get(ha); for
				 * (Point p : ha.getPoints()) { model.addConstraint(Choco.geq(1,
				 * Choco.sum(e_vars.get(p).get(ha), vf))); } } } }
				 */

				// INTERPLAY CONSTRAINTS //////////////////////////////////////
				{
					// bound nodes and their ports
					for (Node ni : agent_nodes) {
						Map<PlaceEntity, IntVar> p_row = p_vars
								.get(ni);
						for (Node nj : redex_nodes) {
							IntVar m = p_row.get(nj);
							boolean comp = areMatchable(agent, ni, redex, nj);
							// ! Place constraint 2 //
							if (comp) {
								Integer w = matchingWeight(agent, ni, redex, nj);
								if (w != 0) {
									weights.add(m.mul(w).intVar());
								}
							} else {
								model.arithm(m, "=", 0).post();
							}
							for (int i = ni.getControl().getArity() - 1; 0 <= i; i--) {
								Map<LinkEntity, IntVar> e_row = e_vars
										.get(ni.getPort(i));
								for (int j = nj.getControl().getArity() - 1; 0 <= j; j--) {
									if (comp && i == j) {
										/* ni <-> nj iff ni[k] <-> nj[k] */
										model.arithm(m, "=", 
												e_row.get(nj.getPort(j))).post();
									} else {
										/* ni[f] <!> nj[g] if ni<!>nj || f != g */
										model.arithm(e_row.get(nj.getPort(j)), 
												"=", 0).post();
									}
								}
							}
						}
					}
				}
				{
					for (Node ni : agent_nodes) {
						// sum over ni anchestors and redex roots
						Collection<Parent> ancs = agent_ancestors.get(ni);
						IntVar[] vars2 = new IntVar[(1 + ancs .size()) * rss];
						int k2 = 0;
						for (Parent f : ancs) {
							Map<PlaceEntity, IntVar> row = p_vars
									.get(f);
							for (Site g : redex_sites) {
								vars2[k2++] = row.get(g);
							}
						}
						{
							Map<PlaceEntity, IntVar> row = p_vars
									.get(ni);
							for (Site g : redex_sites) {
								vars2[k2++] = row.get(g);
							}
						}
						IntVar sum2 = model.intVar(0);
						for (IntVar v : vars2) {
							sum2 = sum2.add(v).intVar();
						}

						for (Port pi : ni.getPorts()) {
							Map<LinkEntity, IntVar> row = e_vars
									.get(pi);
							// all the redex points
							// IntegerVariable[] vars3 = new
							// IntegerVariable[rprs];
							IntVar[] vars4 = new IntVar[rins];
							// int k3 = 0;
							int k4 = 0;
							for (Point in : redex.getInnerNames()) {
								IntVar var = row.get(in);
								vars4[k4++] = var;
							}
							/*
							 * a port can match an inner name in the redex if
							 * its node is in the params.
							 */
							model.sum(vars4, "<=", sum2).post();
						}
					}
				}

				// weight var
				{
					IntVar[] vars = new IntVar[weights.size()];
					vars = weights.toArray(vars);
					model.sum(vars, "=", weight).post();
				}
				// END OF CONSTRAINTS /////////////////////////////////////////

				model.setObjective(maximizing, weight);
				Solver solver = model.getSolver();

				return solver;
			}

			@Override
			public boolean hasNext() {
				if (mayHaveNext && nextMatch == null) {
					fetchSolution();
				}
				return mayHaveNext && nextMatch != null;
			}

			@Override
			public WeightedMatch next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				WeightedMatch res = nextMatch;
				nextMatch = null;
				return res;
			}

			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("");
			}

			private void noMoreSolutions() {
				this.mayHaveNext = false;
				this.solver.hardReset();
			}

			private void fetchSolution() {
				boolean first = firstRun;
				firstRun = false;
				if (DEBUG_PRINT_SOLUTION_FETCH)
					System.out.println("fetch solution has been invoked...");
				// look for a solution for the CSP
				if (!solver.solve()) {	
					if (DEBUG_PRINT_SOLUTION_FETCH)
						System.out
								.println("...but no more solutions where found.");
					noMoreSolutions();
					return;
				}
				
				int mWeight = 0;
				
				if (DEBUG_PRINT_CSP_SOLUTIONS) {
					System.out.println("Solution: #"
							+ solver.getSolutionCount());
					System.out.print('\n');
					int p_cell_width[] = new int[1 + rrs + rns + rss];
					p_cell_width[0] = 6;
					for (Node n : agent_nodes) {
						p_cell_width[0] = Math.max(p_cell_width[0], n
								.toString().length());
					}
					System.out.printf("%-" + p_cell_width[0] + "s|", "P_VARS");
					int c = 1;
					for (int k = 0; k < rrs; k++, c++) {
						String s = "R_" + k;
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c] + "s|", s);
					}
					for (Node n : redex_nodes) {
						String s = n.toString();
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c++] + "s|", s);
					}
					for (int k = 0; k < rss; k++, c++) {
						String s = "S_" + k;
						p_cell_width[c] = s.length();
						System.out.printf("%-" + p_cell_width[c] + "s|", s);
					}
					for (int i = 0; i < ars; i++) {
						System.out.printf("\nR_%-" + (p_cell_width[0] - 2)
								+ "d|", i);
						c = 1;
						Root ri = agent_roots.get(i);
						Map<PlaceEntity, IntVar> row = p_vars.get(ri);
						for (int j = 0; j < rrs; j++) {
							Root rj = redex_roots.get(j);
							IntVar v = findVariable(row.get(rj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
						for (Node nj : redex_nodes) {
							IntVar v = findVariable(row.get(nj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
						for (int j = 0; j < rss; j++) {
							Site sj = redex_sites.get(j);
							IntVar v = findVariable(row.get(sj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
					}
					for (Node ni : agent_nodes) {
						System.out.printf("\n%-" + p_cell_width[0] + "s|", ni);
						c = 1;
						Map<PlaceEntity, IntVar> row = p_vars.get(ni);
						for (int j = 0; j < rrs; j++) {
							Root rj = redex_roots.get(j);
							IntVar v = findVariable(row.get(rj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
						for (Node nj : redex_nodes) {
							IntVar v = findVariable(row.get(nj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
						for (int j = 0; j < rss; j++) {
							Site sj = redex_sites.get(j);
							IntVar v = findVariable(row.get(sj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
					}
					for (int i = 0; i < ass; i++) {
						System.out.printf("\nS_%-" + (p_cell_width[0] - 2)
								+ "d|", i);
						c = 1;
						Root ri = agent_roots.get(i);
						Map<PlaceEntity, IntVar> row = p_vars.get(ri);
						for (int j = 0; j < rrs; j++) {
							System.out.printf("%" + p_cell_width[c++] + "d|",
									' ');
						}
						for (int j = 0; j < rns; j++) {
							System.out.printf("%" + p_cell_width[c++] + "d|",
									' ');
						}
						for (int j = 0; j < rss; j++) {
							Site sj = redex_sites.get(j);
							IntVar v = findVariable(row.get(sj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + p_cell_width[c++] + "d|",
									v.getValue());
						}
					}
					
					System.out.println('\n');
					
					int f_cell_width[] = new int[1 + ahs];
					int e_cell_width[] = new int[1 + rps + ahs];
					f_cell_width[0] = 6;
					for (Handle n : redex_handles) {
						f_cell_width[0] = Math.max(f_cell_width[0], n
								.toString().length());
					}
					e_cell_width[0] = f_cell_width[0];
					for (Point n : agent_points) {
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
					for (Point pi : agent_points) {
						System.out.printf("\n%-" + e_cell_width[0] + "s|", pi);
						c = 1;
						Map<LinkEntity, IntVar> row = e_vars.get(pi);
						for (Point pj : redex_points) {
							IntVar v = findVariable(row.get(pj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + e_cell_width[c++] + "d|",
									v.getValue());
						}
						for (Handle hj : agent_handles) {
							if (row.containsKey(hj)) {
								IntVar v = findVariable(row.get(hj).getName(), 
										model.getVars()).asIntVar();
								System.out.printf("%" + e_cell_width[c++]
										+ "d|", v.getValue());
							} else {
								System.out.printf("%" + e_cell_width[c++]
										+ "c|", ' ');
							}
						}
					}
					for (Handle hi : redex_handles) {
						System.out.printf("\n%-" + e_cell_width[0] + "s|", hi);
						c = 1;
						Map<LinkEntity, IntVar> row = e_vars.get(hi);
						for (int j = rps; 0 < j; j--) {
							System.out.printf("%" + e_cell_width[c++] + "c|",
									' ');
						}
						for (Handle hj : agent_handles) {
							IntVar v = findVariable(row.get(hj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + e_cell_width[c++] + "d|",
									v.getValue());
						}
					}

					System.out.println('\n');

					System.out.printf("%" + f_cell_width[0] + "s|", "F_VARS");
					c = 1;
					for (Handle h : agent_handles) {
						String s = h.toString();
						f_cell_width[c] = s.length();
						System.out.printf("%-" + f_cell_width[c++] + "s|", s);
					}
					for (Handle hi : redex_handles) {
						System.out.printf("\n%-" + f_cell_width[0] + "s|", hi);
						c = 1;
						Map<Handle, IntVar> row = f_vars.get(hi);
						for (Handle hj : agent_handles) {
							IntVar v = findVariable(row.get(hj).getName(), 
									model.getVars()).asIntVar();
							System.out.printf("%" + f_cell_width[c++] + "d|",
									v.getValue());
						}
					}					

					IntVar v = findVariable(weight.getName(), 
							model.getVars()).asIntVar();
					mWeight = v.getValue();
					System.out.printf("\n\nOPT = %d\n\n",mWeight);
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
				Bigraph prm = new Bigraph(agent.signature);
				Bigraph id = new Bigraph(agent.signature);
				// an injective map from redex's nodes to rdx's ones
				BidMap<Node, Node> nEmb = new BidMap<>(rns);

				// replicated sites and roots
				EditableSite ctx_sites_dic[] = new EditableSite[rrs];
				EditableSite rdx_sites_dic[] = new EditableSite[rss];
				EditableRoot rdx_roots_dic[] = new EditableRoot[rrs];
				EditableRoot prm_roots_dic[] = new EditableRoot[rss];
				EditableSite prm_sites_dic[] = new EditableSite[ass];

				// replicated handles lookup tables
				Map<Handle, EditableHandle> ctx_hnd_dic = new IdentityHashMap<>();
				Map<Handle, EditableHandle> rdx_hnd_dic = new IdentityHashMap<>();
				Map<Handle, EditableHandle> prm_hnd_dic = new IdentityHashMap<>();

				Map<Handle, EditableHandle> handle_img = new IdentityHashMap<>(
						rhs);

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

					// @Override
					// public String toString() {
					// return "[p=" +this.p + ", c=" + this.c + ", i=" + this.i
					// + "]";
					// }
				}
				Deque<VState> q = new ArrayDeque<>();

				for (EditableOuterName o1 : agent.outers.values()) {
					EditableOuterName o2 = o1.replicate();
					ctx.outers.put(o2.getName(), o2);
					o2.setOwner(ctx);
					ctx_hnd_dic.put(o1, o2);
				}
				for (EditableOuterName o0 : redex.outers.values()) {
					// replicate the handle
					String name = o0.getName();
					EditableOuterName o2 = new EditableOuterName(name);
					rdx.outers.put(name, o2);
					o2.setOwner(rdx);
					rdx_hnd_dic.put(o0, o2);
					// update ctx inner face
					EditableInnerName i1 = new EditableInnerName(name);
					ctx.inners.put(name, i1);
					// find the handle for i1
					EditableHandle h1 = handle_img.get(o0);
					if (h1 == null) {
						// cache miss
						Map<Handle, IntVar> f_row = f_vars.get(o0);
						for (Handle h : agent_handles) {
							IntVar var = findVariable(f_row.get(h).getName(),
									model.getVars()).asIntVar();
							if (var.getValue() == 1) {
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
							Map<Handle, IntVar> f_row = f_vars.get(h0);
							for (Handle h : agent_handles) {
							IntVar var = findVariable(f_row.get(h).getName(),
									model.getVars()).asIntVar();
								if (var.getValue() == 1) {
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
					o2.setOwner(prm);
					prm.outers.put(name, o2);
				}
				for (EditableInnerName i1 : agent.inners.values()) {
					String name1 = i1.getName();
					EditableInnerName i2 = new EditableInnerName(name1);
					EditableHandle h2 = null;
					Map<LinkEntity, IntVar> row = e_vars.get(i1);
					EditableHandle h1 = i1.getHandle();

					IntVar var = findVariable(row.get(h1).getName(),
							model.getVars()).asIntVar();
					if (var.getValue() == 1) {
						/*
						 * this inner name bypasses the redex. Checks if the
						 * handle already has an image in this parameter
						 * otherwise creates a suitable name in prm. This may
						 * require some additional step if the handle already
						 * has an image in the context.
						 */
						h2 = prm_hnd_dic.get(h1);
						if (h2 == null) {
							EditableHandle h3 = ctx_hnd_dic.get(h1);
							if (h3 != null) {
								/*
								 * h1 has an image in the context, add an inner
								 * to it and link it down to the parameter
								 * passing through id
								 */
								EditableInnerName i3 = new EditableInnerName();
								i3.setHandle(h3);
								String name2 = i3.getName();
								ctx.inners.put(name2, i3);
								// add it also to id
								EditableOuterName o4 = new EditableOuterName(
										name2);
								o4.setOwner(id);
								id.outers.put(name2, o4);
								EditableInnerName i4 = new EditableInnerName(
										name2);
								i4.setHandle(o4);
								id.inners.put(name2, i4);

								EditableOuterName o2 = new EditableOuterName(
										name2);
								o2.setOwner(prm);
								prm.outers.put(name2, o2);
								h2 = o2;
							} else {
								/*
								 * this handle is not required by the context,
								 * use an edge to reduce the interface of id
								 */
								h2 = new EditableEdge(prm);
							}
							prm_hnd_dic.put(h1, h2);
						}
					} else {
						for (InnerName i0 : redex.inners.values()) {
							IntVar v = findVariable(row.get(i0).getName(),
									model.getVars()).asIntVar();
							if (v.getValue() == 1) {
								/*
								 * this port is attached to the redex inner i0.
								 * Add it as an outer of prm, if it is not
								 * already present, and link it to p2 resp.
								 */
								String name = i0.getName();
								h2 = prm.outers.get(name);
								if (h2 == null) {
									EditableOuterName o2 = new EditableOuterName(
											name);
									o2.setOwner(prm);
									prm.outers.put(name, o2);
									h2 = o2;
								}
								break;
							}
						}
					}
					i2.setHandle(h2);
					prm.inners.put(name1, i2);
				}
				for (EditableRoot r0 : agent.roots) {
					q.add(new VState(ctx, null, r0));
				}
				Collection<Root> unseen_rdx_roots = new LinkedList<>(
						redex_roots);
				while (!q.isEmpty()) {
					VState v = q.poll();
					if (v.b == ctx) {
						// the entity visited belongs to the context
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
						Collection<Child> rcs = new HashSet<>(p1.getChildren());
						Map<PlaceEntity, IntVar> p_row = p_vars
								.get(p1);
						Iterator<Root> ir = unseen_rdx_roots.iterator();
						while (ir.hasNext()) {
							Root r0 = ir.next();
							// make a site for each root whose image is p1
							IntVar var = findVariable(p_row.get(r0).getName(),
									model.getVars()).asIntVar();
							if (var.getValue() == 1) {
								// root_img.put(r0, p1);
								ir.remove();
								int k = redex_roots.indexOf(r0);
								EditableSite s = new EditableSite();
								s.setParent(p2);
								ctx_sites_dic[k] = s;
								EditableRoot r2 = new EditableRoot();
								r2.setOwner(rdx);
								rdx_roots_dic[k] = r2;
								for (Child c0 : r0.getChildren()) {
									Iterator<Child> ic = rcs.iterator();
									boolean notMatched = true;
									while (ic.hasNext()) {
										Child c1 = ic.next();
										IntVar var_tmp = findVariable(
												p_vars.get(c1).get(c0)
												.getName(),
												model.getVars()).asIntVar();
										if (var_tmp.getValue() == 1) {
											notMatched = false;
											q.add(new VState(rdx, r2, c1, c0));
											ic.remove();
										}
									}
									if (notMatched && c0.isSite()) {
										// closed site
										q.add(new VState(rdx, r2, null, c0));
									}
								}
							}
						}
						for (Child c1 : rcs) {
							q.add(new VState(ctx, p2, c1));
						}
					} else if (v.b == rdx) {
						// the entity visited is the image of something in the
						// redex
						if (v.i.isNode()) {
							EditableNode n0 = (EditableNode) v.i;
							EditableNode n1 = (EditableNode) v.c;
							EditableNode n2 = n1.replicate();
							nEmb.put(n0, n1);
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
							Collection<Child> cs1 = new HashSet<>(
									n1.getChildren());
							for (Child c0 : n0.getChildren()) {
								Iterator<Child> ic = cs1.iterator();
								boolean notMatched = true;
								while (ic.hasNext()) {
									Child c1 = ic.next();
									IntVar var_tmp = findVariable(
											p_vars.get(c1).get(c0).getName(),
											model.getVars()).asIntVar();
									if (var_tmp.getValue() == 1) {
										notMatched = false;
										q.add(new VState(rdx, n2, c1, c0));
										ic.remove();
									}
								}
								if (notMatched && c0.isSite()) {
									// closed site
									q.add(new VState(rdx, n2, null, c0));
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
							// if (neededParam[k]) {
							if (prm_roots_dic[k] == null) {
								prm_roots_dic[k] = new EditableRoot(prm);
							}
							EditableRoot r2 = prm_roots_dic[k];
							if (v.c != null)
								q.add(new VState(prm, r2, v.c));
							// }
						}
					} else {
						// the entity visited belongs to some parameter
						if (v.c.isNode()) {
							EditableNode n1 = (EditableNode) v.c;
							EditableNode n2 = n1.replicate();
							n2.setParent(v.p);
							for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
								EditablePort p1 = n1.getPort(i);
								EditablePort p2 = n2.getPort(i);

								EditableHandle h2 = null;
								Map<LinkEntity, IntVar> row = e_vars.get(p1);
								EditableHandle h1 = p1.getHandle();

								IntVar var_tmp = findVariable(
									row.get(h1).getName(),
									model.getVars()).asIntVar();
								if (var_tmp.getValue() == 1) {
									/*
									 * this port bypasses the redex. Checks if
									 * the handle already has an image in this
									 * parameter otherwise creates a suitable
									 * name in prm. This may require some
									 * additional step if the handle already has
									 * an image in the context.
									 */
									h2 = prm_hnd_dic.get(h1);
									if (h2 == null) {
										EditableHandle h3 = ctx_hnd_dic.get(h1);
										if (h3 != null) {
											/*
											 * h1 has an image in the context,
											 * add an inner to it and link it
											 * down to the parameter passing
											 * through id
											 */
											EditableInnerName i3 = new EditableInnerName();
											i3.setHandle(h3);
											String name = i3.getName();
											ctx.inners.put(name, i3);
											// add it also to id
											EditableOuterName o4 = new EditableOuterName(
													name);
											o4.setOwner(id);
											id.outers.put(name, o4);
											EditableInnerName i4 = new EditableInnerName(
													name);
											i4.setHandle(o4);
											id.inners.put(name, i4);

											EditableOuterName o2 = new EditableOuterName(
													name);
											o2.setOwner(v.b);
											v.b.outers.put(name, o2);
											h2 = o2;
										} else {
											/*
											 * this handle is not required by
											 * the context, use an edge to
											 * reduce the interface of id
											 */
											h2 = new EditableEdge(prm);
										}
										prm_hnd_dic.put(h1, h2);
									}
								} else {
									for (InnerName i0 : redex.inners.values()) {
										IntVar var = findVariable(
											row.get(i0).getName(),
											model.getVars()).asIntVar();
										if (var.getValue() == 1) {
											/*
											 * this port is attached to the
											 * redex inner i0. Add it as an
											 * outer of prm, if it is not
											 * already present, and link it to
											 * p2 resp.
											 */
											String name = i0.getName();
											h2 = prm.outers.get(name);
											if (h2 == null) {
												EditableOuterName o2 = new EditableOuterName(
														name);
												o2.setOwner(v.b);
												v.b.outers.put(name, o2);
												h2 = o2;
											}
											break;
										}
									}
								}
								p2.setHandle(h2);
							}
							for (Child c1 : n1.getChildren()) {
								q.add(new VState(v.b, n2, c1));
							}
						} else {
							// v.c.isSite()
							EditableSite s1 = (EditableSite) v.c;
							EditableSite s2 = s1.replicate();
							s2.setParent(v.p);
							prm_sites_dic[agent_sites.indexOf(s1)] = s2;
						}
					}
				}

				ctx.sites.addAll(Arrays.asList(ctx_sites_dic));
				rdx.sites.addAll(Arrays.asList(rdx_sites_dic));
				rdx.roots.addAll(Arrays.asList(rdx_roots_dic));
				prm.roots.addAll(Arrays.asList(prm_roots_dic));
				prm.sites.addAll(Arrays.asList(prm_sites_dic));

				if (DEBUG_CONSISTENCY_CHECK) {
					if (!ctx.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (ctx)");
					}
					if (!rdx.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (rdx)");
					}
					if (!id.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (id)");
					}
					if (!prm.isConsistent()) {
						throw new RuntimeException("Inconsistent bigraph (prm)");
					}

				}
				this.nextMatch = new WeightedMatch(ctx, rdx, id, prm, nEmb,mWeight);
			}
		}
	}
}
