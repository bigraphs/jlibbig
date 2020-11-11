package it.uniud.mads.jlibbig.core.imports.ldb.parseinput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfInterface;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfPolarity;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfProperty;
import it.uniud.mads.jlibbig.core.imports.exception.IdAlreadyExists;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingControl;
import it.uniud.mads.jlibbig.core.imports.exception.NoValidInterface;
import it.uniud.mads.jlibbig.core.imports.exception.NoValidPolarity;
import it.uniud.mads.jlibbig.core.imports.exception.NotImplementedType;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphEdgeRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphNameRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphNodeRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphPropertyRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphRootRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphSiteRecord;
import it.uniud.mads.jlibbig.core.imports.parseinput.DirectedParserNode;

/**
 * This class implements methods for parsing the "nodes" block of a json
 * rapresentation of a directed bigraph
 */
public class ParserNode implements DirectedParserNode {

    public ParserNode() {
    }

    /**
     * Parse the "nodes" block of the given Json
     * <p>
     * MODIFY: listOfObject, add a new Object
     * 
     * @param nodes        the json of the block "nodes"
     * @param listOfObject the list of Objects
     */
    @Override
    public void parseNodes(JSONObject nodes, DirectedBigraphListOfObjectRecord listOfObject,
            DirectedBigraphListOfControlRecord listOfControl) {
        final JSONArray nodeNames = nodes.names();
        final int n = nodeNames.length();
        for (int i = 0; i < n; ++i) {
            try {
                final JSONObject element = nodes.getJSONObject(nodeNames.getString(i));
                parseSingleObjectOfNodes(element, nodeNames.getString(i), listOfObject, listOfControl);
            } catch (JSONException e) {
                System.err.println(e);
                System.exit(1);
            } catch (NotImplementedType e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }

    /**
     * Parse a Json Object rapresent a Node, verify the type of the object and parse
     * it
     * <p>
     * MODIFY: listOfObject, add a new Object
     * 
     * @param obj          the json of the Object
     * @param id           the id of the json Object
     * @param listOfObject the list of Objects
     * @throws NotImplementedType if the type is not a valid type
     * @see it.uniud.mads.jlibbig.core.imports.constant.Constants.java
     */
    private void parseSingleObjectOfNodes(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject,
            DirectedBigraphListOfControlRecord listOfControl) throws NotImplementedType {
        final JSONObject metadata = obj.getJSONObject("metadata");
        final String type = metadata.getString("type");
        try {
            TypeOfNodes typeOfNodes = TypeOfNodes.valueOf(type);
            switch (typeOfNodes) {
                case root:
                    parseRootObject(obj, id, listOfObject);
                    break;
                case node:
                    parseNodeObject(obj, id, listOfObject, listOfControl);
                    break;
                case site:
                    parseSiteObject(obj, id, listOfObject);
                    break;
                case name:
                    parseNameObject(obj, id, listOfObject);
                    break;
                case edge:
                    parseEdgeObject(obj, id, listOfObject);
                    break;
                default:
                    throw new NotImplementedType();
            }
        } catch (IllegalArgumentException e) {
            throw new NotImplementedType("Error: Object " + obj.getString("label")
                    + " doesn't have a valid Type. Type found: " + type + ". Types expexted: " + TypeOfNodes.root + ", "
                    + TypeOfNodes.node + ", " + TypeOfNodes.site + ", " + TypeOfNodes.name + ", " + TypeOfNodes.edge);
        } catch (IdAlreadyExists e) {
            System.err.println(e);
            System.exit(1);
        } catch (NoExistingControl e) {
            System.err.println(e);
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Parse a Json Object rapresent a Single Object of type Root
     * <p>
     * MODIFY: listOfObject, add a new Root
     * 
     * @param obj          the json of the Object
     * @param id           the id of the json Object
     * @param listOfObject the list of Objects
     * @throws IdAlreadyExists if the id already exist in listOfObject
     */
    private void parseRootObject(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject)
            throws IdAlreadyExists {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getId().equals(id)) {
                throw new IdAlreadyExists("Error: The root id " + id + " already exists.");
            }
        }
        final JSONObject metadata = obj.getJSONObject("metadata");
        int location = metadata.getInt("location");
        String label;
        try {
            label = obj.getString("label");
        } catch (JSONException e) {
            label = "";
        }
        DirectedBigraphRootRecord root = new DirectedBigraphRootRecord(id, label, location);
        listOfObject.add(root);
    }

    /**
     * Parse a Json Object rapresent a Single Object of type Node
     * <p>
     * MODIFY: listOfObject, add a new Node
     * 
     * @param obj           the json of the Object
     * @param id            the id of the json Object
     * @param listOfObject  the list of Objects
     * @param listOfControl the list of Controls
     * @throws NoExistingControl id the control doens't exist in listOfControl
     * @throws IdAlreadyExists   if the id already exist in listOfObject
     */
    private void parseNodeObject(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject,
            DirectedBigraphListOfControlRecord listOfControl) throws NoExistingControl, IdAlreadyExists {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getId().equals(id)) {
                throw new IdAlreadyExists("Error: The node id " + id + " already exists.");
            }
        }
        final JSONObject metadata = obj.getJSONObject("metadata");
        boolean found = false;
        for (int i = 0; i < listOfControl.size(); i++) {
            DirectedBigraphControlRecord control = listOfControl.get(i);
            if (control.getName().equals(metadata.getString("control"))) {
                String label;
                try {
                    label = obj.getString("label");
                } catch (JSONException e) {
                    label = "";
                }

                JSONObject propertiesJson = metadata.getJSONObject("properties");
                Iterator<String> propertiesKeys = propertiesJson.keys();
                List<DirectedBigraphPropertyRecord> properties = new ArrayList<DirectedBigraphPropertyRecord>();
                while (propertiesKeys.hasNext()) {
                    String propertyKey = propertiesKeys.next();
                    try {
                        JSONArray propertyJsonArray = propertiesJson.getJSONArray(propertyKey);
                        List<String> property = new ArrayList<String>();
                        for (int j = 0; j < propertyJsonArray.length(); j++) {
                            property.add(propertyJsonArray.get(j).toString());
                        }
                        DirectedBigraphPropertyRecord propertyArray = new DirectedBigraphPropertyRecord(propertyKey, TypeOfProperty.array,
                                property);
                        properties.add(propertyArray);
                    } catch (JSONException e) {
                        String propertyJsonString = propertiesJson.getString(propertyKey);
                        List<String> property = new ArrayList<String>();
                        property.add(propertyJsonString);
                        DirectedBigraphPropertyRecord propertyString = new DirectedBigraphPropertyRecord(propertyKey, TypeOfProperty.string,
                                property);
                        properties.add(propertyString);
                    }
                }
                
                DirectedBigraphNodeRecord node = new DirectedBigraphNodeRecord(id, label, control, properties);
                listOfObject.add(node);
                found = true;
            }
        }
        if (found == false) {
            throw new NoExistingControl("Error: No valid control found. The control " + metadata.getString("control")
                    + " of the node " + obj.getString("label") + " must be defined in the signature section.");
        }
    }

