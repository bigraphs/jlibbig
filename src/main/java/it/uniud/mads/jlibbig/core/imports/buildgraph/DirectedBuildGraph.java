package it.uniud.mads.jlibbig.core.imports.buildgraph;

import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraph;

/**
 * Interface for directed build graph
 */
public interface DirectedBuildGraph {
    
    public DirectedBigraph build(DirectedBigraphListOfObjectRecord listOfObject, DirectedBigraphListOfControlRecord listOfControl);
}