package it.uniud.mads.jlibbig.core.imports.ldb.records;

import java.util.ArrayList;
import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.DirectionOfLink;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfInterface;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfPolarity;

public class DirectedBigraphNameRecord extends DirectedBigraphObjectRecord {

    private final TypeOfInterface nameInterface;
    private final int locality;
    private final TypeOfPolarity polarity;

    /**
     * @param id            the Name's id
     * @param label         the Name's label
     * @param nameInterface the Name's nameInterface
     * @param locality      the Name's locality
     * @param polarity      the Name's polarity
     */
    public DirectedBigraphNameRecord(final String id, final String label, final TypeOfInterface nameInterface,
            final int locality, final TypeOfPolarity polarity) {
        super(id, label, TypeOfNodes.name);
        this.nameInterface = nameInterface;
        this.locality = locality;
        this.polarity = polarity;
    }

    /**
     * @return the nameInterface
     */
    public TypeOfInterface getNameInterface() {
        return nameInterface;
    }

    /**
     * @return the locality
     */
    public int getLocality() {
        return locality;
    }

    /**
     * Tell if this has at least one outerName or not
     * 
     * @return true if this has at least one outer name, false elsewhere
     */
    public boolean hasOuterName() {
        for (int i = 0; i < this.getLinks().size(); i++) {
            if (this.getLink(i).getDirection().equals(DirectionOfLink.from)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tell if this has at least one innerName or not
     * 
     * @return true if this has at least one inner name, false elsewhere
     */
    public boolean hasInnerName() {
        for (int i = 0; i < this.getLinks().size(); i++) {
            if (this.getLink(i).getDirection().equals(DirectionOfLink.to)) {
                return true;
            }
        }
        return false;
    }

    public List<DirectedBigraphLinkRecord> getInnerName() {
        List<DirectedBigraphLinkRecord> linkTo = new ArrayList<DirectedBigraphLinkRecord>();
        for (int i = 0; i < this.getLinks().size(); i++) {
            if (this.getLink(i).getDirection().equals(DirectionOfLink.to)) {
                linkTo.add(this.getLink(i));
            }
        }
        return linkTo;
    }

    /**
     * @return the polarity
     */
    public TypeOfPolarity getPolarity() {
        return polarity;
    }
}