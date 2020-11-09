package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingId extends Exception {
    private static final long serialVersionUID = 1L;

    public NoExistingId(String message) {
        super(message);
    }

    public NoExistingId() {
        super();
    }
}