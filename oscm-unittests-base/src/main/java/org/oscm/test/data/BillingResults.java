/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.08.2012                                                                                                                                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Utility class for the creation of BillingResults
 * 
 * @author cheld
 */
@SuppressWarnings("boxing")
public class BillingResults {

    public static BillingResult createBillingResult(DataService ds,
            Subscription sub, long periodStart, long periodEnd,
            BigDecimal netAmount, BigDecimal grossAmount)
            throws NonUniqueBusinessKeyException {
        return createBillingResult(ds, sub, 0, 0, periodStart, periodEnd,
                netAmount, grossAmount,
                SupportedCurrencies.createOneSupportedCurrency(ds), "");
    }

    public static BillingResult createBillingResult(DataService ds,
            Subscription sub, long chargingOrgKey, long vendorKey,
            long periodStart, long periodEnd, BigDecimal netAmount,
            BigDecimal grossAmount, String xml)
            throws NonUniqueBusinessKeyException {
        return createBillingResult(ds, sub, chargingOrgKey, vendorKey,
                periodStart, periodEnd, netAmount, grossAmount,
                SupportedCurrencies.createOneSupportedCurrency(ds), xml);
    }

    public static BillingResult createBillingResult(DataService ds,
            Subscription sub, long chargingOrgKey, long vendorKey,
            long periodStart, long periodEnd, BigDecimal netAmount,
            BigDecimal grossAmount) throws NonUniqueBusinessKeyException {
        return createBillingResult(ds, sub, chargingOrgKey, vendorKey,
                periodStart, periodEnd, netAmount, grossAmount,
                SupportedCurrencies.createOneSupportedCurrency(ds), "");
    }

    public static BillingResult createBillingResult(DataService ds,
            Subscription sub, long chargingOrgKey, long vendorKey,
            long periodStart, long periodEnd, BigDecimal netAmount,
            BigDecimal grossAmount, SupportedCurrency currency)
            throws NonUniqueBusinessKeyException {
        return createBillingResult(ds, sub, chargingOrgKey, vendorKey,
                periodStart, periodEnd, netAmount, grossAmount, currency, "");
    }

    public static BillingResult createBillingResult(DataService ds,
            Subscription sub, long chargingOrgKey, long vendorKey,
            long periodStart, long periodEnd, BigDecimal netAmount,
            BigDecimal grossAmount, SupportedCurrency currency, String xml)
            throws NonUniqueBusinessKeyException {

        BillingResult br = new BillingResult();
        br.setPeriodStartTime(periodStart);
        br.setPeriodEndTime(periodEnd);
        br.setNetAmount(netAmount);
        br.setGrossAmount(grossAmount);
        br.setResultXML(xml);
        br.setCurrency(currency);
        br.setSubscriptionKey(sub.getKey());
        br.setUsergroupKey(sub.getUserGroup() != null
                ? sub.getUserGroup().getKey() : null);
        br.setOrganizationTKey(sub.getOrganizationKey());
        br.setChargingOrgKey(chargingOrgKey);
        br.setVendorKey(vendorKey);
        ds.persist(br);
        return br;
    }
}
