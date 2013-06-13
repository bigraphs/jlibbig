package jlibbig.core;

import java.util.*;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

public final class BigraphMatcher implements Matcher<Bigraph, Bigraph> {

	public final static BigraphMatcher DEFAULT = new BigraphMatcher();

	@Override
	public Iterable<Match<Bigraph>> match(Bigraph agent, Bigraph redex) {
		return new MatchInstance(agent, redex);
	}

	private static class MatchInstance implements Iterable<Match<Bigraph>>,
			Iterator<Match<Bigraph>> {

		final Bigraph agent, redex;
		final CPModel model;
		final CPSolver solver;
		
		private final Map<PlaceEntity, Map<PlaceEntity, IntegerVariable>> matrix;
		
		private Queue<Match<Bigraph>> matchQueue = null;

		private final List<? extends Root> agent_roots;
		// private final List<? extends Site> agent_sites;
		private final Set<? extends Node> agent_nodes;

		private final List<? extends Root> redex_roots;
		private final List<? extends Site> redex_sites;
		private final Set<? extends Node> redex_nodes;

		private MatchInstance(Bigraph agent, Bigraph redex) {

			if (!agent.isGround()) {
				throw new UnsupportedOperationException(
						"Agent should be a bigraph with empty inner interface i.e. ground.");
			}
			if (agent.signature != redex.signature) {
				throw new UnsupportedOperationException(
						"Agent and redex should have the same singature.");
			}
			this.agent = agent;
			this.redex = redex;

			this.agent_roots = agent.getRoots();
			// this.agent_sites = agent.getSites();
			this.agent_nodes = agent.getNodes();

			this.redex_roots = redex.getRoots();
			this.redex_sites = redex.getSites();
			this.redex_nodes = redex.getNodes();

			// MODEL ///////////////////////////////////////////////////////////
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
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
					model.addVariable(var);
					row.put(j, var);
				}
				// these will be always zero
				for (Node j : redex_nodes) {
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
					model.addVariable(var);
					model.addConstraint(Choco.eq(0, var));
					row.put(j, var);
				}
				for (Site j : redex_sites) {
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
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
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
					model.addVariable(var);
					row.put(j, var);
				}
				for (Node j : redex_nodes) {
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
					model.addVariable(var);
					row.put(j, var);
				}
				for (Site j : redex_sites) {
					IntegerVariable var = Choco.makeBooleanVar("" + ki + ","
							+ kj++);
					model.addVariable(var);
					row.put(j, var);
				}
				matrix.put(i, row);
				ki++;
				kj = 0;
			}

			// Constraints

			// 2 // M_ij = 0 if ctrls are different ////////////////////////////
			for (Node i : agent_nodes) {
				Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
				for (Node j : redex_nodes) {
					IntegerVariable var = row.get(j);
					if (i.getControl() != j.getControl()) {
						model.addConstraint(Choco.eq(0, var));
					}
				}
			}
			// /////////////////////////////////////////////////////////////////

			// 3 // M_ij <= M_fg if f = prnt(i) and g = prnt(j) ////////////////
			for (Node i : agent_nodes) {
				Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
				for (Child j : redex_nodes) {
					Parent f = i.getParent();
					Parent g = j.getParent();
					model.addConstraint(Choco.leq(row.get(j), matrix.get(f)
							.get(g)));
				}
				for (Child j : redex_sites) {
					Parent f = i.getParent();
					Parent g = j.getParent();
					model.addConstraint(Choco.leq(row.get(j), matrix.get(f)
							.get(g)));
				}
			}
			// /////////////////////////////////////////////////////////////////

			// 4 // M_ij = 0 if j is a root and i is not in an active context //

