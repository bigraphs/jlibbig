package it.uniud.mads.jlibbig.core.ldb;

import java.util.*;

import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableInPort;
import it.uniud.mads.jlibbig.core.ldb.EditableNode.EditableOutPort;
import it.uniud.mads.jlibbig.core.ldb.InPort;
import it.uniud.mads.jlibbig.core.ldb.OutPort;
import it.uniud.mads.jlibbig.core.ldb.EditableHandle;
import it.uniud.mads.jlibbig.core.util.BidMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

/**
 * Provides services for computing matches of bigraphs with abstract
 * internal names; matches are described by {@link DirectedMatch}.
 * 
 * The field {@link #DEFAULT} refers to a default instance of the matcher.
 * 
 * The standard matching of nodes can be changed by re-implementing the
 * protected method {@link #areMatchable}.
 */
public class DirectedMatcher implements it.uniud.mads.jlibbig.core.DirectedMatcher<DirectedBigraph, DirectedBigraph> {

	private final static boolean DEBUG = Boolean
			.getBoolean("it.uniud.mads.jlibbig.debug")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.debug.matchers");
	private final static boolean DEBUG_PRINT_CSP_SOLUTIONS = DEBUG;
	private final static boolean DEBUG_PRINT_SOLUTION_FETCH = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = Boolean
			.getBoolean("it.uniud.mads.jlibbig.consistency")
			|| Boolean.getBoolean("it.uniud.mads.jlibbig.consistency.matchers");
	//private final static boolean DEBUG_TIMESTAMPS = Boolean.getBoolean("it.uniud.mads.jlibbig.timestamps.matchers");
	/**
	 * The default instance of the matcher.
	 */
	public final static DirectedMatcher DEFAULT = new DirectedMatcher();

	@Override
	public Iterable<? extends DirectedMatch> match(DirectedBigraph agent, DirectedBigraph redex) {
		return new MatchIterable(agent, redex);
	}

	/**
	 * The method is called to asses if a pair of nodes (one from the redex and
	 * the other from the agent bigraph) is a potential match or not. The
	 * default implementation requires the two nodes to have the same control.
	 * 
	 * An inheriting class can strengthen or weaken this constrain but has to
	 * ensure that matchable nodes have at least the same number of ports due to
	 * wiring preservation under matching.
	 * 
	 * @param agent
	 *            the bigraph describing the agent.
	 * @param fromAgent
	 *            the node from the agent bigraph.
	 * @param redex
	 *            the bigraph describing the redex.
	 * @param fromRedex
	 *            the node from the redex bigraph.
	 * @return a boolean indicating whether the two nodes are can be matched.
	 */
	protected boolean areMatchable(DirectedBigraph agent, Node fromAgent,
			DirectedBigraph redex, Node fromRedex) {
		return fromAgent.getControl().equals(fromRedex.getControl());
	}

	private class MatchIterable implements Iterable<DirectedMatch> {
		
		final DirectedBigraph agent, redex;

		// boolean agent_ancestors_is_empty = true;
		// final Map<Child, Collection<Parent>> agent_ancestors;

		// caches some collections of entities (e.g. nodes and edges are
		// computed on the fly)
		final List<? extends Root> agent_roots;
		final List<? extends Site> agent_sites;
		final Collection<? extends Node> agent_nodes;
		final Collection<InPort> agent_inports;
		final Collection<OutPort> agent_outports;
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
		final int ars, ans, ass, ahs, aps, aiprs, aoprs, rrs, rns, rss, rhs, rps, rprs, rins;

