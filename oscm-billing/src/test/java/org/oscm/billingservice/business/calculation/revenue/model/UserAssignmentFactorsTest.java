/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 27.07.2010                                                      
 *                                                                              
 *  Completion Time: 27.07.2010                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import static org.oscm.test.Numbers.L1;
import static org.oscm.test.Numbers.L2;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.UsageDetails.UsagePeriod;

public class UserAssignmentFactorsTest {

    private UserAssignmentFactors factors;

    @Before
    public void setUp() throws Exception {
        factors = new UserAssignmentFactors();
    }

    @Test
    public void testAddUsageDataForUserAndRole() throws Exception {
        factors.addUsageDataForUserAndRole(L1, "xy", L1, new UsageDetails());
        Set<Long> userKeys = factors.getUserKeys();
        Assert.assertNotNull(userKeys);
        Assert.assertEquals(1, userKeys.size());
        Assert.assertEquals(1, factors.getUserAssignmentDetails(L1)
                .getRoleKeys().size());
        Assert.assertEquals("xy", factors.getUserAssignmentDetails(L1)
                .getUserId());
        Assert.assertEquals(0, factors.getBasicFactor(), 0);
    }

    @Test
    public void testAddUsageDataForUserAndRoleTwoRoles() throws Exception {
        factors.addUsageDataForUserAndRole(L1, "xy", L1, new UsageDetails());
        factors.addUsageDataForUserAndRole(L1, null, L2, new UsageDetails());
        Set<Long> userKeys = factors.getUserKeys();
        Assert.assertNotNull(userKeys);
        Assert.assertEquals(1, userKeys.size());
        Assert.assertEquals(2, factors.getUserAssignmentDetails(L1)
                .getRoleKeys().size());
        Assert.assertEquals(2, factors.getUserAssignmentDetails(L1)
                .getRoleKeys().size());
        Assert.assertEquals("xy", factors.getUserAssignmentDetails(L1)
                .getUserId());
    }

    @Test
    public void testAddUsageDataForUserAndRoleTwoUsers() throws Exception {
        factors.addUsageDataForUserAndRole(L1, "xy", L1, new UsageDetails());
        factors.addUsageDataForUserAndRole(L2, "xyz", L2, new UsageDetails());
        Set<Long> userKeys = factors.getUserKeys();
        Assert.assertNotNull(userKeys);
        Assert.assertEquals(2, userKeys.size());
        Assert.assertEquals(1, factors.getUserAssignmentDetails(L1)
                .getRoleKeys().size());
        Assert.assertEquals(1, factors.getUserAssignmentDetails(L2)
                .getRoleKeys().size());
        Assert.assertEquals("xy", factors.getUserAssignmentDetails(L1)
                .getUserId());
        Assert.assertEquals("xyz", factors.getUserAssignmentDetails(L2)
                .getUserId());
    }

    @Test
    public void testAddUsageDataForUserAndRoleNoData() throws Exception {
        Set<Long> userKeys = factors.getUserKeys();
        Assert.assertNotNull(userKeys);
        Assert.assertEquals(0, userKeys.size());
    }

    @Test
    public void testAddUsageDataForUser() throws Exception {
        UsageDetails ud = new UsageDetails();
        ud.addUsagePeriod(100, 112);
        ud.setFactor(0.25);
        factors.addUsageDataForUser(L1, null, ud);

        UsageDetails usageDetails = factors.getUserAssignmentDetails(L1)
                .getUsageDetails();
        Assert.assertNotNull(usageDetails);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(1, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(0.25, usageDetails.getFactor(), 0);
    }

    @Test
    public void testAddUsageDataForUserTwoUsers() throws Exception {
        UsageDetails ud = new UsageDetails();
        ud.addUsagePeriod(100, 112);
        ud.setFactor(0.25);
        UsageDetails ud2 = new UsageDetails();
        ud2.addUsagePeriod(150, 273);
        ud2.setFactor(0.125);
        factors.addUsageDataForUser(L1, null, ud);
        factors.addUsageDataForUser(L2, null, ud2);

        UsageDetails usageDetails = factors.getUserAssignmentDetails(L1)
                .getUsageDetails();
        Assert.assertNotNull(usageDetails);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(1, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(0.25, usageDetails.getFactor(), 0);

        usageDetails = factors.getUserAssignmentDetails(L2).getUsageDetails();
        Assert.assertNotNull(usageDetails);
        usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(1, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(150, 273), usagePeriods.get(0));
        Assert.assertEquals(0.125, usageDetails.getFactor(), 0);
    }

    @Test
    public void testAddUsageDataForUserTwoAddsForOneUser() throws Exception {
        UsageDetails ud = new UsageDetails();
        ud.addUsagePeriod(100, 112);
        ud.setFactor(0.25);
        factors.addUsageDataForUser(L1, null, ud);
        factors.addUsageDataForUser(L1, null, ud);
        UsageDetails usageDetails = factors.getUserAssignmentDetails(L1)
                .getUsageDetails();
        Assert.assertNotNull(usageDetails);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(2, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(1));
        Assert.assertEquals(0.5, usageDetails.getFactor(), 0);
    }

    @Test
    public void testAddUsageDataForUserAndRoleImpactOnUserData()
            throws Exception {
        UsageDetails ud = new UsageDetails();
        ud.addUsagePeriod(100, 112);
        ud.setFactor(0.25);
        factors.addUsageDataForUserAndRole(L1, null, L1, ud);
        UsageDetails usageDetails = factors.getUserAssignmentDetails(L1)
                .getUsageDetails(L1);
        Assert.assertNotNull(usageDetails);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(1, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(0.25, usageDetails.getFactor(), 0);

        // also validate settings for the user without role context
        usageDetails = factors.getUserAssignmentDetails(L1).getUsageDetails();
        Assert.assertNotNull(usageDetails);
        usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(1, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(0.25, usageDetails.getFactor(), 0);
    }

    @Test
    public void testSetFactorForRolesTwoUsersMultipleAdd() throws Exception {
        UsageDetails ud = new UsageDetails();
        ud.addUsagePeriod(100, 112);
        factors.addUsageDataForUserAndRole(L1, null, L1, ud);

        UsageDetails ud2 = new UsageDetails();
        ud.addUsagePeriod(120, 126);
        factors.addUsageDataForUserAndRole(L1, null, L2, ud2);

        UsageDetails ud3 = new UsageDetails();
        ud.addUsagePeriod(100, 118);
        factors.addUsageDataForUserAndRole(L2, null, L1, ud3);

        factors.getUserAssignmentDetails(L1).addRoleFactor(L1, 0.125);
        factors.getUserAssignmentDetails(L1).addRoleFactor(L1, 0.25);
        factors.getUserAssignmentDetails(L1).addRoleFactor(L2, 0.5);
        factors.getUserAssignmentDetails(L2).addRoleFactor(L1, 0.125);
        factors.getUserAssignmentDetails(L2).addRoleFactor(L1, 0.75);

        Assert.assertEquals(1.25, factors.getRoleFactors().get(L1)
                .doubleValue(), 0);
        Assert.assertEquals(0.5,
                factors.getRoleFactors().get(L2).doubleValue(), 0);
    }
}
