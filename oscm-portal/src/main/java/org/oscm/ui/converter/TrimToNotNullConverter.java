/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-3                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * @author yuyin
 * 
 */
public class TrimToNotNullConverter extends TrimConverter {

    /**
     * Removes leading and trailing blank spaces.
     * 
     * @param context
     *            JSF context
     * @param component
     *            component which value will be processed
     * @param value
     *            value
     */
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        String result = (String) super.getAsObject(context, component, value);
        return replaceNullValue(result);
    }

    /**
     * Removes leading and trailing blank spaces.
     * 
     * @param context
     *            JSF context
     * @param component
     *            component which value will be processed
     * @param object
     *            value 
     * @return the trimmed value (not null).
     */
    public String getAsString(FacesContext context, UIComponent component,
            Object object) {

        String result = super.getAsString(context, component, object);

        return replaceNullValue(result);
    }

    /**
     * replace the <code>null</code> value with empty string.
     */
    static public String replaceNullValue(String value) {
        return value == null ? "" : value;
    }
}
