package it.uniud.mads.jlibbig.core.std;

import it.uniud.mads.jlibbig.core.Named;

/**
 * Describes an entity with a name. <br />
 * e.g.: Controls, outer/innernames
 * 
 */
interface EditableNamed extends Named {
	/**
	 * Set the entity's name
	 * 
	 * @param name
	 *            entity's new name
	 */
	void setName(String name);
}
