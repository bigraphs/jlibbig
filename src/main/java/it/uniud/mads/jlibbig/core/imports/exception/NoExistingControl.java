package it.uniud.mads.jlibbig.core.imports.exception;

public class NoExistingControl extends Exception {
    private static final long serialVersionUID = 1L;

    public NoExistingControl(String message) {
        super(message);
    }

    public NoExistingControl() {
        super();
    }

}