package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingSon extends Exception {
    private static final long serialVersionUID = 1L;

    public NoExistingSon(String message) {
        super(message);
    }

    public NoExistingSon() {
        super();
    }

}