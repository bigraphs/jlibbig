package it.uniud.mads.jlibbig.core.imports.exception;

public class NoValidPort extends Exception {
    private static final long serialVersionUID = 1L;

    public NoValidPort(String message) {
        super(message);
    }

    public NoValidPort() {
        super();
    }
}