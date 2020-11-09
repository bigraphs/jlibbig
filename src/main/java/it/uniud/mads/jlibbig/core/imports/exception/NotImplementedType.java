package it.uniud.mads.jlibbig.core.imports.exception;

public class NotImplementedType extends Exception {
    private static final long serialVersionUID = 1L;

    public NotImplementedType(String message) {
        super(message);
    }

    public NotImplementedType() {
        super();
    }
}