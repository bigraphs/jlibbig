package it.uniud.mads.jlibbig.core.imports.ldb.records;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;

public class DirectedBigraphEdgeRecord extends DirectedBigraphObjectRecord {

    /**
     * @param id        the Edge's id
     * @param label     the Edge's label
     * @param type      the Edge's type
     */
    public DirectedBigraphEdgeRecord(String id, String label) {
        super(id, label, TypeOfNodes.edge);
    }

}