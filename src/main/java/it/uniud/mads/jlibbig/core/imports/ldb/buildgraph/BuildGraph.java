package it.uniud.mads.jlibbig.core.imports.ldb.buildgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.uniud.mads.jlibbig.core.attachedProperties.SharedProperty;
import it.uniud.mads.jlibbig.core.attachedProperties.SimpleProperty;
import it.uniud.mads.jlibbig.core.imports.buildgraph.DirectedBuildGraph;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfInterface;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfPolarity;
import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfProperty;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingId;
import it.uniud.mads.jlibbig.core.imports.exception.NoExistingParent;
import it.uniud.mads.jlibbig.core.imports.exception.NoValidPort;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphLinkRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphNameRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphNodeRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphObjectRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphPropertyRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphRootRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphSiteRecord;
import it.uniud.mads.jlibbig.core.imports.records.BigraphObjectRecord;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraph;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraphBuilder;
import it.uniud.mads.jlibbig.core.ldb.DirectedSignature;
import it.uniud.mads.jlibbig.core.ldb.DirectedSignatureBuilder;
import it.uniud.mads.jlibbig.core.ldb.Handle;
import it.uniud.mads.jlibbig.core.ldb.InnerName;
import it.uniud.mads.jlibbig.core.ldb.Node;
import it.uniud.mads.jlibbig.core.ldb.OuterName;
import it.uniud.mads.jlibbig.core.ldb.Parent;
import it.uniud.mads.jlibbig.core.ldb.Point;
import it.uniud.mads.jlibbig.core.ldb.Root;
import it.uniud.mads.jlibbig.core.ldb.Site;

/**
 * This class implements methods for build a Directed Bigraph with data parsed
 * from a json
 */
public class BuildGraph implements DirectedBuildGraph {

    private List<Root> rootsList = new ArrayList<Root>();
    private List<String> rootListId = new ArrayList<String>();
    private List<OuterName> outerNames = new ArrayList<OuterName>();
    private List<String> outerNamesId = new ArrayList<String>();
    private List<InnerName> innerNames = new ArrayList<InnerName>();
    private List<String> innerNamesId = new ArrayList<String>();
    private List<Node> nodesList = new ArrayList<Node>();
    private List<String> nodesListId = new ArrayList<String>();
    private List<Site> sitesList = new ArrayList<Site>();
    private List<String> siteListId = new ArrayList<String>();
    private DirectedBigraphBuilder builderRecord = null;

    public BuildGraph() {
    }

    /**
     * Create a Directed Bigraph
     * 
     * @param listOfObject  the list of Directed Bigraph's Objects
     * @param listOfControl the list of Directed Bigraph's Controls
     * @return the directed bigraph created
     */
    @Override
    public DirectedBigraph build(DirectedBigraphListOfObjectRecord listOfObject,
            DirectedBigraphListOfControlRecord listOfControl) {
        DirectedSignature signature = buildSignatures(listOfControl);
        DirectedBigraphBuilder builder = createBuilder(signature);
        buildRoots(builder, listOfObject);
        buildOuterName(builder, listOfObject);
        buildObject(builder, listOfObject);
        buildInnerName(builder, listOfObject);
        try {
            reLinkGraph(builder, listOfObject);
        } catch (NoValidPort e) {
            System.err.println(e);
            System.exit(1);
        }
        builderRecord = builder;
        DirectedBigraph bigraph = builder.makeBigraph();
        return bigraph;
    }

    /**
     * Create the Directed Bigraph's signature
     * 
     * @param listOfControl the list of controls
     * @return the Directed Bigraph's signature
     */
    private DirectedSignature buildSignatures(DirectedBigraphListOfControlRecord listOfControl) {
        DirectedSignatureBuilder signatureBuilder = new DirectedSignatureBuilder();
        for (int i = 0; i < listOfControl.size(); i++) {
            DirectedBigraphControlRecord control = listOfControl.get(i);
            signatureBuilder.add(control.getName(), control.isActive(), control.getArityOut(), control.getArityIn());
        }
        DirectedSignature signature = signatureBuilder.makeSignature();
        return signature;
    }