		private MatchIterable(DirectedBigraph agent, DirectedBigraph redex) {
			// boolean[] neededParams) {
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
			// Handles are edges, negative ports, downwards inner interface and 
			// upwards outer interface
			this.agent_handles = new LinkedList<Handle>(agent_edges);
			for (Node n : agent_nodes) {
				agent_handles.addAll(n.getInPorts());
			}
			agent_handles.addAll(agent.getInnerInterface().getDesc().values());
			agent_handles.addAll(agent.getOuterInterface().getAsc().values());

			ars = agent_roots.size();
			ans = agent_nodes.size();
			ass = agent_sites.size();
			ahs = agent_handles.size();

			this.agent_inports = new HashSet<>(2*ans);
			this.agent_outports = new HashSet<>(2*ans);
			for (Node n : agent_nodes) {
				agent_outports.addAll(n.getOutPorts());
				agent_inports.addAll(n.getInPorts());
			}
			aiprs = agent_inports.size();
			aoprs = agent_outports.size();

			// points are positive ports, upwards inner interface and downwards 
			// outer interface
			int innerup_size = agent.getInnerInterface().getAsc().size();
			int outerdown_size = agent.getOuterInterface().getDesc().size();
			this.agent_points = new HashSet<>(ans + innerup_size + outerdown_size);
			for (Node n : agent_nodes) {
				agent_points.addAll(n.getOutPorts());
			}
			agent_points.addAll(agent.getInnerInterface().getAsc().values());
			agent_points.addAll(agent.getOuterInterface().getDesc().values());
			aps = agent_points.size();

			// this.agent_ancestors = new HashMap<>(ans);

			this.redex_roots = redex.getRoots();
			this.redex_sites = redex.getSites();
			this.redex_nodes = redex.getNodes();
			this.redex_edges = redex.getEdges();//redex_nodes);
			// handles are edges, negative ports, downwards inner interface and 
			// upwards outer interface
			this.redex_handles = new LinkedList<Handle>(redex_edges);
			for (Node n : redex_nodes) {
				redex_handles.addAll(n.getInPorts());
			}
			redex_handles.addAll(redex.getInnerInterface().getDesc().values());
			redex_handles.addAll(redex.getOuterInterface().getAsc().values());

			rrs = redex_roots.size();
			rns = redex_nodes.size();
			rss = redex_sites.size();
			rhs = redex_handles.size();

			// points are positive ports, upwards inner interface and downwards 
			// outer interface
			innerup_size = redex.getInnerInterface().getAsc().size();
			outerdown_size = redex.getOuterInterface().getDesc().size();
			this.redex_points = new HashSet<>(rns + innerup_size + outerdown_size);
			for (Node n : redex_nodes) {
				redex_points.addAll(n.getOutPorts());
			}
			// only ports
			int rprts = 0;
			for (Node n : redex_nodes) {
				rprts += n.getOutPorts().size() + n.getInPorts().size();
			}
			rprs = rprts;
			redex_points.addAll(redex.getInnerInterface().getAsc().values());
			redex_points.addAll(redex.getOuterInterface().getDesc().values());
			rps = redex_points.size();
			rins = redex.getInnerInterface().getAsc().size() + 
				redex.getInnerInterface().getDesc().size();

			// this.neededParam = new boolean[rss];
			// for (int i = 0; i < this.neededParam.length; i++) {
			// this.neededParam[i] = (neededParams == null) || neededParams[i];
			// }
		}

		@Override
		public Iterator<DirectedMatch> iterator() {
			return new MatchIterator();
		}

		private class MatchIterator implements Iterator<DirectedMatch> {

			private boolean mayHaveNext = true;
			private boolean firstRun = true;

