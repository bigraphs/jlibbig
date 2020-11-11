package it.uniud.mads.jlibbig.core.imports.ldb.records;

import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.records.BigraphPendingLinkRecord;

public class DirectedBigraphPendingLinkRecord extends BigraphPendingLinkRecord {

    /**
     * @param source   the PendingLink's source
     * @param target   the PendingLink's target
     * @param metadata the PendingLink's metadata
     */
    public DirectedBigraphPendingLinkRecord(String source, String target, JSONObject metadata) {
        super(source, target, metadata);
    }
}