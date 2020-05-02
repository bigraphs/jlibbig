package it.uniud.mads.jlibbig.core.ldb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The class provides methods for signature construction since {@link DirectedSignature}
 * is immutable. Every instance of the class maintains a collection of instances
 * of {@link DirectedControl} which are used to instantiate signatures on demand.
 */
public class DirectedSignatureBuilder extends
        it.uniud.mads.jlibbig.core.SignatureBuilder<DirectedControl> {
    private Map<String, DirectedControl> ctrls = new HashMap<>();

    @Override
    public DirectedSignature makeSignature() {
        return new DirectedSignature(ctrls.values());
    }

    @Override
    public DirectedSignature makeSignature(String usid) {
        return new DirectedSignature(usid, ctrls.values());
    }

    /**
     * Creates a new control and add it to the builder.
     *
     * @param name       name of the control
     * @param active     control's activity
     * @param arityPlus  number of upper ports
     * @param arityMinus number of lower ports
     */
    public void add(String name, boolean active, int arityPlus, int arityMinus) {
        ctrls.put(name, new DirectedControl(name, active, arityPlus, arityMinus));
    }

    @Override
    public void add(DirectedControl control) {
        ctrls.put(control.getName(), control);
    }

    @Override
    public boolean contains(String name) {
        return ctrls.containsKey(name);
    }

    @Override
    public DirectedControl get(String name) {
        return ctrls.get(name);
    }

    @Override
    public Collection<DirectedControl> getAll() {
        return Collections.unmodifiableCollection(ctrls.values());
    }

    @Override
    public void remove(String name) {
        ctrls.remove(name);
    }

    @Override
    public void clear() {
        ctrls.clear();
    }
}
