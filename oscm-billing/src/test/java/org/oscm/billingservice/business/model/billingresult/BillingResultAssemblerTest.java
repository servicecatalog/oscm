/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 16.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.billingresult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.BillingInputFactory;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.test.DateTimeHandling;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author baumann
 * 
 */
public class BillingResultAssemblerTest {

    private final BillingResultAssembler assembler = new BillingResultAssembler();

    @Test
    public void initializePriceModelType() throws Exception {

        // given
        final String currencyCode = "EUR";
        final PricingPeriod pricingPeriod = PricingPeriod.MONTH;
        final BigDecimal pricePerPeriod = new BigDecimal("25.20");
        final long priceModelKey = 4711;
        final long priceModelStart = DateTimeHandling
                .calculateMillis("2015-01-01 00:00:00");
        final long priceModelEnd = DateTimeHandling
                .calculateMillis("2015-02-01 00:00:00");
        final org.oscm.internal.types.enumtypes.PriceModelType priceModelType = org.oscm.internal.types.enumtypes.PriceModelType.PRO_RATA;
        final String productId = "MyProduct";

        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2015-01-01 00:00:00", "2015-02-01 00:00:00", currencyCode);
        PriceModelInput priceModelInput = createPriceModelInput(priceModelKey,
                priceModelStart, priceModelEnd, priceModelType, pricingPeriod,
                pricePerPeriod, productId);
        PriceModelsType pmsType = new PriceModelsType();

        // when
        PriceModelType pmType = assembler.initializePriceModelType(
                billingInput, priceModelInput, pmsType);

        // then
        assertNotNull(pmType);
        List<PriceModelType> pmTypes = pmsType.getPriceModel();
        assertEquals("Wrong size of price model type list", 1, pmTypes.size());
        assertEquals("Wrong price model type in list", pmType, pmTypes.get(0));

        assertEquals("Wrong price model ID", String.valueOf(priceModelKey),
                pmType.getId());
        assertEquals("Wrong service ID", productId, pmType.getServiceId());
        assertEquals("Wrong calculation mode",
                PriceModelCalculationType.fromValue(priceModelType.name()),
                pmType.getCalculationMode());

        assertNotNull("No usage period", pmType.getUsagePeriod());
        assertEquals("Wrong start date", Long.valueOf(priceModelStart), pmType
                .getUsagePeriod().getStartDate());
        assertEquals("Wrong end date", Long.valueOf(priceModelEnd), pmType
                .getUsagePeriod().getEndDate());

        assertNotNull("No period fee", pmType.getPeriodFee());
        assertEquals("Wrong pricing period", pricingPeriod.name(), pmType
                .getPeriodFee().getBasePeriod().name());
        assertEquals("Wrong price per period", pricePerPeriod, pmType
                .getPeriodFee().getBasePrice());

        assertNotNull("No price model costs", pmType.getPriceModelCosts());
        assertEquals("Wrong currency", currencyCode, pmType
                .getPriceModelCosts().getCurrency());
    }

    private PriceModelInput createPriceModelInput(
            long priceModelKey,
            long priceModelStart,
            long priceModelEnd,
            org.oscm.internal.types.enumtypes.PriceModelType priceModelType,
            PricingPeriod period, BigDecimal pricePerPeriod, String productId) {
        PriceModelHistory pmHistory = createPriceModelHistory(priceModelKey,
                priceModelType, period, pricePerPeriod);
        PriceModelInput priceModelInput = new PriceModelInput(priceModelKey,
                priceModelStart, priceModelEnd, priceModelStart, pmHistory, 0,
                false, false, false, priceModelStart, false, priceModelEnd,
                false, productId);
        return priceModelInput;
    }

    private PriceModelHistory createPriceModelHistory(
            long priceModelKey,
            org.oscm.internal.types.enumtypes.PriceModelType priceModelType,
            PricingPeriod pricingPeriod, BigDecimal pricePerPeriod) {
        PriceModelHistory pmHistory = new PriceModelHistory();
        pmHistory.setObjKey(priceModelKey);
        pmHistory.setType(priceModelType);
        pmHistory.setPeriod(pricingPeriod);
        pmHistory.setPricePerPeriod(pricePerPeriod);
        return pmHistory;
    }

    @Test
    public void createBasicBillDocumentForOrganization() throws Exception {

        // given
        final long billingResultKey = 4711;
        final long startDate = DateTimeHandling
                .calculateMillis("2015-01-01 00:00:00");
        final long endDate = DateTimeHandling
                .calculateMillis("2015-02-01 00:00:00");
        final long orgKey = 10000;
        final String orgId = "afc969b7";
        final String orgName = "MyOrg";
        final String orgEmail = "org@org.com";
        final String orgAddress = "Broadway, NYC";
        final String ptId = "INVOICE";

        BillingResult billingResult = createBillingResult(billingResultKey);
        OrganizationAddressData orgAddrData = createOrganizationAddressData(
                orgId, orgName, orgEmail, orgAddress, ptId);

        // when
        BillingDetailsType bdType = assembler
                .createBasicBillDocumentForOrganization(orgAddrData, null,
                        orgKey, 0, billingResult, startDate, endDate, true);

        // then
        assertNotNull("No Billing Details Type", bdType);
        assertEquals("Wrong billing result key",
                Long.valueOf(billingResultKey), bdType.getKey());

        assertNotNull("No Period Type", bdType.getPeriod());
        assertEquals("Wrong start date", Long.valueOf(startDate), bdType
                .getPeriod().getStartDate());
        assertEquals("Wrong end date", Long.valueOf(endDate), bdType
                .getPeriod().getEndDate());

        assertNotNull("No Organization Details Type",
                bdType.getOrganizationDetails());
        assertEquals("Wrong organization ID", orgId, bdType
                .getOrganizationDetails().getId());
        assertEquals("Wrong organization name", orgName, bdType
                .getOrganizationDetails().getName());
        assertEquals("Wrong organization address", orgAddress, bdType
                .getOrganizationDetails().getAddress());
        assertEquals("Wrong organization email", orgEmail, bdType
                .getOrganizationDetails().getEmail());
        assertEquals("Wrong payment type", ptId, bdType
                .getOrganizationDetails().getPaymenttype());

        assertNotNull("No Subscriptions Type", bdType.getSubscriptions());
    }

    @Test(expected = BillingRunFailed.class)
    public void createBasicBillDocumentForOrganization_noOrgId()
            throws Exception {
        // when
        assembler.createBasicBillDocumentForOrganization(null, null, 0, 0,
                null, 0, 0, false);
    }

    private OrganizationAddressData createOrganizationAddressData(String orgId,
            String orgName, String orgEmail, String orgAddress, String ptId) {
        OrganizationAddressData orgAddrData = new OrganizationAddressData(
                orgAddress, orgName, orgEmail, orgId);
        orgAddrData.setPaymentTypeId(ptId);
        return orgAddrData;
    }

    private BillingResult createBillingResult(long key) {
        BillingResult billingResult = new BillingResult();
        billingResult.setKey(key);
        return billingResult;
    }
}
