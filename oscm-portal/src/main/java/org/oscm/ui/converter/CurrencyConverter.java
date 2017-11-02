/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 26.04.2010                                                      
 *                                                                              
 *  Completion Time: 26.04.2010                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.converter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.oscm.converter.PriceConverter;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.validation.Invariants;

/**
 * Currency converter for UI.
 * 
 * @author Aleh Khomich.
 * 
 */
public class CurrencyConverter implements Converter {

    /**
     * Conversion to server representation, so converting currency to internal
     * integer with cents format. Prior to the conversion the input value is
     * validated.
     * 
     * @param context
     *            JSF context.
     * @param component
     *            Component which value will be processed.
     * @param value
     *            Value.
     */
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        final PriceConverter converter = getConverter(context);

        try {
            return converter.parse(value);
        } catch (ParseException e) {
            String msg = e.getMessage();
            if (msg != null
                    && msg.equals("ERROR_PRICEMODEL_INVALID_FRACTIONAL_PART")) {
                throw new ConverterException(JSFUtils.getFacesMessage(
                        component, context,
                        BaseBean.ERROR_PRICEMODEL_INVALID_FRACTIONAL_PART));
            }
            throw new ConverterException(JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_PRICEMODEL_INPUT));
        }
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
    public String getAsString(FacesContext context, UIComponent component,
            Object object) {
        if (object == null) {
            return null;
        }
        Invariants.asserType(object, BigDecimal.class);

        final PriceConverter converter = getConverter(context);        
        return converter.getValueToDisplay((BigDecimal) object, true);

    }

    /**
     * Getting converter with needed local.
     * 
     * @param context
     *            JSF context.
     * @return Instance of old price converter. TODO Refactor all code for using
     *         only UI conversion.
     */
    private PriceConverter getConverter(FacesContext context) {
        Locale locale;
        if (context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        } else {
            locale = context.getApplication().getDefaultLocale();
        }
        final PriceConverter converter = new PriceConverter(locale);

        return converter;
    }
}
