package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingParent extends Exception {
    private static final long serialVersionUID = 1L;

    public NoExistingParent(String message) {
        super(message);
    }

    public NoExistingParent() {
        super();
    }
    
}