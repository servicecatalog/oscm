/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 29.11.2010                                                      
 *                                                                              
 *  Completion Time: 03.02.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.bundles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Class to read, sort and update property files.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PropertyFileSorter {

    private final List<String> filePaths;

    public PropertyFileSorter(String... filePaths) {
        this.filePaths = Arrays.asList(filePaths);
        this.props = new Properties();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println(String.format(
                    "Usage: java '%s' {<path-to-prop-file>}*",
                    PropertyFileSorter.class.getSimpleName()));
            return;
        }

        PropertyFileSorter sorter = new PropertyFileSorter(args);
        sorter.sortFiles();
    }

    /**
     * Reads the properties from the first specified file as master key set,
     * sorts them and writes the values back to the files, properly escaped.
     * Then reads the remaining files, sorting the properties and adding empty
     * entries for the missing keys.
     * 
     * @throws IOException
     */
    private void sortFiles() throws IOException {
        for (String filePath : filePaths) {
            readProperties(filePath);
            Map<String, String> sortedProps = sort(props);
            write(sortedProps, filePath);
        }
    }

    /**
     * Writes out the properties to a new file named '_&lt;old_source_file&gt;'.
     * 
     * @param sortedProps
     *            The properties to write out.
     * @param filePath
     *            The path to write the file to.
     * @throws IOException
     */
    private void write(Map<String, String> sortedProps, String filePath)
            throws IOException {
        File outFile = new File(filePath);
        if (outFile.exists()) {
            outFile.delete();
        }
        outFile.createNewFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            for (String key : sortedProps.keySet()) {
                String entry = String.format("%s=%s%n", key,
                        saveConvert(sortedProps.get(key), false));
                fos.write(entry.getBytes("UTF-8"));
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
            case ' ':
                if (x == 0 || escapeSpace)
                    outBuffer.append('\\');
                outBuffer.append(' ');
                break;
            case '\t':
                outBuffer.append('\\');
                outBuffer.append('t');
                break;
            case '\n':
                outBuffer.append('\\');
                outBuffer.append('n');
                break;
            case '\r':
                outBuffer.append('\\');
                outBuffer.append('r');
                break;
            case '\f':
                outBuffer.append('\\');
                outBuffer.append('f');
                break;
            case '=': // Fall through
            case ':': // Fall through
            case '#': // Fall through
            case '!':
                outBuffer.append('\\');
                outBuffer.append(aChar);
                break;
            default:
                if ((aChar < 0x0020) || (aChar > 0x007e)) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex((aChar >> 12) & 0xF));
                    outBuffer.append(toHex((aChar >> 8) & 0xF));
                    outBuffer.append(toHex((aChar >> 4) & 0xF));
                    outBuffer.append(toHex(aChar & 0xF));
                } else {
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     * 
     * @param nibble
     *            the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private Properties props;

    /**
     * Sorts the given properties alphabetically.
     * 
     * @param properties
     *            The properties to sort.
     * @return The sorted properties.
     */
    private Map<String, String> sort(Properties properties) {
        List<String> propertyKeys = new ArrayList<String>();
        for (Object propKey : properties.keySet()) {
            String key = (String) propKey;
            propertyKeys.add(key);
        }
        Collections.sort(propertyKeys);
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (String key : propertyKeys) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }

    /**
     * Reads the properties from the file.
     * 
     * @param filePath
     *            The path to the file to be read.
     * 
     * @return The properties.
     * @throws IOException
     */
    private Properties readProperties(String filePath) throws IOException {
        File inFile = new File(filePath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(inFile);
            if (props.size() > 0) {
                // set all values to an empty string
                for (Object entryKey : props.keySet()) {
                    props.put(entryKey, "TODO");
                }
            }
            props.load(fis);
            return props;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

}
