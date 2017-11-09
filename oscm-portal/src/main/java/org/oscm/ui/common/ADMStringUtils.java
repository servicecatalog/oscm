/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 03.06.2009  
 *                                                      
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Random;

/**
 * Utility class which provides helper methods around the String class.
 * 
 */
public class ADMStringUtils {

    private static final String[] RANDOM_CHARSET = { "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z" };

    /**
     * Get a random string with the request length
     * 
     * @param length
     *            of the random string
     * 
     * @returns a random string
     */
    public static String getRandomString(int len) {
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            sb = sb.append(RANDOM_CHARSET[rand.nextInt(RANDOM_CHARSET.length)]);
        }
        return sb.toString();
    }

    /**
     * Parses the string argument as a signed long with the radix 16.
     * 
     * @param str
     *            the string to parse
     * @return the long represented by the argument in decimal.
     * @throws NumberFormatException
     *             if the string does not contain a parsable long.
     */
    public static long parseUnsignedLong(String str)
            throws NumberFormatException {
        if (str.length() > 16) {
            throw new NumberFormatException();
        }
        int lowstart = str.length() - 8;
        if (lowstart <= 0)
            return Long.parseLong(str, 16);
        else
            return Long.parseLong(str.substring(0, lowstart), 16) << 32
                    | Long.parseLong(str.substring(lowstart), 16);
    }

    /**
     * Checks if a String is whitespace, empty ("") or null.
     * 
     * @param str
     *            the String to check, may be null
     * @return true if the String is null, empty or whitespace
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * Checks if a String is empty ("") or null.
     * 
     * @param str
     *            the String to check, may be null
     * @return true if the String is null or empty
     */
    public static boolean isBlankNoTrim(String str) {
        return (str == null || str.length() == 0);
    }

    public static String removeEndingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
