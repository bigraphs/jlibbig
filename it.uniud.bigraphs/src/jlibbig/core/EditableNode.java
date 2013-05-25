package jlibbig.core;

import java.util.*;

class EditableNode implements Node, EditableParent, EditableChild {
		private Control control;
		private final List<EditablePort> ports;
		private EditableParent parent;
		private Set<EditableChild> children;
		private final List<? extends Port> ro_ports;
		private final Set<? extends Child> ro_chd;

		EditableNode(Control control){
			this.control = control;
			List<EditablePort> ports = new ArrayList<>();
			for(int i = 0;i<control.getArity();i++){
				ports.add(new EditablePort(i));
			}
			this.ports = Collections.unmodifiableList(ports);
			this.children = new HashSet<>();
			ro_ports = Collections.unmodifiableList(this.ports);
			ro_chd =  Collections.unmodifiableSet(this.children);
		}
		
		EditableNode(Control control,EditableParent parent){
			this(control);
			setParent(parent);
		}
		
		@Override
		public EditableParent getParent() {
			return this.parent;
		}

		@Override
		public Set<? extends Child> getChildren() {
			return this.ro_chd;
		}

		@Override
		public List<? extends Port> getPorts(){
			return this.ro_ports;
		}
		
		public List<EditablePort> getPortsForEdit(){
			return this.ports;
		}

		@Override
		public EditablePort getPort(int index){
			return this.ports.get(index);
		}

		@Override
		public Control getControl(){
			return this.control;
		}
		
		public void setControl(Control value){
			//TODO implement setControl; what about arity changes?
			throw new UnsupportedOperationException("Not implemented yet.");
			//this.control = value;
		}
		
		@Override
		public void setParent(EditableParent parent){
			if(this.parent != null){
				if(!this.parent.equals(parent)){
					this.parent.removeChild(this);
				}
			}
			this.parent = parent;
			if(parent != null){
				parent.addChild(this);
			}
		}
		
		@Override
		public void addChild(EditableChild child) {
			if(child == null)
				return;
			this.children.add(child);
			if(!this.equals(child.getParent())){
				child.setParent(this);
			}
		}

		@Override
		public void removeChild(EditableChild child) {
			if(child == null)
				return;
			this.children.remove(child);
			if(this.equals(child.getParent()))
				child.setParent(null);
		}
		
		public Set<EditableChild> getEditableChildren(){
			return this.children;
		}
		
		@Override
		public EditableNode replicate(){
			return new EditableNode(this.control);
		}
		
		public class EditablePort implements Port, EditablePoint{
			private final int number;
			private EditableHandle handle;
			
			private EditablePort(int number){
				this.number = number;
			}
			
			public EditableNode getNode(){
				return EditableNode.this;
			}
			
			public int getNumber(){
				return this.number;
			}

			@Override
			public EditableHandle getHandle() {
				return handle;
			}
			
			@Override
			public void setHandle(EditableHandle value){
				this.handle = value;
			}
		}
	}
