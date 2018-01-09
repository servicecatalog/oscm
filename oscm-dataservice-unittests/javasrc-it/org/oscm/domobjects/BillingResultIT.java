/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 30.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.converter.BigDecimalComparator;
import org.oscm.test.data.SupportedCurrencies;

/**
 * Tests for handling CLOB data for type BillingResult.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BillingResultIT extends DomainObjectTestBase {

    private static final String VALID_XML = "<BillingDetails><Period endDate=\"1246399200000\" "
            + "startDate=\"1243807200000\"/><OrganizationDetails><Name>SubMgmt Organization1</Name>"
            + "<Address>Address 1</Address></OrganizationDetails><Subscriptions>"
            + "<Subscription id=\"first Subscription, billable\"><PriceModels>"
            + "<PriceModel id=\"1\"><UsagePeriod endDate=\"1246399200000\" "
            + "startDate=\"1244983263921\"/><GatheredEvents><Event id=\"Operation A\">"
            + "<SingleCost amount=\"100\"/><NumberOfOccurrence amount=\"2\"/>"
            + "<CostForEventType amount=\"200\"/></Event></GatheredEvents>"
            + "<PeriodFee basePeriod=\"DAY\" basePrice=\"1\" factor=\"16.0\" "
            + "price=\"16\"/><UserAssignmentCosts/><PriceModelCosts amount=\"216\"/>"
            + "</PriceModel></PriceModels></Subscription></Subscriptions>"
            + "<OverallCosts/>" + "</BillingDetails>";

    private long billingResultKey;

    @Test
    public void testGetOverallCostsNoCostsSet() {
        BillingResult br = new BillingResult();
        br.setResultXML(VALID_XML);
        BigDecimal overallCosts = br.getGrossAmount();
        Assert.assertNull("Wrong return value", overallCosts);
    }

    @Test
    public void testGetOverallCostsCodeNullInput() {
        BillingResult br = new BillingResult();
        BigDecimal overallCosts = br.getGrossAmount();
        Assert.assertNull(
                "For a non-set XML structure the return value must be null",
                overallCosts);
    }

    @Test
    public void testGetCurrencyCodeNullInput() {
        BillingResult br = new BillingResult();
        Assert.assertNull(
                "For a non-set XML structure the return value must be null",
                br.getCurrency());
    }

    @Test
    public void testGetVAT_NullInput() throws Exception {
        BillingResult br = new BillingResult();
        assertNull(br.getVAT());
    }

    @Test
    public void testGetVAT_NoVatInput() throws Exception {
        BillingResult br = new BillingResult();
        br.setResultXML("<result/>");
        assertEquals("0", br.getVAT());
    }

    /**
     * Subscription with 2 price models and 2 different vats. Currently, only
     * the first is retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testGetVAT_VatInput() throws Exception {
        BillingResult br = new BillingResult();
        br.setResultXML("<BillingDetails><OverallCosts><VAT percent='10.0'/></OverallCosts></BillingDetails>");
        assertEquals("10.0", br.getVAT());
    }

    @Test
    public void testGetVATAmount_NullInput() throws Exception {
        BillingResult br = new BillingResult();
        assertNull(br.getVATAmount());
    }

    @Test
    public void testGetVATAmount_NoVatInput() throws Exception {
        BillingResult br = new BillingResult();
        br.setResultXML("<result/>");
        Assert.assertTrue(BigDecimalComparator.isZero(br.getVATAmount()));
    }

    /**
     * Subscription with 2 price models and 2 different vat amounts. Currently,
     * only the first is retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testGetVATAmount_VatInput() throws Exception {
        BillingResult br = new BillingResult();
        br.setResultXML("<BillingDetails><OverallCosts><VAT amount='100'/></OverallCosts></BillingDetails>");
        checkEquals(100, br.getVATAmount());
    }

    /**
     * Subscription with 2 price models and 2 different discounts. Currently,
     * only the first is retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testGetNetDiscount() throws Exception {
        BillingResult br = new BillingResult();
        br.setResultXML("<BillingDetails><OverallCosts><Discount discountNetAmount='180.00' /></OverallCosts></BillingDetails>");
        checkEquals(180, br.getNetDiscount());
    }

    // /////////////////////////////////////////
    // internal helper methods

    private BillingResult loadBillingResult() throws Exception {
        return runTX(new Callable<BillingResult>() {
            public BillingResult call() {
                BillingResult storedResult = mgr.find(BillingResult.class,
                        billingResultKey);

                return storedResult;
            }
        });

    }

    private Void persisting(final BillingResult br) throws Exception {
        return runTX(new Callable<Void>() {
            public Void call() throws Exception {
                br.setCurrency(SupportedCurrencies.findOrCreate(mgr, "EUR"));
                mgr.persist(br);
                billingResultKey = br.getKey();
                return null;
            }
        });
    }

    private BillingResult newBillingResult(String xml) {
        BillingResult br = new BillingResult();
        br.setCreationTime(System.currentTimeMillis());
        br.setResultXML(xml);
        br.setOrganizationTKey(1L);
        br.setPeriodStartTime(0L);
        br.setPeriodEndTime(0L);
        br.setNetAmount(BigDecimal.ZERO);
        br.setGrossAmount(BigDecimal.ZERO);
        return br;
    }

    @Test
    public void persist_ValidBillingResult() throws Exception {

        // given
        BillingResult br = newBillingResult(VALID_XML);

        // when
        persisting(br);

        // then
        BillingResult storedResult = loadBillingResult();
        assertNotNull("Stored billing result could not be found", storedResult);
        assertEquals("Stored result string is corrupt", VALID_XML,
                storedResult.getResultXML());
        assertEquals("Wrong organization key", 1L,
                storedResult.getOrganizationTKey());
        assertTrue("No creation time set", storedResult.getCreationTime() != 0);
        assertTrue("Wrong creation time",
                storedResult.getCreationTime() <= System.currentTimeMillis());
    }

    /**
     * Test to make sure that hibernate can persist very long xml strings. 10MB
     * in this case.
     */
    @Test
    public void persist_VeryLongXML() throws Exception {

        // given
        String veryLongXML = createVeryLongXML();
        BillingResult br = newBillingResult(veryLongXML);

        // when
        persisting(br);

        // then
        BillingResult storedResult = loadBillingResult();
        assertEquals("Stored result string is corrupt", veryLongXML,
                storedResult.getResultXML());
    }

    private String createVeryLongXML() {
        char[] xml = new char[1024 * 1024 * 10];
        Arrays.fill(xml, '*');
        return new String(xml);
    }
}
