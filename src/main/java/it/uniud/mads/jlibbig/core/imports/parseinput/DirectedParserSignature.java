package it.uniud.mads.jlibbig.core.imports.parseinput;

import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;

/**
 * Interface for directed parser Signature
 */
public interface DirectedParserSignature {

    public void parseSignature(JSONObject obj, DirectedBigraphListOfControlRecord listOfControl);
}