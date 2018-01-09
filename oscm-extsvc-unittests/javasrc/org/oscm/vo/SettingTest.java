/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 17.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingTest {

    @Test
    public void equalsSameObject() {
        Setting s = new Setting();
        s.setKey("key");
        assertTrue(s.equals(s));
    }

    @Test
    public void equalsOtherObject() {
        Setting s = new Setting();
        s.setKey("key");

        Setting o = new Setting();
        o.setKey("key");

        assertTrue(s.equals(o));
    }

    @Test
    public void equalsOtherObjectNull() {
        Setting s = new Setting();
        s.setKey("key");

        Setting o = null;

        assertFalse(s.equals(o));
    }

    @Test
    public void equalsOtherType() {
        Setting s = new Setting();
        String o = "string";
        assertFalse(s.equals(o));
    }

    @Test
    public void equalsOtherValue() {
        Setting s = new Setting();
        s.setKey("key1");
        s.setValue("value");

        Setting o = new Setting();
        o.setKey("key2");
        o.setValue("value");

        assertFalse(s.equals(o));
    }

    @Test
    public void equalsNull() {
        Setting s = new Setting();
        s.setKey("key");
        assertFalse(s == null);
    }

    @Test
    public void hashcodeTest() {
        Setting s = new Setting();
        assertEquals(s.hashCode(), 0);
    }

    @Test
    public void hashCodeSameObject() {
        Setting s = new Setting();
        assertEquals(s.hashCode(), s.hashCode());
    }

    @Test
    public void hashCodeObjects() {
        Setting s = new Setting();
        s.setKey("key1");

        Setting o = new Setting();
        o.setKey("key2");

        assertTrue(s.hashCode() != o.hashCode());
    }

}
