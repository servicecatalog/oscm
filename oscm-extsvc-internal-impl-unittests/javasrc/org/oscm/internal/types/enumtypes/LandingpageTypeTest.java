/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Test cases for class <code>LandingpageType</code>
 * 
 * @author zankov
 * 
 */
public class LandingpageTypeTest {

    /**
     * The default landingpage type is PUBLIC
     */
    @Test
    public void getDefault() {
        assertEquals(LandingpageType.PUBLIC, LandingpageType.getDefault());
    }

    /**
     * The default landingpage type is PUBLIC
     */
    @Test
    public void isDefault() {
        assertTrue(LandingpageType.PUBLIC.isDefault());
    }

    /**
     * The default landingpage type is PUBLIC
     */
    @Test
    public void isDefault_Enterprise() {
        assertFalse(LandingpageType.isDefault("ENTERPRISE"));
    }

    /**
     * The default landingpage type is PUBLIC
     */
    @Test
    public void isDefault_UnknownValue() {
        assertFalse(LandingpageType.isDefault("UNKNOWN"));
    }

    @Test
    public void isDefault_null() {
        assertFalse(LandingpageType.isDefault(null));
    }

    /**
     * Check if the enum contains some element with given name
     */
    @Test
    public void contains_PUBLIC() {
        // given
        String name = "PUBLIC";

        // when
        boolean result = LandingpageType.contains(name);

        // expected
        assertTrue(result);
    }

    /**
     * Check if the enum contains some element with given name
     */
    @Test
    public void contains_ENTERPRISE() {
        // given
        String name = "ENTERPRISE";

        // when
        boolean result = LandingpageType.contains(name);

        // expected
        assertTrue(result);
    }

    /**
     * Check if the enum contains some element with given name
     */
    @Test
    public void contains_UNKNOWN() {
        // given
        String name = "UNKNOWN";

        // when
        boolean result = LandingpageType.contains(name);

        // expected
        assertFalse(result);
    }

    /**
     * Check if the enum contains some element with given name
     */
    @Test
    public void contains_null() {
        assertFalse(LandingpageType.contains(null));
    }

    /**
     * List the names of the enum elements
     */
    @Test
    public void names() {
        // when
        List<String> names = LandingpageType.names();

        // than
        assertTrue(names.contains("PUBLIC"));
        assertTrue(names.contains("ENTERPRISE"));
        assertFalse(names.contains("UNKNOWN"));
    }
}
