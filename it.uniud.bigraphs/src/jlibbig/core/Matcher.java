package jlibbig.core;

public interface Matcher<A extends AbstBigraph, R extends AbstBigraph>  {
	Iterable<Match<A>> match(A agent, R redex);
}
