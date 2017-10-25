/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 24, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.util.Calendar;

/**
 * @author tokoda
 * 
 */
class CutOffDayConverter {

    /**
     * Return the billing start date for the specified cut off day.
     * 
     * @param time
     * @param subscriptionCutOffDay
     * @return
     */
    public static Calendar getBillingStartTimeForCutOffDay(long time,
            int cutOffDay) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final long day = cal.get(Calendar.DAY_OF_MONTH);
        if (cutOffDay < day
                || (cutOffDay == day && time > cal.getTimeInMillis())) {
            cal.add(Calendar.MONTH, 1);
        }
        cal.set(Calendar.DAY_OF_MONTH, cutOffDay);
        return cal;
    }

    /**
     * Return the billing end date for the specified cut off day.
     * 
     * @param time
     * @param subscriptionCutOffDay
     * @return
     */
    public static Calendar getBillingEndTimeForCutOffDay(long time) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        return cal;
    }

}
