package jlibbig.core.exceptions;

import java.util.*;

public class NameClashException extends RuntimeException {
	private static final long serialVersionUID = 7906403825825798881L;

	private final Set<String> names;

	public NameClashException(Set<String> names){
		this(null, names);
	} 
	
	public NameClashException(String message, Set<String> names) {
		super(message);
		if (!names.isEmpty()) {
			Set<String> ns = new HashSet<>();
			for (String n : names) {
				ns.add(n);
			}
			this.names = Collections.unmodifiableSet(ns);
		} else {
			this.names = null;
		}
	}
	
	public NameClashException(String message, String... names) {
		super(message);
		if (names.length > 0) {
			Set<String> ns = new HashSet<>();
			for (int i = 0; i < names.length; i++) {
				ns.add(names[i]);
			}
			this.names = Collections.unmodifiableSet(ns);
		} else {
			this.names = null;
		}
	}

	public Set<String> getNames() {
		return this.names;
	}

	@Override
	public String toString() {
		if (names != null) {
			return super.toString() + "\nClash over '" + names + "'";
		} else {
			return super.toString();
		}
	}

}
