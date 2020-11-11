package it.uniud.mads.jlibbig.core.imports.ldb.records;

import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;

/**
 * This class implement a record for Directed Bigraph's Node
 */
public class DirectedBigraphNodeRecord extends DirectedBigraphObjectRecord {

    private DirectedBigraphControlRecord control;

    List<DirectedBigraphPropertyRecord> properties;

    /**
     * @param id        the Node's id
     * @param label     the Node's label
     * @param control   the Node's control
     * @param attribute the Node's attribute
     * @param params    the Node's params
     * @param behaviour the Node's behaviour
     * @param events    the Node's events
     */
    public DirectedBigraphNodeRecord(String id, String label, DirectedBigraphControlRecord control, List<DirectedBigraphPropertyRecord> properties) {
        super(id, label, TypeOfNodes.node);
        this.control = control;
        this.properties = properties;
    }

    /**
     * @return this Node's control
     */
    public DirectedBigraphControlRecord getControl() {
        return control;
    }

    /**
     * 
     * @return this Node's Properties
     */
    public List<DirectedBigraphPropertyRecord> getProperties() {
        return properties;
    } 
}