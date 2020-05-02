package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyContainer;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;
import it.uniud.mads.jlibbig.core.util.NameGenerator;

import java.util.Collection;
import java.util.Iterator;

/**
 * Objects created from this class are bigraphical controls describing the
 * modality (i.e. active or passive) and the arity (i.e. the number of ports) of
 * nodes decorated with it. Every {@link DirectedBigraph} has a {@link DirectedSignature}
 * describing the controls that can be assigned to its nodes; every {@link Node}
 * should be assigned exactly one control.
 */
public final class DirectedControl extends it.uniud.mads.jlibbig.core.Control implements PropertyTarget {

    private final boolean active;
    private final PropertyContainer props;
    private int arityOut;
    private int arityIn;
    String _tostring;

    /**
     * Creates a control for the given arity and modality and assign it a fresh
     * name.
     *
     * @param active   a boolean indicating whether the control is active or passive.
     * @param arityOut a non-negative integer defining the number of upper ports of the
     *                 nodes decorated with this control.
     * @param arityIn  a non-negative integer defining the number of lower ports of the
     *                 nodes decorated with this control.
     */
    public DirectedControl(boolean active, int arityOut, int arityIn) {
        this("DC_" + NameGenerator.DEFAULT.generate(), active, arityOut, arityIn);
    }

    /**
     * Creates a control for the given name, arity and modality.
     *
     * @param name     the name of the control.
     * @param active   a boolean indicating whether the control is active or passive.
     * @param arityOut a non-negative integer defining the number of upper ports of the
     *                 nodes decorated with this control.
     * @param arityIn  a non-negative integer defining the number of lower ports of the
     *                 nodes decorated with this control.
     */
    public DirectedControl(String name, boolean active, int arityOut, int arityIn) {
        super(name, arityOut + arityIn);
        this.arityOut = arityOut;
        this.arityIn = arityIn;
        this.active = active;
        this.props = new PropertyContainer(this);
    }

    /**
     * Get control's positive arity. This corresponds to the number of upper ports of a node.
     *
     * @return control's positive arity.
     */
    public int getArityOut() {
        return arityOut;
    }

    /**
     * Get control's negative arity. This corresponds to the number of lower ports of a node.
     *
     * @return control's negative arity.
     */
    public int getArityIn() {
        return arityIn;
    }

    /**
     * Checks if the control is active.
     *
     * @return a boolean indicating whether the control is active or passive.
     */
    public final boolean isActive() {
        return active;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + getArity();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DirectedControl other = (DirectedControl) obj;
        return !(getArityOut() != other.getArityOut()
                || getArityIn() != other.getArityIn()
                || !super.getName().equals(other.getName()));
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

    public String toString(boolean includeProperties) {
        StringBuilder builder = null;
        if (_tostring == null) {
            builder = new StringBuilder();
            builder.append(getName()).append(":(").append(getArityOut()).append("+,").append(getArityIn()).append("-").append((active) ? ",a)" : ",p)");
            _tostring = builder.toString();
        }
        if (includeProperties) {
            Collection<Property<?>> ps = props.getProperties();
            if (ps.isEmpty())
                return _tostring;
            if (builder == null) {
                builder = new StringBuilder(_tostring);
            }
            builder.append(" [");
            Iterator<Property<?>> ip = ps.iterator();
            while (ip.hasNext()) {
                Property<?> p = ip.next();
                builder.append(p.getName()).append('=').append(p.get());
                if (ip.hasNext()) {
                    builder.append("; ");
                } else {
                    builder.append(']');
                }
            }
            return builder.toString();
        } else {
            return _tostring;
        }
    }

	/* Properties handling */

    @Override
    public Property<?> attachProperty(Property<?> prop) {
        return props.attachProperty(prop);
    }

    @Override
    public <V> Property<V> detachProperty(Property<V> prop) {
        return props.detachProperty(prop);
    }

    @Override
    public <V> Property<V> detachProperty(String name) {
        return props.detachProperty(name);
    }

    @Override
    public <V> Property<V> getProperty(String name) {
        return props.getProperty(name);
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return props.getProperties();
    }

    @Override
    public Collection<String> getPropertyNames() {
        return props.getPropertyNames();
    }

}
