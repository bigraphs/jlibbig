package it.uniud.mads.jlibbig.core.imports.ldb.records;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;

/**
 * This class implement a record for Directed Bigraph's Site
 */
public class DirectedBigraphSiteRecord extends DirectedBigraphObjectRecord {

    /**
     * @param id        the Site's id
     * @param label     the Site's label
     */
    public DirectedBigraphSiteRecord(String id, String label) {
        super(id, label, TypeOfNodes.site);
    }
}