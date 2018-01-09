/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 26.05.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test of configuration bean
 */
public class ConfigurationItemTest {

    @Test
    public void testGetConfigurationItem() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key", "value");

        assertEquals("key", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("key", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertFalse(item.isDirty());
        assertFalse(item.isPasswordField());
        assertFalse(item.isReadOnly());
    }

    @Test
    public void testGetConfigurationItemPassword() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key_PWD", "value");

        assertEquals("key_PWD", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("key_PWD", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertFalse(item.isDirty());
        assertTrue(item.isPasswordField());
        assertFalse(item.isReadOnly());
    }

    @Test
    public void testGetConfigurationItemPassword_SUFFIX_PASS() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key_PASS", "value");

        assertEquals("key_PASS", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("key_PASS", item.getDisplayName());
        assertFalse(item.isDirty());
        assertTrue(item.isPasswordField());
        assertFalse(item.isReadOnly());
    }

    @Test
    public void testGetConfigurationItemReadOnly() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key", "value");
        item.setReadOnly(true);

        assertEquals("key", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("key", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertFalse(item.isDirty());
        assertFalse(item.isPasswordField());
        assertTrue(item.isReadOnly());
    }

    @Test
    public void testGetConfigurationItemDisplayNames() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key", "value");
        item.setDisplayName("display");
        item.setTooltip("tooltip");

        assertEquals("key", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("display", item.getDisplayName());
        assertEquals("tooltip", item.getTooltip());
        assertFalse(item.isDirty());
        assertFalse(item.isPasswordField());
        assertFalse(item.isReadOnly());
    }

    @Test
    public void testGetConfigurationItemDirty() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key", "value");
        item.setDisplayName("display");

        assertEquals("key", item.getKey());
        assertEquals("value", item.getValue());
        assertEquals("display", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertFalse(item.isDirty());

        item.setValue("new_value");

        assertEquals("key", item.getKey());
        assertEquals("new_value", item.getValue());
        assertEquals("display", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertTrue(item.isDirty());
    }

    @Test
    public void testGetConfigurationItemNullValue() throws Exception {
        ConfigurationItem item = new ConfigurationItem("key", null);
        assertEquals("key", item.getKey());
        assertEquals("", item.getValue());
        assertEquals("key", item.getDisplayName());
        assertEquals("", item.getTooltip());
        assertFalse(item.isDirty());
        assertFalse(item.isPasswordField());
        assertFalse(item.isReadOnly());
    }

}
