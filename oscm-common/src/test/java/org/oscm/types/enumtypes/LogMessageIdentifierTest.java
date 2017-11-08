/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 31.10.2011                                                      
 *                                                                              
 *  Completion Time: 31.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.ResourceLoader;

/**
 * Tests if the enum and the properties file for the log messages are in sync.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class LogMessageIdentifierTest {

    private List<String> enumMessageKeys;
    private List<String> propfileMessageKeys;

    @Before
    public void setup() throws Exception {
        propfileMessageKeys = new ArrayList<String>();
        enumMessageKeys = new ArrayList<String>();
        for (LogMessageIdentifier entry : LogMessageIdentifier.values()) {
            enumMessageKeys.add(entry.getMsgId());
        }
        Properties props = new Properties();
        try (InputStream is = ResourceLoader.getResourceAsStream(getClass(),
                "LogMessages.properties")) {
            props.load(is);
        }
        Set<String> keySet = ParameterizedTypes.set(props.keySet(),
                String.class);
        propfileMessageKeys.addAll(keySet);
    }

    @Test
    public void missingPropKeys() throws Exception {
        Set<String> missingKeys = new HashSet<String>();
        for (String enumKey : enumMessageKeys) {
            if (isMissingKey(enumKey, propfileMessageKeys)) {
                missingKeys.add(enumKey);
            }
        }
        assertTrue(missingKeys.size()
                + " missing keys in log message properties file: "
                + missingKeys.toString(), missingKeys.isEmpty());
    }

    @Test
    public void obsoletePropKeys() throws Exception {
        Set<String> obsoleteKeys = new HashSet<String>();
        for (String propKey : propfileMessageKeys) {
            if (isMissingKey(propKey, enumMessageKeys)) {
                obsoleteKeys.add(propKey);
            }
        }
        assertTrue("Obsolete keys in log message properties file are: "
                + obsoleteKeys.toString(), obsoleteKeys.isEmpty());
    }

    @Test
    public void obsoletePropKeys_add() throws Exception {
        Set<String> obsoleteKeys = new HashSet<String>();
        propfileMessageKeys.add("YYY");
        for (String propKey : propfileMessageKeys) {
            if (isMissingKey(propKey, enumMessageKeys)) {
                obsoleteKeys.add(propKey);
            }
        }
        assertFalse("Obsolete keys in log message properties file are: "
                + obsoleteKeys.toString(), obsoleteKeys.isEmpty());
    }

    /**
     * Checks if the key is contained in the list.
     * 
     * @param key
     *            The key to look for.
     * @param listToCheck
     *            The list to check.
     * @return <code>true</code> if the key is not contained in the list,
     *         <code>false</code> otherwise.
     */
    private boolean isMissingKey(String key, List<String> listToCheck) {
        boolean isUserOpLogKey = Pattern.matches("3\\d\\d\\d\\d", key);
        boolean isDebugKey = "00000".equals(key);
        boolean isErrorKey = "-00001".equals(key);
        return !(listToCheck.contains(key) || isUserOpLogKey || isDebugKey || isErrorKey);
    }
}
