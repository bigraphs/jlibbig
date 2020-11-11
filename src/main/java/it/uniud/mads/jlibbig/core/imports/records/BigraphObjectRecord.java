package it.uniud.mads.jlibbig.core.imports.records;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;

/**
 * This class implement a record for Bigraph's Object
 */
public abstract class BigraphObjectRecord {

    private String id;
    private String label;
    private TypeOfNodes type;

    /**
     * 
     * @param id    the Object Id
     * @param label the Object Label
     * @param type  the Object type
     */
    public BigraphObjectRecord(String id, String label, TypeOfNodes type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    /**
     * 
     * @return this Object id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @return this Object Label
     */
    public final String getLabel() {
        return label;
    }

    /**
     * 
     * @return this Object Type
     */
    public final TypeOfNodes getType() {
        return type;
    }    
}