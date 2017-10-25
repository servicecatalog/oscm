/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Christian Brandstetter                                         
 *                                                                              
 *  Creation Date: 09.12.2011                                                     
 *                                                                              
 *  Completion Time: 09.12.2011                                   
 *                                                                              
 *******************************************************************************/
package org.oscm.converter;

/**
 * The white space converter converts all white space characters (e.g. \u3000)
 * into a plain space characters ( ).
 */
public class WhiteSpaceConverter {

    /**
     * Convert all white space characters into plain space characters.<br>
     * Note: value.replaceAll("\\s", " ") does not work for e.g. \u3000 space!
     */
    static public String replace(String value) {

        String result = new String();

        if (value != null) {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);

                if (Character.isWhitespace(c)) {
                    result = result.concat(" ");
                } else {
                    result = result.concat(Character.toString(c));
                }
            }
        }
        return result;
    }
}
