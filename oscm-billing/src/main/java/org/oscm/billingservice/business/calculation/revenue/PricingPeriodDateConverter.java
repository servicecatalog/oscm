/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.util.Calendar;

import org.oscm.converter.DateConverter;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class PricingPeriodDateConverter {

    /**
     * Converts the given time to a start time of the corresponding time period.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @param period
     *            Type of period to convert the time
     * @return Calendar representing the start time of the current time period
     *         for current time
     */
    public static Calendar getStartTime(long baseTime, PricingPeriod period) {
        Calendar startTime = null;
        switch (period) {
        case HOUR:
            startTime = DateConverter.getStartOfHour(baseTime);
            break;
        case DAY:
            startTime = DateConverter.getStartOfDay(baseTime);
            break;
        case WEEK:
            startTime = DateConverter.getStartOfWeek(baseTime);
            break;
        case MONTH:
            startTime = DateConverter.getFirstDayMonth(baseTime);
            break;
        default:
            break;
        }
        return startTime;
    }

    /**
     * Determines the start time of the time period, which succeeds the time
     * period, that corresponds to the given time.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @param period
     *            Type of period to convert the time
     * @return Calendar representing the start time of the next time period
     */
    public static Calendar getStartTimeOfNextPeriod(long baseTime,
            PricingPeriod period) {
        Calendar startTimeOfNextPeriod = null;
        switch (period) {
        case HOUR:
            startTimeOfNextPeriod = DateConverter.getStartOfNextHour(baseTime);
            break;
        case DAY:
            startTimeOfNextPeriod = DateConverter.getStartOfNextDay(baseTime);
            break;
        case WEEK:
            startTimeOfNextPeriod = DateConverter.getStartOfNextWeek(baseTime);
            break;
        case MONTH:
            startTimeOfNextPeriod = DateConverter
                    .getFirstDayOfNextMonth(baseTime);
            break;
        default:
            break;
        }
        return startTimeOfNextPeriod;
    }

    /**
     * Determines the start time of the time period, which succeeds the time
     * period, that corresponds to the given time.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @param period
     *            Type of period to convert the time
     * @return <code>long</code> value representing the start time of the next
     *         time period
     */
    public static long getStartTimeOfNextPeriodAsLong(long baseTime,
            PricingPeriod period) {
        return getStartTimeOfNextPeriod(baseTime, period).getTimeInMillis();
    }

}
