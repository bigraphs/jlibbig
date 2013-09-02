package jlibbig.core;

import java.util.*;

/**
 * Helper for signature construction. Every instance maintains a set of controls
 * which are used to instantiate signatures on demand.
 * 
 * @see Signature
 */
public class SignatureBuilder {

	private Map<String, Control> ctrls = new HashMap<>();

	/**
	 * Creates an empty builder.
	 */
	public SignatureBuilder() {
	}

	/**
	 * Creates a signature with the control present in the builder
	 * @return a signature
	 */
	public Signature makeSignature() {
		return new Signature(ctrls.values());
	}

	/**
	 * Creates a signature with the control present in the builder and the given
	 * signature identifier.
	 * @return a signature
	 */
	public Signature makeSignature(String usid) {
		return new Signature(usid, ctrls.values());
	}

	/**
	 * Make a control and add it to the signature
	 * @param name name of the control
	 * @param active control's activity
	 * @param arity number of ports
	 */
	public void put(String name, boolean active, int arity) {
		ctrls.put(name, new Control(name,active,arity));
	}

	/**
	 * Check if a name is already used by a signature's control
	 * @param name control's name that will be checked
	 * @return boolean value
	 */
	public boolean contains(String name) {
		return ctrls.containsKey(name);
	}

	/**
	 * Get the control corresponding to the name in input
	 * @param name control's name
	 * @return the corresponding control
	 */
	public Control  get(String name) {
		return ctrls.get(name);
	}

	/**
	 * Get a collection of all controls in the signature
	 * @return collection of signature's controls
	 */
	public Collection<Control > getAll() {
		return Collections.unmodifiableCollection(ctrls.values());
	}

	/**
	 * Remove a control from the signature.
	 * @param name control's name
	 */
	public void remove(String name) {
		ctrls.remove(name);
	}

	/**
	 * Remove all controls from the signature.
	 */
	public void clear() {
		ctrls.clear();
	}

//
//	private static class BGControl extends AbstNamed implements Control{
//		private final boolean active;
//		private final int arity;
//
//		BGControl(String name,boolean active, int arity){
//			super(name);
//			this.arity = arity;
//			this.active = active;
//		}
//
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = super.hashCode();
//			result = prime * result + arity;
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (!super.equals(obj))
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			BGControl other = (BGControl) obj;
//			if (arity != other.arity || super.getName() != other.getName())
//				return false;
//			return true;
//		}
//
//		@Override
//		public String toString() {
//			return getName() + ":(" + arity + ((active) ? ",a)" : ",p)");
//		}
//
//		@Override
//		public int getArity() {
//			return arity;
//		}
//
//		@Override
//		public boolean isActive() {
//			return active;
//		}
//
//	}

}
