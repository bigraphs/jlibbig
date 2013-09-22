package it.uniud.mads.jlibbig.core.exceptions;

import java.util.*;

import it.uniud.mads.jlibbig.core.Control;
import it.uniud.mads.jlibbig.core.Signature;

public class IncompatibleSignatureException extends RuntimeException {

	private static final long serialVersionUID = -2623617487911027928L;

	List<Signature<? extends Control>> sigs;

	public IncompatibleSignatureException(
			Collection<? extends Signature<? extends Control>> signatures) {
		super();
		List<Signature<? extends Control>> sigs = new ArrayList<>(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	public IncompatibleSignatureException(String message,
			Collection<? extends Signature<? extends Control>> signatures) {
		super(message);
		List<Signature<? extends Control>> sigs = new ArrayList<>(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	@SafeVarargs
	public IncompatibleSignatureException(
			Signature<? extends Control>... signatures) {
		List<Signature<? extends Control>> sigs = Arrays.asList(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	@SafeVarargs
	public IncompatibleSignatureException(String message,
			Signature<? extends Control>... signatures) {
		super(message);
		List<Signature<? extends Control>> sigs = Arrays.asList(signatures);
		this.sigs = Collections.unmodifiableList(sigs);
	}

	public List<Signature<? extends Control>> getClashingSignatures() {
		return this.sigs;
	}

}
