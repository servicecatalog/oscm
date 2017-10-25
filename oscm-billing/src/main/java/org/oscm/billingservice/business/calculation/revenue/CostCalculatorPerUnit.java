/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Dec 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.billingservice.business.calculation.revenue.model.UsageDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.ParameterHistory;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.IllegalArgumentException;

public class CostCalculatorPerUnit extends CostCalculator {

    CostCalculatorPerUnit() {
        super();
    }

    @Override
    public double computeFactorForUsageTime(PricingPeriod pricingPeriod,
            BillingInput billingInput, long usagePeriodStart,
            long usagePeriodEnd) {
        return computeFactor(pricingPeriod, billingInput, usagePeriodStart,
                usagePeriodEnd, true, true);
    }

    /**
     * Calculate the factor for a specific billing period and usage period.
     * According to the flag of extend usage start/end, the usage period time is
     * extended (or not) to the start/end time of the time unit before the
     * factor is calculated.
     */
    double computeFactor(PricingPeriod pricingPeriod,
            BillingInput billingInput, long usagePeriodStart,
            long usagePeriodEnd, boolean adjustsPeriodStart,
            boolean adjustsPeriodEnd) {

        if (usagePeriodEnd < usagePeriodStart) {
            throw new IllegalArgumentException("Usage period end ("
                    + new Date(usagePeriodEnd)
                    + ") before usage period start ("
                    + new Date(usagePeriodStart) + ")");
        }

        Calendar adjustedBillingPeriodStart = PricingPeriodDateConverter
                .getStartTime(billingInput.getCutOffDate(), pricingPeriod);
        Calendar adjustedBillingPeriodEnd = PricingPeriodDateConverter
                .getStartTime(billingInput.getBillingPeriodEnd(), pricingPeriod);

        if (usagePeriodOutsideOfAdjustedBillingPeriod(usagePeriodStart,
                usagePeriodEnd, adjustedBillingPeriodStart.getTimeInMillis(),
                adjustedBillingPeriodEnd.getTimeInMillis())) {
            return 0D;
        } else {
            Calendar startTimeForFactorCalculation = determineStartTimeForFactorCalculation(
                    pricingPeriod, adjustedBillingPeriodStart,
                    usagePeriodStart, adjustsPeriodStart);
            Calendar endTimeForFactorCalculation = determineEndTimeForFactorCalculation(
                    pricingPeriod, adjustedBillingPeriodEnd, usagePeriodEnd,
                    adjustsPeriodEnd);

            return computeFractionalFactor(
                    startTimeForFactorCalculation.getTimeInMillis(),
                    endTimeForFactorCalculation.getTimeInMillis(),
                    pricingPeriod);
        }
    }

    /**
     * Check if the usage period for factor calculation is located outside of
     * the billing period, which was adjusted to an overlapping time unit. In
     * this case no billing is done because the overlapping time unit is charged
     * in another billing period.
     */
    private boolean usagePeriodOutsideOfAdjustedBillingPeriod(
            long usagePeriodStart, long usagePeriodEnd,
            long adjustedBillingPeriodStart, long adjustedBillingPeriodEnd) {
        return (usagePeriodStart > adjustedBillingPeriodEnd || usagePeriodEnd < adjustedBillingPeriodStart);
    }

    /**
     * Calculate the factor for a specific time slice and usage period.
     * According to the flag of extend usage start/end, the usage period time is
     * extended (or not) to the start/end time of the time unit before the
     * factor is calculated.
     */
    double computeFactorForTimeSlice(TimeSlice timeSlice,
            long usagePeriodStart, long usagePeriodEnd,
            boolean adjustsPeriodStart, boolean adjustsPeriodEnd) {

        if (usagePeriodEnd < usagePeriodStart) {
            throw new IllegalArgumentException("Usage period end ("
                    + new Date(usagePeriodEnd)
                    + ") before usage period start ("
                    + new Date(usagePeriodStart) + ")");
        }

        Calendar startTimeForFactorCalculation = determineStartTimeForFactorCalculation(
                timeSlice.getPeriod(), timeSlice.getStartAsCalendar(),
                usagePeriodStart, adjustsPeriodStart);
        Calendar endTimeForFactorCalculation = determineEndTimeForFactorCalculation(
                timeSlice.getPeriod(),
                timeSlice.getStartOfNextSliceAsCalendar(), usagePeriodEnd,
                adjustsPeriodEnd);

        if (startTimeForFactorCalculation.after(endTimeForFactorCalculation)) {
            throw new BillingRunFailed("Usage period start ("
                    + new Date(usagePeriodStart) + ") and usage period end ("
                    + new Date(usagePeriodEnd) + ") do not match time slice ("
                    + timeSlice + ")");
        } else {
            return computeFractionalFactorForTimeUnit(
                    startTimeForFactorCalculation.getTimeInMillis(),
                    endTimeForFactorCalculation.getTimeInMillis(),
                    timeSlice.getPeriod());
        }
    }

    private Calendar determineStartTimeForFactorCalculation(
            PricingPeriod pricingPeriod, Calendar chargedPeriodStart,
            long usagePeriodStart, boolean adjustUsagePeriodStart) {
        Calendar startTime;
        if (adjustUsagePeriodStart) {
            startTime = PricingPeriodDateConverter.getStartTime(
                    usagePeriodStart, pricingPeriod);
        } else {
            startTime = Calendar.getInstance();
            startTime.setTimeInMillis(usagePeriodStart);
        }

        if (startTime.before(chargedPeriodStart)) {
            return chargedPeriodStart;
        } else {
            return startTime;
        }
    }

