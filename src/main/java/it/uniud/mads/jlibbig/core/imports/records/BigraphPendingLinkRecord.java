package it.uniud.mads.jlibbig.core.imports.records;

import org.json.JSONObject;

/**
 * This class implement a record for Bigraph's pending link
 */
public abstract class BigraphPendingLinkRecord {

    private String source;
    private String target;
    private JSONObject metadata;

    /**
     * @param source   the PendingLink's source
     * @param target   the PendingLink's target
     * @param metadata the PendingLink's metadata
     */
    public BigraphPendingLinkRecord(String source, String target, JSONObject metadata) {
        this.source = source;
        this.target = target;
        this.metadata = metadata;
    }

    /**
     * @return the source
     */
    public final String getSource() {
        return source;
    }

    /**
     * @return the target
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @return the metadata
     */
    public final JSONObject getMetadata() {
        return metadata;
    }

}