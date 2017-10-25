/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.util.Locale;

/**
 * Provides utility methods to work with locale objects.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class LocaleHandler {

    /**
     * Returns the locale according to the given locale String.
     * 
     * @param localeString
     *            The locale-specifying string.
     * @return The locale as defined in the string.
     */
    public static Locale getLocaleFromString(String localeString) {
        Locale locale = null;
        // to guarantee stable behavior perform null check
        if (localeString == null) {
            return Locale.ENGLISH;
        }

        if (localeString.length() == 2) {
            // only language
            locale = new Locale(localeString);
        } else if (localeString.length() > 4 && localeString.charAt(2) == '_'
                && localeString.charAt(3) == '_') {
            // language and version without country
            locale = new Locale(localeString.substring(0, 2), "",
                    localeString.substring(4));
        } else if (localeString.length() == 5 && localeString.charAt(2) == '_') {
            // language and country without version
            locale = new Locale(localeString.substring(0, 2),
                    localeString.substring(3));
        } else if (localeString.length() > 6 && localeString.charAt(2) == '_'
                && localeString.charAt(5) == '_') {
            // language, country and version
            locale = new Locale(localeString.substring(0, 2),
                    localeString.substring(3, 5), localeString.substring(6));
        }
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        return locale;
    }

    /**
     * check if the locale is standard language
     * 
     * @param locale
     *            locale to be checked
     * @return <code>true</code> in case the locale is standard language,
     *         <code>false</code> in case the locale is not standard language.
     */
    public static boolean isStandardLanguage(Locale locale) {
        if (Locale.ENGLISH.equals(locale) || Locale.GERMAN.equals(locale)
                || Locale.JAPANESE.equals(locale)) {
            return true;
        }
        return false;
    }
}
