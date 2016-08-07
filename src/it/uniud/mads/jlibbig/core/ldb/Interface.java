package it.uniud.mads.jlibbig.core.ldb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Interface {
    private final List<InterfacePair> names = new ArrayList<InterfacePair>();

    public Interface() {
        names.add(new InterfacePair());
    }

    public static Interface join(Interface x, Interface y) {
        List<InterfacePair> xn = x.names;
        List<InterfacePair> yn = y.names;
        List<InterfacePair> zn = new ArrayList<>();

        Interface z = new Interface();
        z.names.set(0, InterfacePair.merge(xn.get(0), yn.get(0)));
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
        for (InterfacePair ip : names) {
            os.addAll(ip.getOuterNames());
        }
        return os;
    }

    public Collection<? extends OuterName> getAsc(int index) {
        return this.names.get(index).getOuterNames();
    }

    public Collection<? extends InnerName> getDesc() {
        List<InnerName> is = new ArrayList<>();
        for (InterfacePair ip : names) {
            is.addAll(ip.getInnerNames());
        }
        return is;
    }

    public Collection<? extends InnerName> getDesc(int index) {
        return this.names.get(index).getInnerNames();
    }
}
