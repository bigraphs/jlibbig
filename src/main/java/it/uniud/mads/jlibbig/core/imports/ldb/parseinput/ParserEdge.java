package it.uniud.mads.jlibbig.core.imports.ldb.parseinput;

import org.json.JSONArray;
import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.DirectionOfLink;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfLink;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingId;
import it.uniud.mads.jlibbig.core.imports.exception.NoImplementedLink;
import it.uniud.mads.jlibbig.core.imports.exception.NoValidPlaceRelationship;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphLinkRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphObjectRecord;
import it.uniud.mads.jlibbig.core.imports.parseinput.DirectedParserEdge;

public class ParserEdge implements DirectedParserEdge {

    public ParserEdge() {
    }

    /**
     * Parse the "edge" block of the given Json
     * <p>
     * MODIFY: listOfObject, set some properties of some object
     * 
     * @param arr          the JsonArray to be parsed
     * @param listOfObject the list of Objects
     */
    @Override
    public void parseEdges(JSONArray arr, DirectedBigraphListOfObjectRecord listOfObject) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                parseSingleLink(arr.getJSONObject(i), listOfObject);
            } catch (NoImplementedLink e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }

    /**
     * Parse an Json Object rapresented a link, verify the type of the link and
     * parse it
     * <p>
     * MODIFY: listOfObject, set some properties of some object
     * 
     * @param obj          the json of the Object
     * @param listOfObject the list of Objects
     * @throws NoImplementedLink if the link hasn't a valid type
     * @see it.uniud.mads.jlibbig.core.imports.constant.Constants.java
     */
    private void parseSingleLink(JSONObject obj, DirectedBigraphListOfObjectRecord listOfObject)
            throws NoImplementedLink {
        String source = obj.getString("source");
        String target = obj.getString("target");
        String relation = obj.getString("relation");
        JSONObject metadata = obj.getJSONObject("metadata");
        try {
            TypeOfLink typeOfLink = TypeOfLink.valueOf(relation);
            switch (typeOfLink) {
                case place:
                    parsePlace(source, target, listOfObject);
                    break;
                case linkedTo:
                    parseLink(source, target, metadata, listOfObject);
                    break;
                default:
                    throw new NoImplementedLink();
            }
        } catch (IllegalArgumentException e) {
            throw new NoImplementedLink("Error: Wrong type of link found. Types expexted: " + TypeOfLink.place + " or "
                    + TypeOfLink.linkedTo);
        }
    }

    /**
     * Parse a place relationship
     * <p>
     * MODIFY: listOfObject, set the place relationship in the correct Object
     * 
     * @param source       the parent id
     * @param target       the son id
     * @param listOfObject the list of Objects
     */
    private void parsePlace(String source, String target, DirectedBigraphListOfObjectRecord listOfObject) {
        try {
            setPlaceRelationship(findObject(source, listOfObject), findObject(target, listOfObject));
        } catch (NoExistingId e) {
            System.err.println(e);
            System.exit(1);
        } catch (NoValidPlaceRelationship e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Search and find the Object given his id
     * 
     * @param id           the id of the wanted Object
     * @param listOfObject the list of Objects
     * @return the Object with the given id
     * @throws NoExistingId if the id doesn't exist in the listOfObject
     */
    private DirectedBigraphObjectRecord findObject(String id, DirectedBigraphListOfObjectRecord listOfObject)
            throws NoExistingId {
        for (int i = 0; i < listOfObject.size(); i++) {
            DirectedBigraphObjectRecord object = listOfObject.get(i);
            if (object.getId().equals(id)) {
                return object;
            }
        }
        throw new NoExistingId("Error: the id " + id + " doesn't exist");
    }

    /**
     * Set place relationship
     * <p>
     * MODIFY: listOfObject, set the place relationship in the correct Object
     * 
     * @param parent the Object parent
     * @param son    the Object son
     */
    private void setPlaceRelationship(DirectedBigraphObjectRecord parent, DirectedBigraphObjectRecord son)
            throws NoValidPlaceRelationship {
        if (son.getType().equals(TypeOfNodes.root)) {
            throw new NoValidPlaceRelationship(
                    "Error: a root cannot be a son. See place relationship for " + son.getId());
        }
        if (parent.getType().equals(TypeOfNodes.site)) {
            throw new NoValidPlaceRelationship(
                    "Error: a site cannot be a parent. See place relationship for " + parent.getId());
        }
        parent.addSon(son);
        son.setParent(parent);
    }

    /**
     * Parse a linkedTo relationship
     * <p>
     * MODIFY: listOfObject, set the linkedTo relationship in the correct Object
     * 
     * @param source       the source id
     * @param target       the target id
     * @param metadata     the metadata of the relationship
     * @param listOfObject the list of Objects
     */
    private void parseLink(String source, String target, JSONObject metadata,
            DirectedBigraphListOfObjectRecord listOfObject) {
        try {
            setLinkRelationship(findObject(source, listOfObject), findObject(target, listOfObject), metadata);
        } catch (NoExistingId e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Parse a linkedTo relationship
     * <p>
     * MODIFY: listOfObject, set the linkedTo relationship in the correct Object
     * 
     * @param source   the source of the relationship
     * @param target   the target of the relationship
     * @param metadata the metadata of the relationship
     */
    private static void setLinkRelationship(DirectedBigraphObjectRecord source, DirectedBigraphObjectRecord target,
            JSONObject metadata) {
        int sourcePort;
        int targetPort;
        if (source.getType().equals(TypeOfNodes.name) || source.getType().equals(TypeOfNodes.edge)) {
            sourcePort = -1;
        } else {
            sourcePort = metadata.getInt("portFrom");
        }
        if (target.getType().equals(TypeOfNodes.name) || target.getType().equals(TypeOfNodes.edge)) {
            targetPort = -1;
        } else {
            targetPort = metadata.getInt("portTo");
        }
        DirectedBigraphLinkRecord sourceLink = new DirectedBigraphLinkRecord(target, sourcePort, targetPort, DirectionOfLink.to);
        DirectedBigraphLinkRecord targetLink = new DirectedBigraphLinkRecord(source, targetPort, sourcePort, DirectionOfLink.from);
        source.addLink(sourceLink);
        target.addLink(targetLink);
    }

}