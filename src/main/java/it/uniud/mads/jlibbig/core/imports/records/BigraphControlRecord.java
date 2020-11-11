package it.uniud.mads.jlibbig.core.imports.records;

/**
 * This class implement a record for Controls (signature)
 */
public abstract class BigraphControlRecord {
    private String name = "";
    private boolean active;
    private int arityIn = 0;
    private int arityOut = 0;

    /**
     * @param name     the Control's name
     * @param arityIn  the Control's in port
     * @param arityOut the Control's out port
     * @param active   the Control's status
     */
    public BigraphControlRecord(String name, int arityIn, int arityOut, boolean active) {
        this.name = name;
        this.arityIn = arityIn;
        this.arityOut = arityOut;
        this.active = active;
    }

    /**
     * @param name     the Control's name
     * @param arityIn  the Control's in port
     * @param arityOut the Control's out port
     */
    public BigraphControlRecord(String name, String arityIn, String arityOut) {
        this.name = name;
        this.arityIn = Integer.parseInt(arityIn);
        this.arityOut = Integer.parseInt(arityOut);
    }

    /**
     * @return this Control's name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return this Control's in port
     */
    public final int getArityIn() {
        return arityIn;
    }

    /**
     * @return this Control's out port
     */
    public final int getArityOut() {
        return arityOut;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
}