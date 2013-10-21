package it.uniud.mads.jlibbig.core.std;

import java.util.*;

/**
 * Bigraphs' signatures are immutable. To make a signature, users can use
 * {@link SignatureBuilder}.
 */
public class Signature extends it.uniud.mads.jlibbig.core.Signature<Control> {

	public Signature(Collection<Control> controls) {
		this(null, controls);
	}

	public Signature(String usid, Collection<Control> controls) {
		super(usid, controls);
	}

	public Signature(Control... controls) {
		this(null, controls);
	}

	public Signature(String usid, Control... controls) {
		super(usid, controls);
	}

	public boolean isSubSignature(Signature other){
		return other != null && this.containsAll(other.ctrls.values());
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.equals(obj,false);
	}
	
	public boolean equals(Object obj, boolean ignoreUSID) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Signature))
			return false;
		Signature other = (Signature) obj;
		if (!(ignoreUSID || USID.equals(other.USID)))
			return false;
		for (Control c : this) {
			if (!c.equals(other.getByName(c.getName())))
				return false;
		}
		for (Control c : other) {
			if (!c.equals(this.getByName(c.getName())))
				return false;
		}
		return true;
	}
}
