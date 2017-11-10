/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oscm.interceptor.DateFactory;

/**
 * @author muenz
 * 
 */
public class DateTimeHandling {

    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Returns the defined invocation time in millis. DateSource string by using
     * following pattern {@link DateTimeHandling#DATE_FORMAT_PATTERN}
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @return invocation time in millis.
     */
    public static long defineInvocationTime(String dateSource) {
        final long invocationTime = calculateMillis(dateSource);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(invocationTime);
        DateFactory.setInstance(new TestDateFactory(calendar.getTime()));
        return invocationTime;
    }

    /**
     * Calculates millis for dateSource string by using following pattern
     * {@link DateTimeHandling#DATE_FORMAT_PATTERN}. Milliseconds are set to 0.
     * 
     * @param dateFormat
     * @return millis for given date string
     */
    public static long calculateMillis(String dateSource) {
        return parseToCalendar(dateSource).getTimeInMillis();
    }

    /**
     * Calculates Date for dateSource string by using following pattern
     * {@link DateTimeHandling#DATE_FORMAT_PATTERN}. Milliseconds are set to 0.
     * 
     * @param dateFormat
     * @return Date for given date string
     */
    public static Date calculateDate(String dateSource) {
        return parseToCalendar(dateSource).getTime();
    }

    private static Calendar parseToCalendar(String dateSource) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(DATE_FORMAT_PATTERN);
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(dateSource));
            c.set(Calendar.MILLISECOND, 0);
            return c;
        } catch (ParseException e) {
            fail("Unable to parse date.");
            return null;
        }
    }

    /**
     * Converts the given number of weeks to milliseconds
     * 
     * @param weeks
     * @return the weeks in milliseconds
     */
    public static long weeksToMillis(double weeks) {
        return (long) (weeks * 1000 * 7 * 24 * 60 * 60);
    }

    /**
     * Converts the given number of days to milliseconds
     * 
     * @param days
     * @return the days in milliseconds
     */
    public static long daysToMillis(double days) {
        return (long) (days * 1000 * 24 * 60 * 60);
    }

    /**
     * Converts the given number of hours to milliseconds
     * 
     * @param hours
     * @return the hours in milliseconds
     */
    public static long hoursToMillis(double hours) {
        return (long) (hours * 1000 * 60 * 60);
    }

    /**
     * check if Day light saving TimeZone
     * 
     * @param startTime
     * @param endTime
     * @return true:day light saving TimeZone; false Normal TimeZone
     */
    public static boolean isDayLightSaving(long startTime, long endTime) {
        long offset = TimeZone.getDefault().getOffset(startTime)
                - TimeZone.getDefault().getOffset(endTime);
        if (offset != 0) {
            return true;
        }
        return false;
    }

}
