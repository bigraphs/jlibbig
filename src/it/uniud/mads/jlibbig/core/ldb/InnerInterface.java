package it.uniud.mads.jlibbig.core.ldb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InnerInterface {
    private final List<OuterInterfacePair> names = new ArrayList<>();

    public InnerInterface() {
        names.add(new OuterInterfacePair());
    }

    public static InnerInterface join(InnerInterface x, InnerInterface y) {
        List<OuterInterfacePair> xn = x.names;
        List<OuterInterfacePair> yn = y.names;
        List<OuterInterfacePair> zn = new ArrayList<>();

        InnerInterface z = new InnerInterface();
        z.names.set(0, OuterInterfacePair.merge(xn.get(0), yn.get(0)));
        xn.remove(0);
        yn.remove(0);
        zn.addAll(xn);
        zn.addAll(yn);
        z.names.addAll(zn);

        return z;
    }

    public int getWidth() {
        return names.size() - 1;
    }

    public Collection<? extends OuterName> getAsc() {
        List<OuterName> os = new ArrayList<>();
        for (OuterInterfacePair ip : names) {
            os.addAll(ip.getAscendants());
        }
        return os;
    }

    public Collection<? extends OuterName> getAsc(int index) {
        return this.names.get(index).getAscendants();
    }

    public Collection<? extends InnerName> getDesc() {
        List<InnerName> is = new ArrayList<>();
        for (OuterInterfacePair ip : names) {
            is.addAll(ip.getDescendants());
        }
        return is;
    }

    public Collection<? extends InnerName> getDesc(int index) {
        return this.names.get(index).getDescendants();
    }
}
