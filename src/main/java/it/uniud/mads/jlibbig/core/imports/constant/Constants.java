package it.uniud.mads.jlibbig.core.imports.constant;

/**
 * This class implements some constant value used by import's methods
 */
public class Constants {

    public enum TypeOfNodes {
        root, node, site, name, edge;
    }

    public enum TypeOfInterface {
        outer, inner;
    }

    public enum TypeOfLink {
        place, linkedTo;
    }

    public enum TypeOfPolarity {
        plus, minus;
    }

    public enum TypeOfProperty {
        string, array;
    }

    public enum DirectionOfLink {
        to, from;
    }
}