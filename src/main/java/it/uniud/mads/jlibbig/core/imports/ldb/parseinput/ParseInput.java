package it.uniud.mads.jlibbig.core.imports.ldb.parseinput;

import org.json.JSONArray;
import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.imports.parseinput.DirectedParseInput;

/**
 * This class implements methods for parsing a Json rapresentation of a directed
 * bigraph
 */
public class ParseInput implements DirectedParseInput {

    DirectedBigraphListOfObjectRecord listOfObject = new DirectedBigraphListOfObjectRecord();
    DirectedBigraphListOfControlRecord listOfControl = new DirectedBigraphListOfControlRecord();

    ParserSignature parserSignature = new ParserSignature();
    ParserNode parserNode = new ParserNode();
    ParserEdge parserEdge = new ParserEdge();

    public ParseInput() {
    }

    /**
     * Parse a json rapresentation of a directed bigraph
     * <p>
     * MODIFY: listOfObject, listOfControl
     *
     * @param json the json rapresentation of a directed bigraph
     */
    @Override
    public void parseJson(String json) {
        final JSONObject obj = new JSONObject(json);
        final JSONObject graph = obj.getJSONObject("graph");
        final JSONObject nodes = graph.getJSONObject("nodes");
        final JSONArray edges = graph.getJSONArray("edges");
        final JSONObject metadata = graph.getJSONObject("metadata");
        final JSONObject signatures = metadata.getJSONObject("signature");

        parserSignature.parseSignature(signatures, listOfControl);
        parserNode.parseNodes(nodes, listOfObject, listOfControl);
        parserEdge.parseEdges(edges, listOfObject);

        listOfObject.sortList();
    }

    /**
     * @return the listOfObject
     */
    public DirectedBigraphListOfObjectRecord getListOfObject() {
        return listOfObject;
    }

    /**
     * @return the listOfControl
     */
    public DirectedBigraphListOfControlRecord getListOfControl() {
        return listOfControl;
    }
    

}