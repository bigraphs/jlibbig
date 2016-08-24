package it.uniud.mads.jlibbig.core.ldb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class InterfacePair<Asc, Desc> {
    private final Set<Asc> left = new HashSet<Asc>();
    private final Set<Desc> right = new HashSet<Desc>();

    InterfacePair(Set<Asc> left, Set<Desc> right) {
        this.left.addAll(left);
        this.right.addAll(right);
    }

    Set<Asc> getLeft() {
        return this.left;
    }

    Set<Desc> getRight() {
        return this.right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InterfacePair)) return false;
        InterfacePair interfacePairObj = (InterfacePair) o;
        return this.left.equals(interfacePairObj.getLeft()) &&
                this.right.equals(interfacePairObj.getRight());
    }

    @Override
    public String toString() {
        return "({" + left.toString() + "}+, {" + right.toString() + "}-)";
    }

    /**
     * mergePairs merges two pairs
     *
     * @param p1 the first pair
     * @param p2 the second pair
     * @return the merged pair
     */
    static <Asc extends EditableLinkFacet, Desc extends EditableLinkFacet> InterfacePair<Asc, Desc> mergePairs(
            InterfacePair<Asc, Desc> p1, InterfacePair<Asc, Desc> p2) {

        InterfacePair<Asc, Desc> p;

        Map<String, Asc> left = new HashMap<>();
        for (Asc a : p1.left) {
            left.put(a.getName(), a);
        }
        for (Asc a : p2.left) {
            if (!left.containsKey(a.getName())) {
                left.put(a.getName(), a);
            }
        }

        Map<String, Desc> right = new HashMap<>();
        for (Desc d : p1.right) {
            right.put(d.getName(), d);
        }
        for (Desc d : p2.right) {
            if (!left.containsKey(d.getName())) {
                right.put(d.getName(), d);
            }
        }

        p = new InterfacePair<>(new HashSet<>(left.values()), new HashSet<>(right.values()));

        return p;
    }
}
