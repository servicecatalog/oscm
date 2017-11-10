/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Test;

public class CustomerEventDataIT {

    private final long subscriptiontkey1 = 100L;
    private final long subscriptiontkey2 = 200L;

    @Test
    public void testEqualityPositive() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setLocale(Locale.ENGLISH.toString());

        CustomerEventData data2 = new CustomerEventData();
        data2.setLocale(Locale.ENGLISH.toString());

        assertEquals(data1, data2);
    }

    @Test
    public void testEqualityNegative() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setLocale(Locale.ENGLISH.toString());

        CustomerEventData data2 = new CustomerEventData();
        data2.setLocale(Locale.GERMAN.toString());

        assertEquals(data1, data2);
    }

    @Test
    public void testHashCodeDifferentObj() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setLocale(Locale.ENGLISH.toString());

        CustomerEventData data2 = new CustomerEventData();
        data2.setLocale(Locale.GERMAN.toString());

        assertFalse(data1.hashCode() == data2.hashCode());
    }

    @Test
    public void testHashCodeDifferentEqualObj() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setLocale(Locale.ENGLISH.toString());

        CustomerEventData data2 = new CustomerEventData();
        data2.setLocale(Locale.ENGLISH.toString());

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    public void testHashCodeSameObj() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setLocale(Locale.ENGLISH.toString());
        CustomerEventData data2 = data1;

        assertSame("the two objects should be the same", data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    public void testSubscriptiontkeyHashCodeSameObj() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setSubscriptiontkey(new BigDecimal(subscriptiontkey1));

        CustomerEventData data2 = new CustomerEventData();
        data2.setSubscriptiontkey(new BigDecimal(subscriptiontkey1));

        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    public void testSubscriptiontkeyHashCodeDifferentObj() {
        CustomerEventData data1 = new CustomerEventData();
        data1.setSubscriptiontkey(new BigDecimal(subscriptiontkey1));

        CustomerEventData data2 = new CustomerEventData();
        data2.setSubscriptiontkey(new BigDecimal(subscriptiontkey2));

        assertFalse(data1.hashCode() == data2.hashCode());
    }
}
