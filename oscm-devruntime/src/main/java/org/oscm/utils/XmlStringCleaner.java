/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 07.07.15 12:39
 *
 *******************************************************************************/

package org.oscm.utils;

import java.util.regex.Pattern;

/**
 * Utility class for removing invalid character (such as '\u000b' - see Bug
 * 9711) from an XML string.
 * <p>
 * Sample: \uD83F\uDFFE-\uD83F\uDFFF is the code point notation (double byte
 * character) for \u1FFFE-\u1FFFF
 *
 * @author weiser
 *
 */
public class XmlStringCleaner {

    final static Pattern INVALID_CHARACTERS = Pattern
            .compile("<char\\scode=\"#.*\"/>");

    /**
     * All character that are not part of the character range of the XML
     * definition ((http://www.w3.org/TR/xml/#charsets)
     */
    final static Pattern INVALID_XML_CHARS = Pattern
            .compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");

    /**
     * All discouraged characters that should not be used within XML
     * (http://www.w3.org/TR/xml/#charsets)
     */
    final static Pattern DISCOURAGED_XML_CHARS = Pattern
            .compile("[\\u007F-\\u0084\\u0086-\\u009F\\uFDD0-\\uFDEF"
                    + "\uD83F\uDFFE-\uD83F\uDFFF" + "\uD87F\uDFFE-\uD87F\uDFFF"
                    + "\uD8BF\uDFFE-\uD8BF\uDFFF" + "\uD8FF\uDFFE-\uD8FF\uDFFF"
                    + "\uD93F\uDFFE-\uD93F\uDFFF" + "\uD97F\uDFFE-\uD97F\uDFFF"
                    + "\uD9BF\uDFFE-\uD9BF\uDFFF" + "\uD9FF\uDFFE-\uD9FF\uDFFF"
                    + "\uDA3F\uDFFE-\uDA3F\uDFFF" + "\uDA7F\uDFFE-\uDA7F\uDFFF"
                    + "\uDABF\uDFFE-\uDABF\uDFFF" + "\uDAFF\uDFFE-\uDAFF\uDFFF"
                    + "\uDB3F\uDFFE-\uDB3F\uDFFF" + "\uDB7F\uDFFE-\uDB7F\uDFFF"
                    + "\uDBBF\uDFFE-\uDBBF\uDFFF"
                    + "\uDBFF\uDFFE-\uDBFF\uDFFF]");

    /**
     * Removes all characters from the passed XML string that are not allowed
     * per definition and discouraged to be used.
     *
     * @param toClean
     *            the XML string to clean
     * @return the cleaned XML string
     */
    public static String cleanString(String toClean) {
        String tmp = INVALID_XML_CHARS.matcher(toClean).replaceAll("");
        tmp = DISCOURAGED_XML_CHARS.matcher(tmp).replaceAll("");
        return INVALID_CHARACTERS.matcher(tmp).replaceAll("");
    }
}
