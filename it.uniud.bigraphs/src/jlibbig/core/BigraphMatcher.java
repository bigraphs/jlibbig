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

/**
 * Implements a matcher for bigraphs.
 * 
 * @deprecated BUGGY...very buggy!
 * @see Matcher
 */
public final class BigraphMatcher implements Matcher<Bigraph, Bigraph> {

	private final static boolean DEBUG = false;
	private final static boolean DEBUG_PRINT_CSP_SOLUTIONS = DEBUG;
	private final static boolean DEBUG_PRINT_QUEUE_REFILL = DEBUG;
	private final static boolean DEBUG_CONSISTENCY_CHECK = DEBUG || true;
	private final static boolean OPTIMIZE_FOR_GROUND = true;

	private final NodeEquivalence eq;

	public BigraphMatcher(){
		this(StandardNodeEquivalence.DEFAULT);
	}

	public BigraphMatcher(NodeEquivalence eq){
		if(eq == null)
			throw new IllegalArgumentException("Euivalence can not be null.");
		this.eq = eq;
	}

	/**
	 * The default instance of the macher.
	 */
	public final static BigraphMatcher DEFAULT = new BigraphMatcher();

	/**
	 * @see jlibbig.core.abstractions.Matcher#match(jlibbig.core.AbstBigraph,
	 *      jlibbig.core.AbstBigraph)
	 */
	@Override
	public Iterable<? extends BigraphMatch> match(Bigraph agent, Bigraph redex) {
		return match(agent, redex, eq);
	}

	public Iterable<? extends BigraphMatch> match(Bigraph agent, Bigraph redex, NodeEquivalence eq) {
		if(eq == null)
			throw new IllegalArgumentException("Euivalence can not be null.");
		if (OPTIMIZE_FOR_GROUND && agent.isGround())
			return AgentMatcher.DEFAULT.match(agent, redex);
		else
			return new MatchIterable(agent, redex, eq);
	}

	/**
	 * A class for incrementally solving the matching problem. Solutions are
	 * computed on demand. The algorithm divides the matching in two phases:
	 * first a place graph match is solved, then the solution is checked for
	 * extensibility to a bigraph match (is the given place match consistent
	 * w.r.t. the linkings?) then a sub-problem is solved yielding all the
	 * possible link matching compatible with the given place match. These may
	 * be more than one due to the combinatorics introduced by inner names etc.
	 * and therefore, this set of solutions is stored into a queue. When asked
	 * for a match, the object tries to fetch it from the queue and if its is
	 * empty looks for a place graph match and iterates the algorithm outlined
	 * above.
	 */
	private static class MatchIterable implements Iterable<BigraphMatch> {
		final Bigraph agent, redex;

		boolean agent_ancestors_is_empty = true;
		final Map<Node, Set<Node>> agent_ancestors;

		// caches some collections of entities (e.g. nodes and edges are
		// computed on the fly)
		final List<? extends Root> agent_roots;
		final List<? extends Site> agent_sites;
		final Collection<? extends Node> agent_nodes;
		final Collection<? extends Edge> agent_edges;

		final List<? extends Root> redex_roots;
		final List<? extends Site> redex_sites;
		final Collection<? extends Node> redex_nodes;

		final Collection<Handle> redex_handles;

		// relates redex handles and inner names and describes aliasied names
		final InvMap<InnerName, Handle> aliased_inners;
		// "phantom" edges emulate edges with no points in the agent
		final List<Edge> pht_edges;

		// caches the set of descendants of a agents entities
		// final Map<Parent, Set<Node>> descendants_cache;

		private final NodeEquivalence eq;

		private MatchIterable(Bigraph agent, Bigraph redex, NodeEquivalence eq) {
			if (!agent.isGround()) {
				throw new UnsupportedOperationException(
						"Agent should be a bigraph with empty inner interface i.e. ground.");
			}
			if (!agent.signature.equals(redex.signature)) {
				throw new UnsupportedOperationException(
						"Agent and redex should have the same singature.");
			}

			this.eq = eq;

			this.agent = agent;
			this.redex = redex;

			this.agent_ancestors = new HashMap<>();

			this.agent_roots = agent.getRoots();
			this.agent_sites = agent.getSites();
			this.agent_nodes = agent.getNodes();
			this.agent_edges = agent.getEdges(this.agent_nodes);

			this.redex_roots = new ArrayList<>(redex.getRoots());
			this.redex_sites = redex.getSites();
			this.redex_nodes = redex.getNodes();

			this.redex_handles = new HashSet<Handle>(
					redex.getEdges(this.redex_nodes));
			for (OuterName o : redex.outers.values()) {
				this.redex_handles.add(o);
			}

			this.aliased_inners = new InvMap<>();
			for (InnerName i : redex.inners.values()) {
				aliased_inners.put(i, i.getHandle());
			}

			this.pht_edges = new LinkedList<>();
			for (Handle h : this.redex_handles) {
				if (!aliased_inners.containsValue(h))
					pht_edges.add(new EditableEdge());
			}

		}

		@Override
		public Iterator<BigraphMatch> iterator() {
			return new MatchIterator();
		}

		private class MatchIterator implements Iterator<BigraphMatch> {

			private boolean exhausted = false;

			final CPModel model;
			final CPSolver solver;
			final Map<PlaceEntity, Map<PlaceEntity, IntegerVariable>> matrix;

			Queue<BigraphMatch> matchQueue = null;

