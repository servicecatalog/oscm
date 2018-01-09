/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 01.06.2011                                                      
 *                                                                              
 *  Completion Time: 01.06.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read config-settings. In case a folder local exists, then read properties
 * from this location.
 * 
 */
public class PropertiesReader {

    Properties props;

    /**
     * persistent config file that are used for a specific installation, but are
     * checked in. The name pattern is local-<hostname>.properties
     */
    private String configFile;

    public PropertiesReader() {
        props = new Properties();
        configFile = System.getProperty("user.dir")
                + "/../oscm-devruntime/javares/local/"
                + retrieveComputerName() + "/test.properties";
    }

    public Properties load() throws IOException {
        if (fileExists(configFile)) {
            load(configFile);
        } else {
            System.out
                    .println(" ***** WARNING: File oscm-devruntime/javares/local/<hostname>/test.properties not found!");
        }
        replacePlaceholders();
        return props;
    }

    private boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }

    private void load(String configFile) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(configFile);
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Config path:" + configFile, e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String retrieveComputerName() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.indexOf('.') > 0) {
                hostName = hostName.substring(0, hostName.indexOf('.'));
            }
            return hostName;
        } catch (UnknownHostException e) {
            return "";
        }
    }

    /**
     * Sets the environment variable values in the properties, where required.
     * 
     * @param props
     *            The properties to set the values in.
     * @return The properties with the changed settings.
     */
    private void replacePlaceholders() {
        Properties result = new Properties();
        for (Object keyValue : props.keySet()) {
            String key = (String) keyValue;
            String value = props.getProperty(key);
            Pattern pattern = Pattern.compile("[$]\\{[\\w.]+\\}");
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                String match = matcher.toMatchResult().group();
                match = match.substring(2);
                match = match.substring(0, match.length() - 1);
                if (match.startsWith("env.")) {
                    String envValue = System.getenv(match.substring(4));
                    if (envValue != null) {
                        value = matcher.replaceAll(envValue);
                    }
                }
            }
            result.setProperty(key, value);
        }
        props = result;
    }

}
