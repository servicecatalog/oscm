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
 * Specifies string values for the context that defines the element for which a
 * price model is to be returned.
 *
 */
public class ContextValueString extends ContextValue<String> implements
        Serializable {

    private static final long serialVersionUID = -7405786827911921276L;

    /**
     * Constructs a context value object with the given string value.
     * 
     * @param value
     *            the value
     */
    public ContextValueString(String value) {
        super(value);
    }

    /**
     * Returns the context value.
     * 
     * @return the value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Sets the context value.
     * 
     * @param value
     *            the value
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
