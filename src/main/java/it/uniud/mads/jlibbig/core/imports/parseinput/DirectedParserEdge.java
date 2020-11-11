package it.uniud.mads.jlibbig.core.imports.parseinput;

import org.json.JSONArray;

import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;

/**
 * Interface for directed parser Edge
 */
public interface DirectedParserEdge {

    public void parseEdges(JSONArray arr, DirectedBigraphListOfObjectRecord listOfObject);
    
}