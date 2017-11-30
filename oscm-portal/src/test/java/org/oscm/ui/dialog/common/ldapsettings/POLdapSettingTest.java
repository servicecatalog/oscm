/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.usermanagement.POLdapSetting;

public class POLdapSettingTest {

    private POLdapSetting value1;

    @Before
    public void setup() {
        value1 = new POLdapSetting("key", "value", false);
    }

    @Test
    public void equals_Same() {
        assertTrue(value1.equals(value1));
    }

    @Test
    public void equals_EqualFieldValues() {
        assertTrue(value1.equals(new POLdapSetting("key", "value", false)));
    }

    @Test
    public void equals_EqualKey() {
        assertTrue(value1.equals(new POLdapSetting("key", "value1", true)));
    }

    @Test
    public void equals_DifferentKey() {
        assertFalse(value1.equals(new POLdapSetting("key1", "value", false)));
    }

    @Test
    public void equals_OtherType() {
        assertFalse(value1.equals("bla"));
    }

    @Test
    public void equals_Null() {
        assertFalse(value1 == null);
    }

    @Test
    public void hashCode_NullKey() {
        value1.setSettingKey(null);
        assertEquals(0, value1.hashCode());
    }

    @Test
    public void hashCode_Same() {
        assertEquals(value1.hashCode(), value1.hashCode());
    }

    @Test
    public void hashCode_EqualKeys() {
        assertEquals(value1.hashCode(),
                new POLdapSetting("key", "value", false).hashCode());
    }

    @Test
    public void hashCode_DifferentKeys() {
        assertFalse(value1.hashCode() == new POLdapSetting("key2", "value",
                false).hashCode());
    }

}
