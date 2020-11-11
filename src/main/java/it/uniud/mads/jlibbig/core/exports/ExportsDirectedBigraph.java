package it.uniud.mads.jlibbig.core.exports;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.uniud.mads.jlibbig.core.Interface;
import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.ldb.Child;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraph;
import it.uniud.mads.jlibbig.core.ldb.DirectedControl;
import it.uniud.mads.jlibbig.core.ldb.DirectedSignature;
import it.uniud.mads.jlibbig.core.ldb.Edge;
import it.uniud.mads.jlibbig.core.ldb.Handle;
import it.uniud.mads.jlibbig.core.ldb.InPort;
import it.uniud.mads.jlibbig.core.ldb.Node;
import it.uniud.mads.jlibbig.core.ldb.OutPort;
import it.uniud.mads.jlibbig.core.ldb.Point;
import it.uniud.mads.jlibbig.core.ldb.Root;
import it.uniud.mads.jlibbig.core.ldb.Site;

/**
 * This class implement the export of a Direct Bigraph
 */
public class ExportsDirectedBigraph {

    private static String tab4 = "    ";
    private static String jsonPreFix = "{\n" + tab4 + "\"graph\": {\n";
    private static String jsonPostFix = tab4 + "}\n}\n";

    /**
     * Export the Directed Bigraph in a Json format
     * 
     * @param bigraph the bigraph to export
     * @return the Json String represent the Bigraph
     */
    public static String export(DirectedBigraph bigraph) {
        String jsonOut = jsonPreFix;
        jsonOut += getNodes(bigraph);
        jsonOut += getEdge(bigraph);
        jsonOut += tab4 + tab4 + "\"type\": \"ldb\",\n";
        jsonOut += getMetadata(bigraph);
        jsonOut += jsonPostFix;
        return jsonOut;
    }

