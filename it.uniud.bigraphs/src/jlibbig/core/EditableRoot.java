package jlibbig.core;

import java.util.*;

class EditableRoot implements EditableParent, Root, PlaceEntity, EditableOwned{
	
	private Set<EditableChild> children = new HashSet<>();
	private final Set<Child> ro_chd;
	private Owner owner;
	
	@SuppressWarnings("unchecked")
	EditableRoot(){
		ro_chd = (Set<Child>) (Set<? extends Child>)  Collections.unmodifiableSet(this.children);
	}

	@Override
	public Set<Child> getChildren() {
		return this.ro_chd;
	}
	
	@Override
	public void addChild(EditableChild child) {
		if(child == null)
			return;
		this.children.add(child);
		if(this != child.getParent()){
			child.setParent(this);
		}
	}

	@Override
	public void removeChild(EditableChild child) {
		if(child == null)
			return;
		this.children.remove(child);
		if(this == child.getParent())
				child.setParent(null);
	}
	
	public Set<EditableChild> getEditableChildren(){
		return this.children;
	}	
	
	@Override
	public EditableRoot getRoot() {
		return this;
	}
	
	@Override
	public EditableRoot replicate(){
		return new EditableRoot();
	}

	@Override
	public Owner getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(Owner value){
		this.owner = value;
	}

	@Override
	public boolean isParent() {
		return true;
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	public boolean isRoot() {
		return true;
	}

	@Override
	public boolean isSite() {
		return false;
	}

	@Override
	public boolean isNode() {
		return false;
	}
}
