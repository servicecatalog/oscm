/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                      
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *  Completion Time: 12.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOOption;
import org.oscm.reportingservice.business.model.billing.RDOParameter;
import org.oscm.reportingservice.business.model.billing.RDOPriceModel;
import org.oscm.reportingservice.business.model.billing.RDOSteppedPrice;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Tests for the DetailedBillingReport (DBR)
 * 
 * @author kulle
 */
public class ReportingServiceBeanBillingZeroPricesTest extends
        BaseBillingReport {

    @Test
    public void testDBR_userAssignmentElementMissing() throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_6).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        assertNull("base period wrong", pm.getUserFees().getBasePeriod());
        assertNull("base price wrong", pm.getUserFees().getBasePrice());
        assertEquals("factor wrong", "",
                String.valueOf(pm.getUserFees().getFactor()));
        assertNull("number of users wrong", pm.getUserFees()
                .getNumberOfUsersTotal());
        assertNull("price wrong", pm.getUserFees().getPrice());
    }

    @Test
    public void testDBR_zeroStepCostsAtUserAssignment() throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_ZEROPRICES).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        assertEquals(1, pm.getUserFees().getSteppedPrices().size());
        RDOSteppedPrice steppedPrice = pm.getUserFees().getSteppedPrices()
                .get(0);
        assertEquals("base price wrong", "2.00", steppedPrice.getBasePrice());
        assertEquals("factor wrong", "1.00000",
                String.valueOf(steppedPrice.getFactor()));
        assertEquals("id wrong", "", steppedPrice.getId());
        assertEquals("limit wrong", "1", steppedPrice.getLimit());
        assertEquals("price wrong", "2.00", steppedPrice.getPrice());
        assertEquals("quantity wrong", "",
                String.valueOf(steppedPrice.getQuantity()));
    }

    @Test
    public void testDBR_zeroStepCostAtPeriodParameter() throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_ZEROPRICES).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOParameter parameter = pm.getSubscriptionFees().getParameters()
                .get(2);
        assertEquals("CONCURRENT_USER", parameter.getId());
        assertEquals(1, parameter.getSteppedPrices().size());
        assertEquals("wrong bae price", "1.00", parameter.getSteppedPrices()
                .get(0).getBasePrice());
        assertEquals("wrong factor", "442.72996",
                String.valueOf(parameter.getSteppedPrices().get(0).getFactor()));
        assertEquals("wrong id", "CONCURRENT_USER", parameter
                .getSteppedPrices().get(0).getId());
        assertEquals("wrong limit", "1", parameter.getSteppedPrices().get(0)
                .getLimit());
        assertEquals("wrong price", "442.73",
                parameter.getSteppedPrices().get(0).getPrice());
        assertEquals(
                "1",
                String.valueOf(parameter.getSteppedPrices().get(0)
                        .getQuantity()));
    }

    @Test
    public void testDBR_zeroStepCostAtEvent() throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_ZEROPRICES).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        List<RDOSteppedPrice> steppedPrices = pm.getEventFees()
                .getEvent("TEST").getSteppedPrices();
        assertEquals(1, steppedPrices.size());
        assertEquals("0.80", steppedPrices.get(0).getBasePrice());
        assertEquals("1", String.valueOf(steppedPrices.get(0).getFactor()));
        assertEquals("TEST", steppedPrices.get(0).getId());
        assertEquals("1", steppedPrices.get(0).getLimit());
        assertEquals("0.80", steppedPrices.get(0).getPrice());
        assertEquals("", String.valueOf(steppedPrices.get(0).getQuantity()));
    }

    @Test
    public void testDBR_zeroUserCostsAtOption() throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_3).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        Set<RDOOption> options = new HashSet<RDOOption>();
        for (RDOParameter param : pm.getSubscriptionFees().getParameters()) {
            options.addAll(param.getOptions());
        }
        for (RDOParameter param : pm.getUserFees().getParameters()) {
            options.addAll(param.getOptions());
        }
        assertEquals(5, options.size());
    }

    // Option "1" of parameter OPTIONS_WITH_ROLES has a factor "0.0"
    // in the UserAssignmentCosts -> no RDOOption!
    @Test
    public void testDBR_zeroUserCostsAtOption_BeforeValueChange()
            throws Exception {
        // given
        mockResultData(new File(this.getClass().getResource(XML_FILE_OPTIONS).getFile()));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        List<RDOParameter> params = pm.getUserFees().getParameters();
        assertEquals(3, params.size());
        assertEquals(1, params.get(2).getOptions().size());
        RDOOption option = params.get(2).getOptions().get(0);
        assertEquals("2", option.getValue());
        assertEquals("400.00", option.getBasePrice());
        assertEquals("2.00000", option.getFactor());
        assertEquals("800.00", option.getPrice());
    }

}
