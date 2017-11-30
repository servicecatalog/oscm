/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 02.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.resources;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

/**
 * @author Sdehn
 * 
 */
public class MissingKeysMessagesTest {

    /**
     * Load properties for UI in Locale.ENGLISH and check if all keys also exist
     * in Locale.GERMAN and Locale.JAPANESE
     */
    @Test
    public void printMissingKeys() throws Exception {

        final Properties prop_en = new Properties();
        final Properties prop_de = new Properties();
        final Properties prop_jp = new Properties();
        InputStream input = null;

        input = DefaultMessages.openUiMessages(Locale.ENGLISH);
        if (input == null) {
            fail("Cannot find " + Locale.ENGLISH);
        }

        prop_en.load(input);

        input = DefaultMessages.openUiMessages(Locale.GERMAN);
        if (input == null) {
            fail("Cannot find " + Locale.GERMAN);
        }
        prop_de.load(input);

        input = DefaultMessages.openUiMessages(Locale.JAPANESE);
        if (input == null) {
            fail("Cannot find " + Locale.JAPANESE);
        }
        prop_jp.load(input);
        
        final Set<String> allProperties = getUnionSet(prop_en, prop_de,
                prop_jp);

        final Iterator<String> i = allProperties.iterator();
        final List<String> missingKeys = new ArrayList<String>();
        while (i.hasNext()) {
            String key = i.next();
            String value_de = prop_de.getProperty(key);
            if (value_de == null) {
                missingKeys.add(key + " (GERMAN)");

            }
            String value_jp = prop_jp.getProperty(key);
            if (value_jp == null) {
                missingKeys.add(key + " (JAPANESE)");

            }
            String value_en = prop_en.getProperty(key);
            if (value_en == null) {
                missingKeys.add(key + " (ENGLISH)");
            }
        }

        assertTrue("Following message keys were not found: " + missingKeys,
                missingKeys.isEmpty());

    }

    Set<String> getUnionSet(Properties prop_en, Properties prop_de,
            Properties prop_jp) {
        final Set<String> allProperties = new HashSet<String>();
        for (Enumeration<?> e = prop_en.propertyNames(); e.hasMoreElements();) {
            allProperties.add((String) e.nextElement());
        }
        for (Enumeration<?> e = prop_de.propertyNames(); e.hasMoreElements();) {
            allProperties.add((String) e.nextElement());
        }
        for (Enumeration<?> e = prop_jp.propertyNames(); e.hasMoreElements();) {
            allProperties.add((String) e.nextElement());
        }
        return allProperties;
    }

}
