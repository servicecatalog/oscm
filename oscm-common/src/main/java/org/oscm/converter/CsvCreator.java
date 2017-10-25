/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Aug 5, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 5, 2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

/**
 * Utility class to create CSV form string
 * 
 * @author tokoda
 * 
 */
public class CsvCreator {

    private static final String SEPARATOR = ",";
    private static final String ESCAPE_SEQUENCE = "\"";
    private static final String DOUBLE_ESCAPE_SEQUENCE = "\"\"";
    private static final String NEWLINE = "\n";

    /**
     * Return the CSV form string line from the String array
     * 
     * @param line
     *            the String array for one CSV line
     * @return the CSV form string
     */
    public static String createCsvLine(String[] line) {
        if(line == null)
            return null;
        
        boolean isFirst = true;
        StringBuffer csvLine = new StringBuffer("");
        for (String token : line) {
            if (isFirst) {
                isFirst = false;
            } else {
                csvLine.append(SEPARATOR);
            }
            csvLine.append(createCsvToken(token));
        }
        return csvLine.toString();
    }

    private static String createCsvToken(final String value) {
        if (value == null || value.length() == 0)
            return "";

        String csvToken = value;
        if (csvToken.indexOf(SEPARATOR) >= 0
                || csvToken.indexOf(ESCAPE_SEQUENCE) >= 0
                || csvToken.indexOf(NEWLINE) >= 0) {
            csvToken = csvToken.replaceAll(ESCAPE_SEQUENCE,
                    DOUBLE_ESCAPE_SEQUENCE);
            csvToken = ESCAPE_SEQUENCE + csvToken + ESCAPE_SEQUENCE;
        }
        return csvToken;
    }
}
