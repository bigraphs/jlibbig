package it.uniud.mads.jlibbig.core.imports.ldb.records;

import java.util.ArrayList;
import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.DirectionOfLink;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingLink;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingSon;
import it.uniud.mads.jlibbig.core.imports.records.BigraphObjectRecord;

/**
 * This class implement a record for Directed Bigraph's Object
 */
public abstract class DirectedBigraphObjectRecord extends BigraphObjectRecord
        implements Comparable<DirectedBigraphObjectRecord> {

    private DirectedBigraphObjectRecord parent = null;
    private List<DirectedBigraphObjectRecord> sons = new ArrayList<DirectedBigraphObjectRecord>();
    private List<DirectedBigraphLinkRecord> links = new ArrayList<DirectedBigraphLinkRecord>();

    /**
     * @param id        the Object's id
     * @param label     the Object's label
     * @param type      the Object's type
     */
    public DirectedBigraphObjectRecord(String id, String label, TypeOfNodes type) {
        super(id, label, type);
    }

    /**
     * @return this Object's parent
     */
    public final DirectedBigraphObjectRecord getParent() {
        return parent;
    }

    /**
     * 
     * @param parent the parent to set
     */
    public final void setParent(DirectedBigraphObjectRecord parent) {
        this.parent = parent;
    }

    /**
     * Tell if this Object has a parent or not
     * 
     * @return true if has a parent, false if not
     */
    public final boolean hasParent() {
        return (parent != null) ? true : false;
    }

    /**
     * @return this list of sons
     */
    public final List<DirectedBigraphObjectRecord> getSons() {
        return sons;
    }

    /**
     * Get the son in the given position
     * 
     * @param index the index of wanted son
     * @return the son in the given index
     */
    public final DirectedBigraphObjectRecord getSon(int index) {
        return sons.get(index);
    }

    /**
     * Get the son with a specific id
     * 
     * @param id the id of the wanted son
     * @return the son with the given id
     * @throws NoExistingSon if no son has the given id
     */
    public final DirectedBigraphObjectRecord getSon(String id) throws NoExistingSon {
        for (int i = 0; i < sons.size(); i++) {
            DirectedBigraphObjectRecord son = sons.get(i);
            if (son.getId().equals(id)) {
                return son;
            }
        }
        throw new NoExistingSon("Error: No existing son with id " + id + "for this parent");
    }

    /**
     * @param sons the list of sons to set
     */
    public final void setSons(List<DirectedBigraphObjectRecord> sons) {
        this.sons = sons;
    }

    /**
     * MODIFY: sons
     * 
     * @param son the son to add in the list
     */
    public final void addSon(DirectedBigraphObjectRecord son) {
        this.sons.add(son);
    }

    /**
     * Tell if the Object has at least one son or not
     * 
     * @return true if has at lease one son, false has no son
     */
    public final boolean hasSon() {
        return (!sons.isEmpty()) ? true : false;
    }

    /**
     * @return this links
     */
    public final List<DirectedBigraphLinkRecord> getLinks() {
        return links;
    }

    /**
     * Get the link in the given position
     * 
     * @param index the position of the wanted link
     * @return the link in the given position
     */
    public final DirectedBigraphLinkRecord getLink(int index) {
        return links.get(index);
    }

    /**
     * Get the link to/from the specified Object
     * 
     * @param obj the wanted Object
     * @return the list of all connection with the Object specified
     * @throws NoExistingLink if this has no link to/from the Object specified
     */
    public final List<DirectedBigraphLinkRecord> getLink(DirectedBigraphObjectRecord obj) throws NoExistingLink {
        List<DirectedBigraphLinkRecord> toLinks = new ArrayList<DirectedBigraphLinkRecord>();
        for (int i = 0; i < links.size(); i++) {
            DirectedBigraphLinkRecord link = links.get(i);
            if (link.getLink().getId().equals(obj.getId())) {
                toLinks.add(link);
            }
        }
        if (toLinks.isEmpty()) {
            throw new NoExistingLink("Error: no existing link to/from the object " + obj.getId());
        } else {
            return toLinks;
        }
    }

    /**
     * Get the link to/from the specified Object's id
     * 
     * @param id the wanted Object's id
     * @return the list of all connection with the Object's id specified
     * @throws NoExistingLink if this has no link to/from the Object's id specified
     */
    public final List<DirectedBigraphLinkRecord> getLink(String id) throws NoExistingLink {
        List<DirectedBigraphLinkRecord> toLinks = new ArrayList<DirectedBigraphLinkRecord>();
        for (int i = 0; i < links.size(); i++) {
            DirectedBigraphLinkRecord link = links.get(i);
            if (link.getLink().getId().equals(id)) {
                toLinks.add(link);
            }
        }
        if (toLinks.isEmpty()) {
            throw new NoExistingLink("Error: no existing link to/from the object " + id);
        } else {
            return toLinks;
        }
    }

    /**
     * Get all links from this to another Object
     * 
     * @return the links to another object, may be null
     */
    public final List<DirectedBigraphLinkRecord> getToLinks() {
        List<DirectedBigraphLinkRecord> toLinks = new ArrayList<DirectedBigraphLinkRecord>();
        for (int i = 0; i < links.size(); i++) {
            DirectedBigraphLinkRecord link = links.get(i);
            if (link.getDirection().equals(DirectionOfLink.to)) {
                toLinks.add(link);
            }
        }
        return toLinks;
    }

    /**
     * Get all links from another Object to this
     * 
     * @return the links from another object, may be null
     */
    public final List<DirectedBigraphLinkRecord> getFromLinks() {
        List<DirectedBigraphLinkRecord> toLinks = new ArrayList<DirectedBigraphLinkRecord>();
        for (int i = 0; i < links.size(); i++) {
            DirectedBigraphLinkRecord link = links.get(i);
            if (link.getDirection().equals(DirectionOfLink.from)) {
                toLinks.add(link);
            }
        }
        return toLinks;
    }

    /**
     * @param links the list of links to set
     */
    public final void setLinks(List<DirectedBigraphLinkRecord> links) {
        this.links = links;
    }

    /**
     * MODIFY: links
     * 
     * @param link the link to add in the list
     */
    public final void addLink(DirectedBigraphLinkRecord link) {
        this.links.add(link);
    }

    /**
     * Tell if the Object has at least one link in any direction or not
     * 
     * @return true if has at lease one link, false has no link
     */
    public final boolean hasLink() {
        return (!links.isEmpty()) ? true : false;
    }

    /**
     * Calculate the number of parent of this Object
     * 
     * @return the number of parent
     */
    private int depht() {
        if (!hasParent()) {
            return 0;
        } else {
            return parent.depht() + 1;
        }
    }

    /**
     * Innate order for this class:
     * <p>
     * <ul>
     * <li>this Object is less than the specified Object if has fewer parent than
     * the specified Object
     * <li>this Object is equal to the specified Object if has the same number of
     * parent than the specified Object
     * <li>this Object is greater than the specified Object if has more parent than
     * the specified Object
     * </ul>
     * <p>
     * 
     * @param o the specified Object
     */
    @Override
    public final int compareTo(DirectedBigraphObjectRecord o) {
        return this.depht() - o.depht();
    }
}