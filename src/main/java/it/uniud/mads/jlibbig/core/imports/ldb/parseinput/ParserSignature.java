package it.uniud.mads.jlibbig.core.imports.ldb.parseinput;

import org.json.JSONArray;
import org.json.JSONObject;

import it.uniud.mads.jlibbig.core.imports.exception.ControlAlreadyExists;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphControlRecord;
import it.uniud.mads.jlibbig.core.imports.ldb.records.DirectedBigraphListOfControlRecord;
import it.uniud.mads.jlibbig.core.imports.parseinput.DirectedParserSignature;

/**
 * This class implements methods for parsing the "signature" block of a json
 * rapresentation of a directed bigraph
 */
public class ParserSignature implements DirectedParserSignature {

    /**
     * Parse the "signature" block of the given Json
     * <p>
     * MODIFY: listOfControl, add a new signature
     * 
     * @param arr           the json of the block "signature"
     * @param listOfControl the list of the signature
     */
    @Override
    public void parseSignature(JSONObject obj, DirectedBigraphListOfControlRecord listOfControl) {
        final JSONArray arr = obj.names();
        final int n = arr.length();
        for (int i = 0; i < n; i++) {
            try {
                final JSONObject element = obj.getJSONObject(arr.getString(i));
                parseSingleSignatureObject(element, arr.getString(i), listOfControl);
            } catch (ControlAlreadyExists e) {
                System.err.println(e);
                System.exit(1);
            }

        }
    }

    /**
     * Parse a rapresentation of a Signature
     * <p>
     * MODIFY: listOfControl, add the new signature
     * 
     * @param obj           the json of a signature
     * @param listOfControl the list of the signature
     * @throws ControlAlreadyExists if the signature already exist
     */
    private void parseSingleSignatureObject(JSONObject obj, String id, DirectedBigraphListOfControlRecord listOfControl)
            throws ControlAlreadyExists {
        for (int i = 0; i < listOfControl.size(); i++) {
            if (listOfControl.get(i).getName().equals(id)) {
                throw new ControlAlreadyExists("Error: The control " + id + " already exists. ");
            }
        }
        DirectedBigraphControlRecord control = new DirectedBigraphControlRecord(id,
                obj.getInt("arityIn"), obj.getInt("arityOut"), obj.getBoolean("active"));
        listOfControl.add(control);
    }
}