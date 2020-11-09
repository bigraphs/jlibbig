package it.uniud.mads.jlibbig.core.imports.records;

import java.util.ArrayList;
import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfProperty;

/**
 * This class implement a record for Bigraph's Property
 */
public abstract class BigraphPropertyRecord {

    private String id;
    private TypeOfProperty type;
    private List<String> property = new ArrayList<String>();

    /**
     * 
     * @param id       the Property Id
     * @param property the Property content
     */
    public BigraphPropertyRecord(String id, TypeOfProperty type, List<String> property) {
        this.id = id;
        this.type = type;
        this.property = property;
    }

    /**
     * 
     * @return this Object id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @return this Object type
     */
    public TypeOfProperty getType() {
        return type;
    }

    /**
     * 
     * @return this Object property
     */
    public List<String> getProperties() {
        return property;
    }

    /**
     * 
     * @return this Object property
     */
    public String getProperty(int index) {
        return property.get(index);
    }
}
