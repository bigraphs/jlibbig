package it.uniud.mads.jlibbig.core.imports.ldb.records;

import it.uniud.mads.jlibbig.core.imports.records.BigraphControlRecord;

/**
 * This class implement a record for Directed Bigraph's Control (signature)
 */
public class DirectedBigraphControlRecord extends BigraphControlRecord {

    /**
     * @param name     the Control's name
     * @param arityIn  the Control's in port
     * @param arityOut the Control's out port
     * @param active   the Control's status
     */
    public DirectedBigraphControlRecord(String name, int arityIn, int arityOut, boolean active) {
        super(name, arityIn, arityOut, active);
    }

    /**
     * @param name     the Control's name
     * @param arityIn  the Control's in port
     * @param arityOut the Control's out port
     */
    public DirectedBigraphControlRecord(String name, String arityIn, String arityOut) {
        super(name, arityIn, arityOut);
    }
}