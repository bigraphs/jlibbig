package it.uniud.mads.jlibbig.core.imports.ldb.records;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;

/**
 * This class implement a record for Directed Bigraph's Root
 */
public class DirectedBigraphRootRecord extends DirectedBigraphObjectRecord {

    private int location = 0;

    /**
     * @param id        the Root's id
     * @param label     the Root's label
     * @param location  the Root's location
     */
    public DirectedBigraphRootRecord(String id, String label, int location) {
        super(id, label, TypeOfNodes.root);
        this.location = location;
    }

    /**
     * @return the Root's location
     */
    public int getLocation() {
        return location;
    }
}