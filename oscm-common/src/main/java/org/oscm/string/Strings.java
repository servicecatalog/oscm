/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.string;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Utility class for working with strings.
 */
public class Strings {

    /**
     * Shortens the given text to the given maximum number of characters. The
     * shortened text is appended with dots.
     * 
     * @param text
     *            The text to shorten
     * @param maxNumberOfChars
     *            Max length of the returned string
     * @return String
     */
    public static String shortenText(String text, int maxNumberOfChars) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.length() > maxNumberOfChars) {
            return trimmedText.substring(0, maxNumberOfChars - 3) + "..."; //$NON-NLS-1$
        }
        return trimmedText;
    }

    /**
     * Encodes the given string as bytes in UTF-8 encoding.
     * 
     * @param value
     *            Value to encode
     * @return byte[]
     */
    public static byte[] toBytes(String value) {
        return doToBytes(value, "UTF-8"); //$NON-NLS-1$
    }

    static byte[] doToBytes(String value, String encoding) {
        try {
            return value.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(
                    "UTF-8 encoding is not supported", e); //$NON-NLS-1$
        }
    }

    /**
     * Constructs a new string by decoding the given bytes using UTF-8. <br>
     * <br>
     * WARNING: This method cannot be applied to random bytes. The result might
     * be crippled in this case. This method may only be used if the given bytes
     * have been encoded from a string. <br>
     * 
     * @param value
     *            Value to decode
     * @return String
     */
    public static String toString(byte[] value) {
        return doToString(value, "UTF-8");//$NON-NLS-1$     
    }

    static String doToString(byte[] value, String encoding) {
        try {
            return new String(value, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(
                    "UTF-8 encoding is not supported", e); //$NON-NLS-1$
        }
    }

    /**
     * Checks if the provided value is null or trimmed empty
     * 
     * @param value
     *            the {@link String} to check
     * @return <code>true</code> if empty otherwise false
     */
    public static boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    /**
     * Joins the elements of the provided collection into a single
     * <code>String</code> containing the provided elements. No delimiter is
     * added before or after the list. A <code>null</code> separator is the same
     * as an empty <code>String</code> ("").
     * 
     * @param collection
     *            the Collection of values to join together, may be null
     * @param separator
     *            the separator string to use
     * @return the joined <code>String</code>, <code>null</code> if null
     *         iterator input
     * @see org.apache.commons.lang.StringUtils#join()
     */
    public static String join(Collection<?> collection, String separator) {
        if (collection == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        StringBuffer buff = new StringBuffer();
        for (Object object : collection) {
            if (buff.length() > 0) {
                buff.append(separator);
            }
            buff.append(object);
        }
        return buff.toString();
    }

    /**
     * Returns if the two strings are equal. In case both are <code>null</code>,
     * they are also considered to be equal.
     * 
     * @param s1
     *            String 1.
     * @param s2
     *            String 2.
     * @return <code>true</code> in case the strings are equal,
     *         <code>false</code> otherwise.
     */
    public static boolean areStringsEqual(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    /**
     * Returns a string whose first character is upper case, following
     * characters of the given string are untouched. Given null or empty strings
     * are returned as they are.
     */
    public static String firstCharToUppercase(String word) {
        if (word == null || word.length() == 0) {
            return word;
        }

        return word.substring(0, 1).toUpperCase().concat(word.substring(1));
    }

    /**
     * Reads a text file and converts it into a String object.
     * 
     * @param path
     *            the path to the text file
     * @return string object representing the text file
     * @throws IOException
     *             if an I/O error occurs reading from the stream
     */
    public static String textFileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded))
                .toString();
    }

    /**
     * Returns an empty string in case of null, otherwise the same string.
     * 
     * @param str
     * @return string object
     */
    public static String nullToEmpty(String str) {
        if (str == null) {
            return "";
        }

        return str;
    }

    /**
     * Replaces a substring, from and to are included.
     */
    public static String replaceSubstring(int from, int to, String str,
            String value) {

        String result = null;

        if ((str != null) && (value != null) && (from >= 0) && (from <= to)
                && (to < str.length())) {

            String head = null;
            String tail = null;

            head = str.substring(0, from);
            tail = str.substring(to + 1, str.length());

            result = head + value + tail;
        }
        return result;
    }
}
