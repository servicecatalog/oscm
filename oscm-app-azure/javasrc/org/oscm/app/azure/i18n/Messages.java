/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.i18n;

import org.oscm.app.v2_0.data.LocalizedText;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class representing a singleton (GoF) facility for I18N handling based on
 * locale specific message files.
 * <p>
 * A message file is a standard Java property file that contains key/value
 * pairs. The key represents the message ID, and the value the I18N string. The
 * I18N location is determined by the file name, for example: <br>
 * messages_jp.properties stands for the Japanese message file. <br>
 * messages_EN_us.properties stands for the US English message file.
 */
public class Messages {

    private static final String BUNDLE_NAME = "org.oscm.app.azure.i18n.messages";

    public static final String DEFAULT_LOCALE = "en";

    private static Map<String, ResourceBundle> bundleList;
    static {
        bundleList = new HashMap<String, ResourceBundle>();
        bundleList.put("en",
                ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en")));
        bundleList.put("de",
                ResourceBundle.getBundle(BUNDLE_NAME, new Locale("de")));
        bundleList.put("ja",
                ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ja")));
    };

    private Messages() {
        // do not allow instantiation
    }

    public static String get(String locale, String key) {
        try {
            if (bundleList.containsKey(locale)) {
                return bundleList.get(locale).getString(key);
            }
            return bundleList.get(DEFAULT_LOCALE).getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String get(String locale, String key, Object... args) {
        return MessageFormat.format(get(locale, key), args);
    }

    public static List<LocalizedText> getAll(String key) {
        List<LocalizedText> list = new ArrayList<LocalizedText>();
        for (String locale : bundleList.keySet()) {
            list.add(new LocalizedText(locale, get(locale, key)));
        }
        return list;
    }

    public static List<LocalizedText> getAll(String key, Object... args) {
        List<LocalizedText> list = new ArrayList<LocalizedText>();
        for (String locale : bundleList.keySet()) {
            list.add(new LocalizedText(locale, get(locale, key, args)));
        }
        return list;
    }
}
