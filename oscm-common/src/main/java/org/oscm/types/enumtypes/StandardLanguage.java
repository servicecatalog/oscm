/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-2-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Qiu
 * 
 */
public enum StandardLanguage {
    DE(Locale.GERMAN), EN(Locale.ENGLISH), JA(Locale.JAPANESE);

    private Locale locale;
    public final static String COLUMN_HEADING_SUFFIX = " system";

    private StandardLanguage(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public static boolean isStandardLanguage(String localeCode) {

        return isStandardLanguage(localeCode, "");
    }

    public static boolean isStandardLanguage(String localeCode, String suffix) {
        for (StandardLanguage standardLanguage : StandardLanguage.values()) {
            if ((standardLanguage.toString() + suffix).equals(localeCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.locale.getLanguage();
    }

    public static List<Locale> getStandardLocales() {
        List<Locale> locales = new ArrayList<Locale>();
        for (StandardLanguage standardLanguage : StandardLanguage.values()) {
            locales.add(standardLanguage.getLocale());
        }
        return locales;

    }
}