    private Calendar determineEndTimeForFactorCalculation(
            PricingPeriod pricingPeriod, Calendar chargedPeriodEnd,
            long usagePeriodEnd, boolean adjustUsagePeriodEnd) {
        Calendar endTime;
        if (adjustUsagePeriodEnd) {
            endTime = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                    usagePeriodEnd, pricingPeriod);
        } else {
            endTime = Calendar.getInstance();
            endTime.setTimeInMillis(usagePeriodEnd);
        }

        if (endTime.after(chargedPeriodEnd)) {
            return chargedPeriodEnd;
        } else {
            return endTime;
        }
    }

    @Override
    public long computeUserAssignmentStartTimeForParameters(
            PricingPeriod period, long paramValueEndTime,
            ParameterHistory paramHist, PriceModelHistory pmh,
            long paramValueStartTime) {
        long userAssignmentValueStartTime = paramValueStartTime;
        if (paramHist.getModtype().equals(ModificationType.MODIFY)) {
            Calendar adjustedUserAssignmentValueStartTime = PricingPeriodDateConverter
                    .getStartTimeOfNextPeriod(paramValueStartTime, period);
            if (adjustedUserAssignmentValueStartTime.getTimeInMillis() <= paramValueEndTime) {
                userAssignmentValueStartTime = adjustedUserAssignmentValueStartTime
                        .getTimeInMillis();
            }
        }
        return userAssignmentValueStartTime;
    }

    @Override
    public UserAssignmentFactors computeUserAssignmentsFactors(
            List<UsageLicenseHistory> ulHistList,
            PriceModelHistory referencePMHistory, BillingInput billingInput,
            long periodStart, long periodEnd) {

        final PricingPeriod period = referencePMHistory.getPeriod();
        final UserAssignmentFactors result = new UserAssignmentFactors();
        if (ulHistList != null && !ulHistList.isEmpty()) {
            UserAssignmentExtractor uaExtractor = new UserAssignmentExtractor(
                    ulHistList, periodStart, periodEnd);
            uaExtractor.extract();
            for (Long userKey : uaExtractor.getUserKeys()) {
                computeUserFactorAndRoleFactorForOneUser(result,
                        uaExtractor.getUserAssignments(userKey), period,
                        billingInput);
            }
        }
        return result;
    }

    /**
     * Compute user factor and role factor from user assignments for one user.
     * Iterate all assignments from newest one to oldest one. When it found that
     * two neighboring assignments belongs to different time slices, it's
     * considered as those two assignments have to be calculated separately and
     * calculate factor to previous ones. In the end, the factor for current
     * assignment and ones considerable as connected to the current one are
     * computed.
     */
    void computeUserFactorAndRoleFactorForOneUser(UserAssignmentFactors result,
            final List<UserAssignment> userAssignmentsForOneUser,
            PricingPeriod period, BillingInput billingInput) {

        UserAssignment previousAssignment = null;
        List<UserAssignment> userAssignmentsForSameSlices = new ArrayList<UserAssignment>();

        if (!userAssignmentsForOneUser.isEmpty()) {
            long userKey = userAssignmentsForOneUser.get(0).getUserKey();
            String userId = userAssignmentsForOneUser.get(0).getUserId();
            for (UserAssignment userAssignment : userAssignmentsForOneUser) {
                if (previousAssignment != null
                        && areNotAffectingSameTimeSlice(userAssignment,
                                previousAssignment, period)) {
                    computeUserFactorForAssignmentsAffectingSameSlices(result,
                            userAssignmentsForSameSlices, period, billingInput,
                            userKey, userId);
                    computeUserRoleFactorForAssignmentsAffectingSameSlices(
                            result, userAssignmentsForSameSlices, period,
                            billingInput, userKey);
                    userAssignmentsForSameSlices.clear();
                }

                userAssignmentsForSameSlices.add(userAssignment);
                previousAssignment = userAssignment;
            }

            computeUserFactorForAssignmentsAffectingSameSlices(result,
                    userAssignmentsForSameSlices, period, billingInput,
                    userKey, userId);
            computeUserRoleFactorForAssignmentsAffectingSameSlices(result,
                    userAssignmentsForSameSlices, period, billingInput, userKey);
        }
    }

    private boolean areNotAffectingSameTimeSlice(
            UserAssignment currentAssignment,
            UserAssignment previousAssignment, PricingPeriod period) {
        long nextSliceStartForCurrentEndTime = PricingPeriodDateConverter
                .getStartTimeOfNextPeriod(currentAssignment.getUsageEndTime(),
                        period).getTimeInMillis();
        long sliceStartForPreviousStartTime = PricingPeriodDateConverter
                .getStartTime(previousAssignment.getUsageStartTime(), period)
                .getTimeInMillis();
        if (nextSliceStartForCurrentEndTime <= sliceStartForPreviousStartTime) {
            return true;
        }
        return false;
    }

    private void computeUserFactorForAssignmentsAffectingSameSlices(
            UserAssignmentFactors result, List<UserAssignment> userAssignments,
            PricingPeriod period, BillingInput billingInput, long userKey,
            String userId) {

        long startTime = userAssignments.get(userAssignments.size() - 1)
                .getUsageStartTime();
        long endTime = userAssignments.get(0).getUsageEndTime();

        double factor = computeFactor(period, billingInput, startTime, endTime,
                true, true);
        storeUserFactorToResult(result, factor, userKey, userId);
    }

    private void storeUserFactorToResult(UserAssignmentFactors result,
            double factor, long userKey, String userId) {
        if (factor != 0) {
            UsageDetails usageDetails = new UsageDetails();
            usageDetails.setFactor(factor);
            result.addUsageDataForUser(Long.valueOf(userKey), userId,
                    usageDetails);
        }
    }

    private void computeUserRoleFactorForAssignmentsAffectingSameSlices(
            UserAssignmentFactors result,
            final List<UserAssignment> userAssignments, PricingPeriod period,
            BillingInput billingInput, long userKey) {

        List<UserRoleAssignment> extendedRoleAssignments = determineRoleAssignmentsWithFillingBlank(userAssignments);
        for (UserRoleAssignment roleAssignment : extendedRoleAssignments) {
            double factor = computeFactor(period, billingInput,
                    roleAssignment.getStartTime(), roleAssignment.getEndTime(),
                    roleAssignment.isFirstRoleAssignment(),
                    roleAssignment.isLastRoleAssignment());
            if (factor != 0) {
                storeUserRoleFactorToResult(result, roleAssignment, factor,
                        userKey);
            }
        }
    }

    /**
     * Create list of arranged user role assignments so that they can be
     * calculated with filling blanks which are between user assignments
     * affecting one same time slice. The end time of user role assignment is
     * extended to the start time of one newer user assignment, if the last user
     * role of older user assignment is different from the first role of newer
     * user assignment.
     */
    List<UserRoleAssignment> determineRoleAssignmentsWithFillingBlank(
            List<UserAssignment> userAssignmentsForOneUser) {
        List<UserRoleAssignment> determinedRoleAssignments = new ArrayList<UserRoleAssignment>();
        if (!userAssignmentsForOneUser.isEmpty()
                && userAssignmentsForOneUser.get(0).hasUserRole()) {
            UserRoleAssignment previousRoleAssignment = null;
            long endTime = 0;
            for (UserAssignment userAssignment : userAssignmentsForOneUser) {
                for (UserRoleAssignment roleAssignment : userAssignment
                        .getRoleAssignments()) {
                    if (previousRoleAssignment == null) {
                        endTime = roleAssignment.getEndTime();
                    } else if (!roleAssignment.getRoleKey().equals(
                            previousRoleAssignment.getRoleKey())) {
                        determinedRoleAssignments
                                .add(new UserRoleAssignment(
                                        previousRoleAssignment.getRoleKey(),
                                        previousRoleAssignment.getStartTime(),
                                        endTime));
                        endTime = previousRoleAssignment.getStartTime();
                    }

                    previousRoleAssignment = roleAssignment;
                }
            }

            if (previousRoleAssignment != null) {
                determinedRoleAssignments.add(new UserRoleAssignment(
                        previousRoleAssignment.getRoleKey(),
                        previousRoleAssignment.getStartTime(), endTime));
            }

            setFlagForFirstAndLastRole(determinedRoleAssignments);
        }
        return determinedRoleAssignments;
    }

    private void setFlagForFirstAndLastRole(
            List<UserRoleAssignment> determinedRoleAssignments) {
        if (!determinedRoleAssignments.isEmpty()) {
            determinedRoleAssignments.get(0).setLastRoleAssignment(true);
            determinedRoleAssignments.get(determinedRoleAssignments.size() - 1)
                    .setFirstRoleAssignment(true);
        }
    }

    private void storeUserRoleFactorToResult(UserAssignmentFactors result,
            UserRoleAssignment roleAssignment, double factor, long userKey) {
        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(userKey));
        if (detail != null) {
            detail.addRoleFactor(roleAssignment.getRoleKey(), factor);
        }
    }

    @Override
    public long determineStartTime(long startTimeForPeriod,
            long endTimeForPeriod, PricingPeriod period) {
        if (startTimeForPeriod == endTimeForPeriod) {
            return startTimeForPeriod;
        }
        return PricingPeriodDateConverter.getStartTime(startTimeForPeriod,
                period).getTimeInMillis();
    }

    @Override
    public long computeEndTimeForPaymentPreview(long endTimeForPeriod,
            long billingPeriodEnd, PricingPeriod period) {
        long endTimeForPaymentPreview = PricingPeriodDateConverter
                .getStartTimeOfNextPeriod(endTimeForPeriod - 1, period)
                .getTimeInMillis();
        if (endTimeForPaymentPreview > billingPeriodEnd) {
            endTimeForPaymentPreview = billingPeriodEnd;
        }
        return endTimeForPaymentPreview;
    }

    @Override
    public void computeParameterPeriodFactor(BillingInput billingInput,
            XParameterData parameterData, long startTimeForPeriod,
            long endTimeForPeriod) {

        if (subscriptionHasParameters(parameterData)) {
            List<XParameterIdData> notChargedParameters = new ArrayList<XParameterIdData>();
            Map<XParameterIdData, Set<XParameterPeriodValue>> notChargedParameterValues = new LinkedHashMap<XParameterIdData, Set<XParameterPeriodValue>>();

            for (XParameterIdData parameterIdData : parameterData.getIdData()) {
                resetParameterPeriodFactorsToZero(parameterIdData);
                Map<TimeSlice, LinkedList<XParameterPeriodValue>> valuesPerSlice = mapParameterValuesToTimeSlices(
                        billingInput, parameterData, startTimeForPeriod,
                        endTimeForPeriod, parameterIdData);
                if (valuesPerSlice.isEmpty()) {
                    notChargedParameters.add(parameterIdData);
                } else {
                    addNotChargedParameterValues(parameterIdData,
                            valuesPerSlice, notChargedParameterValues);
                    updateParameterPeriodFactorsPerTimeSlice(valuesPerSlice);
                }
            }

            removeNotChargedParametersAndValues(parameterData,
                    notChargedParameters, notChargedParameterValues);
        }
    }

    /**
     * Add all parameter values, which don't belong to any time slice, to the
     * given map
     * 
     * @param parameterIdData
     *            the parameterIdData
     * @param valuesPerSlice
     *            a map with the parameter values per time slice
     * @param notChargedParameterValues
     *            a map with the parameterValues, which are not charged (the key
     *            is the parameterIdData object)
     */
    private void addNotChargedParameterValues(
            XParameterIdData parameterIdData,
            Map<TimeSlice, LinkedList<XParameterPeriodValue>> valuesPerSlice,
            Map<XParameterIdData, Set<XParameterPeriodValue>> notChargedParameterValues) {
        Set<XParameterPeriodValue> parValuesInSlices = new HashSet<XParameterPeriodValue>();
        for (List<XParameterPeriodValue> parValues : valuesPerSlice.values()) {
            parValuesInSlices.addAll(parValues);
        }

        Set<XParameterPeriodValue> notChargedParValueSet = new HashSet<XParameterPeriodValue>(
                parameterIdData.getPeriodValues());
        notChargedParValueSet.removeAll(parValuesInSlices);
        if (notChargedParValueSet.size() > 0) {
            notChargedParameterValues.put(parameterIdData,
                    notChargedParValueSet);
        }
    }

    /**
     * Remove all parameters and parameter values, that are not charged, from
     * the given parameter data
     * 
     * @param parameterData
     *            the parameter data
     * @param notChargedParameters
     *            a list with the parameters, that are not charged
     * @param notChargedParameterValues
     *            a map with the parameterValues, which are not charged (the key
     *            is the parameterIdData object)
     */
    private void removeNotChargedParametersAndValues(
            XParameterData parameterData,
            List<XParameterIdData> notChargedParameters,
            Map<XParameterIdData, Set<XParameterPeriodValue>> notChargedParameterValues) {

        parameterData.getIdData().removeAll(notChargedParameters);

        for (XParameterIdData parameterIdData : parameterData.getIdData()) {
            Set<XParameterPeriodValue> notChargedParValueSet = notChargedParameterValues
                    .get(parameterIdData);
            if (notChargedParValueSet != null) {
                parameterIdData.getPeriodValues().removeAll(
                        notChargedParValueSet);
            }
        }
    }

    private boolean subscriptionHasParameters(XParameterData parameterData) {
        return parameterData != null;
    }

    private Map<TimeSlice, LinkedList<XParameterPeriodValue>> mapParameterValuesToTimeSlices(
            BillingInput billingInput, XParameterData parameterData,
            long startTimeForPeriod, long endTimeForPeriod,
            XParameterIdData parameterIdData) {
        Map<TimeSlice, LinkedList<XParameterPeriodValue>> valuesPerSlice = assignParameterValuesToTimeSlices(
                billingInput, startTimeForPeriod, endTimeForPeriod,
                parameterIdData, parameterData.getPeriod());
        markLastTimeSliceHavingParameters(valuesPerSlice);
        markFirstTimeSliceHavingParameters(valuesPerSlice);
        return valuesPerSlice;
    }

    private void updateParameterPeriodFactorsPerTimeSlice(
            Map<TimeSlice, LinkedList<XParameterPeriodValue>> perSlice) {
        for (TimeSlice timeSlice : perSlice.keySet()) {
            updateParameterPeriodFactor(timeSlice, perSlice.get(timeSlice));
        }
    }

    private void resetParameterPeriodFactorsToZero(
            XParameterIdData parameterIdData) {
        for (XParameterPeriodValue periodValue : parameterIdData
                .getPeriodValues()) {
            periodValue.setPeriodFactor(0D);
        }
    }

    private Map<TimeSlice, LinkedList<XParameterPeriodValue>> assignParameterValuesToTimeSlices(
            BillingInput billingInput, long startTimeForPeriod,
            long endTimeForPeriod, XParameterIdData parameterIdData,
            PricingPeriod pricingPeriod) {

        Map<TimeSlice, LinkedList<XParameterPeriodValue>> result = new LinkedHashMap<TimeSlice, LinkedList<XParameterPeriodValue>>();
        TimeSlice timeSlice = lastPeriodTimeSlice(endTimeForPeriod,
                billingInput.getBillingPeriodEnd(), pricingPeriod);
        while (timeSliceInRange(timeSlice, startTimeForPeriod, endTimeForPeriod)) {
            LinkedList<XParameterPeriodValue> values = retrieveParametersForTimeSlice(
                    parameterIdData.getPeriodValues(), timeSlice);
            result.put(timeSlice, values);
            timeSlice = timeSlice.previous();
        }
        return result;
    }

    private void markFirstTimeSliceHavingParameters(
            Map<TimeSlice, LinkedList<XParameterPeriodValue>> result) {
        Object[] timeSlices = result.keySet().toArray();
        for (int i = timeSlices.length - 1; i > -1; i--) {
            if (result.get(timeSlices[i]).size() > 0) {
                ((TimeSlice) timeSlices[i]).setFirstSlice(true);
                break;
            }
        }
    }

    private void markLastTimeSliceHavingParameters(
            Map<TimeSlice, LinkedList<XParameterPeriodValue>> result) {
        Object[] timeSlices = result.keySet().toArray();
        for (int i = 0; i < timeSlices.length; i++) {
            if (result.get(timeSlices[i]).size() > 0) {
                ((TimeSlice) timeSlices[i]).setLastSlice(true);
                break;
            }
        }
    }

    /**
     * @param periodValues
     *            expected to be sorted descending
     */
    LinkedList<XParameterPeriodValue> retrieveParametersForTimeSlice(
            List<XParameterPeriodValue> periodValues, TimeSlice timeSlice) {

        LinkedList<XParameterPeriodValue> result = new LinkedList<XParameterPeriodValue>();
        for (XParameterPeriodValue paramValue : periodValues) {
            if (paramValue.getStartTime() >= timeSlice.getStart()
                    && paramValue.getStartTime() <= timeSlice.getEnd()) {
                result.add(paramValue);
            }
        }

        XParameterPeriodValue lastBeforePeriod = lastParamBeforeTimeSlice(
                periodValues, timeSlice.getStart());
        if (lastBeforePeriod != null) {
            result.add(lastBeforePeriod);
        }

        return result;
    }

    XParameterPeriodValue lastParamBeforeTimeSlice(
            List<XParameterPeriodValue> periodValues, long timeSliceStart) {
        for (int i = 0; i < periodValues.size(); i++) {
            XParameterPeriodValue periodValue = periodValues.get(i);
            if (periodValue.getStartTime() < timeSliceStart) {
                return periodValue;
            }
        }
        return null;
    }

    /**
     * NOTE: in order to calculate the correct factor the valuesPerSlice list
     * needs to be modified by this method. Keep this in mind.
     */
    private void updateParameterPeriodFactor(TimeSlice timeSlice,
            LinkedList<XParameterPeriodValue> valuesPerSlice) {

        if (timeSlice.isLastButNotFirst()) {
            updatePeriodFactorLastSliceLastValue(timeSlice, valuesPerSlice);
        }

        if (timeSlice.isFirstButNotLast()) {
            updatePeriodFactorFirstSliceFirstValue(timeSlice, valuesPerSlice);
        }

        if (timeSlice.isFirstAndLast()) {
            updatePeriodFactorOneSliceLastValues(timeSlice, valuesPerSlice);
        }

        for (XParameterPeriodValue parameterPeriodValue : valuesPerSlice) {
            Calendar endTimeForCalculation = parameterEndTimeForPeriodCalculation(
                    timeSlice, parameterPeriodValue);
            Calendar startTimeForCalculation = parameterStartTimeForPeriodCalculation(
                    timeSlice, parameterPeriodValue);
            updateParameterPeriodFactor(timeSlice, parameterPeriodValue,
                    startTimeForCalculation, endTimeForCalculation);
        }

    }

    /**
     * The end time of the last parameter and the start time of the first
     * parameter within the single time slice have to be extended to the time
     * slice start respectively end time.
     * 
     * @param timeSlice
     *            the single time slice having at least on parameter
     * @param valuesPerSlice
     *            all parameter values for the time slice, the list will be
     *            modified by this method
     */
    private void updatePeriodFactorOneSliceLastValues(TimeSlice timeSlice,
            LinkedList<XParameterPeriodValue> valuesPerSlice) {
        if (valuesPerSlice.size() == 1) {
            valuesPerSlice.getFirst().setPeriodFactor(1D);
            valuesPerSlice.removeFirst();
        } else {
            updatePeriodFactorLastSliceLastValue(timeSlice, valuesPerSlice);
            updatePeriodFactorFirstSliceFirstValue(timeSlice, valuesPerSlice);
        }
    }

    /**
     * The end time of the last parameter value within the last time slice has
     * to be extended to the time slice end.
     * 
     * @param timeSlice
     *            the last time slice having parameters of the billing relevant
     *            period
     * @param valuesPerSlice
     *            all parameter values for the given slice, the list will be
     *            modified by this method
     */
    private void updatePeriodFactorLastSliceLastValue(TimeSlice timeSlice,
            LinkedList<XParameterPeriodValue> valuesPerSlice) {

        // The beginning of the next time slice is relevant for the period
        // calculation, not the last millisecond of the current time slice.
        Calendar startOfNextSlice = timeSlice.getStartOfNextSliceAsCalendar();

        XParameterPeriodValue lastPeriodValue = valuesPerSlice.getFirst();
        Calendar startTime = parameterStartTimeForPeriodCalculation(timeSlice,
                lastPeriodValue);
        updateParameterPeriodFactor(timeSlice, lastPeriodValue, startTime,
                startOfNextSlice);
        valuesPerSlice.removeFirst();
    }

    /**
     * The start time of the first parameter within the first time slice has to
     * be extended to the beginning of the time slice.
     * 
     * @param timeSlice
     *            the first time slice having parameters of the billing relevant
     *            period
     * @param valuesPerSlice
     *            all parameter values for the given slice, the list will be
     *            modified by this method
     */
    private void updatePeriodFactorFirstSliceFirstValue(TimeSlice timeSlice,
            LinkedList<XParameterPeriodValue> valuesPerSlice) {
        XParameterPeriodValue firstPeriodValue = valuesPerSlice.getLast();
        updateParameterPeriodFactor(
                timeSlice,
                firstPeriodValue,
                timeSlice.getStartAsCalendar(),
                parameterEndTimeForPeriodCalculation(timeSlice,
                        firstPeriodValue));
        valuesPerSlice.removeLast();
    }

    private void updateParameterPeriodFactor(TimeSlice timeSlice,
            XParameterPeriodValue parameterPeriodValue, Calendar start,
            Calendar end) {
        double periodFactor = computeFractionalFactorForTimeUnit(
                start.getTimeInMillis(), end.getTimeInMillis(),
                timeSlice.getPeriod());
        double currentFactor = parameterPeriodValue.getPeriodFactor();
        parameterPeriodValue.setPeriodFactor(currentFactor + periodFactor);
    }

    private Calendar parameterEndTimeForPeriodCalculation(TimeSlice timeSlice,
            XParameterPeriodValue parameterPeriodValue) {

        long endTimeForCalculation = 0;
        if (parameterPeriodValue.getEndTime() <= timeSlice.getEnd()) {
            endTimeForCalculation = parameterPeriodValue.getEndTime();
        } else {
            // The beginning of the next time slice is relevant for the
            // period calculation, not the last millisecond of the
            // current time slice.
            endTimeForCalculation = timeSlice.getStartOfNextSlice();
        }

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(endTimeForCalculation);
        return result;
    }

    private Calendar parameterStartTimeForPeriodCalculation(
            TimeSlice timeSlice, XParameterPeriodValue parameterPeriodValue) {

        long startTimeForCalculation = 0;
        if (parameterPeriodValue.getStartTime() < timeSlice.getStart()) {
            startTimeForCalculation = timeSlice.getStart();
        } else {
            startTimeForCalculation = parameterPeriodValue.getStartTime();
        }

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(startTimeForCalculation);
        return result;
    }

    private TimeSlice lastPeriodTimeSlice(long endTimeForPeriod,
            long billingPeriodEnd, PricingPeriod pricingPeriod) {

        long adjustedBillingPeriodEnd = PricingPeriodDateConverter
                .getStartTime(billingPeriodEnd, pricingPeriod)
                .getTimeInMillis();
        if (endTimeForPeriod >= adjustedBillingPeriodEnd) {
            endTimeForPeriod = adjustedBillingPeriodEnd - 1;
        }
        long start = startOfTimeSlice(endTimeForPeriod, pricingPeriod);
        long end = endOfTimeSlice(endTimeForPeriod, pricingPeriod);
        return new TimeSlice(start, end, pricingPeriod);
    }

    private long startOfTimeSlice(long baseTime, PricingPeriod period) {
        return PricingPeriodDateConverter.getStartTime(baseTime, period)
                .getTimeInMillis();
    }

    private long endOfTimeSlice(long baseTime, PricingPeriod period) {
        return PricingPeriodDateConverter.getStartTimeOfNextPeriod(baseTime,
                period).getTimeInMillis() - 1;
    }

    private boolean timeSliceInRange(TimeSlice timeSlice,
            long startTimeForPeriod, long endTimeForPeriod) {
        if ((timeSlice.getStart() < startTimeForPeriod && timeSlice.getEnd() < startTimeForPeriod)
                || (timeSlice.getStart() > endTimeForPeriod && timeSlice
                        .getEnd() > endTimeForPeriod)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isSuspendedAndResumedInSameTimeUnit(
            SubscriptionHistory current, SubscriptionHistory next,
            PriceModelHistory pm) {
        // is suspended and resumed in the same time unit? If so, ignore this
        // history entry, so that it is not charged twice!
        if (current.getStatus().isSuspendedOrSuspendedUpd()
                && next.getStatus().isActiveOrPendingUpd()
                && current.getProductObjKey() == next.getProductObjKey()
                && PricingPeriodDateConverter.getStartTime(
                        current.getModdate().getTime(), pm.getPeriod())
                        .getTimeInMillis() == PricingPeriodDateConverter
                        .getStartTime(next.getModdate().getTime(),
                                pm.getPeriod()).getTimeInMillis()) {
            return true;
        } else {
            return false;
        }
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

        resetUserFactors(parameterData);
        final List<UsageLicenseHistory> ulHistList = billingDao
                .loadUsageLicenses(input.getSubscriptionKey(),
                        startTimeForPeriod, endTimeForPeriod);
        List<TimeSlice> relevantTimeSlices = relevantTimeSlices(input,
                startTimeForPeriod, endTimeForPeriod, parameterData.getPeriod());
        Collection<XParameterIdData> parameterIds = parameterData.getIdData();
        for (XParameterIdData parameterIdData : parameterIds) {
            computeParameterUserFactorForParameterId(parameterIdData,
                    relevantTimeSlices, ulHistList);
        }
    }

    /**
     * Sets user assignment factors and role factors to zero.
     */
    private void resetUserFactors(XParameterData parameterData) {
        for (XParameterIdData parameterIdData : parameterData.getIdData()) {
            for (XParameterPeriodValue periodValue : parameterIdData
                    .getPeriodValues()) {
                periodValue.setUserAssignmentFactor(0);
                if (periodValue.getRolePrices() != null) {
                    Set<Long> containerKeys = periodValue.getRolePrices()
                            .getContainerKeys();
                    for (Long containerKey : containerKeys) {
                        Map<Long, RolePricingDetails> rolePricesForContainerKey = periodValue
                                .getRolePrices().getRolePricesForContainerKey(
                                        containerKey);
                        for (Long k : rolePricesForContainerKey.keySet()) {
                            rolePricesForContainerKey.get(k).setFactor(0D);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a list of time slices for the given period. Each time slice holds
     * the period type (week, day etc.) and its start and end date. <br />
     */
    List<TimeSlice> relevantTimeSlices(BillingInput input,
            long startTimeForPeriod, long endTimeForPeriod, PricingPeriod period) {

        List<TimeSlice> timeSlices = new ArrayList<TimeSlice>();
        TimeSlice timeSlice = lastPeriodTimeSlice(endTimeForPeriod,
                input.getBillingPeriodEnd(), period);
        long timeForBeginningOfTimeSlices = PricingPeriodDateConverter
                .getStartTime(input.getBillingPeriodStart(), period)
                .getTimeInMillis();
        if (timeForBeginningOfTimeSlices < startTimeForPeriod) {
            timeForBeginningOfTimeSlices = startTimeForPeriod;
        }
        while (timeSlice.getEnd() > timeForBeginningOfTimeSlices) {
            timeSlices.add(timeSlice);
            timeSlice = timeSlice.previous();
        }
        return timeSlices;
    }

    private void computeParameterUserFactorForParameterId(
            XParameterIdData parameterIdData, List<TimeSlice> timeSlices,
            List<UsageLicenseHistory> ulHistList) {

        for (TimeSlice timeSlice : timeSlices) {
            LinkedList<XParameterPeriodValue> periodValueForTimeSlice = retrieveParametersForTimeSlice(
                    parameterIdData.getPeriodValues(), timeSlice);
            UserAssignmentExtractor uaExtractor = new UserAssignmentExtractor(
                    ulHistList, timeSlice.getStart(), timeSlice.getEnd());
            uaExtractor.extract();
            for (Long userKey : uaExtractor.getUserKeys()) {
                computeParameterUserFactorForOneTimeSliceAndOneUser(timeSlice,
                        periodValueForTimeSlice,
                        uaExtractor.getUserAssignments(userKey));
            }
        }
    }

    void computeParameterUserFactorForOneTimeSliceAndOneUser(
            TimeSlice timeSlice,
            LinkedList<XParameterPeriodValue> periodValues,
            List<UserAssignment> userAssignmentsForOneUser) {

        List<XParameterPeriodValue> periodValuesHavingUserAssignment = determinePeriodValuesHavingUserAssignment(
                periodValues, userAssignmentsForOneUser, timeSlice.getPeriod());

        long endTime = timeSlice.getEnd();
        int index = 0;
        for (XParameterPeriodValue periodValue : periodValuesHavingUserAssignment) {
            boolean isLastPeriodValue = (index == 0);
            boolean isFirstPeriodValue = (index == periodValuesHavingUserAssignment
                    .size() - 1);

            long startTime;
            if (isFirstPeriodValue) {
                startTime = timeSlice.getStart();
            } else {
                startTime = determineUserAssignmentStartTimeForPeriodValue(
                        userAssignmentsForOneUser, periodValue);
            }

            UserAssignmentFactors factors = computeParameterUserFactorForOnePeriodValue(
                    timeSlice, userAssignmentsForOneUser, startTime, endTime,
                    isFirstPeriodValue, isLastPeriodValue);

            periodValue.setUserAssignmentFactor(periodValue
                    .getUserAssignmentFactor() + factors.getBasicFactor());
            updateRoleFactorForPeriodValue(periodValue, factors);

            endTime = startTime;
            index++;
        }
    }

    private List<XParameterPeriodValue> determinePeriodValuesHavingUserAssignment(
            LinkedList<XParameterPeriodValue> periodValues,
            List<UserAssignment> userAssignments, PricingPeriod period) {
        List<XParameterPeriodValue> periodValuesHavingUserAssignment = new ArrayList<>();
        int index = 0;

        for (XParameterPeriodValue periodValue : periodValues) {
            long periodEnd = periodValue.getEndTime();
            // Extend the last parameter value to the end of the time slice
            if (index == 0) {
                periodEnd = PricingPeriodDateConverter
                        .getStartTimeOfNextPeriod(periodEnd, period)
                        .getTimeInMillis();
            }

            if (userAssignmentExistInPeriod(userAssignments,
                    periodValue.getStartTime(), periodEnd)) {
                periodValuesHavingUserAssignment.add(periodValue);
            }
            index++;
        }

        return periodValuesHavingUserAssignment;
    }

    boolean userAssignmentExistInPeriod(List<UserAssignment> userAssignments,
            long periodStart, long periodEnd) {
        for (UserAssignment userAssignment : userAssignments) {
            if (isUserAssignmentInPeriod(userAssignment, periodStart, periodEnd)) {
                return true;
            }
        }
        return false;
    }

    private long determineUserAssignmentStartTimeForPeriodValue(
            List<UserAssignment> userAssignmentsForOneUser,
            XParameterPeriodValue periodValue) {
        UserAssignment oldestUserAssignment = findOldestUserAssignmentForPeriod(
                userAssignmentsForOneUser, periodValue.getStartTime(),
                periodValue.getEndTime());
        long startTime = periodValue.getStartTime();
        if (startTime < oldestUserAssignment.getUsageStartTime()) {
            startTime = oldestUserAssignment.getUsageStartTime();
        }
        return startTime;
    }

    UserAssignment findOldestUserAssignmentForPeriod(
            List<UserAssignment> userAssignments, long periodStart,
            long periodEnd) {
        UserAssignment oldestUserAssignment = null;
        for (UserAssignment userAssignment : userAssignments) {
            if (isUserAssignmentInPeriod(userAssignment, periodStart, periodEnd)) {
                oldestUserAssignment = userAssignment;
            }
        }
        return oldestUserAssignment;
    }

    boolean isUserAssignmentInPeriod(UserAssignment userAssignment,
            long periodStart, long periodEnd) {
        if (userAssignment.getUsageStartTime() < periodEnd
                && userAssignment.getUsageEndTime() >= periodStart) {
            return true;
        }
        return false;
    }

    private UserAssignmentFactors computeParameterUserFactorForOnePeriodValue(
            TimeSlice timeSlice, List<UserAssignment> userAssignments,
            long periodStart, long periodEnd, boolean needsExtendToSliceStart,
            boolean needsExtendToSliceEnd) {

        String userId = userAssignments.get(0).getUserId();
        long userKey = userAssignments.get(0).getUserKey();

        double factor = computeFactorForTimeSlice(timeSlice, periodStart,
                periodEnd, needsExtendToSliceStart, needsExtendToSliceEnd);
        UserAssignmentFactors result = new UserAssignmentFactors();
        storeUserFactorToResult(result, factor, userKey, userId);

        computeParameterUserRoleFactorForOnePeriodValue(result, userKey,
                timeSlice, userAssignments, periodStart, periodEnd,
                needsExtendToSliceStart, needsExtendToSliceEnd);
        return result;
    }

    private UserAssignmentFactors computeParameterUserRoleFactorForOnePeriodValue(
            UserAssignmentFactors result, long userKey, TimeSlice timeSlice,
            List<UserAssignment> userAssignments, long periodStart,
            long periodEnd, boolean needsExtendToSliceStart,
            boolean needsExtendToSliceEnd) {

        List<UserRoleAssignment> roleAssignments = determineRoleAssignmentsForOnePeriodValue(
                userAssignments, periodStart, periodEnd);

        long endTime = periodEnd;
        int index = 0;
        for (UserRoleAssignment roleAssignment : roleAssignments) {
            boolean isLastRoleForPeriod = (index == 0);
            boolean isFirstRoleForPeriod = (index == roleAssignments.size() - 1);

            long startTime = roleAssignment.getStartTime();
            if (startTime < periodStart) {
                startTime = periodStart;
            }

            double factor = computeFactorForTimeSlice(timeSlice, startTime,
                    endTime, needsExtendToSliceStart && isFirstRoleForPeriod,
                    needsExtendToSliceEnd && isLastRoleForPeriod);
            storeUserRoleFactorToResult(result, roleAssignment, factor, userKey);

            endTime = startTime;
            index++;
        }
        return result;
    }

    private List<UserRoleAssignment> determineRoleAssignmentsForOnePeriodValue(
            List<UserAssignment> userAssignments, long periodStart,
            long periodEnd) {

        List<UserRoleAssignment> result = new ArrayList<UserRoleAssignment>();
        List<UserRoleAssignment> roleAssignments = determineRoleAssignmentsWithFillingBlank(userAssignments);
        for (UserRoleAssignment roleAssignment : roleAssignments) {
            if (isUserRoleAssignmentInPeriod(roleAssignment, periodStart,
                    periodEnd)) {
                result.add(roleAssignment);
            }
        }
        return result;
    }

    boolean isUserRoleAssignmentInPeriod(UserRoleAssignment roleAssignment,
            long periodStart, long periodEnd) {
        if (roleAssignment.getStartTime() < periodEnd
                && roleAssignment.getEndTime() >= periodStart) {
            return true;
        }
        return false;
    }

    void updateRoleFactorForPeriodValue(XParameterPeriodValue periodValue,
            UserAssignmentFactors factors) {

        Map<Long, Double> roleFactors = factors.getRoleFactors();

        if (roleFactors.isEmpty() || parameterRolePriceNotDefined(periodValue)) {
            return;
        }

        RolePricingData pricingData = periodValue.getRolePrices();
        Long priceParamKey = periodValue.getKey();
        Map<Long, RolePricingDetails> rolePrices = pricingData
                .getRolePricesForContainerKey(priceParamKey);
        for (Long roleKey : roleFactors.keySet()) {
            if (parameterRolePriceDefined(rolePrices, roleKey)) {
                RolePricingDetails rolePricingDetails = rolePrices.get(roleKey);
                double currentFactor = rolePricingDetails.getFactor();
                rolePrices.get(roleKey).setFactor(
                        currentFactor + roleFactors.get(roleKey).doubleValue());
            }
        }
    }

    boolean parameterRolePriceNotDefined(XParameterPeriodValue periodValue) {
        return periodValue.getRolePrices() == null;
    }

    boolean parameterRolePriceDefined(Map<Long, RolePricingDetails> rolePrices,
            Long roleKey) {
        return rolePrices.get(roleKey) != null;
    }

}
