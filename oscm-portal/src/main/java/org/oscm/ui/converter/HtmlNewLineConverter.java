/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Lorenz Goebel                                         
 *                                                                              
 *  Creation Date: 14.11.2012                                                      
 *                                                                              
 *  Completion Time: 14.12.2012                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * @author goebel
 */
public class HtmlNewLineConverter implements Converter {

    /**
     * Conversion to server representation, converting text with new line to
     * text with <code>&lt;br/&gt;</code> tags.
     * 
     * @param context
     *            JSF context.
     * @param component
     *            Component which value will be processed.
     * @param pValue
     *            Value.
     */
    public Object getAsObject(FacesContext context, UIComponent comp,
            String value) {
        return value;
    }

    /**
     * Conversion to UI representation as String.
     * 
     * @param context
     *            JSF context.
     * @param component
     *            Component which value will be processed.
     * @param object
     *            Value.
     */
    public String getAsString(FacesContext context, UIComponent comp, Object obj) {
        if (obj == null || !(obj instanceof String)) {
            return null;
        }
        return ((String) obj).replaceAll("\n", "<br />");
    }

}
