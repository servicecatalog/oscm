/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Dec 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.billingservice.business.calculation.revenue.model.UsageDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.ParameterHistory;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author afschar
 * 
 */
public class CostCalculatorProRata extends CostCalculator {

    CostCalculatorProRata() {
        super();
    }

    /**
     * Returns the date representing the first day of the previous month, 0:00.
     * 
     * @param baseTime
     *            The base time. Based on it the previous month will be
     *            determined.
     * @return The long representation of the date for the first day of the
     *         previous month.
     */
    public static final long getStartDateOfLastMonth(long baseTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Returns the date representing the first day of the current month, 0:00.
     * As this allows equivalent usage as taking the last day of the previous
     * month, 23:59:59, this proceeding is okay.
     * 
     * @param baseTime
     *            The current time, based on which the time will be determined.
     * @return The long representation of the first day of this month.
     */
    public static final long getEndDateOfLastMonth(long baseTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getStartDateOfLastMonth(baseTime));
        cal.add(Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }

    @Override
    public double computeFactorForUsageTime(PricingPeriod pricingPeriod,
            BillingInput billingInput, long usagePeriodStart,
            long usagePeriodEnd) {

        long billingPeriodStart = billingInput.getCutOffDate();
        long billingPeriodEnd = billingInput.getBillingPeriodEnd();

        if (usagePeriodStart < billingPeriodStart) {
            usagePeriodStart = billingPeriodStart;
        }
        if (usagePeriodEnd > billingPeriodEnd) {
            usagePeriodEnd = billingPeriodEnd;
        }

        return computeFractionalFactor(usagePeriodStart, usagePeriodEnd,
                pricingPeriod);
    }

    @Override
    public UserAssignmentFactors computeUserAssignmentsFactors(
            List<UsageLicenseHistory> ulHistList,
            PriceModelHistory referencePMHistory, BillingInput billingInput,
            long periodStart, long periodEnd) {

        // now determine the billable period for every user
        final UserAssignmentFactors result = new UserAssignmentFactors();
        Long referenceUserKey = null;
        long periodEndTime = periodEnd;
        for (UsageLicenseHistory ulHist : ulHistList) {
            if (referenceUserKey == null
                    || referenceUserKey.longValue() != ulHist.getUserObjKey()) {
                // if a new user has to be handled, only reset the
                // temporary variables
                referenceUserKey = Long.valueOf(ulHist.getUserObjKey());
                periodEndTime = periodEnd;
            }

            // if the status is not deleted, register the used period,
            // which is from mod date to period end time
            if (ulHist.getModtype() != ModificationType.DELETE) {
                long entryModTime = ulHist.getModdate().getTime();
                UsageDetails ud = new UsageDetails();
                ud.addUsagePeriod(Math.max(entryModTime, periodStart),
                        periodEndTime);

                if (ulHist.getRoleDefinitionObjKey() != null) {
                    result.addUsageDataForUserAndRole(referenceUserKey, ulHist
                            .getDataContainer().getApplicationUserId(), ulHist
                            .getRoleDefinitionObjKey(), ud);
                } else {
                    result.addUsageDataForUser(referenceUserKey, ulHist
                            .getDataContainer().getApplicationUserId(), ud);
                }
            }

            periodEndTime = ulHist.getModdate().getTime();
        }

        // the periods for each user of this subscription have been
        // determined. Now handle them according to the settings in the
        // price model
        Set<Long> userKeys = result.getUserKeys();
        if (userKeys != null) {
            for (Long userKey : userKeys) {
                UserAssignmentDetails userAssignmentDetails = result
                        .getUserAssignmentDetails(userKey);
                final double modifiedFactorForDefinedHandling = computeFactorForUsageDetails(
                        userAssignmentDetails.getUsageDetails(),
                        referencePMHistory.getPeriod());

                // update in user table for later xml representation
                UsageDetails ud = new UsageDetails();
                ud.setFactor(modifiedFactorForDefinedHandling);
                result.addUsageDataForUser(userKey,
                        userAssignmentDetails.getUserId(), ud);

                // the periods for each user of this subscription have been
                // determined. Now handle them according to the settings in the
                // price model
                for (Long roleKey : userAssignmentDetails.getRoleKeys()) {
                    final UsageDetails usageDetails = userAssignmentDetails
                            .getUsageDetails(roleKey);
                    final double roleAssignmentFactor = computeFactorForUsageDetails(
                            usageDetails, referencePMHistory.getPeriod());
                    userAssignmentDetails.addRoleFactor(roleKey,
                            roleAssignmentFactor);
                }
            }
        }
        return result;
    }

    private double computeFactorForUsageDetails(UsageDetails usageDetails,
            PricingPeriod period) {
        double factor = 0.0D;

        for (UsageDetails.UsagePeriod usagePeriod : usageDetails
                .getUsagePeriods()) {
            factor += computeFractionalFactor(usagePeriod.getStartTime(),
                    usagePeriod.getEndTime(), period);
        }

        return factor;
    }

    @Override
    public long computeUserAssignmentStartTimeForParameters(
            PricingPeriod period, long paramValueEndTime,
            ParameterHistory paramHist, PriceModelHistory pmh,
            long paramValueStartTime) {
        return paramValueStartTime;
    }

    @Override
    public long determineStartTime(long startTimeForPeriod,
            long endTimeForPeriod, PricingPeriod period) {
        return startTimeForPeriod;
    }

    @Override
    public long computeEndTimeForPaymentPreview(long endTimeForPeriod,
            long billingPeriodEnd, PricingPeriod period) {
        return endTimeForPeriod;
    }

    @Override
    public void computeParameterPeriodFactor(BillingInput billingInput,
            XParameterData parameterData, long startTimeForPeriod,
            long endTimeForPeriod) {
        if (parameterData != null) {
            for (XParameterIdData parameterIdData : parameterData.getIdData()) {
                for (XParameterPeriodValue parameterPeriodValue : parameterIdData
                        .getPeriodValues()) {
                    double periodFactor = computeFractionalFactor(
                            parameterPeriodValue.getStartTime(),
                            parameterPeriodValue.getEndTime(),
                            parameterData.getPeriod());
                    parameterPeriodValue.setPeriodFactor(periodFactor);
                }
            }
        }
    }

    @Override
    public boolean isSuspendedAndResumedInSameTimeUnit(
            SubscriptionHistory current, SubscriptionHistory next,
            PriceModelHistory pm) {
        // All history entries are relevant in pro rata calculation
        return false;
    }

    @Override
    public BigDecimal calculateParameterUserCosts(
            XParameterPeriodValue parameterPeriodValue,
            BigDecimal valueMultplier) {
        BigDecimal costs = parameterPeriodValue.getPricePerUser().multiply(
                valueMultplier);
        costs = BigDecimals.multiply(costs,
                parameterPeriodValue.getUserAssignmentFactor());
        return costs;
    }

    @Override
    public void computeParameterUserFactorAndRoleFactor(
            BillingDataRetrievalServiceLocal billingDao, BillingInput input,
            XParameterData parameterData, long startTimeForPeriod,
            long endTimeForPeriod) {
        // already calculated by billing data retrieval service when loading
        // parameter data
    }
}
