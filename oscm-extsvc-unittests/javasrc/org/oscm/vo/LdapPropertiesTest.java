/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 17.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Test;

public class LdapPropertiesTest {

    @Test
    public void addProperty() {
        LdapProperties lp = new LdapProperties();
        lp.setProperty("key1", "value1");

        assertEquals(1, lp.getSettings().size());
        assertEquals("value1", lp.getProperty("key1"));
    }

    @Test
    public void addProperties() {
        LdapProperties lp = new LdapProperties();
        lp.setProperty("key1", "value1");
        lp.setProperty("key2", "value2");

        assertEquals(2, lp.getSettings().size());
        assertEquals("value1", lp.getProperty("key1"));
        assertEquals("value2", lp.getProperty("key2"));
    }

    @Test
    public void addPropertyTwice() {
        LdapProperties lp = new LdapProperties();
        lp.setProperty("key1", "value1");
        lp.setProperty("key1", "value2");

        assertEquals(1, lp.getSettings().size());
        assertEquals("value2", lp.getProperty("key1"));
    }

    @Test
    public void addNullValue() {
        LdapProperties lp = new LdapProperties();
        lp.setProperty("key1", null);

        assertEquals(1, lp.getSettings().size());
        assertNull(lp.getProperty("key1"));
    }

    @Test
    public void asProperties() {
        LdapProperties lp = new LdapProperties();
        lp.setProperty("key1", "value1");
        lp.setProperty("key2", "value2");

        Properties properties = lp.asProperties();
        assertEquals(2, properties.size());
        assertEquals("value1", properties.getProperty("key1"));
        assertEquals("value2", properties.getProperty("key2"));
    }

    @Test
    public void getNull() {
        assertNull(LdapProperties.get(null));
    }

    @Test
    public void getProperties() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");

        LdapProperties ldapProperties = LdapProperties.get(p);
        assertNotNull(ldapProperties);
        assertEquals("value1", ldapProperties.getProperty("key1"));
    }

}
