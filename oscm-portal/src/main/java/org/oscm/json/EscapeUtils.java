/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: stavreva                                                      
 *                                                                              
 *  Creation Date: 09.01.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.json;

import java.text.StringCharacterIterator;

public class EscapeUtils {

    public static String escapeJSON(String aText) {
        if (aText == null) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != StringCharacterIterator.DONE) {
            if (character == '\"') {
                result.append("\\\"");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '/') {
                result.append("\\/");
            } else if (character == '\b') {
                result.append("\\b");
            } else if (character == '\f') {
                result.append("\\f");
            } else if (character == '\n') {
                result.append("\\n");
            } else if (character == '\r') {
                result.append("\\r");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                // the char is not a special one
                // add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static String unescapeJSON(String aText) {
        if (aText == null) {
            return null;
        }
        String result = aText;
        result = result.replace("\\\"", "\"");
        result = result.replace("\\\\", "\\");
        result = result.replace("\\/", "/");
        result = result.replace("\\b", "\b");
        result = result.replace("\\f", "\f");
        result = result.replace("\\n", "\n");
        result = result.replace("\\r", "\r");
        result = result.replace("\\t", "\t");
        return result;
    }
}
