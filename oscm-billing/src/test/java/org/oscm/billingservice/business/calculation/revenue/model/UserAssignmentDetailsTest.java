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

public class UserAssignmentDetailsTest {

    private UserAssignmentDetails details;
    private UserAssignmentFactors userAssignmentFactors;

    @Before
    public void setUp() throws Exception {
        userAssignmentFactors = new UserAssignmentFactors();
        details = new UserAssignmentDetails(userAssignmentFactors);
    }

    @Test
    public void testSetUserKey() throws Exception {
        details.setUserKey(12);
        Assert.assertEquals(12, details.getUserKey());
    }

    @Test
    public void testGetRoleKeysEmpty() throws Exception {
        Set<Long> roleKeys = details.getRoleKeys();
        Assert.assertNotNull(roleKeys);
        Assert.assertEquals(0, roleKeys.size());
    }

    @Test
    public void testGetUsageDetailsForNonExistingRole() throws Exception {
        UsageDetails usageDetails = details.getUsageDetails(L1);
        Assert.assertNull(usageDetails);
    }

    @Test
    public void testGetUsageDetailsForRole() throws Exception {
        details.putUsageDetails(L1, new UsageDetails());
        UsageDetails usageDetails = details.getUsageDetails(L1);
        Assert.assertNotNull(usageDetails);
    }

    @Test
    public void testPutUsageDetailsForRoleOverwrite() throws Exception {
        UsageDetails det1 = new UsageDetails();
        det1.setFactor(1);
        UsageDetails det2 = new UsageDetails();
        det2.setFactor(2);
        details.putUsageDetails(L1, det1);
        details.putUsageDetails(L1, det2);
        UsageDetails usageDetails = details.getUsageDetails(L1);
        Assert.assertNotNull(usageDetails);
        Assert.assertEquals(2.0, usageDetails.getFactor(), 0);
    }

    @Test
    public void testAddUsageDetailsForRole() throws Exception {
        details.addUsageDetails(L1, new UsageDetails());
        UsageDetails usageDetails = details.getUsageDetails(L1);
        Assert.assertNotNull(usageDetails);
    }

    @Test
    public void testAddUsageDetailsForRoleOverwrite() throws Exception {
        UsageDetails det1 = new UsageDetails();
        det1.addUsagePeriod(100, 112);
        det1.setFactor(1);
        UsageDetails det2 = new UsageDetails();
        det2.setFactor(2);
        det2.addUsagePeriod(200, 233);
        details.addUsageDetails(L1, det1);
        details.addUsageDetails(L1, det2);
        UsageDetails usageDetails = details.getUsageDetails(L1);
        Assert.assertNotNull(usageDetails);
        Assert.assertEquals(3.0, usageDetails.getFactor(), 0);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();
        Assert.assertEquals(2, usagePeriods.size());
        Assert.assertEquals(new UsagePeriod(100, 112), usagePeriods.get(0));
        Assert.assertEquals(new UsagePeriod(200, 233), usagePeriods.get(1));
    }

    @Test
    public void testAddRoleFactorOneRole() throws Exception {
        details.addUsageDetails(L1, new UsageDetails());
        details.addRoleFactor(L1, 0.25);
        Assert.assertEquals(0.25, userAssignmentFactors.getRoleFactors()
                .get(L1).doubleValue(), 0);
    }

    @Test
    public void testAddRoleFactorOneRoleTwoAdds() throws Exception {
        details.addUsageDetails(L1, new UsageDetails());
        details.addRoleFactor(L1, 0.25);
        details.addRoleFactor(L1, 0.125);
        Assert.assertEquals(0.375,
                userAssignmentFactors.getRoleFactors().get(L1).doubleValue(), 0);
    }

    @Test
    public void testAddRoleFactorTwoRolesTwoAdds() throws Exception {
        details.addUsageDetails(L1, new UsageDetails());
        details.addUsageDetails(L2, new UsageDetails());
        details.addRoleFactor(L1, 0.25);
        details.addRoleFactor(L2, 0.125);
        Assert.assertEquals(0.25, userAssignmentFactors.getRoleFactors()
                .get(L1).doubleValue(), 0);
        Assert.assertEquals(0.125,
                userAssignmentFactors.getRoleFactors().get(L2).doubleValue(), 0);
    }
}
