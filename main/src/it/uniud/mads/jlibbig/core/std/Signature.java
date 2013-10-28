package it.uniud.mads.jlibbig.core.std;

/**
 * Objects created from this class are bigraphical dynamic signatures.A
 * signature defines the controls that can be assigned to the nodes of bigraphs
 * over it. A dynamic {@link Control} describes the arity the arity (i.e. the
 * number of ports) and the modality (i.e. active or passive) of a {@link Node}
 * decorated with it. The class {@link SignatureBuilder} provides some helper
 * methods for signature construction since objects created from the signature
 * class are immutable and all controls have to be specified on instantiation
 */
public class Signature extends it.uniud.mads.jlibbig.core.Signature<Control> {

	/**
	 * Creates a new signature for the given list of controls; a fresh identifier is choosen.
	 * Controls can not have the same name. 
	 * 
	 * @param controls the controls contained within the signature.
	 */
	public Signature(Iterable<Control> controls) {
		this(null, controls);
	}

	/**
	 * Creates a new signature for the given identifier and list of controls.
	 * Controls can not have the same name.
	 * 
	 * @param usid
	 *            the identifier of the signature.
	 * @param controls
	 *            the controls contained within the signature.
	 */
	public Signature(String usid, Iterable<Control> controls) {
		super(usid, controls);
	}

	/**
	 * Creates a new signature for the given list of controls; a fresh identifier is choosen.
	 * Controls can not have the same name. 
	 * 
	 * @param controls the controls contained within the signature.
	 */
	public Signature(Control... controls) {
		this(null, controls);
	}

	/**
	 * Creates a new signature for the given identifier and list of controls.
	 * Controls can not have the same name. 
	 * 
	 * @param usid the identifier of the signature.
	 * @param controls the controls contained within the signature.
	 */
	public Signature(String usid, Control... controls) {
		super(usid, controls);
	}

	/**
	 * Checks whatever the signature is contained in the given one. Containment
	 * is based on set inclusion and {@link Control} equality; USIDs are
	 * ignored.
	 * 
	 * @param other
	 *            the other signature.
	 * @return
	 */
	public boolean isSubSignature(Signature other) {
		return other != null
				&& super.ctrls.values().containsAll(other.ctrls.values());
	}
}
