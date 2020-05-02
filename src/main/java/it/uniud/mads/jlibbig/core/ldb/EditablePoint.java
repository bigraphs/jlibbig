package it.uniud.mads.jlibbig.core.ldb;

interface EditablePoint extends Point {
    @Override
    EditableHandle getHandle();

    /**
     * Set point's handle.
     *
     * @param handle point's new handle
     */
    void setHandle(EditableHandle handle);
}