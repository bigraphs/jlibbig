package it.uniud.mads.jlibbig.core.ldb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OuterInterface {
    private final List<OuterInterfacePair> pairs = new ArrayList<>();

    public OuterInterface() {
        pairs.add(new OuterInterfacePair());
    }

    public static OuterInterface join(OuterInterface x, OuterInterface y) {
        List<OuterInterfacePair> xn = x.pairs;
        List<OuterInterfacePair> yn = y.pairs;
        List<OuterInterfacePair> zn = new ArrayList<>();

        OuterInterface z = new OuterInterface();
        z.pairs.set(0, OuterInterfacePair.merge(xn.get(0), yn.get(0)));
        xn.remove(0);
        yn.remove(0);
        zn.addAll(xn);
        zn.addAll(yn);
        z.pairs.addAll(zn);

        return z;
    }

    public int getWidth() {
        return pairs.size() - 1;
    }

    public Collection<? extends OuterName> getAsc() {
        List<OuterName> os = new ArrayList<>();
        for (OuterInterfacePair ip : pairs) {
            os.addAll(ip.getAscendants());
        }
        return os;
    }

    public Collection<? extends OuterName> getAsc(int index) {
        return this.pairs.get(index).getAscendants();
    }

    public Collection<? extends InnerName> getDesc() {
        List<InnerName> is = new ArrayList<>();
        for (OuterInterfacePair ip : pairs) {
            is.addAll(ip.getDescendants());
        }
        return is;
    }

    public Collection<? extends InnerName> getDesc(int index) {
        return this.pairs.get(index).getDescendants();
    }
}
