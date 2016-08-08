package it.uniud.mads.jlibbig.core.ldb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OuterInterface {
    private final List<OuterInterfacePair> pairs = new ArrayList<>();

    public OuterInterface() {
        pairs.add(0, new OuterInterfacePair());
    }

    public static OuterInterface join(OuterInterface x, OuterInterface y) {
        List<OuterInterfacePair> xPairs = x.pairs;
        List<OuterInterfacePair> yPairs = y.pairs;
        List<OuterInterfacePair> zPairs = new ArrayList<>();

        OuterInterface z = new OuterInterface();
        z.pairs.set(0, OuterInterfacePair.merge(xPairs.get(0), yPairs.get(0)));
        xPairs.remove(0);
        yPairs.remove(0);
        zPairs.addAll(xPairs);
        zPairs.addAll(yPairs);
        z.pairs.addAll(zPairs);

        return z;
    }

    public int getWidth() {
        return pairs.size() - 1;
    }

    public Collection<? extends OuterName> getAsc() {
        List<OuterName> asc = new ArrayList<>();
        for (OuterInterfacePair ip : pairs) {
            asc.addAll(ip.getAscendants());
        }
        return asc;
    }

    public Collection<? extends OuterName> getAsc(int index) {
        return this.pairs.get(index).getAscendants();
    }

    public Collection<? extends InnerName> getDesc() {
        List<InnerName> desc = new ArrayList<>();
        for (OuterInterfacePair ip : pairs) {
            desc.addAll(ip.getDescendants());
        }
        return desc;
    }

    public Collection<? extends InnerName> getDesc(int index) {
        return this.pairs.get(index).getDescendants();
    }
}
