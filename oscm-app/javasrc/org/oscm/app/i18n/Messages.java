/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 07.05.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.i18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * This class represents a singleton [GoF] facility for the I18N handling based
 * on locale specific message files.
 * 
 * A message file is a standard Java property file that contains key- value
 * pairs. The key represents the message id and the value the I18N string.
 * 
 * The I18N location is distinguished by the file name. For example
 * 
 * messages_jp.properties stands for the japanese message file.
 * messages_EN_us.properties stands for the US english message file.
 */
public class Messages {

    private static final String BUNDLE_NAME = "org.oscm.app.i18n.messages"; //$NON-NLS-1$

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
            if (bundleList.containsKey(locale))
                return bundleList.get(locale).getString(key);
            else
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

}
