/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 07.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.oscm.converter.LocaleHandler;
import org.oscm.ui.beans.BaseBean;

/**
 * @author Thomas Baumann
 */
public class DurationValidation {

    public final static long MILLISECONDS_PER_DAY = 24L * 3600000L;
    public static final long DURATION_MAX_DAYS_VALUE = Long.MAX_VALUE
            / MILLISECONDS_PER_DAY;
    private static final String DURATION_FORMAT = "#0";
    private static final int DEC_PLACES_BOUND = 0;

    /**
     * Convert a duration String to a Long value, that represents the duration
     * in Milliseconds. The duration must be a valid positive Long with a 
     * maximum value of Long.MAX_VALUE / MILLISECONDS_PER_DAY.
     * 
     * @return the duration in milliseconds; <code>null</code> if the duration
     *         is invalid.
     */
    public static Long getDurationInMs(FacesContext context,
            String durationValue) {
        if (!validateOnlyDigits(durationValue, context)
                || !validatePrecision(durationValue, context)) {
            return null;
        }

        return convertDurationToMs(context, durationValue);
    }

    /**
     * Validates that the given value contains only digits, but not e.g. a
     * character 'd'. Java would interpret the input 3d as double value 3.0.
     * Anyway, this must not succeed.
     * 
     * @param valueToCheck
     *            The value to be checked.
     * @param component
     *            The current component.
     * @return <code>true</code> if the value is valid.
     */
    private static boolean validateOnlyDigits(String valueToCheck,
            FacesContext facesContext) {
        Locale locale = LocaleHandler.getLocaleFromString(BaseBean
                .getUserFromSession(facesContext).getLocale());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        boolean decSepFound = false;

        for (char c : valueToCheck.toCharArray()) {
            if (!decSepFound && c == dfs.getDecimalSeparator()) {
                decSepFound = true;
                continue;
            }
            if (c == dfs.getGroupingSeparator()) {
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that the number of decimal places is not exceeding the amount
     * given in {@link DEC_PLACES_BOUND}.
     * 
     * @param valueToCheck
     *            The value to be checked.
     * @param component
     *            The current component.
     * @return <code>true</code> if the value is valid.
     */
    private static boolean validatePrecision(String valueToCheck,
            FacesContext facesContext) {
        Locale locale = LocaleHandler.getLocaleFromString(BaseBean
                .getUserFromSession(facesContext).getLocale());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        char decimalSeparator = dfs.getDecimalSeparator();
        int pos = valueToCheck.lastIndexOf(decimalSeparator);
        if (pos != -1 && valueToCheck.length() - 1 > pos + DEC_PLACES_BOUND) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parses the given value considering the current locale and returns the
     * number representation of the string. The representation is based on the
     * {@link #DURATION_FORMAT}.
     * 
     * @param valueToCheck
     *            The string to be parsed. Must not be <code>null</code>.
     * @return The number representation of the parameter.
     * @throws ParseException
     */
    public static Number getParsedDuration(FacesContext facesContext,
            String valueToCheck) {
        Locale locale = LocaleHandler.getLocaleFromString(BaseBean
                .getUserFromSession(facesContext).getLocale());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        DecimalFormat df = new DecimalFormat(DURATION_FORMAT, dfs);
        df.setGroupingUsed(true);
        try {
            return df.parse(valueToCheck);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Long convertDurationToMs(FacesContext context,
            String durationValue) {
        Number parsedDuration = getParsedDuration(context, durationValue);
        if (parsedDuration != null) {
            // as validation passed, the value is not null
            double days = parsedDuration.doubleValue();
            double milliseconds = days * MILLISECONDS_PER_DAY;
            if (milliseconds > Long.MAX_VALUE) {
                return null;
            } else {
                return Long.valueOf(Math.round(milliseconds));
            }
        } else {
            return null;
        }
    }

    public static Long getDurationMaxDaysValue() {
        return Long.valueOf(DURATION_MAX_DAYS_VALUE);
    }

}
