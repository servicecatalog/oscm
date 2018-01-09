/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscm.converter.ResourceLoader;
import org.oscm.string.Strings;

/**
 * Warning: a property to be replaced must be in one line.
 * 
 * @author kulle
 * 
 */
public class TechnicalServiceReader {

    private Pattern pattern = Pattern.compile("\\^(.+?)\\^");
    private Properties properties;

    public TechnicalServiceReader(Properties properties) {
        this.properties = properties;
    }

    public byte[] loadTechnicalService(String filename) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] lineBytes;
        try (InputStream inputStream = ResourceLoader.getResourceAsStream(
                getClass(), filename);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));) {
            String line = null;
            String newLine = null;
            while ((line = reader.readLine()) != null) {
                newLine = replaceProperties(line);
                lineBytes = newLine.getBytes("UTF-8");
                outputStream.write(lineBytes);
            }
        }
        return outputStream.toByteArray();
    }

    String replaceProperties(String line) throws Exception {
        String newLine = line;
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String propertyToReplace = matcher.group(1);
            String property = properties.getProperty(propertyToReplace);
            if (property != null) {
                newLine = Strings.replaceSubstring(matcher.start(),
                        matcher.end() - 1, line, property);
                newLine = replaceProperties(newLine);
            }
        }
        return newLine;
    }

}
