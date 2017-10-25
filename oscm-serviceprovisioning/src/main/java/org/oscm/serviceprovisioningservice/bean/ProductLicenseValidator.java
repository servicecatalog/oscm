/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.oscm.domobjects.TechnicalProduct;

/**
 * Class to validate the update of a technical product license.
 * 
 */
public class ProductLicenseValidator {

    /**
     * Checks that no "usable" subscription exists for a technical product if
     * the oldLiceseText and the newLicenseText are not equal. If the old
     * License is <code>null</code>, no license has been set for the current
     * user's locale and an update is possible. Hence this will not cause an
     * exception.
     * 
     * @param techProduct
     *            The technical product.
     * @param oldLicenseText
     *            The old license text, prior to changes.
     * @param newLicenseText
     *            The potentially modified license text.
     */
    public static void validate(TechnicalProduct techProduct,
            String oldLicenseText, String newLicenseText) {
        if (techProduct == null) {
            return;
        }
        if (oldLicenseText == null) {
            return;
        }
        if (equalsContent(oldLicenseText, newLicenseText)) {
            return;
        }
        if (oldLicenseText.equals(newLicenseText)) {
            return;
        }
    }

    /**
     * Compare two strings for content only. Ignore all formatting.
     * 
     * @param oldLicenseText
     *            First string to compare.
     * @param newLicenseText
     *            Second string to compare.
     * @return result of comparison.
     */
    public static boolean equalsContent(String oldLicenseText,
            String newLicenseText) {
        String contentOldLicenseText;
        String contentNewLicenseText;

        contentOldLicenseText = getContent(oldLicenseText);
        contentNewLicenseText = getContent(newLicenseText);

        return contentOldLicenseText.equalsIgnoreCase(contentNewLicenseText);
    }

    /**
     * Get only content without tags and formatting elements.
     * 
     * @param string
     *            String for processing.
     * @return String content.
     */
    private static String getContent(String string) {
        if (string != null) {
            string = removeLineSeparator(string);

            // remove all html tags, i.e. every construction like <...>
            string = string.replaceAll("\\<.*?>", "");
            string = string.replace("&nbsp;", "");

            string = string.trim();
            string = removeRepeatedBlankSpaces(string);
            string = changeSpecialSequences(string);
            string = string.trim();
        }
        return string;
    }

    /**
     * Remove repeating blank symbols.
     * 
     * @param inputString
     *            String for processing.
     * @return Processed string.
     */
    private static String removeRepeatedBlankSpaces(String inputString) {
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (i < inputString.length()) {
            char ch = inputString.charAt(i);
            if (ch == ' ') {
                buffer.append(' ');
                while ((i < inputString.length())
                        && (inputString.charAt(i) == ch)) {
                    i++;
                }
            } else {
                buffer.append(ch);
                i++;
            }
        }
        return buffer.toString();
    }

    /**
     * Remove line separators.
     * 
     * @param inputString
     *            string for processing.
     * @return String without separators.
     */
    private static String removeLineSeparator(String inputString) {
        inputString = inputString.replace("\n\r", " ");
        inputString = inputString.replace(System.getProperty("line.separator"),
                " ");
        inputString = inputString.replace("\n", " ");
        inputString = inputString.replace("\t", " ");
        inputString = inputString.replace("\r", " ");

        return inputString;
    }

    /**
     * Change special sequences. For example "&sect;" has to be changed to "§".
     * 
     * @param inputString
     *            string for processing.
     * @return String with changed content.
     */
    private static String changeSpecialSequences(String inputString) {
        inputString = inputString.replace("&sect;", "§");

        return inputString;
    }

}
