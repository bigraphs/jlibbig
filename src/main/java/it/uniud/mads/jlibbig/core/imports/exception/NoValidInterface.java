package it.uniud.mads.jlibbig.core.imports.exception;

public class NoValidInterface extends Exception {
    private static final long serialVersionUID = 1L;

    public NoValidInterface(String message) {
        super(message);
    }

    public NoValidInterface() {
        super();
    }

}