    /**
     * Export all the node declaration of the Directed Bigraph Json Format: Root,
     * Node, Name, Site, Edge
     * 
     * @param bigraph the bigraph to export
     * @return The Json String of this part of the Directed Bigraph
     */
    private static String getNodes(DirectedBigraph bigraph) {
        String jsonPre = tab4 + tab4 + "\"nodes\": {\n";
        String jsonPost = tab4 + tab4 + "},\n";
        String jsonOut = jsonPre;
        // Root
        List<? extends Root> roots = bigraph.getRoots();
        for (int i = 0; i < roots.size(); i++) {
            Root root = roots.get(i);
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + root.toString() + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"root\",\n" + tab4 + tab4
                    + tab4 + tab4 + tab4 + "\"location\": " + i + "\n" + tab4 + tab4 + tab4 + tab4 + "},\n" + tab4
                    + tab4 + tab4 + tab4 + "\"label\": \"" + root.toString() + "\"\n" + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        // Node
        Collection<? extends Node> nodes = bigraph.getNodes();
        Iterator<? extends Node> nodesIterator = nodes.iterator();
        while (nodesIterator.hasNext()) {
            Node node = nodesIterator.next();
            // Get Label
            String label;
            try {
                label = node.<String>getProperty("Label").get();
            } catch (NullPointerException e) {
                label = "";
            }
            // End Get Label
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + node.getName() + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"node\",\n" + tab4 + tab4
                    + tab4 + tab4 + tab4 + "\"control\": \"" + node.getControl().getName() + "\",\n" + tab4 + tab4
                    + tab4 + tab4 + tab4 + "\"properties\": {\n";
            Collection<Property<?>> properties = node.getProperties();
            Iterator<Property<?>> propertiesIt = properties.iterator();
            while (propertiesIt.hasNext()) {
                Property<?> property = propertiesIt.next();
                String propertyName = property.getName();
                String propertyJson = tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + "\"" + propertyName + "\": ";
                if ((!(propertyName.equals("Label"))) && (!(propertyName.equals("Owner")))) {
                    String propertyContent = property.get().toString();
                    try {
                        if (propertyContent.substring(0, 3).equals("[#]")) {
                            String[] propertyArr = propertyContent.substring(3).split("#, ");
                            propertyJson += "[ \n";
                            for (int i = 0; i < propertyArr.length; i++) {
                                propertyJson += tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + "\"" + propertyArr[i]
                                        + "\",\n";
                            }
                            propertyJson = propertyJson.substring(0, propertyJson.length() - 2) + "\n";
                            propertyJson += tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + " ],\n";
    
                            jsonSingle += propertyJson;
                        } else {
                            propertyJson += "\"" + propertyContent + "\",\n";
                            jsonSingle += propertyJson;
                        }
                    } catch (java.lang.StringIndexOutOfBoundsException e) {
                        propertyJson += "\"" + propertyContent + "\",\n";
                        jsonSingle += propertyJson;
                    }
                }
            }
            jsonSingle = jsonSingle.substring(0, jsonSingle.length() - 2) + "\n";
            jsonSingle += tab4 + tab4 + tab4 + tab4 + tab4 + "}\n" + tab4 + tab4 + tab4 + tab4 + "},\n" + tab4 + tab4 + tab4 + tab4 + "\"label\": \"" + label
                    + "\"\n" + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;

        }
        // Site
        List<? extends Site> sites = bigraph.getSites();
        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + site.toString() + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"site\"\n" + tab4 + tab4
                    + tab4 + tab4 + "},\n" + tab4 + tab4 + tab4 + tab4 + "\"label\": \"" + site.toString() + "\"\n"
                    + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        // Name
        Interface in = bigraph.getInnerInterface();
        Interface out = bigraph.getOuterInterface();
        Set<String> setIn = in.keySet();
        Iterator<String> setInIterator = setIn.iterator();
        Set<String> setOut = out.keySet();
        Iterator<String> setOutIterator = setOut.iterator();
        while (setInIterator.hasNext()) {
            String name = setInIterator.next();
            String[] nameArr = name.split(" ");
            String polarity = nameArr[1];
            if (polarity.equals("r")) {
                polarity = "-";
            } else if (polarity.equals("l")) {
                polarity = "+";
            }
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + nameArr[2] + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"name\",\n" + tab4 + tab4
                    + tab4 + tab4 + tab4 + "\"interface\": \"inner\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                    + "\"locality\": " + nameArr[0] + ",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"polarity\": \""
                    + polarity + "\"\n" + tab4 + tab4 + tab4 + tab4 + "},\n" + tab4 + tab4 + tab4 + tab4
                    + "\"label\": \"" + nameArr[2] + "\"\n" + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        while (setOutIterator.hasNext()) {
            String name = setOutIterator.next();
            String[] nameArr = name.split(" ");
            String polarity = nameArr[1];
            if (polarity.equals("r")) {
                polarity = "-";
            } else if (polarity.equals("l")) {
                polarity = "+";
            }
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + nameArr[2] + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"name\",\n" + tab4 + tab4
                    + tab4 + tab4 + tab4 + "\"interface\": \"outer\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                    + "\"locality\": " + nameArr[0] + ",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"polarity\": \""
                    + polarity + "\"\n" + tab4 + tab4 + tab4 + tab4 + "},\n" + tab4 + tab4 + tab4 + tab4
                    + "\"label\": \"" + nameArr[2] + "\"\n" + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        // Edge
        Collection<? extends Edge> edges = bigraph.getEdges();
        Iterator<? extends Edge> edgeIterator = edges.iterator();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            String jsonSingle = tab4 + tab4 + tab4 + "\"" + edge.toString() + "\": {\n" + tab4 + tab4 + tab4 + tab4
                    + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"type\": \"edge\"\n" + tab4 + tab4
                    + tab4 + tab4 + "},\n" + tab4 + tab4 + tab4 + tab4 + "\"label\": \"" + edge.toString() + "\"\n"
                    + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        jsonOut = jsonOut.substring(0, jsonOut.length() - 3);
        jsonOut += "}\n";
        jsonOut += jsonPost;
        return jsonOut;
    }

    /**
     * Export all the Edges of the Directed Bigraph, both place and link
     * relationship
     * 
     * @param bigraph the bigraph to export
     * @return The Json String of this part of the Directed Bigraph
     */
    private static String getEdge(DirectedBigraph bigraph) {
        String jsonPre = tab4 + tab4 + "\"edges\": [\n";
        String jsonPost = tab4 + tab4 + "],\n";
        String jsonOut = jsonPre;
        jsonOut += getPlaceRelationship(bigraph);
        jsonOut += getLinkedToRelationship(bigraph);
        jsonOut = jsonOut.substring(0, jsonOut.length() - 3);
        jsonOut += "}\n";
        jsonOut += jsonPost;
        return jsonOut;
    }

    /**
     * Export the place relationship of the Directed Bigraph
     * 
     * @param bigraph the bigraph to export
     * @return The Json String of this part of the Directed Bigraph
     */
    private static String getPlaceRelationship(DirectedBigraph bigraph) {
        String jsonOut = "";
        List<? extends Root> roots = bigraph.getRoots();
        for (int i = 0; i < roots.size(); i++) {
            Root root = roots.get(i);
            Collection<? extends Child> children = root.getChildren();
            Iterator<? extends Child> childrenIterator = children.iterator();
            while (childrenIterator.hasNext()) {
                Child child = childrenIterator.next();
                String jsonSingle = tab4 + tab4 + tab4 + tab4 + "{\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "\"source\": \"" + root.toString() + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "\"relation\": \"place\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"target\": \""
                        + child.toString().split(":")[0] + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "}\n" + tab4 + tab4 + tab4 + tab4
                        + "},\n";
                jsonOut += jsonSingle;
            }
        }
        Collection<? extends Node> nodes = bigraph.getNodes();
        Iterator<? extends Node> nodesIterator = nodes.iterator();
        while (nodesIterator.hasNext()) {
            Node node = nodesIterator.next();
            if (node.isParent()) {
                Collection<? extends Child> children = node.getChildren();
                Iterator<? extends Child> childrenIterator = children.iterator();
                while (childrenIterator.hasNext()) {
                    Child child = childrenIterator.next();
                    String jsonSingle = tab4 + tab4 + tab4 + tab4 + "{\n" + tab4 + tab4 + tab4 + tab4 + tab4
                            + "\"source\": \"" + node.getName() + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                            + "\"relation\": \"place\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"target\": \""
                            + child.toString().split(":")[0] + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                            + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "}\n" + tab4 + tab4 + tab4 + tab4
                            + "},\n";
                    jsonOut += jsonSingle;
                }
            }
        }
        return jsonOut;
    }

    /**
     * Export the link relationship of the Directed bigraph
     * 
     * @param bigraph the bigraph to export
     * @return The Json String of this part of the Directed Bigraph
     */
    private static String getLinkedToRelationship(DirectedBigraph bigraph) {
        String jsonOut = "";
        // Node TO direction
        Collection<? extends Node> nodes = bigraph.getNodes();
        Iterator<? extends Node> nodesIterator = nodes.iterator();
        while (nodesIterator.hasNext()) {
            Node node = nodesIterator.next();
            List<? extends OutPort> nodeOutPortList = node.getOutPorts();
            Iterator<? extends OutPort> nodeOutPortIterator = nodeOutPortList.iterator();
            while (nodeOutPortIterator.hasNext()) {
                OutPort outport = nodeOutPortIterator.next();
                Handle handle = outport.getHandle();
                String handleName = handle.toString();
                String portFrom = Integer.toString(outport.getNumber());
                String portTo = "";
                if (handle.isPort()) {
                    portTo = handle.toString().substring(0, 1);
                    handleName = handleName.substring(3, 7);
                }
                String jsonSingle = tab4 + tab4 + tab4 + tab4 + "{\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "\"source\": \"" + outport.getNode().getName() + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "\"relation\": \"linkedTo\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"target\": \""
                        + handleName + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"metadata\": {\n" + tab4 + tab4
                        + tab4 + tab4 + tab4 + tab4 + "\"portFrom\": \"" + portFrom + "\",\n" + tab4 + tab4 + tab4
                        + tab4 + tab4 + tab4 + "\"portTo\": \"" + portTo + "\"\n" + tab4 + tab4 + tab4 + tab4 + tab4
                        + "}\n" + tab4 + tab4 + tab4 + tab4 + "},\n";
                jsonOut += jsonSingle;
            }
            // Node FROM direction
            List<? extends InPort> nodeInPortList = node.getInPorts();
            Iterator<? extends InPort> nodeInPortIterator = nodeInPortList.iterator();
            while (nodeInPortIterator.hasNext()) {
                InPort inport = nodeInPortIterator.next();
                Collection<? extends Point> points = inport.getPoints();
                Iterator<? extends Point> pointsIterator = points.iterator();
                while (pointsIterator.hasNext()) {
                    Point point = pointsIterator.next();
                    // Avoid redundant node to node link
                    if (!point.isPort()) {
                        String portFrom = "";
                        String portTo = Integer.toString(inport.getNumber());
                        String jsonSingle = tab4 + tab4 + tab4 + tab4 + "{\n" + tab4 + tab4 + tab4 + tab4 + tab4
                                + "\"source\": \"" + point.toString() + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                                + "\"relation\": \"linkedTo\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"target\": \""
                                + inport.getNode().getName() + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                                + "\"metadata\": {\n" + tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + "\"portFrom\": \""
                                + portFrom + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + tab4 + "\"portTo\": \""
                                + portTo + "\"\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "}\n" + tab4 + tab4 + tab4 + tab4
                                + "},\n";
                        jsonOut += jsonSingle;
                    }
                }
            }
        }
        jsonOut += getLinkNameToName(bigraph);
        return jsonOut;
    }

    private static String getLinkNameToName(DirectedBigraph bigraph) {
        String jsonOut = "";
        String bigraphToString = bigraph.toString();
        String[] bigraphArr = bigraphToString.split("\n");
        for (int i = 0; i < bigraphArr.length; i++) {
            if ((bigraphArr[i].startsWith("I")) || (bigraphArr[i].startsWith("O"))) {
                if ((bigraphArr[i].startsWith("I", 10)) || (bigraphArr[i].startsWith("O", 10))) {
                    String source = bigraphArr[i].substring(13, 14);
                    String target = bigraphArr[i].substring(4, 5);
                    String jsonSingle = tab4 + tab4 + tab4 + tab4 + "{\n" + tab4 + tab4 + tab4 + tab4 + tab4
                            + "\"source\": \"" + source + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                            + "\"relation\": \"linkedTo\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"target\": \""
                            + target + "\",\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "\"metadata\": {\n" + tab4 + tab4
                            + tab4 + tab4 + tab4 + tab4 + "\"portFrom\": \"" + "\",\n" + tab4 + tab4 + tab4 + tab4
                            + tab4 + tab4 + "\"portTo\": \"" + "\"\n" + tab4 + tab4 + tab4 + tab4 + tab4 + "}\n" + tab4
                            + tab4 + tab4 + tab4 + "},\n";
                    jsonOut += jsonSingle;
                }
            }
        }
        return jsonOut;
    }

    /**
     * Export the metadata of the Directed Bigraph
     * 
     * @param bigraph the bigraph to export
     * @return The Json String of this part of the Directed BigraphTODO: problema: n
     */
    private static String getMetadata(DirectedBigraph bigraph) {
        String jsonPre = tab4 + tab4 + "\"metadata\": {\n" + tab4 + tab4 + tab4 + "\"signature\": {\n";
        String jsonPost = tab4 + tab4 + tab4 + "}\n" + tab4 + tab4 + "}\n";
        String jsonOut = jsonPre;
        DirectedSignature signature = bigraph.getSignature();
        Iterator<DirectedControl> signatureIterator = signature.iterator();
        while (signatureIterator.hasNext()) {
            DirectedControl control = signatureIterator.next();
            String jsonSingle = tab4 + tab4 + tab4 + tab4 + "\"" + control.getName() + "\": {\n" + tab4 + tab4 + tab4
                    + tab4 + tab4 + "\"active\": " + control.isActive() + ",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                    + "\"arityOut\": " + control.getArityOut() + ",\n" + tab4 + tab4 + tab4 + tab4 + tab4
                    + "\"arityIn\": " + control.getArityIn() + "\n" + tab4 + tab4 + tab4 + tab4 + "},\n";
            jsonOut += jsonSingle;
        }
        jsonOut = jsonOut.substring(0, jsonOut.length() - 3);
        jsonOut += "}\n";
        jsonOut += jsonPost;
        return jsonOut;
    }
}