package it.uniud.mads.jlibbig.core.imports;

import it.uniud.mads.jlibbig.core.imports.ldb.buildgraph.BuildGraph;
import it.uniud.mads.jlibbig.core.imports.ldb.parseinput.ParseInput;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfObjectRecord;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraph;
import it.uniud.mads.jlibbig.core.ldb.DirectedBigraphBuilder;

public class ImportDirectedBigraph {

    private DirectedBigraphBuilder builder = null;

    public DirectedBigraph doImport(String json) {
        ParseInput parseInput = new ParseInput();
        parseInput.parseJson(json);
        DirectedBigraphListOfObjectRecord listOfObject = parseInput.getListOfObject();
        DirectedBigraphListOfControlRecord listOfControl = parseInput.getListOfControl();
        BuildGraph buildGraph = new BuildGraph();
        DirectedBigraph bigraph = buildGraph.build(listOfObject, listOfControl);
        builder = buildGraph.getBuilderRecord();
        return bigraph;
    }

    /**
     * Get the builder, if exist.
     * @return the builder if exists. Null otherwise.
     */
    public DirectedBigraphBuilder getBuilder() {
        return builder;
    }
    // End Class
}