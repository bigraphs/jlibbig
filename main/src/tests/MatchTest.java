package tests;

import java.io.*;
import java.util.*;
import it.uniud.mads.jlibbig.udlang.*;
import it.uniud.mads.jlibbig.core.*;
import it.uniud.mads.jlibbig.core.lang.*;
import it.uniud.mads.jlibbig.core.std.Bigraph;

public class MatchTest {

	public static void main(String[] args) throws Exception {
		/*
		 * Tests a set of rules against a set of agents. Bigraphs are loaded
		 * incrementally to reduce the memory footprint
		 */

		// queue of redexes and agents
		Queue<Pair<String, Bigraph>> rQueue = new LinkedList<>();
		Queue<Pair<String, Bigraph>> aQueue = new LinkedList<>();
		// max number of redexes and agents loaded at any given time
		int rLimit = 10;
		int aLimit = 1;
		//
		Reader rReader;
		Reader aReader;

	}

	private static class Pair<A, B> {
		final A a;
		final B b;

		Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}

}