			private DirectedMatch nextMatch = null;

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
						 * for (Root j : redex_roots) { IntVar var =
						 * Choco.makeBooleanVar(ki + "_" + kj++);
						 * model.addVariable(var); row.put(j, var); } /*for
						 * (Node j : redex_nodes) { IntVar var =
						 * Choco.makeBooleanVar(ki + "_" + kj++);
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

				// PLACE CONSTRAINTS //////////////////////////////////////////

				// 2 // M_ij = 0 if nodes are different in the sense of this.eq
				// merged with interplay constraints
				/*
				 * { for (Node i : agent_nodes) { Map<PlaceEntity,
				 * IntVar> row = p_vars.get(i); for (Node j :
				 * redex_nodes) { IntVar var = row.get(j); if
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
					 * IntVar> row = p_vars.get(i); IntVar[]
					 * vars = new IntVar[rss]; int k = 0; for
					 * (PlaceEntity j : redex_sites) { vars[k++] = row.get(j); }
					 * IntegerExpressionVariable c = Choco.mult(rrs,
					 * Choco.sum(vars));
					 * 
					 * vars = new IntVar[rrs]; k = 0; for (Root j :
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
							IntVar[] vars = new IntVar[cf
									.size() * cg.size()];
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
							IntVar[] vars = new IntVar[cf
									.size() * cg.size()];
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
/* The class bigraph provides ancestor for us				
 				if (agent_ancestors_is_empty) {
					
					 * acquire agent_ancestors and re-check if it still is empty
					 * if this is the case, descends the agent parent map and
					 * builds agent_ancestors
					 
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
				}*/
				{
					for (Node i : agent_nodes) {
						Collection<Parent> ancs = agent.getAncestors(i);
						IntVar[] vars = new IntVar[(ancs.size()) * rss];
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
						if (ha.getPoints().size() > 0) {
							Collection<? extends Point> ps = ha.getPoints();
							IntVar[] vars1 = new IntVar[rhs
									+ ps.size()];
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
					IntVar[] vars_tmp = new IntVar[aps];
					for (Point pr : redex_points) {
						if (pr.isPort() || pr.isInnerName()) {
							int k = 0;
							for (Point pa : agent_points) {
								vars_tmp[k++] = e_vars.get(pa).get(pr);
							}
						}
						// Remove any null elements
						IntVar[] vars = Arrays.stream(vars_tmp).
							filter(Objects::nonNull).toArray(IntVar[]::new);
						
                        if (vars.length > 0)
                            model.sum(vars, "<=", 1).post();
					}
				}

				{
					for (Point pr : redex_points) {
						if (pr.isInnerName() 
								&& redex.outers.getDesc().values().contains(pr)) {
							for (Point pa : agent_points) {
								if (pa.isPort()) {
									model.arithm(e_vars.get(pa).get(pr), "=", 0).post();
								}
							}
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
							if (!ps.isEmpty() && !ha.getPoints().isEmpty())
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
					 * Redex handles can not be matched to agent outers
					 */
					ListIterator<Handle> ir1 = redex_handles.listIterator(0);
					while (ir1.hasNext()) {
						Handle hr1 = ir1.next();
						Map<Handle, IntVar> f_row1 = f_vars.get(hr1);
						if (hr1.isEdge()) {
							for (Handle ha : agent_handles) {
								// edges belongs to edges
								if (ha.isOuterName() || ha.isPort()) {
									model.arithm(f_row1.get(ha), "=", 0)
										.post();
								}
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
					if (redex_edges.size() != 0) {
						IntVar[] vars = new IntVar[redex_edges.size()];
						for (Handle ha : agent_handles) {
							int k = 0;
							for (Handle hr : redex_edges) {
								vars[k++] = f_vars.get(hr).get(ha);
							}
							model.sum(vars, "<=", 1).post();
						}
					}
				}
				// 10 // points of handles mapped to redex edges can not bypass
				// it
				// ! merged with constraint 6 //
				/*
				 * { for (Handle hr : redex_edges) { Map<Handle,
				 * IntVar> f_row = f_vars.get(hr); for (Handle ha :
				 * agent_handles) { IntVar vf = f_row.get(ha); for
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
							if (!comp) {
								model.arithm(m, "=", 0).post();
							}
							for (int i = ni.getControl().getArityOut() - 1; 0 <= i; i--) {
								Map<LinkEntity, IntVar> e_row = e_vars
										.get(ni.getOutPort(i));
								for (int j = nj.getControl().getArityOut() - 1; 0 <= j; j--) {
									if (comp && i == j) {
										/* ni <-> nj iff ni[k] <-> nj[k] */
										model.arithm(e_row.get(nj.getOutPort(j)),
												"=", m).post();
									} else {
										/* ni[f] <!> nj[g] if ni<!>nj || f != g */
										model.arithm(e_row.get(nj.getOutPort(j)),
												"=", 0).post();
									}
								}
							}

							for (int i = nj.getControl().getArityIn() - 1; 0 <= i; i--) {
								Map<Handle, IntVar> f_row = f_vars
										.get(nj.getInPort(i));
								for (int j = ni.getControl().getArityIn() - 1; 0 <= j; j--) {
									if (comp && i == j) {
										/* ni <-> nj iff ni[k] <-> nj[k] */
										model.arithm(f_row.get(ni.getInPort(j)),
												"=", m).post();
									} else {
										/* ni[f] <!> nj[g] if ni<!>nj || f != g */
										model.arithm(f_row.get(ni.getInPort(j)),
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
						Collection<Parent> ancs = agent.getAncestors(ni);
						IntVar[] vars2 = new IntVar[(1 + ancs
								.size()) * rss];
						int k2 = 0;
						for (Parent f : ancs) {
							Map<PlaceEntity, IntVar> row = p_vars
									.get(f);
							for (Site g : redex_sites) {
								vars2[k2++] = row.get(g);
							}
						}
						{// ancs does not include ni
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

						for (OutPort pi : ni.getOutPorts()) {
							Map<LinkEntity, IntVar> row = e_vars
									.get(pi);
							// all the redex points
							// IntVar[] vars3 = new
							// IntVar[rprs];
							IntVar[] vars4 = new IntVar[
								redex.inners.getAsc().size()];
							// int k3 = 0;
							int k4 = 0;
							for (Point in : redex.inners.getAsc().values()) {
								IntVar var = row.get(in);
								vars4[k4++] = var;
							}

							// for (Handle h : redex.inners.getDesc().values()) {
							// 	for (Point in : h.getPoints()) {
							// 		IntVar var = row.get(in);
							// 		vars4[k4++] = var;
							// 	}
							// }
							/*
							 * a port can match an inner name in the redex if
							 * its node is in the params.
							 */
							model.sum(vars4, "<=", sum2).post();
						}

						// for (InPort pi : ni.getInPorts()) {
						// 	Map<LinkEntity, IntVar> row = e_vars
						// 			.get(pi);
						// 	// all the redex points
						// 	// IntVar[] vars3 = new
						// 	// IntVar[rprs];
						// 	IntVar[] vars4 = new IntVar[
						// 		redex.inners.getDesc().size()];
						// 	// int k3 = 0;
						// 	int k4 = 0;
						// 	for (Handle h : redex.inners.getDesc().values()) {
						// 		for (Point in : h.getPoints()) {
						// 			IntVar var = row.get(in);
						// 			vars4[k4++] = var;
						// 		}
						// 	}
						// 	/*
						// 	 * a port can match an inner name in the redex if
						// 	 * its node is in the params.
						// 	 */
						// 	model.sum(vars4, "<=", sum2).post();
						// }
					}
				}
				// END OF CONSTRAINTS /////////////////////////////////////////
				
				return model.getSolver();
			}

			@Override
			public boolean hasNext() {
				if (mayHaveNext && nextMatch == null) {
					fetchSolution();
				}
				return mayHaveNext && nextMatch != null;
			}

			@Override
			public DirectedMatch next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				DirectedMatch res = nextMatch;
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
				firstRun = false;
				if (DEBUG_PRINT_SOLUTION_FETCH)
					System.out.println("fetch solution has been invoked...");
				// look for a solution for the CSP
				boolean hasSolution = solver.solve();
				if (hasSolution) {
					if (DEBUG_PRINT_SOLUTION_FETCH)
						System.out.println("...but no more solutions where found.");
					noMoreSolutions();
					return;
				}
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
					System.out.println('\n');
				}

				/*
				 * Visit the agent and clones it adding replicas to context,
				 * redex or params bigraphs depending on the seolution of the
				 * CSP above
				 */

				// context
				DirectedBigraph ctx = new DirectedBigraph(agent.signature);
				// redex
				DirectedBigraph rdx = new DirectedBigraph(agent.signature);
				// parameters
				DirectedBigraph prm = new DirectedBigraph(agent.signature);
				DirectedBigraph id = new DirectedBigraph(agent.signature);
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

				// replicated points lookup tables
				Map<EditablePoint, EditablePoint> ctx_pnt_dic = new IdentityHashMap<>();
				Map<EditablePoint, EditablePoint> rdx_pnt_dic = new IdentityHashMap<>();

				Map<EditablePoint, EditableHandle> prm_dic = new IdentityHashMap<>();

                Map<EditableNode, EditableNode> node_dic = new IdentityHashMap<>();

				Map<Handle, EditableHandle> handle_img = new IdentityHashMap<>(
						rhs);

				class VState {
					final PlaceEntity c; // the agent root/node to be visited
					final PlaceEntity i; // if present, is the image of c in the
											// redex
					final EditableParent p; // the replicated parent
					final DirectedBigraph b;

					VState(DirectedBigraph b, EditableParent p, PlaceEntity c) {
						this(b, p, c, null);
					}

					VState(DirectedBigraph b, EditableParent p, PlaceEntity c,
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

				// clone agent upwards outer interface into the context
				for (EditableOuterName o1 : agent.outers.getAsc().values()) {
					EditableOuterName o2 = o1.replicate();
					ctx.outers.addAsc(0, o2);
					o2.setOwner(ctx);
					ctx_hnd_dic.put(o1, o2);
				}
				// clone agent downwards outer interface into the context
				for (EditableInnerName i1 : agent.outers.getDesc().values()) {
					String name = i1.getName();
					EditableInnerName i2 = new EditableInnerName(name);
					ctx.outers.addDesc(0, i2);
					ctx_pnt_dic.put(i1, i2);
				}
				/*
				 * replicate outer name in the redex; create inner name
				 * in the context with the same name; get the image of the
				 * outer name from the CSP and set it as handle of the inner
				 * name. This effectively links outer names in the redex with
				 * inner names in the context.
				 * o+ redex -> i+ context
				 */
				for (EditableOuterName o0 : redex.outers.getAsc().values()) {
					// replicate the handle
					String name = o0.getName();
					EditableOuterName o2 = new EditableOuterName(name);
					rdx.outers.addAsc(0, o2);
					o2.setOwner(rdx);
					rdx_hnd_dic.put(o0, o2);
					// update ctx inner face
					EditableInnerName i1 = new EditableInnerName(name);
					ctx.inners.addAsc(0, i1);
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
				/*
				 * replicates descending outer names of the redex; gets 
				 * its handle; if a replica exists (from the CSP), sets the 
				 * handle of the replicated inner name to it and adds it to the
				 * redex; creates a new descending outer name in the context 
				 * with the same name.
				 * i- context <- o- redex
				 */
				for (EditableInnerName i0 : redex.outers.getDesc().values()) {
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
								IntVar var = findVariable(
										f_row.get(h).getName(),
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
					rdx.outers.addDesc(0, i2);
					rdx_pnt_dic.put(i0, i2);

					EditableOuterName o2 = new EditableOuterName(name);
					o2.setOwner(ctx);
					ctx.inners.addDesc(0, o2);

					// connect descending outer name of context w/ descending
					// inner name.
					for (EditableInnerName i3 : agent.outers.getDesc().values()) {
						EditablePoint pnt = ctx_pnt_dic.get(i3);
						if (pnt != null) {
							pnt.setHandle(o2);
						}
					}
				}
				/*
				 * replicates inner names of the redex; gets its handle;
				 * if a replica exists (from the CSP), sets the handle of the 
				 * replicated inner name to it and adds it to the redex;
				 * creates a new outer name in the parameter with the same 
				 * name.
				 * o+ param <- i+ redex
				 */
				for (EditableInnerName i0 : redex.inners.getAsc().values()) {
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
								IntVar var = findVariable(
										f_row.get(h).getName(),
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
					rdx.inners.addAsc(0, i2);
					rdx_pnt_dic.put(i0, i2);

					EditableOuterName o2 = new EditableOuterName(name);
					o2.setOwner(prm);
					prm.outers.addAsc(0, o2);
					prm_dic.put(i0, o2);
				}
				/* 
				 * replicate descending inner name in the redex; create 
				 * descending outer name in the parameter with the same name; 
				 * get the image of the inner name from the CSP and set it as 
				 * handle of the outer name. 
				 * This effectively links descending inner names in the redex 
				 * with descending outer names in the paremeter.
				 * i- redex -> o- param
				 */
				for (EditableOuterName i0 : redex.inners.getDesc().values()) {
					String name = i0.getName();
					EditableOuterName i2 = new EditableOuterName(name);
					rdx.inners.addDesc(0, i2);
					i2.setOwner(rdx);
					rdx_hnd_dic.put(i0, i2);

					EditableInnerName o1 = new EditableInnerName(name);
					prm.outers.addDesc(0, o1);
					// looks for an existing replica
					EditableHandle h1 = handle_img.get(i0);
					if (h1 == null) {
							// cache miss
							Map<Handle, IntVar> f_row = f_vars.get(i0);
							for (Handle h : agent_handles) {
								IntVar var = findVariable(
										f_row.get(h).getName(),
										model.getVars()).asIntVar();
								if (var.getValue() == 1) {
									h1 = h.getEditable();
									break;
								}
							}
							if (h1 == null) {
								h1 = new EditableEdge();
							}
							handle_img.put(i0, h1);
					}
					EditableHandle h2 = prm_hnd_dic.get(h1);
					if (h2 == null) {
						h2 = h1.replicate();
						h2.setOwner(prm);
						prm_hnd_dic.put(h1, h2);
					}
					o1.setHandle(h2);
				}
				/*
				 * for each inner name of the agent, creates a new inner
				 * name with the same name (for the parameter); gets the handle
				 * of the image of the current inner name from the CSP.
				 * If the inner name bypasses the redex, checks if its handle
				 * already has an image. If it has an image in the context, add
				 * an inner name to it and link it down to the parameter passing
				 * through id.
				 * If it does not bypass the redex, for each inner name of the
				 * redex that is mapped to the current inner name of the agent
				 * links the outer name of the parameter (with the same name)
				 * to the new inner name of the parameter.
				 */
				for (EditableInnerName i1 : agent.inners.getAsc().values()) {
					String name1 = i1.getName();
					EditableInnerName i2 = new EditableInnerName(name1);
					EditableHandle h2 = null;
					Map<LinkEntity, IntVar> row = e_vars.get(i1);
					EditableHandle h1 = i1.getHandle();
					ctx_pnt_dic.put(i1, i2);

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
								ctx.inners.addAsc(0, i3);
								// add it also to id
								EditableOuterName o4 = new EditableOuterName(
										name2);
								o4.setOwner(id);
								id.outers.addAsc(0, o4);
								EditableInnerName i4 = new EditableInnerName(
										name2);
								i4.setHandle(o4);
								id.inners.addAsc(0, i4);

								EditableOuterName o2 = new EditableOuterName(
										name2);
								o2.setOwner(prm);
								prm.outers.addAsc(0, o2);
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
						for (InnerName i0 : redex.inners.getAsc().values()) {
							IntVar v = findVariable(row.get(i0).getName(),
									model.getVars()).asIntVar();
							if (v.getValue() == 1) {
								/*
								 * this port is attached to the redex inner i0.
								 * Add it as an outer of prm, if it is not
								 * already present, and link it to p2 resp.
								 */
								String name = i0.getName();
								h2 = prm.outers.getAsc(0).get(name);
								if (h2 == null) {
									EditableOuterName o2 = new EditableOuterName(
											name);
									o2.setOwner(prm);
									prm.outers.addAsc(0, o2);
									h2 = o2;
								}
								break;
							}
						}
					}
					i2.setHandle(h2);
					prm.inners.addAsc(0, i2);
				}

				/*
				 * for each descending inner name of the agent, creates a
				 * new inner name with the same name (for the parameter); gets 
				 * the handle of the image of the current inner name from the CSP.
				 * If the inner name bypasses the redex, checks if its handle
				 * already has an image. If it has an image in the context, add
				 * an inner name to it and link it down to the parameter passing
				 * through id.
				 * If it does not bypass the redex, for each inner name of the
				 * redex that is mapped to the current inner name of the agent
				 * links the descending outer name of the parameter (with the 
				 * same name) to the new inner name of the parameter.
				 * per ogni outer name discendente del parametro lo linko all'inner
				 * discendente del redex
				 */
				for (EditableOuterName o1 : agent.inners.getDesc().values()) {
					String name1 = o1.getName();
					EditableOuterName o2 = new EditableOuterName(name1);
					EditableHandle h2 = null;

					for (Point pnt : o1.getEditablePoints()) {
						Map<LinkEntity, IntVar> row = e_vars.get(pnt);
						IntVar var = findVariable(row.get(o1).getName(),
								model.getVars()).asIntVar();

						EditableInnerName i1 = null;
						if (var.getValue() == 0) {
							for (EditableOuterName i0 : redex.inners.getDesc().values()) {
								for (Point pnt1 : i0.getEditablePoints()) {
									IntVar v = findVariable(row.get(pnt1).getName(),
											model.getVars()).asIntVar();
									if (v.getValue() == 1) {
										/*
										* this port is attached to the redex inner i0.
										* Add it as an outer of prm, if it is not
										* already present, and link it to p2 resp.
										*/
										String name = i0.getName();
										i1 = prm.outers.getDesc(0).get(name);
										if (i1 == null) {
											EditableInnerName o3 = new EditableInnerName(
													name);
											prm.outers.addDesc(0, o3);
											i1 = o3;
										}
										break;
									}
								}
							}
						}
						o2.setOwner(prm);
						if (i1 != null) {
							i1.setHandle(o2);
						}
						prm.inners.addDesc(0, o2);
						prm_hnd_dic.put(o1, o2);
					}
				}
				for (EditableRoot r0 : agent.roots) {
					q.add(new VState(ctx, null, r0));
				}
				Collection<Root> unseen_rdx_roots = new LinkedList<>(
						redex_roots);
				while (!q.isEmpty()) {
					VState v = q.poll();
					if (v.b == rdx) {
						// the entity visited is the image of something in the
						// redex
						if (v.i.isNode()) {
							EditableNode n0 = (EditableNode) v.i;
							EditableNode n1 = (EditableNode) v.c;
							EditableNode n2 = n1.replicate();
							nEmb.put(n0, n1);
							n2.setParent(v.p);
                            node_dic.put(n0, n2);
							// replicate links from node ports
							for (int i = n0.getControl().getArityOut() - 1; -1 < i; i--) {
								EditableOutPort o0 = n0.getOutPortsForEdit().get(i);
								EditableHandle h0 = o0.getHandle();
								// looks for an existing replica
								EditableHandle h2 = rdx_hnd_dic.get(h0);
								if (h2 == null) {
									h2 = n1.getOutPortsForEdit().get(i).getHandle().replicate();
									h2.setOwner(rdx);
									rdx_hnd_dic.put(h0, h2);
								}
								n2.getOutPortsForEdit().get(i).setHandle(h2);
							}

							for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
								EditableNode.EditableInPort ip1 = n1.getInPort(i);
								EditableHandle h2 = rdx_hnd_dic.get(ip1);

								Collection<? extends EditablePoint> pnts = new ArrayList<>(ip1.getEditablePoints());
								for (EditablePoint p : pnts) {
									Map<LinkEntity, IntVar> row = e_vars.get(p);
									IntVar var = findVariable(row.get(ip1).
											getName(), model.getVars()).asIntVar();
									if (var.getValue() == 1) {
										Collection<EditableInnerName> ins = agent.inners.getAsc().values();
										ins.removeAll(redex.inners.getAsc().values());
										ins = redex.inners.getAsc().values();
										EditableHandle i2 = null; 
										for (InnerName in : ins) {
											i2 = prm_dic.get(in);
										}

										if (ctx_pnt_dic.get(p) != null) {
											EditablePoint pnt2 = ctx_pnt_dic.get(p);
											if (i2 != null)
												pnt2.setHandle(i2);
										}
									}
								}

								if (h2 == null) {
									EditableNode.EditableInPort ip2 = n2.getInPort(i);
									rdx_hnd_dic.put(ip1, ip2);
									for (InnerName i1 : redex.inners.getAsc().values()) {
										EditablePoint pnt = rdx_pnt_dic.get(i1);
										if (pnt != null && n0.getInPort(i).getPoints().contains(i1)) {
											pnt.setHandle(ip2);
										}
									}
									for (InnerName i1 : redex.outers.getDesc().values()) {
										EditablePoint pnt = rdx_pnt_dic.get(i1);
										if (pnt != null && n0.getInPort(i).getPoints().contains(i1)) {
											pnt.setHandle(ip2);
										}
									}
								}
							}
							Collection<Child> cs1 = new HashSet<>(
									n1.getChildren());
							for (Child c0 : n0.getChildren()) {
								Iterator<Child> ic = cs1.iterator();
								boolean notMatched = true;
								while (ic.hasNext()) {
									Child c1 = ic.next();
									IntVar var = findVariable(p_vars.get(c1)
											.get(c0).getName(),
											model.getVars()).asIntVar();
									if (var.getValue() == 1) {
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
                    } else if (v.b == ctx) {
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
                            node_dic.put(n1, n2);
							// replicate links from node ports
							for (int i = n1.getControl().getArityOut() - 1; -1 < i; i--) {
								EditableOutPort o = n1.getOutPortsForEdit().get(i);
								EditableHandle h1 = o.getHandle();
								// looks for an existing replica
								EditableHandle h2 = ctx_hnd_dic.get(h1);
								if (h2 == null) {
									h2 = h1.replicate();
									h2.setOwner(ctx);
									ctx_hnd_dic.put(h1, h2);
								}
								n2.getOutPortsForEdit().get(i).setHandle(h2);
							}

							for (int i = n1.getControl().getArityIn() - 1; 0 <= i; i--) {
								EditableNode.EditableInPort ip1 = n1.getInPort(i);
								EditableHandle h2 = ctx_hnd_dic.get(ip1);

								Collection<? extends Point> pnts = new ArrayList<>(ip1.getPoints());
								for (Point p : pnts) {
									Map<LinkEntity, IntVar> row = e_vars.get(p);
									IntVar var = findVariable(row.get(ip1).
											getName(), model.getVars()).asIntVar();
									if (var.getValue() == 1) {
										if (agent.inners.getAsc().values().contains(p)) {
											String name = ((InnerName) p).getName();

											EditableInnerName i3 = new EditableInnerName(
													name);
											i3.setHandle(n2.getInPort(i));
											ctx.inners.addAsc(0, i3);

											EditableOuterName o3 = new EditableOuterName(
													name);
											id.outers.addAsc(0, o3);
											o3.setOwner(id);

											EditableInnerName i4 = new EditableInnerName(
													name);
											i4.setHandle(o3);
											id.inners.addAsc(0, i4);

											// add it also to prm
											EditableOuterName o4 = new EditableOuterName(
													name);
											prm.outers.addAsc(0, o4);
											o4.setOwner(prm);

											if (ctx_pnt_dic.get(p) != null) {
												EditablePoint pnt2 = ctx_pnt_dic.get(p);
												pnt2.setHandle(o4);
											}
										}
									}
								}

								if (h2 == null) {
									EditableNode.EditableInPort ip2 = n2.getInPort(i);
									ctx_hnd_dic.put(ip1, ip2);
									for (InnerName i1 : agent.outers.getDesc().values()) {
										EditablePoint pnt = ctx_pnt_dic.get(i1);
										if (pnt != null && ip1.getEditablePoints().contains(i1)) {
											pnt.setHandle(ip2);
										}
									}
									// for (InnerName i1 : agent.inners.getAsc().values()) {
									// 	EditablePoint pnt = ctx_pnt_dic.get(i1);
									// 	if (pnt != null && ip1.getEditablePoints().contains(i1)) {
									// 		pnt.setHandle(ip2);
									// 	}
									// }
								}
							}

							for (int j = n1.getControl().getArityOut() - 1; -1 < j; j--) {
								EditableOutPort op1 = n1.getOutPortsForEdit().get(j);
								EditableOutPort op2 = n2.getOutPortsForEdit().get(j);

								EditableHandle h2 = null;
								Map<LinkEntity, IntVar> row = e_vars.get(op1);
								EditableHandle h1 = op1.getHandle();

								IntVar var = findVariable(row.get(h1).
										getName(), model.getVars()).asIntVar();
								if (var.getValue() == 1) {
									if (agent.inners.getDesc().values().contains(h1)) {
										/*
										* this port bypasses the redex. Add an outer to
										* it and link it up to the context passing
										* through id 
										*/
										EditableOuterName i2 = new EditableOuterName();
										String name = i2.getName();
										i2.setOwner(v.b);
										v.b.inners.addDesc(0, i2);

										EditableInnerName i3 = new EditableInnerName(
												name);
										i3.setHandle(i2);
										id.outers.addDesc(0, i3);
										EditableOuterName o3 = new EditableOuterName(
												name);
										o3.setOwner(id);
										id.inners.addDesc(0, o3);
										i3.setHandle(o3);

										// add it also to prm
										EditableInnerName i4 = new EditableInnerName(
												name);
										prm.outers.addDesc(0, i4);
										if (prm_hnd_dic.get(h1) != null) {
											EditableHandle h3 = prm_hnd_dic.get(h1);
											h3.linkPoint(i4);
										}
										h2 = i2;
										ctx_hnd_dic.put(h1, h2);
                                    } else if (h1.isPort()) {
                                        EditableHandle hr = rdx_hnd_dic.get(h1);
                                        if (hr != null) {
                                            for (EditableInnerName i2 : rdx.outers.getDesc().values()) {
                                                if (hr.getPoints().contains(i2)) {
                                                    for (EditableOuterName o2 : v.b.inners.getDesc().values()) {
                                                        if (o2.getName().equals(i2.getName())) {
                                                            h2 = o2;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        }
									} else {
										// It's an ascending outer name
										h2 = ctx_hnd_dic.get(h1);
									}
								} else {
									for (InnerName i0 : redex.outers.getDesc().values()) {
										IntVar var_tmp = findVariable(row.get(i0).
												getName(), model.getVars()).asIntVar();
										if (var_tmp.getValue() == 1) {
											/*
											 * this port is attached to the
											 * redex descending outer i0. Add 
											 * it as an inner of ctx, if it is 
											 * not already present, and link it 
											 * to op2 resp.
											 */
											String name = i0.getName();
											h2 = ctx.inners.getDesc(0).get(name);
											if (h2 == null) {
												EditableOuterName o2 = new EditableOuterName(
														name);
												o2.setOwner(v.b);
												v.b.inners.addDesc(0, o2);
												h2 = o2;
											}
											break;
										}
									}
								}
								op2.setHandle(h2);
							}
						}
						// enqueue children, if necessary
						Collection<Child> rcs = new HashSet<>(p1.getChildren());
						Map<PlaceEntity, IntVar> p_row = p_vars.get(p1);
						Iterator<Root> ir = unseen_rdx_roots.iterator();
						while (ir.hasNext()) {
							Root r0 = ir.next();
							// make a site for each root whose image is p1
							IntVar var_tmp = findVariable(p_row.get(r0).getName(),
									model.getVars()).asIntVar();
							if (var_tmp.getValue() == 1) {
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
										IntVar var = findVariable(p_vars.get(c1)
												.get(c0).getName(),
												model.getVars()).asIntVar();
										if (var.getValue() == 1) {
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
					} else {
						// the entity visited belongs to some parameter
						if (v.c.isNode()) {
							EditableNode n1 = (EditableNode) v.c;
							EditableNode n2 = n1.replicate();
							n2.setParent(v.p);
							for (int i = n1.getControl().getArityOut() - 1; -1 < i; i--) {
								EditableOutPort p1 = n1.getOutPortsForEdit().get(i);
								EditableOutPort p2 = n2.getOutPortsForEdit().get(i);

								EditableHandle h2 = null;
								Map<LinkEntity, IntVar> row = e_vars
										.get(p1);
								EditableHandle h1 = p1.getHandle();

								IntVar var = findVariable(row.get(h1).
										getName(), model.getVars()).asIntVar();
								if (var.getValue() == 1) {
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
											ctx.inners.addAsc(0, i3);
											// add it also to id
											EditableOuterName o4 = new EditableOuterName(
													name);
											o4.setOwner(id);
											id.outers.addAsc(0, o4);
											EditableInnerName i4 = new EditableInnerName(
													name);
											i4.setHandle(o4);
											id.inners.addAsc(0, i4);

											EditableOuterName o2 = new EditableOuterName(
													name);
											o2.setOwner(v.b);
											v.b.outers.addAsc(0, o2);
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
									for (InnerName i0 : redex.inners.getAsc().values()) {
										IntVar var_tmp = findVariable(row.get(i0).
												getName(), model.getVars()).asIntVar();
										if (var_tmp.getValue() == 1) {
											/*
											 * this port is attached to the
											 * redex inner i0. Add it as an
											 * outer of prm, if it is not
											 * already present, and link it to
											 * p2 resp.
											 */
											String name = i0.getName();
											h2 = prm.outers.getAsc(0).get(name);
											if (h2 == null) {
												EditableOuterName o2 = new EditableOuterName(
														name);
												o2.setOwner(v.b);
												v.b.outers.addAsc(0, o2);
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

                for (Node n1 : redex.getNodes()) {
                    for (Node n2 : redex.getNodes()) {
                        for (OutPort o : n1.getOutPorts()) {
                            int odx = n1.getOutPorts().indexOf(o);
                            EditableOutPort eo = o.getEditable();
                            Handle h = eo.getHandle();
                            
                            if (h.isPort()) {
                                InPort ip = (InPort) h;
                                List<? extends InPort> ips = new ArrayList<>(n2.getInPorts());
                                int idx = ips.indexOf(ip);
                                if (idx != -1) {
                                    EditableNode cn1 = node_dic.get(n1);
                                    EditableNode cn2 = node_dic.get(n2);
                                    cn1.getOutPort(odx).setHandle(cn2.getInPort(idx));
                                }
                            }
                        }
                    }
                }
                for (Node n1 : agent.getNodes()) {
                    for (Node n2 : agent.getNodes()) {
                        for (OutPort o : n1.getOutPorts()) {
                            int odx = n1.getOutPorts().indexOf(o);
                            EditableOutPort eo = o.getEditable();
                            Handle h = eo.getHandle();
                            
                            if (h.isPort()) {
                                InPort ip = (InPort) h;
                                List<? extends InPort> ips = new ArrayList<>(n2.getInPorts());
                                int idx = ips.indexOf(ip);
                                if (idx != -1) {
                                    EditableNode cn1 = node_dic.get(n1);
                                    EditableNode cn2 = node_dic.get(n2);
                                    if (cn1 != null && cn2 != null) {
                                        if (cn1.getOwner().equals(ctx) && cn2.getOwner().equals(ctx)) {
                                            cn1.getOutPort(odx).setHandle(cn2.getInPort(idx));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (Node n1 : agent.getNodes()) {
                    for (Node n2 : agent.getNodes()) {
                        for (OutPort o : n1.getOutPorts()) {
                            int odx = n1.getOutPorts().indexOf(o);
                            EditableOutPort eo = o.getEditable();
                            Handle h = eo.getHandle();
                            
                            if (h.isPort()) {
                                InPort ip = (InPort) h;
                                List<? extends InPort> ips = new ArrayList<>(n2.getInPorts());
                                int idx = ips.indexOf(ip);
                                if (idx != -1) {
                                    EditableNode cn1 = node_dic.get(n1);
                                    EditableNode cn2 = node_dic.get(n2);
                                    if (cn1 != null && cn2 != null) {
                                        if (cn1.getOwner().equals(prm) && cn2.getOwner().equals(prm)) {
                                            cn1.getOutPort(odx).setHandle(cn2.getInPort(idx));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

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
				this.nextMatch = new DirectedMatch(ctx, rdx, id, prm, nEmb);
			}
		}
	}
}
