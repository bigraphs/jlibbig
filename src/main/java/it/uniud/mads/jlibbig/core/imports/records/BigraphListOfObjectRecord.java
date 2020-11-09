package it.uniud.mads.jlibbig.core.imports.records;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implement a record for List of Bigraph's Mutable Object
 */
public abstract class BigraphListOfObjectRecord<T> {

    private List<T> list = new ArrayList<T>();

    public BigraphListOfObjectRecord() {
    }

    /**
     * @return this list
     */
    public final List<T> getList() {
        return list;
    }

    /**
     * Get the element in the given position
     * 
     * @param index the position of the wanted element
     * @return T the element in the given position
     */
    public final T get(int index) {
        return list.get(index);
    }

    /**
     * @param list the list to set
     */
    public final void setList(List<T> list) {
        this.list = list;
    }

    /**
     * @param element the element to add in the list
     */
    public final void add(T element) {
        this.list.add(element);
    }

    /**
     * Get the size of this list
     * @return the size of this list
     */
    public final int size() {
        return list.size();
    }
}
