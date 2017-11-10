/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.profile;

public class FieldData<T> {

    private T value;
    private boolean readOnly;
    private boolean required;
    private boolean rendered = true;

    public FieldData(T value) {
        this.value = value;
    }

    public FieldData(T value, boolean readOnly) {
        this.value = value;
        this.readOnly = readOnly;
    }

    public FieldData(T value, boolean readOnly, boolean required) {
        this.value = value;
        this.readOnly = readOnly;
        this.required = required;
    }

    public FieldData(T value, boolean readOnly, boolean required,
            boolean rendered) {
        this.value = value;
        this.readOnly = readOnly;
        this.required = required;
        this.rendered = rendered;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRendered() {
        return rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

}