			MatchIterator() {

				// MODEL
				// ///////////////////////////////////////////////////////////
				this.model = new CPModel();

				// creates the matrix
				// rows are indexed by agent entities (therefore parents)
				// cols are indexed over redex entities
				matrix = new HashMap<>();
				// counters for rows and cols
				int ki = 0;
				int kj = 0;
				// a spare counter
				int k;
				// and a working array of vars
				IntegerExpressionVariable[] vars;

				int ans = agent_nodes.size();
				int rrs = redex_roots.size();
				int rss = redex_sites.size();

				for (Root i : agent_roots) {
					Map<PlaceEntity, IntegerVariable> row = new HashMap<>();
					for (Root j : redex_roots) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						row.put(j, var);
					}
					// these will be always zero
					for (Node j : redex_nodes) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						model.addConstraint(Choco.eq(0, var));
						row.put(j, var);
					}
					for (Site j : redex_sites) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						model.addConstraint(Choco.eq(0, var));
						row.put(j, var);
					}
					matrix.put(i, row);
					ki++;
				}
				for (Node i : agent_nodes) {
					Map<PlaceEntity, IntegerVariable> row = new HashMap<>();
					for (Root j : redex_roots) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						row.put(j, var);
					}
					for (Node j : redex_nodes) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						row.put(j, var);
					}
					for (Site j : redex_sites) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						row.put(j, var);
					}
					matrix.put(i, row);
					ki++;
					kj = 0;
				}
				for (Site i : agent_sites) {
					Map<PlaceEntity, IntegerVariable> row = new HashMap<>();
					// these will be always zero because a site from the agent
					// can
					// match only with a site
					for (Root j : redex_roots) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						model.addConstraint(Choco.eq(0, var));
						row.put(j, var);
					}
					for (Node j : redex_nodes) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						model.addConstraint(Choco.eq(0, var));
						row.put(j, var);
					}
					for (Site j : redex_sites) {
						IntegerVariable var = Choco.makeBooleanVar("" + ki
								+ "," + kj++);
						model.addVariable(var);
						row.put(j, var);
					}
					matrix.put(i, row);
					ki++;
					kj = 0;
				}

				// Constraints

				// 2 // M_ij = 0 if nodes are different in the sense of this.eq
				// ////////////////////////////
				for (Node i : agent_nodes) {
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
					for (Node j : redex_nodes) {
						IntegerVariable var = row.get(j);
						if (!eq.areEquiv(i, j)) {
							model.addConstraint(Choco.eq(0, var));
						}
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 3 // M_ij <= M_fg if f = prnt(i) and g = prnt(j)
				// ////////////////
                addConstraint3(agent_nodes);
                addConstraint3(agent_sites);
				// /////////////////////////////////////////////////////////////////

				// 4 // M_ij = 0 if j is a root and i is not in an active
				// context //

				/*
				 * Descends the agent parent map deactivating matching (with
				 * redex roots) below every passive node. Nodes in qa were found
				 * in an active context whereas qp are in a passive one or
				 * passive.
				 */
				Queue<Node> qa = new LinkedList<>();
				Queue<Node> qp = new LinkedList<>();
				for (Root r : agent_roots) {
					for (Child c : r.getChildren()) {
						if (c instanceof Node) {
							qa.add((Node) c);
						}
					}
				}
				while (!qa.isEmpty()) {
					Node n = qa.poll();
					if (n.getControl().isActive()) {
						for (Child c : n.getChildren()) {
							if (c instanceof Node) {
								qa.add((Node) c);
							}
						}
					} else {
						qp.add(n);
					}
				}
				Constraint[] cs = new Constraint[rrs];
				while (!qp.isEmpty()) {
					Node i = qp.poll();
					for (Child c : i.getChildren()) {
						if (c instanceof Node) {
							qp.add((Node) c);
						}
					}
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
					k = 0;
					for (Root j : redex_roots) {
						cs[k++] = Choco.eq(0, row.get(j));
					}
					model.addConstraints(cs);
				}

				// /////////////////////////////////////////////////////////////////

				// 5 // sum M_ij = 1 if j not in sites
				// /////////////////////////////
				vars = new IntegerVariable[agent_roots.size() + ans];
				for (Root j : redex_roots) {
					// fetch column
					k = 0;
					for (PlaceEntity i : matrix.keySet()) {
						vars[k++] = matrix.get(i).get(j);
					}
					model.addConstraint(Choco.eq(1, Choco.sum(vars)));
				}
				vars = new IntegerVariable[agent_nodes.size()];
				for (Node j : redex_nodes) {
					k = 0;
					// roots can be skipped since these are always zero
					// for(Parent i : agent_roots){
					// vars[k++] = matrix.get(i).get(j);
					// }
					for (Parent i : agent_nodes) {
						vars[k++] = matrix.get(i).get(j);
					}
					model.addConstraint(Choco.eq(1, Choco.sum(vars)));
				}

				// //////////////////////////////////////////////////////////////////

				// 6 // n sum(j not root) M_ij + sum(j root) M_ij <= n if i in
				// nodes
                addConstraint6(agent_nodes, rrs);
                addConstraint6(agent_sites, rrs);
				// /////////////////////////////////////////////////////////////////

				// 7 // |chld(f)| M_fg <= sum(i chld(f), j in chld(g)) M_ij if
				// f,g
				// in nodes
				for (Parent f : agent_roots) {
					for (Parent g : redex_nodes) {
						Collection<? extends Child> cf = f.getChildren();
						Collection<? extends Child> cg = g.getChildren();
						vars = new IntegerVariable[cf.size() * cg.size()];
						k = 0;
						for (PlaceEntity i : cf) {
							for (PlaceEntity j : cg) {
								vars[k++] = matrix.get(i).get(j);
							}
						}
						model.addConstraint(Choco.leq(
								Choco.mult(cf.size(), matrix.get(f).get(g)),
								Choco.sum(vars)));
					}
				}
				// /////////////////////////////////////////////////////////////////

				// 8 // |chld(g) not sites| M_fg <= sum(i chld(f), j chld(g) not
				// sites) if g in roots
				for (PlaceEntity f : matrix.keySet()) {
					for (Root g : redex_roots) {
						Collection<? extends Child> cf = ((Parent) f).getChildren();
						Collection<? extends Child> cg = new HashSet<>(g.getChildren());
						cg.removeAll(redex_sites);
						vars = new IntegerVariable[cf.size() * cg.size()];
						k = 0;
						for (PlaceEntity i : cf) {
							for (PlaceEntity j : cg) {
								vars[k++] = matrix.get(i).get(j);
							}
						}
						model.addConstraint(Choco.leq(
								Choco.mult(cf.size(), matrix.get(f).get(g)),
								Choco.sum(vars)));
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
							Stack<Node> ancs = new Stack<>();
							Stack<Node> visit = new Stack<>();
							for (Root r : agent_roots) {
								for (Child cr : r.getChildren()) {
									if (cr instanceof Node) {
										ancs.clear();
										visit.add((Node) cr);
										while (!visit.isEmpty()) {
											Node i = visit.pop();
											if (!ancs.isEmpty()
													&& ancs.peek() != i
															.getParent())
												ancs.pop();
											// store ancestors for later
											agent_ancestors.put(i,
													new HashSet<>(ancs));
											// put itself as an ancestor and
											// process each of its children
											ancs.push(i);
											for (Child cn : i.getChildren()) {
												if (cn.isNode()) {
													visit.add((Node) cn);
												}
											}
										}
									}
								}
							}
							agent_ancestors_is_empty = false;
						}
					}
				}
				// and now we are ready to add the constraints of (9)
				for (PlaceEntity i : agent_ancestors.keySet()) {
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
					vars = new IntegerVariable[rrs];
					k = 0;
					for (Root j : redex_roots) {
						vars[k++] = row.get(j);
					}
					IntegerExpressionVariable c = Choco.div(Choco.sum(vars),
							rrs);

					Set<Node> ancs = agent_ancestors.get(i);
					vars = new IntegerExpressionVariable[1 + ancs.size() * rss];
					k = 0;
					for (Node f : ancs) {
						row = matrix.get(f);
						for (Site g : redex_sites) {
							vars[k++] = row.get(g);
						}
					}
					vars[k] = c;
					model.addConstraint(Choco.geq(1, Choco.sum(vars)));
				}
				// end constraints
				// /////////////////////////////////////////////////

				this.solver = new CPSolver();
				solver.read(model);
				solver.generateSearchStrategy();

				if (DEBUG) {
					System.out.println("Model created for agent:");
					System.out.println(agent);
					System.out.println("agent's ancestor map:");
					for (Node i : agent_ancestors.keySet()) {
						String s = agent_ancestors.get(i).toString();
						System.out.println("" + i + ": {"
								+ s.substring(1, s.length() - 1) + "}");
					}
				}

			}

            private void addConstraint3(Collection<? extends Child> agent_children) {
				for (Child i : agent_children) {
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
                    addConstraint3Sub(i, row, redex_nodes);
                    addConstraint3Sub(i, row, redex_sites);
				}
            }

            private void addConstraint3Sub(Child i,
                    Map<PlaceEntity, IntegerVariable> row,
                    Collection<? extends Child> redex_children) {
                for (Child j : redex_children) {
                    Parent f = i.getParent();
                    Parent g = j.getParent();
                    model.addConstraint(Choco.leq(row.get(j), matrix.get(f)
                            .get(g)));
					}
            }

            private void addConstraint6(Collection<? extends Child> agent_children, int rrs) {
				for (Child i : agent_children) {
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
					IntegerVariable[] vars = new IntegerVariable[redex_nodes.size()
							+ redex_sites.size()];
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

			@Override
			public boolean hasNext() {
				if (!exhausted && this.matchQueue == null) {
					matchQueue = new LinkedList<>();
					populateMatchQueue(true);
				}
				return !exhausted && !matchQueue.isEmpty();
			}

			@Override
			public BigraphMatch next() {
				if (exhausted)
					return null;
				if (matchQueue == null) {
					matchQueue = new LinkedList<>();
					populateMatchQueue(true);
				}
				// if (this.matchQueue.isEmpty())
				// return null;
				BigraphMatch match = this.matchQueue.poll();
				if (matchQueue.isEmpty())
					populateMatchQueue(false);
				return match;
			}

			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("");
			}

			private void cleanup() {
				this.exhausted = true;
				this.solver.clear();
				// this.agent_ancestors.clear();
				// this.agent_nodes.clear();
				// this.aliased_inners.clear();
				// this.matrix.clear();
				// this.pht_edges.clear();
				// this.redex_handles.clear();
				// this.redex_nodes.clear();
			}

			private void populateMatchQueue(boolean first) {
				if (DEBUG_PRINT_QUEUE_REFILL)
					System.out
							.println("populate matcher queue has been invoked...");
				// look for a solution for the CSP
				if ((first && !solver.solve())
						|| (!first && !solver.nextSolution())) {
					if (DEBUG_PRINT_QUEUE_REFILL)
						System.out
								.println("...but no more solutions where found.");
					cleanup();
					return;
				}

				do {
					/*
					 * check solution and instantiate params if possible,
					 * otherwise try next one
					 */

					// a bijective map embedding redex nodes into the agent
					BidMap<Node, EditableNode> node_img = new BidMap<>();
					// a (possibly non injective) map embedding redex roots into
					// the
					// agent
					InvMap<Root, Parent> root_img = new InvMap<>();
					// a map embedding redex sites into the agent
					Map<Site, Set<EditableChild>> site_img = new HashMap<>();
					for (Site s : redex_sites) {
						site_img.put(s, new HashSet<EditableChild>());
					}

					// a (possibly non injective) map embedding redex handles
					// into
					// the agent.
					InvMap<Handle, Handle> handle_img = new InvMap<>();

					// plus a quick printout of the matrix
					if (DEBUG_PRINT_CSP_SOLUTIONS) {
						System.out.println("CPS solution #"
								+ solver.getSolutionCount() + ":");
						for (PlaceEntity i : matrix.keySet()) {
							Map<PlaceEntity, IntegerVariable> row = matrix
									.get(i);
							for (PlaceEntity j : row.keySet()) {
								if (solver.getVar(row.get(j)).getVal() == 1) {
									System.out.print("1 ");
								} else {
									System.out.print("0 ");
								}
							}
							System.out.println("");
						}
					}

					/*
					 * check solution consistency wrt linkings. For each handle
					 * in the redex there shall be a compatible one in the agent
					 * where compatibility means that ports are matched with
					 * ports connected together and within the image of the
					 * redex into the agent and that inner names are connected
					 * to ports in the scope of the parameters.
					 */

					// read solution
					// ///////////////////////////////////////////////////

					// the set of nodes making the context for the matching
					Set<Node> ctx_nodes = new HashSet<>();

					// edges without points in the context (these are the only
					// ones
					// assignable to the edges of the redex)
					Set<Edge> non_ctx_edges = new HashSet<>(agent_edges);
					Set<Handle> ctx_handles = new HashSet<>();
					for (Handle h : agent.outers.values()) {
						ctx_handles.add(h);
					}

					for (PlaceEntity i : matrix.keySet()) {
						Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
						for (PlaceEntity j : row.keySet()) {
							// i and j are matched
							if (solver.getVar(row.get(j)).getVal() == 1) {
								if (j.isNode()) {
									Node nj = (Node) j;
									EditableNode ni = (EditableNode) i;
									node_img.put(nj, ni);
									for (int k = nj.getControl().getArity() - 1; k >= 0; k--) {
										Handle h1 = ni.getPort(k).getHandle();
										Handle h2 = handle_img.put(nj
												.getPort(k).getHandle(), h1);
										if (h2 != null && h2 != h1) {
											// skip this match since it doen't
											// extend to
											// a bigraph match.
											if (DEBUG_PRINT_CSP_SOLUTIONS)
												System.out
														.println("DISCARD: inconsistent with linkings");
											populateMatchQueue(false);
											return;
										}
									}
								} else if (j.isRoot()) {
									root_img.put((Root) j, (Parent) i);
									/*
									 * caches context nodes for scope link check
									 * and param instantiation removes edges
									 * having points in the context from
									 * non_ctx_edges
									 */
									if (i.isNode()) {
										for (Node n : agent_ancestors
												.get((Node) i)) {
											ctx_nodes.add(n);
											for (Port p : n.getPorts()) {
												// removes the handle of this
												// port
												// since
												// it has a point in the context
												non_ctx_edges.remove(p
														.getHandle());
												// and put it into the ctx
												ctx_handles.add(p.getHandle());
											}
										}
									}
								} else { // j.isSite()
									// i can be either a node or a site
									site_img.get((Site) j).add(
											(EditableChild) i);
								}
							}
						}
					}

					// check scope for edges
					// ///////////////////////////////////////////

					// relates parameters points and agent handles
					InvMap<Point, Handle> prm_points = new InvMap<>();
					for (Node n : agent_nodes) {
						if (!ctx_nodes.contains(n)
								&& !node_img.containsValue(n)) {
							// this node belongs to the parameters
							for (Point p : n.getPorts()) {
								prm_points.put(p, p.getHandle());
							}
						}
					}

					for (Handle ha : handle_img.values()) {
						/*
						 * ha may be the image of more than one handle of the
						 * redex. either all or none of them are outernames. if
						 * edges are expected, none of their point can't be in
						 * the context if ha has points in the params, then one
						 * of its images has to have at least an inner name.
						 */
						int f = 0;
						// 1 -> all edges; 2 -> all outers; -1 -> reject
						boolean l = prm_points.containsValue(ha);
						// t -> look for inners; f -> inners found or
						// unnecessary;
						for (Handle hr : handle_img.getKeys(ha)) {
							switch (f) {
							case 1:
								if (!(hr instanceof Edge))
									f = -1;
								break;
							case 2:
								if (hr instanceof Edge)
									f = -1;
								break;
							default:
								if (hr instanceof Edge)
									f = 1;
							}
							if (f == -1) {
								// skip this match since it doen't extend to a
								// bigraph
								// match.
								if (DEBUG_PRINT_CSP_SOLUTIONS)
									System.out
											.println("DISCARD: inconsistent with linkings");
								populateMatchQueue(false);
								return;
							}
							if (l) {
								// look for inners since ha has points in params
								l = !aliased_inners.containsValue(hr);
							}
						}
						if (l) {
							// skip this match since it doen't extend to a
							// bigraph
							// match.
							if (DEBUG_PRINT_CSP_SOLUTIONS)
								System.out
										.println("DISCARD: inconsistent with linkings");
							populateMatchQueue(false);
							return;
						}
						if (f == 1) {
							// check scope: no point in the context
							if (ctx_handles.contains(ha)) {
								// for(Point p : ha.getPoints()){
								// if(ctx_nodes.contains(((Port) p).getNode())){
								// skip this match since it doen't extend to a
								// bigraph
								// match.
								if (DEBUG_PRINT_CSP_SOLUTIONS)
									System.out
											.println("DISCARD: inconsistent with linkings");
								populateMatchQueue(false);
								return;
								// }
							}
						}
					}

					/*
					 * If we got there then this place matching can be extended
					 * to a bigaph mathing. However, there are many possible
					 * instantiations for its parameters due to the
					 * combinatorics introduced by inner names and redex handles
					 * not in handle_img.keyset(). In fact, if a redex handle
					 * isn't in handle_img then its only points are inner names
					 * and in the case of outernames they may have no points at
					 * all. These can be mapped more or less on every handle of
					 * the agent. inner names and outer names without points
					 * introduce some combinatorics in the way they can be
					 * matched. These assignments are computed as the solution
					 * of the following CSP
					 */

					// lnk CSP
					// /////////////////////////////////////////////////////////

					CPModel lnk_model = new CPModel();

					// a boolean variable for each assignable pair of handles
					// <Redex,Agent>
					Map<Handle, Map<Handle, IntegerVariable>> lnk_hnd = new HashMap<>();
					// a boolean variable for each assignable pair of points and
					// inner names
					Map<Point, Map<InnerName, IntegerVariable>> lnk_pts = new HashMap<>();

					// variables activating phantom edges
					Map<Edge, IntegerVariable> lnk_pht = new HashMap<>();

					for (Edge e : pht_edges) {
						IntegerVariable var = Choco.makeBooleanVar(e + "-pht");
						lnk_model.addVariable(var);
						lnk_pht.put(e, var);
					}

					int pht_ctx_k = 0;
					int pht_k = 0;
					// instantiates lnk_hnd and lnk_pts
					for (Handle h1 : redex_handles) {
						Map<Handle, IntegerVariable> hr = new HashMap<>();
						String h1_s = h1.toString();
						if (handle_img.containsKey(h1)) {
							// handles in handle_img have a choosen assignement
							if (!aliased_inners.containsValue(h1))
								// this handle is completely assigned since has
								// no
								// inners
								continue;
							// System.out.println("fixed " + h1);
							Handle h2 = handle_img.get(h1);
							IntegerVariable var = Choco.makeBooleanVar(h1_s
									+ " - " + h2);
							lnk_model.addVariable(var);
							hr.put(h2, var);
							// Row constraint is implicit
							lnk_model.addConstraint(Choco.eq(1, var));
						} else {
							if (aliased_inners.containsValue(h1)) {
								// System.out.println("only inners " + h1);
								/*
								 * has only inner names (one or more) if it is
								 * an edge, it can be assigned only to
								 * non_ctx_edges otherwise, to every agent
								 * handle
								 */
								for (Handle h2 : non_ctx_edges) {
									IntegerVariable var = Choco
											.makeBooleanVar(h1_s + " - " + h2);
									lnk_model.addVariable(var);
									hr.put(h2, var);
								}
								pht_k++; // record a phantom edge candidate
								for (Handle h2 : pht_edges) {
									IntegerVariable var = Choco
											.makeBooleanVar(h1_s + " - " + h2);
									lnk_model.addVariable(var);
									hr.put(h2, var);
									// enable this variable obly if the phantom
									// edge
									// can
									// be used (reduce symmetries)
									lnk_model.addConstraint(Choco.geq(
											lnk_pht.get(h2), var));
								}
								if (h1 instanceof OuterName) {
									// it's an outer, add also agent outer and
									// ctx_edges
									for (Handle h2 : ctx_handles) {
										IntegerVariable var = Choco
												.makeBooleanVar(h1_s + " - "
														+ h2);
										lnk_model.addVariable(var);
										hr.put(h2, var);
									}
								}
							} else {
								// outername with no points can be assigned to
								// every
								// ctx_handle + pht_edges
								// System.out.println("outer no points " + h1);
								for (Handle h2 : ctx_handles) {
									IntegerVariable var = Choco
											.makeBooleanVar(h1_s + " - " + h2);
									lnk_model.addVariable(var);
									hr.put(h2, var);
								}
								pht_k++; // record a phantom edge candidate
								pht_ctx_k++; // and that it is used by the ctx
								for (Handle h2 : pht_edges) {
									IntegerVariable var = Choco
											.makeBooleanVar(h1_s + " - " + h2);
									lnk_model.addVariable(var);
									hr.put(h2, var);
									// enable this variable obly if the phantom
									// edge
									// can
									// be used (reduce symmetries)
									lnk_model.addConstraint(Choco.geq(
											lnk_pht.get(h2), var));
								}
							}
							// assignment constraint (row sum = 1)
							IntegerVariable[] vars = new IntegerVariable[hr
									.size()];
							int k = 0;
							for (IntegerVariable var : hr.values()) {
								vars[k++] = var;
							}
							lnk_model
									.addConstraint(Choco.eq(1, Choco.sum(vars)));
						}
						if (aliased_inners.containsValue(h1)) {
							for (Handle h2 : hr.keySet()) {
								// add vars for h1's inners (if any) and h2's
								// ports
								// (if
								// any)
								Set<Point> ps = prm_points.getKeys(h2);
								if (ps == null)
									continue;
								for (Point p : ps) {
									// if(is == null) continue;
									Map<InnerName, IntegerVariable> pr = lnk_pts
											.get(p);
									if (pr == null) {
										pr = new HashMap<>();
										lnk_pts.put(p, pr);
									}
									String p_s = h2.toString() + " <- "
											+ p.toString();
									Set<InnerName> is = aliased_inners
											.getKeys(h1);
									for (InnerName i : is) {
										IntegerVariable var = Choco
												.makeBooleanVar(p_s + " - " + i);
										lnk_model.addVariable(var);
										pr.put(i, var);
									}
								}
							}
						}
						lnk_hnd.put(h1, hr);
					}

					if (pht_k > 0) {
						// enables phantom edges only if they are really used
						List<Handle> rdx_hs = new LinkedList<>(lnk_hnd.keySet());
						rdx_hs.removeAll(handle_img.keySet());
						IntegerVariable var1 = null;
						for (Edge e : pht_edges) {
							IntegerVariable var2 = lnk_pht.get(e);
							// var2 is enabled if some candidate need to use the
							// edge
							// it can be used in the ctx iff it is not used
							// elsewhere
							if (pht_ctx_k > 0 && pht_ctx_k < pht_k) {
								IntegerVariable[] vars = new IntegerVariable[pht_k];
								IntegerVariable[] vars_ctx = new IntegerVariable[pht_ctx_k];
								IntegerVariable[] vars_non_ctx = new IntegerVariable[pht_k
										- pht_ctx_k];
								int k = 0, k_non_ctx = 0, k_ctx = 0;
								for (Handle h : rdx_hs) {
									// if (handle_img.containsKey(h))
									// continue;
									IntegerVariable var = lnk_hnd.get(h).get(e);
									if (aliased_inners.containsValue(h)) {
										vars_non_ctx[k_non_ctx++] = var;
									} else {
										vars_ctx[k_ctx++] = var;
									}
									vars[k++] = var;
								}
								IntegerExpressionVariable use_ctx = Choco.div(
										Choco.sum(vars_ctx), pht_ctx_k);
								IntegerExpressionVariable use_non_ctx = Choco
										.div(Choco.sum(vars_non_ctx), pht_k
												- pht_ctx_k);

								lnk_model.addConstraint(Choco.geq(1,
										Choco.sum(use_ctx, use_non_ctx)));
								lnk_model.addConstraint(Choco.leq(var2,
										Choco.sum(vars)));
							} else {
								IntegerVariable[] vars = new IntegerVariable[pht_k];
								int k = 0;
								for (Handle h : rdx_hs) {
									// if (handle_img.containsKey(h))
									// continue;
									vars[k++] = lnk_hnd.get(h).get(e);
								}
								lnk_model.addConstraint(Choco.leq(var2,
										Choco.sum(vars)));
							}

							// edges are activated incrementally to reduce
							// symmetries
							if (var1 != null) {
								lnk_model.addConstraint(Choco.geq(var1, var2));
							}
							var1 = var2;
						}
						// avoid group permutations imposing a lexicographical
						// ordering
						// if h is matched with e then h'< h can't match with e'
						// > e
						List<Handle> hs = new LinkedList<>();
						ListIterator<Handle> hi = rdx_hs.listIterator();
						hs.add(hi.next());
						while (hi.hasNext()) {
							Handle h1 = hi.next();
							List<Edge> es = new LinkedList<>();
							ListIterator<Edge> ei = pht_edges
									.listIterator(pht_edges.size());
							es.add(ei.previous());
							while (ei.hasPrevious()) {
								Edge e1 = ei.previous();
								var1 = lnk_hnd.get(h1).get(e1);
								int k = 0;
								IntegerVariable[] vars = new IntegerVariable[es
										.size() * hs.size()];
								for (Handle h2 : hs) {
									Map<Handle, IntegerVariable> hr = lnk_hnd
											.get(h2);
									for (Edge e2 : es) {
										vars[k++] = hr.get(e2);
									}
								}
								lnk_model
										.addConstraint(Choco.geq(var1, Choco
												.mult(var1, Choco.div(
														Choco.sum(vars), k))));
								es.add(e1);
							}
							hs.add(h1);
						}
					} else {
						for (Edge e : pht_edges) {
							lnk_model.removeVariable(lnk_pht.get(e));
						}
					}

					// every port is assigned to at most one inner name
					System.out.println(prm_points.keySet());
					System.out.println(lnk_pts.keySet());
					for (Point p : prm_points.keySet()) {
						Map<InnerName, IntegerVariable> pr = lnk_pts.get(p);
						if(pr == null){
							System.err.println(p);
						}
						IntegerVariable[] vars = new IntegerVariable[pr.size()];
						int k = 0;
						for (IntegerVariable var : pr.values()) {
							vars[k++] = var;
						}
						lnk_model.addConstraint(Choco.geq(1, Choco.sum(vars)));
					}

					// if an agent handle h2 is matched the their ports should
					// be
					// assigned
					for (Handle h1 : lnk_hnd.keySet()) {
						Map<Handle, IntegerVariable> hr = lnk_hnd.get(h1);
						for (Handle h2 : hr.keySet()) {
							IntegerExpressionVariable exp = hr.get(h2);
							Set<Point> ps = prm_points.getKeys(h2);
							if (ps == null)
								continue;
							for (Point p : ps) {
								Map<InnerName, IntegerVariable> pr = lnk_pts
										.get(p);
								IntegerVariable[] vars = new IntegerVariable[pr
										.size()];
								int k = 0;
								for (IntegerVariable var : pr.values()) {
									vars[k++] = var;
								}
								lnk_model.addConstraints(Choco.leq(exp,
										Choco.sum(vars)));
							}
						}
					}

					// if h1 and h2 are not matched then neither their points
					// can
					for (Handle h1 : lnk_hnd.keySet()) {
						Set<InnerName> is = aliased_inners.getKeys(h1);
						if (is == null)
							continue;
						Map<Handle, IntegerVariable> hr = lnk_hnd.get(h1);
						for (Handle h2 : hr.keySet()) {
							IntegerExpressionVariable exp = hr.get(h2);
							Set<Point> ps = prm_points.getKeys(h2);
							if (ps == null)
								continue;
							for (Point p : ps) {
								Map<InnerName, IntegerVariable> pr = lnk_pts
										.get(p);
								IntegerVariable[] vars = new IntegerVariable[is
										.size()];
								int k = 0;
								for (InnerName i : is) {
									vars[k++] = pr.get(i);
								}
								lnk_model.addConstraints(Choco.geq(exp,
										Choco.sum(vars)));
							}
						}
					}

					CPSolver lnk_solver = new CPSolver();
					lnk_solver.read(lnk_model);
					lnk_solver.generateSearchStrategy();

					lnk_solver.solve();

					// reads every solution and generate the corresponding match
					do {
						// print the solution
						if (DEBUG_PRINT_CSP_SOLUTIONS) {
							System.out.println("sub solution #"
									+ solver.getSolutionCount() + "."
									+ lnk_solver.getSolutionCount() + ":");
							for (Handle h : lnk_hnd.keySet()) {
								for (IntegerVariable var : lnk_hnd.get(h)
										.values()) {
									System.out.println(lnk_solver.getVar(var));
								}
							}
							for (Point p : lnk_pts.keySet()) {
								for (IntegerVariable var : lnk_pts.get(p)
										.values()) {
									System.out.println(lnk_solver.getVar(var));
								}
							}
						}

						for (Handle h1 : lnk_hnd.keySet()) {
							Map<Handle, IntegerVariable> hr = lnk_hnd.get(h1);
							Handle h3 = null;
							for (Handle h2 : hr.keySet()) {
								if (lnk_solver.getVar(hr.get(h2)).getVal() == 1) {
									h3 = h2;
									break;
								}
							}
							handle_img.put(h1, h3);
						}

						Bigraph ctx = new Bigraph(agent.signature);
						Bigraph rdx = new Bigraph(agent.signature);
						Bigraph prm = new Bigraph(agent.signature);
						Map<Node,EditableNode> nEmb = new HashMap<>();

						// replicated sites lookup table
						EditableSite ctx_sites_dic[] = new EditableSite[redex_roots
								.size()];
						EditableSite rdx_sites_dic[] = new EditableSite[redex_sites
								.size()];
						EditableSite prm_sites_dic[] = new EditableSite[agent_sites
								.size()];

						// replicated handles lookup tables
						Map<Handle, EditableHandle> ctx_hnd_dic = new HashMap<>();
						Map<Handle, EditableHandle> rdx_hnd_dic = new HashMap<>();
						Map<Handle, EditableHandle> prm_hnd_dic = new HashMap<>();
						Map<InnerName, EditableOuterName> prm_ion_dic = new HashMap<>();

						// the queue is used for a breadth first visit
						class VState<C> {
							final C c; // the agent root/node to be
										// visited
							final EditableParent p; // the replicated parent

							// final Bigraph t; // the replicated bigraph:
							// ctx/rdx/prm
							// final int k; // the index of the param
							// (|params|=rdx;+1=ctx)

							VState(EditableParent p, C c) {// ,int k) {
								this.c = c;
								this.p = p;
								/*
								 * this.k = k; if( k < prms.length){ this.t =
								 * prms[k]; }else if( k > prms.length){ this.t =
								 * ctx; }else{ this.t = rdx; }
								 */
							}
						}
						Queue<VState<EditableParent>> qp = new LinkedList<>();

						// Replicates ctx
						// //////////////////////////////////////////////

						for (EditableOuterName o1 : agent.outers.values()) {
							EditableOuterName o2 = o1.replicate();
							ctx.outers.put(o2.getName(),o2);
							o2.setOwner(ctx);
							ctx_hnd_dic.put(o1, o2);
						}
						for (EditableRoot r0 : agent.roots) {
							qp.add(new VState<EditableParent>(null, r0));
						}
						while (!qp.isEmpty()) {
							VState<EditableParent> v = qp.poll();
							// v.c.isNode() || v.c.isRoot() since the agent has
							// not
							// sites
							EditableParent p1 = v.c;
							EditableParent p2 = p1.replicate();
							if (p1.isRoot()) {
								// ordering is ensured by the queue
								EditableRoot r2 = (EditableRoot) p2;
								ctx.roots.add(r2);
								r2.setOwner(ctx);
							} else { // isNode()
								EditableNode n1 = (EditableNode) p1;
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
							if (root_img.containsValue(p1)) {
								// this node/root is in the context-redex image
								// cut
								for (Root r1 : root_img.getKeys(p1)) {
									// make a site for each root whose image is
									// n1
									int k = redex_roots.indexOf(r1);
									EditableSite s = new EditableSite();
									s.setParent(p2);
									ctx_sites_dic[k] = s;
								}
								// enqueues unmatched children to be replicated
								for (EditableChild c : p1.getEditableChildren()) {
									if (node_img.containsValue(c))
										continue;
									qp.add(new VState<>(p2,
											(EditableParent) c));
								}
							} else {
								// enqueues children to be replicated
								for (EditableChild c : p1.getEditableChildren()) {
									qp.add(new VState<>(p2,
											(EditableParent) c));
								}
							}
						}
                        ctx.sites.addAll(Arrays.asList(ctx_sites_dic));
						// Replicates rdx
						// //////////////////////////////////////////////
						/*
						 * visits the redex but replicates its image in the
						 * agent (when possible)
						 */
						// replicate outers
						for (EditableOuterName o1 : redex.outers.values()) {
							// replicate the handle
							EditableOuterName o2 = o1.replicate();
							rdx.outers.put(o2.getName(),o2);
							o2.setOwner(rdx);
							rdx_hnd_dic.put(o1, o2);
							// update ctx inner face
							EditableInnerName i = new EditableInnerName(
									o1.getName());
							ctx.inners.put(i.getName(),i);
							// follow o1 to the agent and then to the context:
							Handle h1 = handle_img.get(o1);
							EditableHandle h2 = ctx_hnd_dic.get(h1);
							if (h2 == null) {
								// h1 is a phantom edge
								h2 = ((EditableHandle) h1).replicate();
								h2.setOwner(ctx);
								ctx_hnd_dic.put(h1, h2);
							}
							i.setHandle(h2);
						}
						// replicate inners
						for (EditableInnerName i1 : redex.inners.values()) {
							EditableInnerName i2 = i1.replicate();
							// set replicated handle for i2
							EditableHandle h1 = i1.getHandle();
							// looks for an existing replica
							EditableHandle h2 = rdx_hnd_dic.get(h1);
							if (h2 == null) {
								h2 = ((EditableHandle) handle_img.get(h1))
										.replicate();
								h2.setOwner(rdx);
								rdx_hnd_dic.put(h1, h2);
							}
							i2.setHandle(h2);
							rdx.inners.put(i2.getName(),i2);
						}
						for (EditableRoot r0 : redex.roots) {
							qp.add(new VState<EditableParent>(null, r0));
						}
						while (!qp.isEmpty()) {
							VState<EditableParent> v = qp.poll();
							// v.c.isNode() || v.c.isRoot() since the agent has
							// not
							// sites
							EditableParent p1 = v.c;
							EditableParent p2;
							if (p1.isRoot()) {
								// ordering is ensured by the queue
								EditableRoot r2 = (EditableRoot) p1.replicate();
								p2 = r2;
								rdx.roots.add(r2);
								r2.setOwner(rdx);
							} else { // isNode()
								EditableNode n1 = (EditableNode) p1;
								EditableNode n0 = node_img.get(n1);
								EditableNode n2 = n0.replicate();
								nEmb.put(n0, n1);
								p2 = n2;
								n2.setParent(v.p);
								// replicate links from node ports
								for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
									EditablePort o = n1.getPort(i);
									EditableHandle h1 = o.getHandle();
									// looks for an existing replica
									EditableHandle h2 = rdx_hnd_dic.get(h1);
									if (h2 == null) {
										h2 = ((EditableHandle) handle_img
												.get(h1)).replicate();
										h2.setOwner(rdx);
										rdx_hnd_dic.put(h1, h2);
									}
									n2.getPort(i).setHandle(h2);
								}
							}
							// enqueues children, if necessary
							for (EditableChild c : p1.getEditableChildren()) {
								if (c.isSite()) {
									EditableSite s1 = (EditableSite) c;
									EditableSite s2 = s1.replicate();
									s2.setParent(p2);
									rdx_sites_dic[redex_sites.indexOf(s1)] = s2;
								} else {
									qp.add(new VState<>(p2,
											(EditableParent) c));
								}
							}
						}
                        rdx.sites.addAll(Arrays.asList(rdx_sites_dic));

						// Replicates prms
						// /////////////////////////////////////////////

						Queue<VState<EditableChild>> qe = new LinkedList<>();
						// replicate inners
						for (EditableInnerName i : redex.inners.values()) {
							EditableOuterName o = new EditableOuterName(
									i.getName());
							prm_ion_dic.put(i, o);
							o.setOwner(prm);
							prm.outers.put(o.getName(),o);
						}
						for (Site s : redex_sites) {
							EditableRoot r = new EditableRoot();
							prm.roots.add(r);
							r.setOwner(prm);
							// enqueue each node that is image of the site
							for (EditableChild c : site_img.get(s)) {
								qe.add(new VState<>(r, c));
							}
						}
						while (!qe.isEmpty()) {
							VState<EditableChild> v = qe.poll();
							if (v.c.isNode()) {
								EditableNode n1 = (EditableNode) v.c;
								EditableNode n2 = n1.replicate();
								n2.setParent(v.p);
								// replicate links from node ports
								for (int j = n1.getControl().getArity() - 1; -1 < j; j--) {
									EditablePort o = n1.getPort(j);
									EditableHandle h2 = null;
									// is this port assigned to an inner name of
									// the redex?
									Map<InnerName, IntegerVariable> pr = lnk_pts
											.get(o);
									if (pr != null) {
										for (InnerName i1 : pr.keySet()) {
											if (lnk_solver.getVar(pr.get(i1))
													.getVal() == 1) {
												h2 = prm_ion_dic.get(i1);
												break;
											}
										}
									}
									if (h2 == null) {
										EditableHandle h1 = o.getHandle();
										h2 = prm_hnd_dic.get(h1);
										if (h2 == null) {
											h2 = h1.replicate();
											h2.setOwner(prm);
										}
										prm_hnd_dic.put(h1, h2);
									}
									n2.getPort(j).setHandle(h2);
								}
								// enqueues children
								for (EditableChild c : n1.getEditableChildren()) {
									qe.add(new VState<>(n2, c));
								}
							} else {
								// v.c.isSite()
								EditableSite s1 = (EditableSite) v.c;
								EditableSite s2 = s1.replicate();
								s2.setParent(v.p);
								prm.sites.add(s2);
								prm_sites_dic[agent_sites.indexOf(s1)] = s2;
							}
						}
                        prm.sites.addAll(Arrays.asList(prm_sites_dic));
						if (DEBUG_CONSISTENCY_CHECK) {
							if (!ctx.isConsistent())
								throw new RuntimeException(
										"Inconsistent bigraph (ctx)");
							if (!rdx.isConsistent())
								throw new RuntimeException(
										"Inconsistent bigraph (rdx)");
							if (!prm.isConsistent())
								throw new RuntimeException(
										"Inconsistent bigraph (prm)");
						}
						//matchQueue.add(new BigraphMatch(ctx, rdx, prm,nEmb));
					} while (lnk_solver.nextSolution());
				} while (this.matchQueue.isEmpty());
			}
		}
	}
}
