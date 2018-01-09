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

public class VOMarketplaceTest {

    @Test
    public void equalsSameObject() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mpid");
        assertTrue(mp.equals(mp));
    }

    @Test
    public void equalsOtherObject() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mpid");

        VOMarketplace mp2 = new VOMarketplace();
        mp2.setMarketplaceId("mpid");

        assertTrue(mp.equals(mp2));
    }

    @Test
    public void equalsOtherObjectNull() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mpid");

        VOMarketplace mp2 = null;

        assertFalse(mp.equals(mp2));
    }

    @Test
    public void equalsOtherType() {
        VOMarketplace mp = new VOMarketplace();
        String o = "string";
        assertFalse(mp.equals(o));
    }

    @Test
    public void equalsOtherValue() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mpid");
        mp.setName("name");

        VOMarketplace mp2 = new VOMarketplace();
        mp2.setMarketplaceId("mpid2");
        mp2.setName("name");

        assertFalse(mp.equals(mp2));
    }

    @Test
    public void equalsNull() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mpid");
        assertFalse(mp == null);
    }

    @Test
    public void equalsMarketplaceIdNull() {
        VOMarketplace mp1 = new VOMarketplace();
        mp1.setMarketplaceId(null);

        VOMarketplace mp2 = new VOMarketplace();
        mp2.setMarketplaceId("mpid");

        assertFalse(mp1.equals(mp2));
    }

    @Test
    public void equalsBothMarketplaceIdNull() {
        VOMarketplace mp1 = new VOMarketplace();
        mp1.setMarketplaceId(null);
        mp1.setName("1");

        VOMarketplace mp2 = new VOMarketplace();
        mp2.setMarketplaceId(null);
        mp2.setName("2");

        assertFalse(mp1.equals(mp2));
    }

    @Test
    public void hashcodeTest() {
        VOMarketplace mp = new VOMarketplace();
        assertEquals(mp.hashCode(), 0);
    }

    @Test
    public void hashCodeSameObject() {
        VOMarketplace s = new VOMarketplace();
        assertEquals(s.hashCode(), s.hashCode());
    }

    @Test
    public void hashCodeObjects() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mp1");

        VOMarketplace mp2 = new VOMarketplace();
        mp2.setMarketplaceId("mp2");

        assertTrue(mp.hashCode() != mp2.hashCode());
    }

}
