/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.07.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;

/**
 * Reads billing results from the database
 * 
 * @author baumann
 */
public class BillingResultReader {

    public static BillingResult loadBillingResult(DataService dm,
            long subscriptionKey, long billingPeriodStart, long billingPeriodEnd) {

        Query query = dm.createNamedQuery("BillingResult.findBillingResult");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        query.setParameter("startPeriod", Long.valueOf(billingPeriodStart));
        query.setParameter("endPeriod", Long.valueOf(billingPeriodEnd));
        try {
            return (BillingResult) query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public static List<BillingResult> loadBillingResults(DataService dm,
            long subscriptionKey, long startOfFirstPeriod, long endOfLastPeriod) {
        List<BillingResult> results = new ArrayList<BillingResult>();
        long periodStart = startOfFirstPeriod;
        long periodEnd = addMonths(periodStart, 1);

        while (periodEnd <= endOfLastPeriod) {
            BillingResult result = loadBillingResult(dm, subscriptionKey,
                    periodStart, periodEnd);
            if (result != null) {
                results.add(result);
            }

            periodStart = periodEnd;
            periodEnd = addMonths(periodStart, 1);
        }

        return results;
    }

    private static long addMonths(long millis, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.add(Calendar.MONTH, months);
        return cal.getTimeInMillis();
    }

}
