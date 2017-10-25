/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utility to create the report engine URL from a template. The template
 * contains key in the format <code>${keyname}</code>.
 * 
 * @author hoffmann
 */
public class ReportEngineUrl {

    public static final String KEY_REPORTNAME = "reportname";
    public static final String KEY_SESSIONID = "sessionid";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_WSDLURL = "wsdlurl";
    public static final String KEY_SOAPENDPOINT = "soapendpoint";

    /**
     * Calculates the placeholder for the given key.
     * 
     * @param key
     * @return placeholder string
     */
    public static String getPlaceholder(final String key) {
        return String.format("${%s}", key);
    }

    /**
     * Replaces the placeholder with key in the given string by the value
     * string. The value will get URL encoded.
     * 
     * @param s
     * @param key
     * @param value
     * @return
     */
    public static String replace(String s, String key, String value) {
        try {
            return s.replace(getPlaceholder(key), URLEncoder.encode(value,
                    "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Must never happen for UTF-8
            throw new RuntimeException(e);
        }
    }

}
