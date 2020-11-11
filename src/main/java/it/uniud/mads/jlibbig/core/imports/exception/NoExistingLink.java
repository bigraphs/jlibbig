package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingLink extends Exception {
    private static final long serialVersionUID = 1L;

    public NoExistingLink(String message) {
        super(message);
    }

    public NoExistingLink() {
        super();
    }

}