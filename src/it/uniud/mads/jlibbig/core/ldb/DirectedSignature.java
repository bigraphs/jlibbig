package it.uniud.mads.jlibbig.core.ldb;

/**
 * Objects created from this class are bigraphical dynamic signatures.A
 * signature defines the controls that can be assigned to the nodes of bigraphs
 * over it. A dynamic {@link DirectedControl} describes the arity the arity (i.e. the
 * number of ports) and the modality (i.e. active or passive) of a {@link Node}
 * decorated with it. The class {@link DirectedSignatureBuilder} provides some helper
 * methods for signature construction since objects created from the signature
 * class are immutable and all controls have to be specified on instantiation
 */
public class DirectedSignature extends it.uniud.mads.jlibbig.core.Signature<DirectedControl> {

    /**
     * Creates a new directed signature for the given list of directed controls; a fresh
     * identifier is choosen. Controls can not have the same name.
     *
     * @param controls the controls contained within the signature.
     */
    public DirectedSignature(Iterable<DirectedControl> controls) {
        this(null, controls);
    }

    /**
     * Creates a new directed signature for the given identifier and list of directed controls.
     * Controls can not have the same name.
     *
     * @param usid     the identifier of the signature.
     * @param controls the controls contained within the signature.
     */
    public DirectedSignature(String usid, Iterable<DirectedControl> controls) {
        super(usid, controls);
    }

    /**
     * Creates a new directed signature for the given list of directed controls; a fresh
     * identifier is choosen. Controls can not have the same name.
     *
     * @param controls the controls contained within the signature.
     */
    public DirectedSignature(DirectedControl... controls) {
        this(null, controls);
    }

    /**
     * Creates a new directed signature for the given identifier and list of directed controls.
     * Controls can not have the same name.
     *
     * @param usid     the identifier of the signature.
     * @param controls the controls contained within the signature.
     */
    public DirectedSignature(String usid, DirectedControl... controls) {
        super(usid, controls);
    }

    /**
     * Checks whether the signature is contained in the given one. Containment
     * is based on set inclusion and {@link DirectedControl} equality; USIDs are
     * ignored.
     *
     * @param other the other signature.
     * @return a boolean indicating whether this object is contained in the
     * argument.
     */
    public boolean isSubSignature(DirectedSignature other) {
        return other != null
                && super.ctrls.values().containsAll(other.ctrls.values());
    }
}
