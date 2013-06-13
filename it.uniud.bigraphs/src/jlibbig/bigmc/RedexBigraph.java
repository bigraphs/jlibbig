package jlibbig.bigmc;

import java.util.*;

import jlibbig.core.*;

public class RedexBigraph{
	private Set<String> outerNames;
	private Bigraph big;

	public RedexBigraph( Bigraph big, Set<String> outerNames ){
		this.outerNames = outerNames;
		this.big = big;
	}

	public Bigraph getBigraph(){
		return big;
	}

	public Set<String> getOuters(){
		return Collections.unmodifiableSet( this.outerNames );
	}
}
