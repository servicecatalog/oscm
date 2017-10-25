/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                    
 *                                                                              
 *  Creation Date: 18.03.2011                                                      
 *                                                                              
 *  Completion Time: 18.03.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author weiser
 * 
 */
public class DateConverter {

    public static final String TIMEZONE_ID_GMT = "GMT";

    public static final String DEFAULT_DTP = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DTP_WITHOUT_MILLIS = "yyyy-MM-dd HH:mm:ss";
    public static final String DTP_WITHOUT_MILLIS_WITH_TIMEZONE = "yyyy-MM-dd HH:mm:ss z";

    public static final long MILLISECONDS_PER_DAY = 86400000L;
    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;
    public static final int MINUTES_PER_HOUR = 60;

    /**
     * Create a calendar object for the given time and convert the time to a
     * time in the current time zone representing the start of the current hour.
     * 
     * @param baseTime
     *            a time in milliseconds
     * @return a Calendar object representing the start of the hour
     */
    private static final Calendar getCalendar(long baseTime) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long timeInMillis = cal.getTimeInMillis();
        if (timeInMillis > baseTime) {
            // A Calendar object may be ambiguous because of a daylight saving
            // time change, i.e. October 27th 2013 02:00 exists two times for
            // CEST and CET. If getTimeInMillis() is called for such a date,
            // and the milliseconds must be computed because some Calendar
            // fields were changed, the GregorianCalendar implementation always
            // takes the standard time, i.e. October 27th 2013 02:00 CET.
            // But this is wrong if we want to compute the beginning of the
            // hour October 27th 2013 02:00 CEST, thus we must subtract one
            // hour in this case.
            cal.setTimeInMillis(timeInMillis - MILLISECONDS_PER_HOUR);
        }

        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the current day.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return start of current day
     */
    public static Calendar getStartOfDay(long baseTime) {
        final Calendar cal = getCalendar(baseTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the next day.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return start of next day
     */
    public static Calendar getStartOfNextDay(long baseTime) {
        final Calendar cal = getStartOfDay(baseTime);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the current hour.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of current hour
     */
    public static Calendar getStartOfHour(long baseTime) {
        return getCalendar(baseTime);
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the next hour.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of the next hour
     */
    public static Calendar getStartOfNextHour(long baseTime) {
        final Calendar cal = getCalendar(baseTime);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the week. It is assumed that weeks always start on MONDAY
     * independently of the server locale.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of the week based on the given
     *         baseTime
     */
    public static Calendar getStartOfWeek(long baseTime) {
        final Calendar cal = getCalendar(baseTime);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the end of the week. It is assumed that weeks always start on MONDAY
     * independently of the server locale.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the end of the week based on the given
     *         baseTime
     */
    public static Calendar getEndOfWeek(long baseTime) {
        final Calendar cal = getCalendar(baseTime);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the start of the next week. It is assumed that weeks always start on
     * MONDAY independently of the server locale.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of the next week
     */
    public static Calendar getStartOfNextWeek(long baseTime) {
        final Calendar cal = getStartOfWeek(baseTime);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        return cal;
    }

    /**
     * Converts the given time to the first day of the month using the current
     * time zone.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of the first day of the month
     */
    public static Calendar getFirstDayMonth(long baseTime) {
        final Calendar cal = getStartOfDay(baseTime);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    /**
     * Converts the given time to the first day of the next month using the
     * current time zone.
     * 
     * @param baseTime
     *            time in milliseconds to be converted
     * @return Calendar representing the start of the first day of the next
     *         month
     */
    public static Calendar getFirstDayOfNextMonth(long baseTime) {
        final Calendar cal = getFirstDayMonth(baseTime);
        cal.add(Calendar.MONTH, 1);
        return cal;
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the beginning of the day - 00:00:00 000 o'clock.
     * 
     * @param timeStamp
     *            the time to convert in milliseconds
     * @return the converted time stamp
     */
    public static final long getBeginningOfDayInCurrentTimeZone(long timeStamp) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the beginning of the day - 00:00:00 000 o'clock.
     * 
     * @param timeStamp
     *            the time to convert as Date object
     * @return the converted time stamp
     */
    public static final Long getBeginningOfDayInCurrentTimeZone(Date date) {
        if (date == null) {
            return null;
        }
        return Long.valueOf(getBeginningOfDayInCurrentTimeZone(date.getTime()));
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the beginning of the next day - 00:00:00 000 o'clock.
     * 
     * @param timeStamp
     *            the time to convert in milliseconds
     * @return the converted time stamp
     */
    public static final long getBeginningOfNextDayInCurrentTimeZone(
            long timeStamp) {
        return getStartOfNextDay(timeStamp).getTimeInMillis();
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the beginning of the next day - 00:00:00 000 o'clock.
     * 
     * @param timeStamp
     *            the time to convert in Date object
     * @return the converted time stamp
     */
    public static final Long getBeginningOfNextDayInCurrentTimeZone(Date date) {
        if (date == null) {
            return null;
        }
        return Long.valueOf(getBeginningOfNextDayInCurrentTimeZone(date
                .getTime()));
    }

    /**
     * Converts the given time to a time in the current time zone representing
     * the end of the day - 23:59:59 999 o'clock.
     * 
     * @param timeStamp
     *            the time to convert in milliseconds
     * @return the converted time stamp
     */
    public static final long getEndOfDayInCurrentTimeZone(long timeStamp) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static final String convertLongToIso8601DateTimeFormat(
            long milliseconds, TimeZone timeZone) {
        final Date date = new Date(milliseconds);
        final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DTP);
        sdf.setTimeZone(timeZone);
        return sdf.format(date);
    }

    public static final String convertLongToDateTimeFormat(long milliseconds,
            TimeZone timeZone, String pattern) {
        final Date date = new Date(milliseconds);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timeZone);
        return sdf.format(date);
    }

    /**
     * Converts an UTC offset into a String showing the time difference to UTC
     * in hours and minutes
     * 
     * @param utcOffset
     *            the offset to UTC in milliseconds
     */
    public static String convertToUTCString(int utcOffset) {
        int absUtcOffset = Math.abs(utcOffset);
        return String.format(
                "%s%s%02d:%02d",
                "UTC",
                utcOffset >= 0 ? "+" : "-",
                Integer.valueOf(absUtcOffset / MILLISECONDS_PER_HOUR),
                Integer.valueOf((absUtcOffset / MILLISECONDS_PER_MINUTE)
                        % MINUTES_PER_HOUR));
    }

    /**
     * Converts the current timezone to an UTC string
     */
    public static String getCurrentTimeZoneAsUTCString() {
        return convertToUTCString(Calendar.getInstance().getTimeZone()
                .getRawOffset());
    }

}
