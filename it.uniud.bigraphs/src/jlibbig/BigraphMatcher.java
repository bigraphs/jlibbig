package jlibbig;

import java.util.*;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.*;

@SuppressWarnings("unused")
public class BigraphMatcher {

	public static Iterator<BigraphMatch> getMatches(Bigraph agent,
			Bigraph redex) throws IncompatibleSignatureException {
		/*
		 * done by the iterator
		 * if(!agent.getSignature().equals(redex.getSignature())) throw new
		 * IncompatibleSignatureException(); if(!agent.isAgent()) throw new
		 * IllegalArgumentException
		 * ("Rewriting rules can be applied only to agents.");
		 */
		// return new BigraphMatchIterator(agent,redex);
		return getAllMatches(agent, redex).iterator();
	}

	public static Set<BigraphMatch> getAllMatches(Bigraph agent,
			Bigraph redex) throws IncompatibleSignatureException {
		/*
		 * Set<BigraphMatch> s = new HashSet<>(); Iterator<BigraphMatch> ir =
		 * getMatches(agent,redex); while(ir.hasNext()){ s.add(ir.next()); }
		 * return s;
		 */
		if (!agent.getSignature().equals(redex.getSignature()))
			throw new IncompatibleSignatureException();
		if (!agent.isAgent())
			throw new IllegalArgumentException(
					"Rewriting rules can be applied only to agents.");

		// matches found
		Set<BigraphMatch> matches = new HashSet<>();

		// some alias
		LinkGraph rlg = redex.getLinkGraph();
		PlaceGraph rpg = redex.getPlaceGraph();
		LinkGraph alg = agent.getLinkGraph();
		PlaceGraph apg = agent.getPlaceGraph();

		/*
		 * // study redex graph combinatorics for parameter instantiation //
		 * sites are handled by the model but links are left out // groups inner
		 * names linked together
		 * Map<LinkGraphAbst.Linker,Set<LinkGraphAbst.Linked>> inners_partition
		 * = new HashMap<>(); for(InnerName n : rlg.getInnerNames()){
		 * LinkGraphAbst.Linker l = rlg.getLink(n);
		 * if(!inners_partition.containsKey(l))
		 * inners_partition.put(l,rlg.getLinked(l)); }
		 */

		// CSP Model ///////////////////////////////////////////////////////////

		
		/*
		// numerical representation of place graph elements

		int rdx_roots = 0; // roots range = index of the first node
		int rdx_nodes; // nodes range = index of the first site
		int rdx_sites; // sites range = number of columns

		int agt_roots = 0; // roots range = index of the first node
		int agt_nodes; // nodes range = number of rows
		// int agt_sites;

		// translation maps
		BiMap<PlaceGraph.Parent, Integer> rdx_map_prn = new BiMap<>();
		BiMap<PlaceGraph.Child, Integer> rdx_map_chd = new BiMap<>();
		BiMap<PlaceGraph.Parent, Integer> agt_map_prn = new BiMap<>();
		BiMap<PlaceGraph.Child, Integer> agt_map_chd = new BiMap<>();

		BiMap<Root, Integer> rdx_map_roots = new BiMap<>();
		BiMap<PlaceGraphNode, Integer> rdx_map_nodes = new BiMap<>();
		BiMap<Site, Integer> rdx_map_sites = new BiMap<>();
		BiMap<Root, Integer> agt_map_roots = new BiMap<>();
		BiMap<PlaceGraphNode, Integer> agt_map_nodes = new BiMap<>();

		// populate them
		for (Root e : rpg.getRoots()) {
			rdx_map_roots.put(e, rdx_roots);
			rdx_map_prn.put(e, rdx_roots);
			rdx_roots++;
		}
		rdx_nodes = rdx_roots;
		for (PlaceGraphNode e : rpg.getNodes()) {
			rdx_map_nodes.put(e, rdx_nodes);
			rdx_map_chd.put(e, rdx_nodes);
			rdx_map_prn.put(e, rdx_nodes);
			rdx_nodes++;
		}
		rdx_sites = rdx_nodes;
		for (Site e : rpg.getSites()) {
			rdx_map_sites.put(e, rdx_sites);
			rdx_map_chd.put(e, rdx_sites);
			rdx_sites++;
		}
		for (Root e : apg.getRoots()){
			agt_map_roots.put(e, agt_roots);
			agt_map_prn.put(e, agt_roots);
			agt_roots++;
		}
		agt_nodes = agt_roots;
		for (PlaceGraphNode e : apg.getNodes()) {
			agt_map_nodes.put(e, agt_nodes);
			agt_map_chd.put(e, agt_nodes);
			agt_map_prn.put(e, agt_nodes);
			agt_nodes++;
		}

		// columns and rows for the CSP matrix
		int cols = rdx_sites; // j
		int rows = agt_nodes; // i

		Model model = new CPModel();

		// declare the match matrix
		IntegerVariable[][] matrix = new IntegerVariable[rows][cols];

		// add to the model
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// these are binary (however some are not used at all)
				matrix[i][j] = Choco.makeIntVar("M_" + i + "_" + j, 0, 1, "");
				model.addVariable(matrix[i][j]);
			}
		}

		// begin constraints ///////////////////////////////////////////////////

		// 1 // M_ij = 0 if i is a root and j not
		for (int i = 0; i < agt_roots; i++) {
			for (int j = rdx_roots; j < cols; j++) {
				model.addConstraint(Choco.eq(0, matrix[i][j]));
			}
		}

		// /////////////////////////////////////////////////////////////////////

		// 2 // M_ij = 0 if ctrls are different
		for (int i = agt_roots; i < rows; i++) { // i in nodes
			for (int j = rdx_roots; j < rdx_nodes; j++) { // j in nodes
				if (agt_map_nodes.getKey(i).getControl() != rdx_map_nodes
						.getKey(j).getControl()) {
					model.addConstraint(Choco.eq(0, matrix[i][j]));
				}
			}
		}

		// /////////////////////////////////////////////////////////////////////

		// 3 // M_ij <= M_fg if f = prnt(i) and g = prnt(j)
		for (int i = agt_roots; i < rows; i++) { // i in nodes
			// the parent of i
			int f = agt_map_prn.get(apg.getParentOf(agt_map_nodes.getKey(i)));
			for (int j = rdx_roots; j < cols; j++) { // j in nodes + sites
				// the parent of j
				int g = rdx_map_prn.get(apg.getParentOf(rdx_map_chd.getKey(j)));
				model.addConstraint(Choco.leq(matrix[i][j], matrix[f][g]));
			}
		}

		// /////////////////////////////////////////////////////////////////////

		// 4 // M_ij = 0 if j is a root and i is not in an active context

		// every control below passive is so
		if (passive > 0) {
			// boolean map for passive nodes
			boolean[] pasv = new boolean[agt_nodes];
			for (int i = 0; i < agt_roots; i++) {
				pasv[i] = false;
			}
			for (int i = agt_roots; i < agt_nodes; i++) {
				pasv[i] = agent.ctrl.get(i) < passive;
			}
			for (int f = agt_roots; f < agt_nodes; f++) {
				List<Integer> ancs = agent.ancs.get(f);
				for (int i : ancs) {
					if (pasv[i]) {
						for (int j = 0; j < rdx_roots; j++) { // j in roots
							model.addConstraint(Choco.eq(0, matrix[i][j]));
						}
					}
				}
			}
		}

		// /////////////////////////////////////////////////////////////////////

		// 5 // sum M_ij = 1 if j not in sites
		for (int j = 0; j < rdx_nodes; j++) { // j in roots + nodes
			IntegerVariable[] col = new IntegerVariable[rows];
			for (int i = 0; i < rows; i++) { // i in roots + nodes
				col[i] = matrix[i][j];
			}
			model.addConstraint(Choco.eq(1, Choco.sum(col)));
		}

		// /////////////////////////////////////////////////////////////////////

		// 6 // n sum(j not root) M_ij + sum(j root) M_ij <= n if i in nodes
		for (int i = agt_roots; i < rows; i++) { // i in nodes
			model.addConstraint(Choco.geq(rdx_roots, Choco.sum(Choco.mult(
					rdx_roots,
					Choco.sum(Arrays.copyOfRange(matrix[i], rdx_roots, cols))),
					Choco.sum(Arrays.copyOfRange(matrix[i], 0, rdx_roots)))));
		}

		// /////////////////////////////////////////////////////////////////////

		// 7 // |chld(f)| M_fg <= sum(i chld(f), j in chld(g)) M_ij if f,g in
		// nodes
		for (PlaceGraphNode an : apg.getNodes()) { // f in nodes
			int f = agt_map_nodes.get(an);
			for (int g = rdx_roots; g < rdx_nodes; g++) {// g in nodes
				Set<Child> cf = apg.getChildrenOf(agt_map_prn.getKey(f));
				List<Integer> cg = redex.chld.get(g);
				// sub-matrix cf,cg
				IntegerVariable[] children = new IntegerVariable[cf.size() * cg.size()];
				int k = 0;
				for (int i : cf) {
					for (int j : cg) {
						children[k++] = matrix[i][j];
					}
				}
				model.addConstraint(Choco.leq(
						Choco.mult(cf.size(), matrix[f][g]),
						Choco.sum(children)));
			}
		}
		// see Lemma 1
		// 11 // M_fg <= sum(j in chld(g)) M_ij if f,g in nodes and i in
		// child(f)
		for (int f = agt_roots; f < rows; f++) { // f in nodes
			for (int g = rdx_roots; g < rdx_nodes; g++) {// g in nodes
				for (int i : agent.chld.get(f)) {
					List<Integer> cg = redex.chld.get(g);
					// children of g
					IntegerVariable[] children = new IntegerVariable[cg.size()];
					int k = 0;
					for (int j : cg) {
						children[k++] = matrix[i][j];
					}
					model.addConstraint(Choco.leq(matrix[f][g],
							Choco.sum(children)));
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////////////////

		// 8 // |chld(g) not sites| M_fg <= sum(i chld(f), j chld(g) not sites)
		// if g in roots
		for (int f = 0; f < rows; f++) { // f in roots + nodes
			for (int g = 0; g < rdx_roots; g++) {// g in roots
				List<Integer> cf = agent.chld.get(f);
				List<Integer> cg = redex.chld.get(g);
				cg.removeAll(redex.sites);
				// sub-matrix cf,cg
				IntegerVariable[] children = new IntegerVariable[cf.size()
						* cg.size()];
				int k = 0;
				for (int i : cf) {
					for (int j : cg) {
						children[k++] = matrix[i][j];
					}
				}
				model.addConstraint(Choco.leq(
						Choco.mult(cg.size(), matrix[f][g]),
						Choco.sum(children)));
			}
		}

		// ////////////////////////////////////////////////////////////////////////////////

		// 9 // sum(f ancs(i)\{i}, g in m) M_fg + M_ij <= 1 if j in roots

		for (int i = agt_roots; i < rows; i++) { // i in nodes
			for (int j = 0; j < rdx_roots; j++) { // j in roots
				// a variable for every ancestor of i but not i, site and M_ij
				List<Integer> ancs = agent.ancs.get(i);
				IntegerVariable[] ms = new IntegerVariable[(ancs.size() - 1)
						* (rdx_sites - rdx_nodes) + 1];
				int k = 0;
				for (int f : ancs) {
					if (f != i) {
						for (int g = rdx_nodes; g < rdx_sites; g++) {
							ms[k++] = matrix[f][g];
						}
					}
				}
				ms[k] = matrix[i][j];
				model.addConstraint(Choco.geq(1, Choco.sum(ms)));
			}
		}

		// end constraints
		// /////////////////////////////////////////////////////////////////////

		
		// Model resolution ////////////////////////////////////////////////////
		CPSolver solver = new CPSolver();
		solver.read(model);
		solver.solve();
		do {
			if (solver.checkSolution()) {
				// read vars
				// check links
				// instantiate args
			}
		} while (solver.nextSolution());
		 */
		
		return matches;
	}

	/*
	 * TODO incremental solution private static class BigraphMatchIterator
	 * implements Iterator<BigraphMatch>{
	 * 
	 * private final BigraphView _redex;
	 * 
	 * // Choco solver CPSolver solver = new CPSolver();
	 * 
	 * BigraphMatchIterator(BigraphView agent, BigraphView redex) throws
	 * IncompatibleSignatureException{
	 * if(!agent.getSignature().equals(redex.getSignature())) throw new
	 * IncompatibleSignatureException(); if(!agent.isAgent()) throw new
	 * IllegalArgumentException
	 * ("Rewriting rules can be applied only to agents."); this._redex = redex;
	 * 
	 * 
	 * // Model ///////////////////////////////////////////////////////////
	 * 
	 * // Choco model of the CSP modeling the empbedding problem Model model=
	 * new CPModel();
	 * 
	 * }
	 * 
	 * @Override public boolean hasNext() { // TODO Auto-generated method stub
	 * return false; }
	 * 
	 * @Override public BigraphMatch next() { // TODO Auto-generated method stub
	 * return null; }
	 * 
	 * @Override public void remove() { throw new
	 * UnsupportedOperationException(); }
	 * 
	 * }
	 */

}
