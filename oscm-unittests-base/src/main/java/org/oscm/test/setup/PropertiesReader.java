/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 01.06.2011                                                      
 *                                                                              
 *  Completion Time: 01.06.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.test.setup;

import java.io.File;
import java.io.FileInputStream;
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

    private Properties testProperties;
    private Properties appDbProperties;

    /**
     * Persistent config file that are used for a specific installation, but are
     * checked in. The name pattern is
     * oscm-devruntime/javares/local/<hostname>/xxx.properties
     */
    private String fileTestProperties;
    private String fileAppDbProperties;

    public PropertiesReader() {
        testProperties = new Properties();
        appDbProperties = new Properties();

        fileTestProperties = System.getProperty("user.dir")
                + "/../oscm-devruntime/javares/local/"
                + retrieveComputerName() + "/test.properties";
        fileAppDbProperties = System.getProperty("user.dir")
                + "/../oscm-devruntime/javares/local/"
                + retrieveComputerName() + "/db-app.properties";
    }

    public Properties load() throws Exception {
        if (fileExists(fileTestProperties)) {
            load(fileTestProperties, testProperties);
        } else {
            System.out
                    .println(" ***** WARNING: File oscm-devruntime/javares/local/<hostname>/test.properties not found!");
        }
        replacePlaceholders();
        return testProperties;
    }

    private boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }

    private void load(String configFile, Properties properties) {
        try (FileInputStream is = new FileInputStream(configFile);) {
            properties.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Config path:" + configFile, e);
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
        for (Object keyValue : testProperties.keySet()) {
            String key = (String) keyValue;
            String value = testProperties.getProperty(key);
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
        testProperties = result;
    }

    public Properties loadAppDbProperties() throws Exception {
        if (fileExists(fileAppDbProperties)) {
            load(fileAppDbProperties, appDbProperties);
            return appDbProperties;
        }

        throw new RuntimeException(
                "WARNING: File oscm-devruntime/javares/local/<hostname>/db-app.properties not found!");
    }
}
