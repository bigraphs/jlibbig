package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.*;
import it.uniud.mads.jlibbig.core.util.NameGenerator;

import java.util.*;

class EditableNode implements Node, EditableParent, EditableChild {
    public static final String PROPERTY_OWNER = "Owner";
    private final List<EditablePort> upperPorts;
    private final List<EditablePort> lowerPorts;
    private final List<? extends Port> ro_upperPorts;
    private final List<? extends Port> ro_lowerPorts;
    private final Collection<? extends Child> ro_chd;
    private final DelegatedProperty.PropertySetter<Owner> ownerSetter;
    private final DelegatedProperty<Owner> ownerProp;
    private final ReplicationListenerContainer rep = new ReplicationListenerContainer();
    private final PropertyContainer props = new PropertyContainer(this);
    private DirectedControl control;
    private EditableParent parent; // redundant with parentProp
    private Collection<EditableChild> children;
    private String name;

    EditableNode(DirectedControl control) {
        this.name = "N_" + NameGenerator.DEFAULT.generate();
        this.control = control;
        List<EditablePort> upperPorts = new ArrayList<>();
        List<EditablePort> lowerPorts = new ArrayList<>();
        for (int i = 0; i < control.getArityPlus(); i++) {
            upperPorts.add(new EditablePort(i, '+'));
        }
        for (int i = 0; i < control.getArityMinus(); i++) {
            lowerPorts.add(new EditablePort(i, '-'));
        }
        this.upperPorts = Collections.unmodifiableList(upperPorts);
        this.lowerPorts = Collections.unmodifiableList(lowerPorts);
        this.children = new HashSet<>();
        this.ro_upperPorts = Collections.unmodifiableList(this.upperPorts);
        this.ro_lowerPorts = Collections.unmodifiableList(this.lowerPorts);
        this.ro_chd = Collections.unmodifiableCollection(this.children);

        this.ownerSetter = new DelegatedProperty.PropertySetter<>();
        this.ownerProp = new DelegatedProperty<Owner>(PROPERTY_OWNER, true,
                ownerSetter);

        props.attachProperty(this.ownerProp);
    }