			/*
			 * Descends the agent parent map deactivating matching (with redex
			 * roots) below every passive node. Nodes in qa were found in an
			 * active context whereas qp are in a passive one or passive.
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

			// 5 // sum M_ij = 1 if j not in sites /////////////////////////////
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

			// 6 // n sum(j not root) M_ij + sum(j root) M_ij <= n if i in nodes
			for (Node i : agent_nodes) {
				Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
				vars = new IntegerVariable[redex_nodes.size()
						+ redex_sites.size()];
				k = 0;
				for (PlaceEntity j : redex_nodes) {
					vars[k++] = row.get(j);
				}
				for (PlaceEntity j : redex_sites) {
					vars[k++] = row.get(j);
				}
				IntegerExpressionVariable c = Choco.mult(rrs, Choco.sum(vars));

				vars = new IntegerVariable[rrs];
				k = 0;
				for (Root j : redex_roots) {
					vars[k++] = row.get(j);
				}
				model.addConstraint(Choco.geq(rrs,
						Choco.sum(c, Choco.sum(vars))));
			}
			// /////////////////////////////////////////////////////////////////

			// 7 // |chld(f)| M_fg <= sum(i chld(f), j in chld(g)) M_ij if f,g in nodes
			for(Parent f : agent_roots){
				for(Parent g : redex_nodes){
					Set<? extends Child> cf = f.getChildren();
					Set<? extends Child> cg = g.getChildren();
					vars = new IntegerVariable[cf.size() * cg.size()];
					k = 0;
					for(PlaceEntity i : cf){
						for(PlaceEntity j : cg){
							vars[k++] = matrix.get(i).get(j);
						}
					}
					model.addConstraint(Choco.leq(
							Choco.mult(cf.size(), matrix.get(f).get(g)),
							Choco.sum(vars)
							));
				}
			}
			// /////////////////////////////////////////////////////////////////
			
			// 8 // |chld(g) not sites| M_fg <= sum(i chld(f), j chld(g) not sites) if g in roots
			for(PlaceEntity f : matrix.keySet()){
				for(Root g : redex_roots){
					Set<? extends Child> cf = ((Parent) f).getChildren();
					Set<? extends Child> cg = new HashSet<>(g.getChildren());
					cg.removeAll(redex_sites);
					vars = new IntegerVariable[cf.size() * cg.size()];
					k = 0;
					for(PlaceEntity i : cf){
						for(PlaceEntity j : cg){
							vars[k++] = matrix.get(i).get(j);
						}
					}
					model.addConstraint(Choco.leq(
							Choco.mult(cf.size(), matrix.get(f).get(g)),
							Choco.sum(vars)
							));
				}
			}			
			// /////////////////////////////////////////////////////////////////
			
			// 9 // sum(f ancs(i)\{i}, g in m) M_fg + M_ij <= 1 if j in roots
			/*
			 * Descends the agent parent map deactivating matching for those having an ancestor matched with a site
			 */
			Stack<Node> ancs = new Stack<>();
			Stack<Node> visit = new Stack<>();
			for (Root r : agent_roots) {
				for (Child cr : r.getChildren()) {
					if (cr instanceof Node) {
						ancs.clear();
						visit.add((Node) cr);
						while(!visit.isEmpty()){
							Node i = visit.pop();
							if(!ancs.isEmpty() && ancs.peek() != i.getParent())
								ancs.pop();
							Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
							vars = new IntegerVariable[rrs];
							k = 0;
							for(Root j : redex_roots){
								vars[k++] = row.get(j);
							}
							IntegerExpressionVariable c = Choco.div(Choco.sum(vars), rrs);
							
							vars = new IntegerExpressionVariable[1 + ancs.size() * rss];
							k = 0;
							for(Node f : ancs){
								row = matrix.get(f);
								for(Site g : redex_sites){
									vars[k++] = row.get(g);
								}
							}
							vars[k] = c;
							model.addConstraint(Choco.geq(1,Choco.sum(vars)));
							// put itself as an ancestor and process each of its children
							ancs.push(i);
							for (Child cn : i.getChildren()) {
								if (cn instanceof Node) {
									visit.add((Node) cn);
								}
							}
						}
					}
				}
			}			
			// end constraints /////////////////////////////////////////////////
			
			this.solver = new CPSolver();
			solver.read(model);
			solver.generateSearchStrategy();
		}

		@Override
		public Iterator<Match<Bigraph>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			if(this.matchQueue == null){
				matchQueue = new LinkedList<>();
				populateMatchQueue(true);
			}
			return !this.matchQueue.isEmpty();
		}

		@Override
		public Match<Bigraph> next() {
			if(this.matchQueue == null){
				matchQueue = new LinkedList<>();
				populateMatchQueue(true);
			}	
			if (this.matchQueue.isEmpty())
				return null;
			Match<Bigraph> match = this.matchQueue.poll();
			if (this.matchQueue.isEmpty())
				populateMatchQueue(false);
			return match;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("");
		}

		private void populateMatchQueue(boolean first) {
			System.out.println("populate matcher queue");
			// look for a solution for the CSP
			if((first && !solver.solve()) || (!first && !solver.nextSolution())){
				System.out.println("no more solutions");
				return;
			}else{
				System.out.println("CPS solution #" + solver.getSolutionCount() + ":");
				for (PlaceEntity i : matrix.keySet()) {
					Map<PlaceEntity, IntegerVariable> row = matrix.get(i);
					for (PlaceEntity j : row.keySet()){
						System.out.print("" + solver.getVar(row.get(j)).getVal() + "; ");
					}
					System.out.println("");
				}
				matchQueue.add(null);
				
			}
			// TODO read vars and make matches
			
		}

	}
}