    /**
     * Create the Directed Bigraph's builder
     * 
     * @param signature the Directed Bigraph's signature
     * @return the Directed Bigraph's builder created
     */
    private DirectedBigraphBuilder createBuilder(DirectedSignature signature) {
        DirectedBigraphBuilder builder = new DirectedBigraphBuilder(signature);
        return builder;
    }

    /**
     * Create the Directed Bigraph's roots
     * <p>
     * MODIFY: rootsList, rootListID adding new Objects
     * 
     * @param builder      the Directed Bigraph's builder
     * @param listOfObject the list of Objects
     */
    private void buildRoots(DirectedBigraphBuilder builder, DirectedBigraphListOfObjectRecord listOfObject) {
        List<DirectedBigraphObjectRecord> rootList = listOfObject.getListRootSorted();
        for (int i = 0; i < rootList.size(); i++) {
            DirectedBigraphRootRecord obj = (DirectedBigraphRootRecord) rootList.get(i);
            Root root = builder.addRoot();
            String rootId = obj.getId();
            rootsList.add(root);
            rootListId.add(rootId);
        }
    }

    /**
     * Create the Directed Bigraph's outer names
     * <p>
     * MODIFY: outerNames, outerNamesId adding new outernames
     * 
     * @param builder      the Directed Bigraph's builder
     * @param listOfObject the list of object
     */
    private void buildOuterName(DirectedBigraphBuilder builder, DirectedBigraphListOfObjectRecord listOfObject) {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getType().equals(TypeOfNodes.name)) {
                DirectedBigraphNameRecord name = (DirectedBigraphNameRecord) listOfObject.get(i);
                if (name.getNameInterface().equals(TypeOfInterface.outer)
                        && name.getPolarity().equals(TypeOfPolarity.plus)) {
                    OuterName newOuterName = builder.addAscNameOuterInterface(name.getLocality(), name.getId());
                    String newOuterNameId = name.getId();
                    outerNames.add(newOuterName);
                    outerNamesId.add(newOuterNameId);
                } else if (name.getNameInterface().equals(TypeOfInterface.inner)
                        && name.getPolarity().equals(TypeOfPolarity.minus)) {
                    OuterName newOuterName = builder.addDescNameInnerInterface(name.getLocality(), name.getId());
                    String newOuterNameId = name.getId();
                    outerNames.add(newOuterName);
                    outerNamesId.add(newOuterNameId);
                }
            }
        }
    }

    /**
     * Create the Directed Bigraph's Objects
     * <p>
     * MODIFY: nodesList, nodesListId, sitesList, siteListId adding new Objects
     * 
     * @param builder      the Directed Bigraph's builder
     * @param listOfObject the list of Objects
     */
    private void buildObject(DirectedBigraphBuilder builder, DirectedBigraphListOfObjectRecord listOfObject) {
        for (int i = 0; i < listOfObject.size(); i++) {
            DirectedBigraphObjectRecord obj = listOfObject.get(i);
            switch (obj.getType()) {
                case node:
                    try {
                        DirectedBigraphNodeRecord elementNode = (DirectedBigraphNodeRecord) obj;
                        Parent parent = searchParent(elementNode.getParent().getId(),
                                elementNode.getParent().getType());
                        try {
                            List<Handle> handles = searchHandles(elementNode.getToLinks());
                            Node node = builder.addNode(elementNode.getControl().getName(), parent, handles);

                            // Attach Property
                            List<DirectedBigraphPropertyRecord> properties = elementNode.getProperties();
                            Iterator<DirectedBigraphPropertyRecord> propertiesIt = properties.iterator();
                            while (propertiesIt.hasNext()) {
                                DirectedBigraphPropertyRecord property = propertiesIt.next();
                                if (property.getType().equals(TypeOfProperty.string)) {
                                    node.attachProperty(new SharedProperty<String>(
                                        new SimpleProperty<String>(property.getId(), property.getProperties().get(0), true)));
                                } else {
                                    String propertiesArg = "[#]";
                                    for(int j = 0; j < property.getProperties().size(); j++) {
                                        propertiesArg += property.getProperty(j) + "#, ";
                                    }
                                    propertiesArg = propertiesArg.substring(0, propertiesArg.length() - 3);
                                    node.attachProperty(new SharedProperty<String>(
                                        new SimpleProperty<String>(property.getId(), propertiesArg, true)));
                                }
                            }
                            if (!(elementNode.getLabel().equals(""))) {
                                node.attachProperty(new SharedProperty<String>(
                                        new SimpleProperty<String>("Label", elementNode.getLabel(), true)));

                            }
                            // End Attach Property

                            String nodeId = elementNode.getId();
                            nodesList.add(node);
                            nodesListId.add(nodeId);
                        } catch (NoValidPort e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                    } catch (NoExistingParent e) {
                        System.err.println(e);
                        System.exit(1);
                    }
                    break;
                case site:
                    try {
                        DirectedBigraphSiteRecord elementSite = (DirectedBigraphSiteRecord) obj;
                        Parent parent = searchParent(elementSite.getParent().getId(),
                                elementSite.getParent().getType());
                        Site site = builder.addSite(parent);
                        String siteId = elementSite.getId();
                        sitesList.add(site);
                        siteListId.add(siteId);
                    } catch (NoExistingParent e) {
                        System.err.println(e);
                        System.exit(1);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Find the parent object given his id and his type
     * 
     * @param id   the parent id
     * @param type the parent type
     * @return the parent Object
     * @throws NoExistingParent if no parent with theese id and type was found
     */
    private Parent searchParent(String id, TypeOfNodes type) throws NoExistingParent {
        switch (type) {
            case root:
                for (int i = 0; i < rootListId.size(); i++) {
                    if (rootListId.get(i).equals(id)) {
                        return rootsList.get(i);
                    }
                }
                throw new NoExistingParent();
            case node:
                for (int i = 0; i < nodesListId.size(); i++) {
                    if (nodesListId.get(i).equals(id)) {
                        return nodesList.get(i);
                    }
                }
                throw new NoExistingParent();
            default:
                throw new NoExistingParent();
        }
    }

    /**
     * Search in the outerNames and outerNamesList all object created with the id of
     * the param objects
     * 
     * @param links the objects to search
     * @return the list of all objects found
     * @throws if no valid port was insert
     */
    private List<Handle> searchHandles(List<DirectedBigraphLinkRecord> links) throws NoValidPort {
        List<Handle> list = new ArrayList<Handle>();
        for (int i = 0; i < outerNamesId.size(); i++) {
            for (int j = 0; j < links.size(); j++) {
                if (outerNamesId.get(i).equals(links.get(j).getLink().getId())) {
                    list.add(outerNames.get(i));
                }
            }
        }
        for (int i = 0; i < nodesListId.size(); i++) {
            for (int j = 0; j < links.size(); j++) {
                if (nodesListId.get(i).equals(links.get(j).getLink().getId())) {
                    try {
                        list.add(nodesList.get(i).getInPort(links.get(j).getLinkedPort()));
                    } catch (IndexOutOfBoundsException e) {
                        throw new NoValidPort("Error: the port of " + links.get(j).getLink().getId()
                                + " it's not valid. Recived port: " + links.get(j).getLinkedPort() + ".");
                    }

                }
            }
        }
        return list;
    }

    /**
     * Create the Directed Bigraph's inner names
     * <p>
     * MODIFY: innerNames, innerNamesId adding new innerName
     * 
     * @param builder      the Directed Bigraph's builder
     * @param listOfObject the list of Objects
     */
    private void buildInnerName(DirectedBigraphBuilder builder, DirectedBigraphListOfObjectRecord listOfObject) {
        for (int i = 0; i < listOfObject.size(); i++) {
            if (listOfObject.get(i).getType().equals(TypeOfNodes.name)) {
                DirectedBigraphNameRecord name = (DirectedBigraphNameRecord) listOfObject.get(i);
                if (name.getNameInterface().equals(TypeOfInterface.outer)
                        && name.getPolarity().equals(TypeOfPolarity.minus)) {
                    List<DirectedBigraphLinkRecord> links = name.getInnerName();
                    try {
                        List<Handle> handles = searchHandles(links);
                        InnerName newInnerName;
                        if (handles.isEmpty()) {
                            newInnerName = builder.addDescNameOuterInterface(name.getLocality(), name.getId());
                        } else {
                            newInnerName = builder.addDescNameOuterInterface(name.getLocality(), name.getId(),
                                    handles.get(0));
                        }
                        String newInnerNameId = name.getId();
                        innerNames.add(newInnerName);
                        innerNamesId.add(newInnerNameId);
                    } catch (NoValidPort e) {
                        System.err.println(e);
                        System.exit(1);
                    }
                } else if (name.getNameInterface().equals(TypeOfInterface.inner)
                        && name.getPolarity().equals(TypeOfPolarity.plus)) {
                    List<DirectedBigraphLinkRecord> links = name.getInnerName();
                    try {
                        List<Handle> handles = searchHandles(links);
                        InnerName newInnerName;
                        if (handles.isEmpty()) {
                            newInnerName = builder.addAscNameInnerInterface(name.getLocality(), name.getId());
                        } else {
                            newInnerName = builder.addAscNameInnerInterface(name.getLocality(), name.getId(),
                                    handles.get(0));
                        }
                        String newInnerNameId = name.getId();
                        innerNames.add(newInnerName);
                        innerNamesId.add(newInnerNameId);
                    } catch (NoValidPort e) {
                        System.err.println(e);
                        System.exit(1);
                    }
                }
            }
        }

    }

    /**
     * Re-link Directed Bigraph's elements
     * <p>
     * MODIFY: elements in listOfObject
     * 
     * @param builder      the Directed Bigraph's builder
     * @param listOfObject the list of Objects
     * @throws if no valid port was insert
     */
    private void reLinkGraph(DirectedBigraphBuilder builder, DirectedBigraphListOfObjectRecord listOfObject)
            throws NoValidPort {
        for (int i = 0; i < listOfObject.size(); i++) {
            DirectedBigraphObjectRecord obj = listOfObject.get(i);
            switch (obj.getType()) {
                case node:
                    DirectedBigraphNodeRecord node = (DirectedBigraphNodeRecord) obj;
                    List<DirectedBigraphLinkRecord> nodeLinksTo = node.getToLinks();
                    Iterator<DirectedBigraphLinkRecord> nodeLinksToIter = nodeLinksTo.iterator();
                    while (nodeLinksToIter.hasNext()) {
                        // Get parsing link information
                        DirectedBigraphLinkRecord singleLinkto = nodeLinksToIter.next();
                        BigraphObjectRecord linkedObj = singleLinkto.getLink();
                        int ownerPort = singleLinkto.getOwnerPort();
                        // Search JLibBig OBJ and relink them
                        Node source;
                        try {
                            source = searchNode(node.getId());
                            if (linkedObj.getType().equals(TypeOfNodes.name)) {
                                // If Target is Name
                                OuterName target;
                                try {
                                    target = searchOuterName(linkedObj.getId());
                                    // RELINK
                                    Point pointSource = source.getOutPort(ownerPort);
                                    Handle handleTarget = target;
                                    builder.relink(handleTarget, pointSource);
                                } catch (NoExistingId e) {
                                    System.err.println(e);
                                    System.exit(1);
                                } catch (IndexOutOfBoundsException e) {
                                    throw new NoValidPort("Error: the port of " + source.getName()
                                            + " it's not valid. Recived port: " + ownerPort + ".");
                                }
                            } else if (linkedObj.getType().equals(TypeOfNodes.node)) {
                                // If Target is Node
                                Node target = null;
                                int linkedPort = singleLinkto.getLinkedPort();
                                try {
                                    target = searchNode(linkedObj.getId());
                                    // RELINK
                                    Point pointSource = source.getOutPort(ownerPort);
                                    Handle handleTarget = target.getInPort(linkedPort);
                                    builder.relink(handleTarget, pointSource);
                                } catch (NoExistingId e) {
                                    System.err.println(e);
                                    System.exit(1);
                                } catch (IndexOutOfBoundsException e) {
                                    throw new NoValidPort("Error: the port of " + source.getName() + " or "
                                            + target.getName() + " it's not valid. Recived owner port: " + ownerPort
                                            + ". Recived target port: " + linkedPort);
                                }
                            } else if (linkedObj.getType().equals(TypeOfNodes.edge)) {
                                // If Target is edge
                            }
                        } catch (NoExistingId e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                    }
                    break;
                case name:
                    DirectedBigraphNameRecord name = (DirectedBigraphNameRecord) obj;
                    List<DirectedBigraphLinkRecord> nameLinksTo = name.getToLinks();
                    Iterator<DirectedBigraphLinkRecord> nameLinksToIter = nameLinksTo.iterator();
                    while (nameLinksToIter.hasNext()) {
                        // Get parsing link information
                        DirectedBigraphLinkRecord singleLinkto = nameLinksToIter.next();
                        BigraphObjectRecord linkedObj = singleLinkto.getLink();
                        // Search JLibBig OBJ and relink them
                        List<InnerName> sourceList;
                        try {
                            sourceList = searchInnerName(name.getId());
                            Iterator<InnerName> sourceIterator = sourceList.iterator();
                            while (sourceIterator.hasNext()) {
                                InnerName source = sourceIterator.next();
                                if (linkedObj.getType().equals(TypeOfNodes.name)) {
                                    // If Target is Name
                                    OuterName target;
                                    try {
                                        target = searchOuterName(linkedObj.getId());
                                        // RELINK
                                        Point pointSource = source;
                                        Handle handleTarget = target;
                                        builder.relink(handleTarget, pointSource);
                                    } catch (NoExistingId e) {
                                        System.err.println(e);
                                        System.exit(1);
                                    }
                                } else if (linkedObj.getType().equals(TypeOfNodes.node)) {
                                    // If Target is Node
                                    Node target = null;
                                    int linkedPort = singleLinkto.getLinkedPort();
                                    try {
                                        target = searchNode(linkedObj.getId());
                                        // RELINK
                                        Point pointSource = source;
                                        Handle handleTarget = target.getInPort(linkedPort);
                                        builder.relink(handleTarget, pointSource);
                                    } catch (NoExistingId e) {
                                        System.err.println(e);
                                        System.exit(1);
                                    } catch (IndexOutOfBoundsException e) {
                                        throw new NoValidPort("Error: the port of " + target.getName()
                                                + " it's not valid. Recived port: " + linkedPort + ".");
                                    }
                                } else if (linkedObj.getType().equals(TypeOfNodes.edge)) {
                                    // If target is edge
                                }
                            }
                        } catch (NoExistingId e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                    }
                    break;
                case edge:
                    // Edge non può essere un point sono un handle
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Search and find the node with the param id
     * 
     * @param id the node id to search
     * @return the node found
     * @throws NoExistingId if no Node has been found
     */
    private Node searchNode(String id) throws NoExistingId {
        Node node;
        for (int i = 0; i < nodesListId.size(); i++) {
            if (nodesListId.get(i).equals(id)) {
                node = nodesList.get(i);
                return node;
            }
        }
        throw new NoExistingId();
    }

    /**
     * Search and find the OuterName with the param id
     * 
     * @param id the OuterName id to search
     * @return the OuterName found
     * @throws NoExistingId if no OuterName has been found
     */
    private OuterName searchOuterName(String id) throws NoExistingId {
        OuterName outerName;
        for (int i = 0; i < outerNamesId.size(); i++) {
            if (outerNamesId.get(i).equals(id)) {
                outerName = outerNames.get(i);
                return outerName;
            }
        }
        throw new NoExistingId();
    }

    /**
     * Search and find the InnerName with the param id
     * 
     * @param id the InnerName id to search
     * @return the InnerName found
     * @throws NoExistingId if no InnerName has been found
     */
    private List<InnerName> searchInnerName(String id) throws NoExistingId {
        List<InnerName> innerNameList = new ArrayList<InnerName>();
        for (int i = 0; i < innerNamesId.size(); i++) {
            if (innerNamesId.get(i).contains(id)) {
                innerNameList.add(innerNames.get(i));
                return innerNameList;
            }
        }
        throw new NoExistingId();
    }

    /**
     * Get the builder, if exist.
     * 
     * @return the builderRecord if exists. Null otherwise.
     */
    public DirectedBigraphBuilder getBuilderRecord() {
        return builderRecord;
    }
}