    EditableNode(DirectedControl control, EditableParent parent) {
        this(control);
        setParent(parent);
    }

//    TODO: handles in directed bigraphs
//    EditableNode(DirectedControl control, EditableParent parent,
//                 List<? extends Handle> handles) {
//        this(control, parent);
//        for (int i = 0; i < Math.min(handles.size(), control.getArity()); i++) {
//            this.ports.get(i).setHandle((it.uniud.mads.jlibbig.core.std.EditableHandle) handles.get(i));
//        }
//    }
//
//    EditableNode(Control control, it.uniud.mads.jlibbig.core.std.EditableParent parent,
//                 it.uniud.mads.jlibbig.core.std.EditableHandle... handles) {
//        this(control, parent);
//        for (int i = 0; i < Math.min(handles.length, control.getArity()); i++) {
//            this.ports.get(i).setHandle(handles[i]);
//        }
//    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + ':' + this.control.getName();
    }

    @Override
    public EditableParent getParent() {
        return this.parent;
    }

    @Override
    public Collection<? extends Child> getChildren() {
        return this.ro_chd;
    }

    @Override
    public List<? extends Port> getPorts() {
        List<Port> ports = new ArrayList<>();
        ports.addAll(this.ro_upperPorts);
        ports.addAll(this.ro_lowerPorts);
        return ports;
    }

    @Override
    public List<? extends Port> getUpperPorts() {
        return this.ro_upperPorts;
    }

    @Override
    public List<? extends Port> getLowerPorts() {
        return this.ro_lowerPorts;
    }

    public List<EditablePort> getPortsForEdit() {
        List<EditablePort> ports = new ArrayList<>();
        ports.addAll(this.upperPorts);
        ports.addAll(this.lowerPorts);
        return ports;
    }

    public List<EditablePort> getUpperPortsForEdit() {
        return this.upperPorts;
    }

    public List<EditablePort> getLowerPortsForEdit() {
        return this.lowerPorts;
    }

    @Override
    public EditablePort getPort(int index) {
        return this.getPortsForEdit().get(index);
    }

    public EditablePort getPort(int index, char direction) {
        if (direction == '+') {
            return this.upperPorts.get(index);
        } else if (direction == '-') {
            return this.lowerPorts.get(index);
        } else {
            return null;
        }
    }

    @Override
    public DirectedControl getControl() {
        return this.control;
    }

    @Override
    public void addChild(EditableChild child) {
        if (child == null)
            return;
        this.children.add(child);
        if (this != child.getParent()) {
            child.setParent(this);
        }
    }

    @Override
    public void removeChild(EditableChild child) {
        if (child == null)
            return;
        this.children.remove(child);
        if (this == child.getParent())
            child.setParent(null);
    }

    @Override
    public Collection<EditableChild> getEditableChildren() {
        return this.children;
    }

    @Override
    public EditableRoot getRoot() {
        return this.parent.getRoot();
    }

    @Override
    public EditableNode replicate() {
        EditableNode copy = new EditableNode(this.control);
        rep.tellReplicated(this, copy);
        return copy;
    }

    @Override
    public boolean isListenerRegistered(ReplicationListener listener) {
        return rep.isListenerRegistered(listener);
    }

    @Override
    public void registerListener(ReplicationListener listener) {
        rep.registerListener(listener);
    }

    @Override
    public boolean unregisterListener(ReplicationListener listener) {
        return rep.unregisterListener(listener);
    }

    @Override
    public Property<?> attachProperty(Property<?> prop) {
        if (prop == null)
            throw new IllegalArgumentException("Argument can not be null.");
        String name = prop.getName();
        if (PROPERTY_OWNER.equals(name))// || PROPERTY_PARENT.equals(name))
            throw new IllegalArgumentException("Property '" + name
                    + "' can not be substituted");
        return props.attachProperty(prop);
    }

    @Override
    public <V> Property<V> detachProperty(Property<V> prop) {
        if (prop == null)
            throw new IllegalArgumentException("Argument can not be null.");
        return this.detachProperty(prop.getName());
    }

    @Override
    public <V> Property<V> detachProperty(String name) {
        if (PROPERTY_OWNER.equals(name))// || PROPERTY_PARENT.equals(name))
            throw new IllegalArgumentException("Property '" + name
                    + "' can not be detached");
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

    @Override
    public Owner getOwner() {
        return this.ownerProp.get();
    }

    @Override
    public EditableNode getEditable() {
        return this;
    }

    @Override
    public boolean isParent() {
        return true;
    }

    @Override
    public void setParent(EditableParent parent) {
        if (this.parent != parent) {
            EditableParent old = this.parent;
            this.parent = parent;
            if (old != null) {
                old.removeChild(this);
            }
            if (parent != null) {
                parent.addChild(this);
                this.ownerSetter.set(parent.<Owner>getProperty(PROPERTY_OWNER));
            }
        }
    }

    @Override
    public boolean isChild() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isSite() {
        return false;
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    public class EditablePort implements Port, EditablePoint {
        private final int number;
        private final char direction;
        private EditableHandle handle;

        private EditablePort(int number, char direction) {
            this.number = number;
            this.direction = direction;
        }

        @Override
        public EditableNode getNode() {
            return EditableNode.this;
        }

        @Override
        public String toString() {
            return number + direction + "@" + EditableNode.this;
        }

        @Override
        public int getNumber() {
            return this.number;
        }

        public char getDirection() {
            return this.direction;
        }

        @Override
        public EditableHandle getHandle() {
            return handle;
        }

        @Override
        public Owner getOwner() {
            return (handle != null) ? handle.getOwner() : EditableNode.this.getOwner();
        }

        @Override
        public EditablePort getEditable() {
            return this;
        }

        @Override
        public boolean isHandle() {
            return false;
        }

        @Override
        public void setHandle(EditableHandle handle) {
            if (this.handle == handle)
                return;
            EditableHandle old = this.handle;
            this.handle = null;
            if (old != null)
                old.unlinkPoint(this);
            this.handle = handle;
            if (handle != null) {
                handle.linkPoint(this);
            }
        }

        @Override
        public boolean isPoint() {
            return true;
        }

        @Override
        public boolean isPort() {
            return true;
        }

        @Override
        public boolean isInnerName() {
            return false;
        }

        @Override
        public boolean isOuterName() {
            return false;
        }

        @Override
        public boolean isEdge() {
            return false;
        }
    }
}
