package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Named;

interface EditableNamed extends Named {
    /**
     * Set the entity's name
     *
     * @param name entity's new name
     */
    void setName(String name);
}
