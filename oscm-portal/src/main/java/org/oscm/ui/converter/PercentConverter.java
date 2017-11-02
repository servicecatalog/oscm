/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author afschar
 * 
 */
public class PercentConverter implements Converter {

    private final ThreadLocal<DecimalFormat> format = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("##0.00%");
        }
    };

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String text) {
        try {
            return format.get().parseObject(text);
        } catch (ParseException e) {
            throw new SaaSSystemException("Text '" + text
                    + "' cannot be converted to a percentage number.", e);
        }
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        return format.get().format(value);
    }

}
