package it.uniud.mads.jlibbig.core.imports.ldb.records;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniud.mads.jlibbig.core.imports.constant.Constants.TypeOfNodes;
import it.uniud.mads.jlibbig.core.imports.records.BigraphListOfObjectRecord;

/**
 * This class implement a record for List of Directed Bigraph's Object
 */
public class DirectedBigraphListOfObjectRecord extends BigraphListOfObjectRecord<DirectedBigraphObjectRecord> {

    public DirectedBigraphListOfObjectRecord() {
        super();
    }

    public List<DirectedBigraphObjectRecord> getType(TypeOfNodes type) {
        List<DirectedBigraphObjectRecord> listToReturn = new ArrayList<DirectedBigraphObjectRecord>();
        List<DirectedBigraphObjectRecord> list = super.getList();
        for(int i = 0; i < list.size(); i++) {
            DirectedBigraphObjectRecord obj = list.get(i);
            if(obj.getType().equals(type)) {
                listToReturn.add(obj);
            }
        }
        return listToReturn;
    }

    /**
     * Sort the list
     */
    public void sortList() {
        Collections.sort(super.getList());
    }

    public List<DirectedBigraphObjectRecord> getListRootSorted() {
        List<DirectedBigraphObjectRecord> rootList = new ArrayList<DirectedBigraphObjectRecord>();
        List<DirectedBigraphObjectRecord> list = super.getList();
        for(int i = 0; i < list.size(); i++) {
            DirectedBigraphObjectRecord obj = list.get(i);
            if(obj.getType().equals(TypeOfNodes.root)) {
                rootList.add(obj);
            }
        }
        for(int i = 1; i < rootList.size(); i++) {
            DirectedBigraphRootRecord val = (DirectedBigraphRootRecord)rootList.get(i);
            int j = i - 1;
            while(j >= 0 && isBigger(rootList.get(j), val)) {
                rootList.set(j+1, rootList.get(j));
                j = j -1;
            }
            rootList.set(j+1, val);
        }
        return rootList;
    }

    private boolean isBigger(DirectedBigraphObjectRecord valj, DirectedBigraphRootRecord val){
        DirectedBigraphRootRecord valJRoot = (DirectedBigraphRootRecord)valj;
        if(valJRoot.getLocation() > val.getLocation()) {
            return true;
        } else {
            return false;
        }
    }
}