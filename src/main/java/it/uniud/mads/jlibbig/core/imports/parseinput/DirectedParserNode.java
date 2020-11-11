package it.uniud.mads.jlibbig.core.imports.parseinput;

import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;

/**
 * Interface for directed parser Node
 */
public interface DirectedParserNode {

    public void parseNodes(JSONObject nodes, DirectedBigraphListOfObjectRecord listOfObject,
            DirectedBigraphListOfControlRecord listOfControl);
}