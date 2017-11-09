/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.oscm.internal.vo.VOLocalizedText;

/**
 * Utility methods for localized texts.
 * 
 * @author hoffmann
 */
public class LocaleUtils {

    /**
     * Returns a new list of the given localized values that correspond to the
     * number and sequence of the given locales. Missing localized values are
     * added. Localized values that are not listed are removed.
     * 
     * @param values
     * @param locales
     * @return
     */
    public static List<VOLocalizedText> trim(
            final List<VOLocalizedText> values, final Iterator<Locale> locales) {

        final Map<String, VOLocalizedText> mappedValues = new HashMap<String, VOLocalizedText>();
        for (final VOLocalizedText value : values) {
            mappedValues.put(value.getLocale(), value);
        }

        final List<VOLocalizedText> result = new ArrayList<VOLocalizedText>();
        while (locales.hasNext()) {
            final String locale = locales.next().toString();
            VOLocalizedText entry = mappedValues.get(locale);
            if (entry == null) {
                entry = new VOLocalizedText(locale, "");
            }
            result.add(entry);
        }
        return result;
    }

    /**
     * Returns a new list of the given localized values that correspond to the
     * number and sequence of the supported locales of the current faces
     * context.
     * 
     * @param values
     * @return
     */
    public static List<VOLocalizedText> trim(final List<VOLocalizedText> values) {
        return trim(values, FacesContext.getCurrentInstance().getApplication()
                .getSupportedLocales());
    }

    /**
     * Returns the value of the list entry with the given locale. If no such
     * entry can be found <code>null</code> is returned.
     * 
     * @param values
     * @param locale
     * @return
     */
    public static String get(final List<VOLocalizedText> values,
            final String locale) {
        for (final VOLocalizedText value : values) {
            if (locale.equals(value.getLocale())) {
                return value.getText();
            }
        }
        return null;
    }

    /**
     * Sets the value of the list entry with the given locale.
     * 
     * @param values
     *            the list with the localized texts
     * @param locale
     *            the locale of the entry which is set
     * @param text
     *            the text to set
     * @return <code>true</code> in case the value was set, <code>false</code>
     *         otherwise, if no entry matching this locale was contained in the
     *         list.
     */
    public static boolean set(final List<VOLocalizedText> values,
            final String locale, final String text) {
        for (final VOLocalizedText value : values) {
            if (locale.equals(value.getLocale())) {
                value.setText(text);
                return true;
            }
        }
        return false;
    }

    public static List<String> getSupportedLocales() {
        Iterator<Locale> supportedLocales = FacesContext.getCurrentInstance()
                .getApplication().getSupportedLocales();
        List<String> localeList = new ArrayList<String>();
        while (supportedLocales.hasNext()) {
            String languageCode = supportedLocales.next().toString();
            localeList.add(languageCode);
        }
        return localeList;
    }

    public static boolean isLocaleSupported(String languageISOCode) {
        List<String> supportedLocales = getSupportedLocales();
        for (String supportedLocale : supportedLocales) {
            if (supportedLocale.equalsIgnoreCase(languageISOCode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLocaleValid(String languageISOCode) {
        String[] locales = Locale.getISOLanguages();
        for (String l : locales) {
            if (l.equalsIgnoreCase(languageISOCode)) {
                return true;
            }
        }
        return false;
    }
}
