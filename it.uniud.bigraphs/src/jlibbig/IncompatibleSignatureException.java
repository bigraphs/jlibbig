package jlibbig;

import java.util.*;

public class IncompatibleSignatureException extends RuntimeException{

	private static final long serialVersionUID = -2623617487911027928L;

	private final List<Signature> sigs;
	
	public IncompatibleSignatureException(Signature s1, Signature s2) {
		List<Signature> sigs = new ArrayList<>(2);
		sigs.add(s1);
		sigs.add(s2);
		this.sigs = Collections.unmodifiableList(sigs);
	}
	
	public List<Signature> getClashingSignatures(){
		return this.sigs;
	}

}
