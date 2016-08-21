package it.uniud.mads.jlibbig.core.ldb;

import java.util.HashSet;
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
    static <Asc, Desc> InterfacePair<Asc, Desc> mergePairs(
            InterfacePair<Asc, Desc> p1, InterfacePair<Asc, Desc> p2) {

        InterfacePair<Asc, Desc> p;

        Set<Asc> left = new HashSet<>();
        left.addAll(p1.left);
        for (Asc x : p2.left) {
            if (!p1.left.contains(x))
                left.add(x);
        }

        Set<Desc> right = new HashSet<>();
        right.addAll(p1.right);
        for (Desc x : p2.right) {
            if (!p1.right.contains(x))
                right.add(x);
        }

        p = new InterfacePair<>(left, right);

        return p;
    }
}
