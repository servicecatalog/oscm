/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 12.10.2010                                                      
 *                                                                              
 *  Completion Time: 12.10.2010                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * The trim converter removes leading and trailing blank spaces.
 */
public class TrimConverter implements Converter {

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

        return stripToNull(value);
    }

    /**
     * Removes leading and trailing blank spaces. Empty strings are converted to
     * <code>null</null> 
     * <p>
     * Note: This method returns null for empty strings. 
     * </p>
     * 
     * @param context
     *            JSF context
     * @param component
     *            component which value will be processed
     * @param object
     *            value
     * @return the trimmed value as string or <code>null</code>.
     */
    public String getAsString(FacesContext context, UIComponent component,
            Object object) {

        if (object == null) {
            return null;
        }

        String result = (String) object;

        return stripToNull(result);
    }

    /**
     * Removes leading and trailing white spaces (e.g. \u3000). <br>
     * Note: org.apache.commons.lang Class StringUtils is not part of this
     * project!
     */
    static public String stripToNull(String value) {

        if (value == null) {
            return null;
        }

        String result = null;
        boolean whiteSpaceFound = false;
        int len = value.length();

        if (len > 0) {
            // get first position of a non-whitespace character
            int beg = 0;
            for (int i = 0; i < len; i++) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    beg = i;
                    break;
                } else {
                    whiteSpaceFound = true;
                }
            }

            // get first trailing whitespace character
            int end = len;
            for (int i = len; i > 0; i--) {
                if (!Character.isWhitespace(value.charAt(i - 1))) {
                    end = i;
                    break;
                } else {
                    whiteSpaceFound = true;
                }
            }

            if (whiteSpaceFound) {
                // return the cut string or null
                if ((end != 0) && ((beg != 0) || (end != len))) {
                    result = value.substring(beg, end);
                }
            } else {
                result = value;
            }
        }

        return result;
    }
}
