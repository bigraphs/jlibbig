package it.uniud.mads.jlibbig.core.imports.records;

/**
 * This class implement a record for Link
 */
public abstract class BigraphLinkRecord {

    /**
     * @return the link
     */
    public abstract BigraphObjectRecord getLink();

    /**
     * @return the owner port
     */
    public abstract int getOwnerPort();

    /**
     * @return the linked port
     */
    public abstract int getLinkedPort();
}