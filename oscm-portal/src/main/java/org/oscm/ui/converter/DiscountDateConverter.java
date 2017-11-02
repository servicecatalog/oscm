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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.validator.ADMValidator;

/**
 * Discount value converter for UI.
 * 
 * @author Aleh Khomich.
 * 
 */
public class DiscountDateConverter implements Converter {

    private static final int NUMBER_OF_MONTH = 12;
    private static final String DATE_FORMAT = "MM/yyyy";
    private static final int LENGTH_MONTH = 2;
    private static final int LENGTH_YEAR = 4;

    /**
     * Conversion to server representation, converting discount value to
     * internal BigDecimal value xx.xx.
     * 
     * @param context
     *            JSF context.
     * @param component
     *            Component which value will be processed.
     * @param value
     *            Value. Expected "MM/yyyy".
     */
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        ParsePosition parsePosition = new ParsePosition(0);

        if (value == null || value.length() == 0 || value.trim().equals("")) {
            // discount date can be empty
            return null;
        }

        if (value.length() < ADMValidator.LENGTH_DISCOUNT_PERIOD) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }

        // check format
        String monthString = value.substring(0, LENGTH_MONTH);
        int monthInt;
        try {
            monthInt = Integer.parseInt(monthString);
        } catch (NumberFormatException ex) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }
        if ((monthInt <= 0) || (monthInt > NUMBER_OF_MONTH)) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }

        String yearString = value.substring(LENGTH_YEAR - 1,
                ADMValidator.LENGTH_DISCOUNT_PERIOD);
        int yearInt;
        try {
            yearInt = Integer.parseInt(yearString);
        } catch (NumberFormatException ex) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }
        if (yearInt <= 0) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }

        Date date = sdf.parse(value, parsePosition);
        if (date == null) {
            // error situation
            FacesMessage facesMessage = JSFUtils.getFacesMessage(component,
                    context, BaseBean.ERROR_DISCOUNT_DATE);
            throw new ConverterException(facesMessage);
        }

        return Long.valueOf(date.getTime());
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
        Long time = (Long) object;
        return convertToDateFormat(time);
    }

    /**
     * Conversion to Date format as String
     * 
     * @param time
     *            the object of discount time
     * @return the formatted discount date
     */
    static public String convertToDateFormat(Long time) {
        Date inputDate = new Date(time.longValue());

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String date = sdf.format(inputDate);

        return date;
    }
}
