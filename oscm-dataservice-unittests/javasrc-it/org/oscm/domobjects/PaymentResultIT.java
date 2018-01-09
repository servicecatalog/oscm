/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import org.oscm.test.data.SupportedCurrencies;
import org.oscm.types.enumtypes.PaymentProcessingStatus;

/**
 * @author barzu
 */
public class PaymentResultIT extends DomainObjectTestBase {

    private static final String XML = "<BillingDetails></BillingDetails>";

    private BillingResult billingResult;

    @Override
    protected void dataSetup() throws Exception {
        long key = persist(givenBillingResult(XML));
        billingResult = loadBillingResult(key);
    }

    @Test
    public void persist_VeryLongProcessingResult() throws Exception {
        // given
        String veryLongString = createVeryLongString(10); // 10MB
        PaymentResult pr = givenPaymentResult();
        pr.setProcessingResult(veryLongString);

        // when
        long key = persist(pr).longValue();

        // then
        PaymentResult storedResult = load(PaymentResult.class, key);
        assertEquals("Stored string is corrupt", veryLongString,
                storedResult.getProcessingResult());
    }

    @Test
    public void persist_VeryLongProcessingException() throws Exception {
        // given
        String veryLongString = createVeryLongString(10); // 10MB
        PaymentResult pr = givenPaymentResult();
        pr.setProcessingException(veryLongString);

        // when
        long key = persist(pr).longValue();

        // then
        PaymentResult storedResult = load(PaymentResult.class, key);
        assertEquals("Stored string is corrupt", veryLongString,
                storedResult.getProcessingException());
    }

    private String createVeryLongString(int sizeInMB) {
        char[] veryLongString = new char[1024 * 1024 * sizeInMB];
        Arrays.fill(veryLongString, '*');
        return new String(veryLongString);
    }

    private PaymentResult givenPaymentResult() {
        PaymentResult pr = new PaymentResult();
        pr.setProcessingStatus(PaymentProcessingStatus.SUCCESS);
        pr.setBillingResult(billingResult);
        return pr;
    }

    private static BillingResult givenBillingResult(String xml) {
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

    private long persist(final BillingResult br) throws Exception {
        br.setCurrency(SupportedCurrencies.findOrCreate(mgr, "EUR"));
        mgr.persist(br);
        return br.getKey();
    }

    private BillingResult loadBillingResult(long key) {
        return mgr.find(BillingResult.class, key);
    }

}
