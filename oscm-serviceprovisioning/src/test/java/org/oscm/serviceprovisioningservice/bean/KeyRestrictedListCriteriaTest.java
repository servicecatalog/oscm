/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 20, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dirk Bernsau
 * 
 */
public class KeyRestrictedListCriteriaTest {
    @Test
    public void testKeyRestriction_Null() throws Throwable {
        KeyRestrictedListCriteria criteria = new KeyRestrictedListCriteria(null);
        Assert.assertFalse("null is not a restriction", criteria.isRestricted());
        Assert.assertEquals("null is not a restriction - ", "",
                criteria.getRestrictionString());
    }

    @Test
    public void testKeyRestriction_Empty() throws Throwable {
        KeyRestrictedListCriteria criteria = new KeyRestrictedListCriteria(
                new HashSet<Long>());
        Assert.assertFalse("empty set is not a restriction",
                criteria.isRestricted());
        Assert.assertEquals("empty set is not a restriction - ", "",
                criteria.getRestrictionString());
    }

    @Test
    public void testKeyRestriction() throws Throwable {
        HashSet<Long> set = new HashSet<Long>();
        set.add(Long.valueOf(1));
        KeyRestrictedListCriteria criteria = new KeyRestrictedListCriteria(set);
        Assert.assertTrue("restriction expected", criteria.isRestricted());
        Assert.assertEquals("Wrong string - ", "1",
                criteria.getRestrictionString());
    }

    @Test
    public void testKeyRestriction_Multi() throws Throwable {
        HashSet<Long> set = new HashSet<Long>();
        set.add(Long.valueOf(1));
        set.add(Long.valueOf(2));
        set.add(Long.valueOf(3));
        KeyRestrictedListCriteria criteria = new KeyRestrictedListCriteria(set);
        Assert.assertTrue("restriction expected", criteria.isRestricted());
        String[] split = criteria.getRestrictionString().split(",");
        Assert.assertEquals("Wrong string - ", set.size(), split.length);
        for (int i = 0; i < split.length; i++) {
            set.remove(Long.valueOf(split[i]));
        }
        Assert.assertTrue("Not all keys in string", set.isEmpty());
    }
}
