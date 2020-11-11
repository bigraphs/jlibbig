package it.uniud.mads.jlibbig.core.imports.ldb.records;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.DirectionOfLink;
import it.uniud.mads.jlibbig.core.imports.records.BigraphLinkRecord;
import it.uniud.mads.jlibbig.core.imports.records.BigraphObjectRecord;

/**
 * This class implement a record for Directed Bigraph's Link
 */
public class DirectedBigraphLinkRecord extends BigraphLinkRecord {

    private DirectedBigraphObjectRecord link;
    private int ownerPort;
    private int linkedPort;
    private DirectionOfLink direction;

    /**
     * @param link       the linked Object
     * @param ownerPort  the this port
     * @param linkedPort the link's port
     * @param direction  the link's direction
     */
    public DirectedBigraphLinkRecord(DirectedBigraphObjectRecord link, int ownerPort, int linkedPort, DirectionOfLink direction) {
        this.link = link;
        this.ownerPort = ownerPort;
        this.linkedPort = linkedPort;
        this.direction = direction;
    }

    /**
     * 
     * @return this linked Object
     */
    @Override
    public BigraphObjectRecord getLink() {
        return link;
    }

    /**
     * 
     * @return this port
     */
    @Override
    public int getOwnerPort() {
        return ownerPort;
    }

    /**
     * 
     * @return this link's port
     */
    @Override
    public int getLinkedPort() {
        return linkedPort;
    }

    /**
     * 
     * @return this link's direction
     */
    public DirectionOfLink getDirection() {
        return direction;
    }
}