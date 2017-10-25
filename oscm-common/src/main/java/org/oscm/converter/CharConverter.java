/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 24, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

/**
 * Convert DBC (Double-Byte Character) to SBC (Single-Byte Character)
 * 
 * @author zhoum.fnst
 * 
 */
public class CharConverter {

    /**
     * DBC start character '！'
     */
    private static final char DBC_CHAR_START = 65281;

    /**
     * DBC start character '～'
     */
    private static final char DBC_CHAR_END = 65374;

    /**
     * The offset between SBC and DBC visible character, except the blank
     */
    private static final int CONVERT_OFFSET = 65248;

    /**
     * DBC blank, no offset, must be handled separately.
     */
    private static final char DBC_SPACE = 12288;

    /**
     * SBC blank
     */
    private static final char SBC_SPACE = ' ';

    /**
     * Convert DBC to SBC, only handle DBC blank and characters from '！' to '～'
     * 
     * @param src
     *            String with DBC characters.
     * @return String with SBC characters.
     */
    public static String convertToSBC(String src) {
        if (src == null) {
            return src;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < src.length(); i++) {
            if (ca[i] >= DBC_CHAR_START && ca[i] <= DBC_CHAR_END) {
                buf.append((char) (ca[i] - CONVERT_OFFSET));
            } else if (ca[i] == DBC_SPACE) {
                buf.append(SBC_SPACE);
            } else {
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }
}
