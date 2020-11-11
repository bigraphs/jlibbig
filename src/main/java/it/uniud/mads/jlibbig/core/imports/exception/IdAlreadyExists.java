package it.uniud.mads.jlibbig.core.imports.exception;

public class IdAlreadyExists extends Exception {
    private static final long serialVersionUID = 1L;

    public IdAlreadyExists(String message) {
        super(message);
    }

    public IdAlreadyExists() {
        super();
    }

}