    /**
     * Parse a Json Object rapresent a Single Object of type Site
     * <p>
     * MODIFY: listOfObject, add a new Site
     * 
     * @param obj          the json of the Object
     * @param id           the id of the json Object
     * @param listOfObject the list of Objects
     * @throws IdAlreadyExists if the id already exist in listOfObject
     */
    private void parseSiteObject(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject)
            throws IdAlreadyExists {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getId().equals(id)) {
                throw new IdAlreadyExists("Error: The site id " + id + " already exists.");
            }
        }
        String label;
        try {
            label = obj.getString("label");
        } catch (JSONException e) {
            label = "";
        }
        DirectedBigraphSiteRecord site = new DirectedBigraphSiteRecord(id, label);
        listOfObject.add(site);
    }

    /**
     * Object of type Name
     * <p>
     * MODIFY: listOfObject, add a new name
     * 
     * @param obj          the json of the Object
     * @param id           the id of the json Object
     * @param listOfObject the list of Objects
     * @throws NoValidInterface if the interface is not a valid type
     * @throws IdAlreadyExists  if the id already exist in listOfObject
     * @throws NoValidPolarity  if the polarity is not a valid type
     * @see it.uniud.mads.jlibbig.core.imports.constant.Constants.java
     */
    private void parseNameObject(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject)
            throws NoValidInterface, IdAlreadyExists, NoValidPolarity {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getId().equals(id)) {
                throw new IdAlreadyExists("Error: The name id " + id + " already exists.");
            }
        }
        final JSONObject metadata = obj.getJSONObject("metadata");
        String nameInterface = metadata.getString("interface");
        int locality;
        try {
            locality = metadata.getInt("locality");
        } catch (JSONException e) {
            locality = 0;
        }
        String pol = metadata.getString("polarity");
        TypeOfPolarity polarity;
        if (pol.equals("+")) {
            polarity = TypeOfPolarity.plus;
        } else if (pol.equals("-")) {
            polarity = TypeOfPolarity.minus;
        } else {
            throw new NoValidPolarity("Error: No valid polarity for name " + id);
        }
        try {
            TypeOfInterface nameInt = TypeOfInterface.valueOf(nameInterface);
            String label;
            try {
                label = obj.getString("label");
            } catch (JSONException e) {
                label = "";
            }
            DirectedBigraphNameRecord name = new DirectedBigraphNameRecord(id, label, nameInt, locality, polarity);
            listOfObject.add(name);
        } catch (IllegalArgumentException e) {
            throw new NoValidInterface("Error: Object " + obj.getString("label")
                    + " doesn't have a valid interface. Interface found: " + nameInterface + ". Interface expexted: "
                    + TypeOfInterface.outer + ", " + TypeOfInterface.inner);
        }
    }

    /**
     * Object of type Edge
     * <p>
     * MODIFY: listOfObject, add a new edge
     * 
     * @param obj          the json of the Object
     * @param id           the id of the json Object
     * @param listOfObject the list of Objects
     * @throws IdAlreadyExists if the id already exist in listOfObject
     * @see it.uniud.mads.jlibbig.core.imports.constant.Constants.java
     */
    private void parseEdgeObject(JSONObject obj, String id, DirectedBigraphListOfObjectRecord listOfObject)
            throws IdAlreadyExists {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getId().equals(id)) {
                throw new IdAlreadyExists("Error: The name id " + id + " already exists.");
            }
        }
        String label;
        try {
            label = obj.getString("label");
        } catch (JSONException e) {
            label = "";
        }
        DirectedBigraphEdgeRecord edge = new DirectedBigraphEdgeRecord(id, label);
        listOfObject.add(edge);
    }
}