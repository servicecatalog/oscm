/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 26.05.2010                                                      
 *                                                                              
 *  Completion Time: 26.05.2010                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.oscm.converter.LocaleHandler;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * Big decimal converter for UI.
 * 
 * @author Aleh Khomich.
 * 
 */
public class BigDecimalConverter implements Converter {

    public static final String FORMAT_PATTERN = "##0.00";

    private ConverterException createConverterException(FacesContext context,
            UIComponent component) {
        ValidationException e = new ValidationException(ReasonEnum.DECIMAL,
                null, null);
        return new ConverterException(JSFUtils.getFacesMessage(component,
                context, e.getMessageKey()));
    }

    /**
     * Conversion to server representation, converting discount value to
     * internal BigDecimal value xx.xx.
     * 
     * @param context
     *            JSF context.
     * @param component
     *            Component which value will be processed.
     * @param pValue
     *            Value.
     */
    public Object getAsObject(FacesContext context, UIComponent component,
            String pValue) {

        if ((pValue == null) || ((pValue = pValue.trim()).length() == 0)) {
            return null;
        }

        BigDecimal value = null;
        try {
            Locale locale = LocaleHandler.getLocaleFromString(BaseBean
                    .getUserFromSession(context).getLocale());
            // parse with standard java functionality
            NumberFormat nf = NumberFormat.getInstance(locale);
            ParsePosition pos = new ParsePosition(0);
            Number myNumber = nf.parse(pValue, pos);
            if (pos.getIndex() != pValue.length()) {
                // there are unparsed characters in the value
                throw createConverterException(context, component);
            }

            value = new BigDecimal(myNumber.toString());
        } catch (NumberFormatException e) {
            throw createConverterException(context, component);
        }

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
    public String getAsString(FacesContext context, UIComponent component,
            Object object) {
        if (object == null) {
            return null;
        }

        Locale locale = LocaleHandler.getLocaleFromString(BaseBean
                .getUserFromSession(context).getLocale());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        final Object displayAs = component == null
                || component.getAttributes() == null ? null : component
                .getAttributes().get("displayAs");
        final NumberFormat df;
        if ("percent".equals(displayAs) && object instanceof BigDecimal) {
            df = new DecimalFormat(FORMAT_PATTERN + '%', dfs);
            object = ((BigDecimal) object).divide(new BigDecimal(100));
        } else {
            df = new DecimalFormat(FORMAT_PATTERN, dfs);
        }

        return df.format(object);
    }
}
