package it.uniud.mads.jlibbig.core.imports.ldb.records;

import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfProperty;
import it.uniud.mads.jlibbig.core.imports.records.BigraphPropertyRecord;

public class DirectedBigraphPropertyRecord extends BigraphPropertyRecord {

    public DirectedBigraphPropertyRecord(String id, TypeOfProperty type, List<String> property) {
        super(id, type, property);
    }
}
