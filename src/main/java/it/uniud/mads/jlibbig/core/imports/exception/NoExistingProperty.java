package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingProperty extends Exception {

    private static final long serialVersionUID = 1L;

    public NoExistingProperty(String message) {
        super(message);
    }

    public NoExistingProperty() {
        super();
    }
}