package it.uniud.mads.jlibbig.core.imports.exception;

public class ControlAlreadyExists extends Exception {
    private static final long serialVersionUID = 1L;

    public ControlAlreadyExists(String message) {
        super(message);
    }

    public ControlAlreadyExists() {
        super();
    }

}