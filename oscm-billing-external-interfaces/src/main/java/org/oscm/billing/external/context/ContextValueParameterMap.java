/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-08-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.context;

import java.io.Serializable;
import java.util.Map;

/**
 * Specifies service parameters as name/value pairs for the context that defines
 * the element for which a price model is to be returned.
 *
 */
public class ContextValueParameterMap extends ContextValue<Map<String, String>>
        implements Serializable {

    private static final long serialVersionUID = -5963298259926778603L;

    /**
     * Constructs a context value parameter map with the given service parameter
     * name and value.
     * 
     * @param value
     *            the name and value of a service parameter
     */
    public ContextValueParameterMap(Map<String, String> value) {
        super(value);
    }

    /**
     * Returns the service parameter name and value set in the context value
     * parameter map.
     * 
     * @return the name and value of a service parameter
     */
    @Override
    public Map<String, String> getValue() {
        return value;
    }

    /**
     * Sets the service parameter name and value in the context value parameter
     * map.
     * 
     * @param value
     *            the name and value of a service parameter
     */
    @Override
    public void setValue(Map<String, String> value) {
        this.value = value;
    }

}
