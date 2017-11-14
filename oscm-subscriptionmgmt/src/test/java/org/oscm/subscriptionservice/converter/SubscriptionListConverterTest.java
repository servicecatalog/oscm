/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.converter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;

/**
 * @author weiser
 * 
 */
public class SubscriptionListConverterTest {

    private SubscriptionListConverter slc;

    @Before
    public void setup() {
        slc = new SubscriptionListConverter();
    }

    @Test
    public void convert_Null() {
        List<SubscriptionWithRoles> list = slc.convert(null);

        assertEquals(new ArrayList<SubscriptionWithRoles>(), list);
    }

    @Test
    public void convert() {
        List<Object[]> result = new ArrayList<Object[]>();
        result.addAll(createSubscription(1, 2));
        result.addAll(createSubscription(2, 0));
        result.addAll(createSubscription(3, 1));

        List<SubscriptionWithRoles> list = slc.convert(result);

        assertEquals(3, list.size());

        verify(list.get(0), 1, 2);
        verify(list.get(1), 2, 0);
        verify(list.get(2), 3, 1);
    }

    private static void verify(SubscriptionWithRoles sub, int subKey,
            int numOfRoles) {
        assertEquals(subKey, sub.getSubscription().getKey());
        assertEquals(numOfRoles, sub.getRoles().size());

        for (int i = 0; i < numOfRoles; i++) {
            RoleDefinition role = sub.getRoles().get(i);
            assertEquals(sub.getSubscription().getSubscriptionId() + "-" + i,
                    role.getRoleId());
        }
    }

    private static List<Object[]> createSubscription(long subkey, int numOfRoles) {
        List<Object[]> result = new ArrayList<Object[]>();
        Subscription sub = new Subscription();
        sub.setKey(subkey);
        sub.setSubscriptionId(String.valueOf(subkey));

        if (numOfRoles > 0) {
            for (int i = 0; i < numOfRoles; i++) {
                Object[] a = new Object[2];
                a[0] = sub;

                RoleDefinition role = new RoleDefinition();
                role.setRoleId(sub.getSubscriptionId() + "-"
                        + String.valueOf(i));
                role.setKey(i);
                a[1] = role;
                result.add(a);
            }
        } else {
            Object[] a = new Object[2];
            a[0] = sub;
            result.add(a);
        }
        return result;
    }
}
