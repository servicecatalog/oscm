/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-08-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.context;

import java.io.Serializable;

/**
 * Specifies values for the keys in the context that defines the element for
 * which a price model is to be returned.
 */
public abstract class ContextValue<T> implements Serializable {

    private static final long serialVersionUID = 4738410026389162268L;

    /**
     * The value for a context key.
     */
    protected T value;

    /**
     * Constructs a context value object with the given value.
     *
     * @param value
     *            the value
     */
    public ContextValue(T value) {
        this.value = value;
    }

    /**
     * Returns the context value.
     * 
     * @return the value
     */
    public abstract T getValue();

    /**
     * Sets the context value.
     * 
     * @param value
     *            the value
     */
    public abstract void setValue(T value);

}
