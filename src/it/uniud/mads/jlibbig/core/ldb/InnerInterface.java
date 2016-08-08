package it.uniud.mads.jlibbig.core.ldb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InnerInterface {
    private final List<InnerInterfacePair> names = new ArrayList<>();

    public InnerInterface() {
        names.add(new InnerInterfacePair());
    }

    public static InnerInterface join(InnerInterface x, InnerInterface y) {
        List<InnerInterfacePair> xn = x.names;
        List<InnerInterfacePair> yn = y.names;
        List<InnerInterfacePair> zn = new ArrayList<>();

        InnerInterface z = new InnerInterface();
        z.names.set(0, InnerInterfacePair.merge(xn.get(0), yn.get(0)));
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

    public Collection<? extends EditableInnerName> getAsc() {
        List<EditableInnerName> os = new ArrayList<>();
        for (InnerInterfacePair ip : names) {
            os.addAll(ip.getAscendants());
        }
        return os;
    }

    public Collection<? extends EditableInnerName> getAsc(int index) {
        return this.names.get(index).getAscendants();
    }

    public Collection<? extends EditableOuterName> getDesc() {
        List<EditableOuterName> is = new ArrayList<>();
        for (InnerInterfacePair ip : names) {
            is.addAll(ip.getDescendants());
        }
        return is;
    }

    public Collection<? extends EditableOuterName> getDesc(int index) {
        return this.names.get(index).getDescendants();
    }
}
