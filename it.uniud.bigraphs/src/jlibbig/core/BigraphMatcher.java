package jlibbig.core;

import java.util.*;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;

public final class BigraphMatcher implements Matcher<Bigraph, Bigraph> {

	@Override
	public Iterable<Match<Bigraph>> match(Bigraph agent, Bigraph redex) {
		return new MatchInstance(agent, redex);
	}

	private static class MatchInstance implements Iterable<Match<Bigraph>>, Iterator<Match<Bigraph>> {

		final Bigraph agent, redex;
		final CPModel model;
		final CPSolver solver;
		private final Queue<Match<Bigraph>> matchQueue = new LinkedList<>();

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

			// MODEL ///////////////////////////////////////////////////////////
			this.model = new CPModel();
			
			// SOLVE ///////////////////////////////////////////////////////////
			this.solver = new CPSolver();
			solver.read(model);
			solver.generateSearchStrategy();
			populateQueue();
		}

		@Override
		public Iterator<Match<Bigraph>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return !this.matchQueue.isEmpty();
		}

		@Override
		public Match<Bigraph> next() {
			if(this.matchQueue.isEmpty())
				return null;
			Match<Bigraph> match = this.matchQueue.poll();
			if(this.matchQueue.isEmpty())
				populateQueue();
			return match;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("");
		}
				
		private void populateQueue(){
			// look for a solution for the CSP
			while(!solver.isConsistent()){
				if(!solver.nextSolution())
					return; // no more consistent solutions
			}
			// TODO read vars and make matches
		}

	}
}
