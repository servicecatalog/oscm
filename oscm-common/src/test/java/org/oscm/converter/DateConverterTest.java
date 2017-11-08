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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author weiser
 * 
 */
public class DateConverterTest {

    @Test
    public void ctors() {
        new DateConverter();
    }

    @Test
    public void testGetBeginningOfDayInCurrentTimeZone_SameTimeZone() {
        Calendar cal = Calendar.getInstance();

        long expected = cal.getTimeInMillis() - differenceToBeginOfDay(cal);
        long result = DateConverter.getBeginningOfDayInCurrentTimeZone(cal
                .getTimeInMillis());
        Assert.assertEquals("Difference: " + (result - expected), expected,
                result);
    }

    @Test
    public void testGetBeginningOfNextDayInCurrentTimeZone_SameTimeZone() {
        Calendar cal = Calendar.getInstance();

        long expected = cal.getTimeInMillis() - differenceToBeginOfDay(cal)
                + DateConverter.MILLISECONDS_PER_DAY;
        long result = DateConverter.getBeginningOfNextDayInCurrentTimeZone(cal
                .getTimeInMillis());
        Assert.assertEquals("Difference: " + (result - expected), expected,
                result);
    }

    @Test
    public void testGetBeginningOfDayInCurrentTimeZone_SameTimeZone_Date() {
        Calendar cal = Calendar.getInstance();

        long expected = cal.getTimeInMillis() - differenceToBeginOfDay(cal);
        Long result = DateConverter
                .getBeginningOfDayInCurrentTimeZone(new Date(cal
                        .getTimeInMillis()));
        Assert.assertEquals("Difference: " + (result.longValue() - expected),
                expected, result.longValue());
    }

    @Test
    public void testGetBeginningOfNextDayInCurrentTimeZone_SameTimeZone_Date() {
        Calendar cal = Calendar.getInstance();

        long expected = cal.getTimeInMillis() - differenceToBeginOfDay(cal)
                + DateConverter.MILLISECONDS_PER_DAY;
        Long result = DateConverter
                .getBeginningOfNextDayInCurrentTimeZone(new Date(cal
                        .getTimeInMillis()));
        Assert.assertEquals("Difference: " + (result.longValue() - expected),
                expected, result.longValue());
    }

    @Test
    public void testGetEndOfDayInCurrentTimeZone_SameTimeZone() {
        Calendar cal = Calendar.getInstance();
        long currentTimeInMillis = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long expected = cal.getTimeInMillis();

        long result = DateConverter
                .getEndOfDayInCurrentTimeZone(currentTimeInMillis);
        Assert.assertNotSame(Long.valueOf(expected),
                Long.valueOf(currentTimeInMillis));
        Assert.assertEquals("Difference: " + (result - expected), expected,
                result);
    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_1() {
        String pattern = "MM/dd/yyyy";

        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone("GMT"), pattern);
        Assert.assertEquals("01/01/1970", dateString);
    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_DEFAULT_DTP_1() {
        String pattern = DateConverter.DEFAULT_DTP; // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone(DateConverter.TIMEZONE_ID_GMT), pattern);
        Assert.assertEquals("1970-01-01T00:00:00.000Z", dateString);
    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_DEFAULT_DTP() {
        String pattern = DateConverter.DEFAULT_DTP; // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone("GMT"), pattern);
        Assert.assertEquals("1970-01-01T00:00:00.000Z", dateString);
    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_DTP_WITHOUT_MILLIS() {
        String pattern = DateConverter.DTP_WITHOUT_MILLIS; // "yyyy-MM-dd HH:mm:ss"
        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone("GMT"), pattern);
        Assert.assertEquals("1970-01-01 00:00:00", dateString);
    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_DTP_WITHOUT_MILLIS_WITH_TIMEZONE() {
        String pattern = DateConverter.DTP_WITHOUT_MILLIS_WITH_TIMEZONE; // "yyyy-MM-dd HH:mm:ss z"
        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone("GMT"), pattern);
        Assert.assertEquals("1970-01-01 00:00:00 GMT", dateString);

    }

    @Test
    public void testConvertZeroToGmtDate_Pattern_2() {
        String pattern = "MM-dd-yyyy";
        String dateString = DateConverter.convertLongToDateTimeFormat(0,
                TimeZone.getTimeZone("GMT"), pattern);
        Assert.assertEquals("01-01-1970", dateString);

    }

    @Test
    public void testConvertZeroToGmtDate() {
        String dateString = DateConverter.convertLongToIso8601DateTimeFormat(0,
                TimeZone.getTimeZone("GMT"));
        Assert.assertEquals("1970-01-01T00:00:00.000Z", dateString);
    }

    @Test
    public void testConvertZeroToCetDate() {
        String dateString = DateConverter.convertLongToIso8601DateTimeFormat(0,
                TimeZone.getTimeZone("CET"));
        Assert.assertEquals("1970-01-01T01:00:00.000Z", dateString);
    }

    @Test
    public void testConvertZeroToGmtMinusOneDate() {
        String dateString = DateConverter.convertLongToIso8601DateTimeFormat(0,
                TimeZone.getTimeZone("GMT-1"));
        Assert.assertEquals("1969-12-31T23:00:00.000Z", dateString);
    }

    @Test
    public void testConvertPositiveLongToGmtDate() {
        String dateString = DateConverter.convertLongToIso8601DateTimeFormat(
                DateConverter.MILLISECONDS_PER_DAY + 1,
                TimeZone.getTimeZone("GMT"));
        Assert.assertEquals("1970-01-02T00:00:00.001Z", dateString);
    }

    @Test
    public void testConvertNegativeLongToGmtDate() {
        String dateString = DateConverter.convertLongToIso8601DateTimeFormat(
                -(DateConverter.MILLISECONDS_PER_DAY + 1),
                TimeZone.getTimeZone("GMT"));
        Assert.assertEquals("1969-12-30T23:59:59.999Z", dateString);
    }

    @Test
    public void testGetBeginningOfDayInCurrentTimeZone_NullDate() {
        Assert.assertNull(DateConverter
                .getBeginningOfDayInCurrentTimeZone(null));
    }

    @Test
    public void testGetBeginningOfNextDayInCurrentTimeZone_NullDate() {
        Assert.assertNull(DateConverter
                .getBeginningOfNextDayInCurrentTimeZone(null));
    }

    private long differenceToBeginOfDay(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int millis = cal.get(Calendar.MILLISECOND);

        long difference = millis + (second * 1000) + (minute * 60 * 1000)
                + (hour * 60 * 60 * 1000);
        return difference;
    }

    @Test
    public void getStartOfDay_middleOfDay() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 12:53:16 GMT)
        Calendar startOfDay = DateConverter.getStartOfDay(1355489596000L);

        // then
        assertEquals(1355443200000L, startOfDay.getTimeInMillis());
    }

    @Test
    public void getStartOfDay_startOfDay() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Tue, 01 Jan 2013 00:00:00 GMT)
        Calendar startOfDay = DateConverter.getStartOfDay(1356998400000L);

        // then
        assertEquals(1356998400000L, startOfDay.getTimeInMillis());
    }

    @Test
    public void getStartOfNextDay() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 12:53:16 GMT)
        Calendar startOfDay = DateConverter.getStartOfNextDay(1355489596000L);

        // then
        assertEquals(1355529600000L, startOfDay.getTimeInMillis());
    }

    @Test
    public void getStartOfNextDay_endOfDay() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Tue, 01 Jan 2013 23:59:59 GMT)
        Calendar startOfDay = DateConverter.getStartOfNextDay(1357084799999L);

        // then
        assertEquals(1357084800000L, startOfDay.getTimeInMillis());
    }

    @Test
    public void getStartOfHour() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 13:16:05 GMT)
        Calendar startOfHour = DateConverter.getStartOfHour(1355490965000L);

        // then
        assertEquals(1355490000000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfHour_SummerTimeToWinterTimeChange() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        // when (Sun, 27 Oct 2013 02:01:40 CEST)
        Calendar startOfHour = DateConverter.getStartOfHour(1382832100000L);

        // then
        assertEquals(1382832000000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfNextHour_SummerTimeToWinterTimeChange() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        // when (Sun, 27 Oct 2013 02:01:40 CEST)
        Calendar startOfHour = DateConverter.getStartOfNextHour(1382832100000L);

        // then
        // should be Sun, 27 Oct 2013 02:00:00 CET!
        assertEquals(1382835600000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfNextHour_SummerTimeToWinterTimeChange2() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        // when (Sun, 27 Oct 2013 01:01:40 CEST)
        Calendar startOfHour = DateConverter.getStartOfNextHour(1382828500000L);

        // then
        // should be Sun, 27 Oct 2013 02:00:00 CEST
        assertEquals(1382832000000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfHour_WinterTimeToSummerTimeChange() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        // when (Sun, 31 March 2013 03:10:00 CEST)
        Calendar startOfHour = DateConverter.getStartOfHour(1364692200000L);

        // then
        // should be Sun, 31 March 2013 03:00:00 CEST
        assertEquals(1364691600000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfNextHour_WinterTimeToSummerTimeChange() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        // when (Sun, 31 March 2013 01:10:00 CEST)
        Calendar startOfHour = DateConverter.getStartOfNextHour(1364688600000L);

        // then
        // should be Sun, 31 March 2013 03:00:00 CEST
        assertEquals(1364691600000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfNextHour() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 13:16:05 GMT)
        Calendar startOfHour = DateConverter.getStartOfNextHour(1355490965000L);

        // then
        assertEquals(1355493600000L, startOfHour.getTimeInMillis());
    }

    @Test
    public void getStartOfWeek_startInLastMonth() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Sat, 01 Dec 2012 14:00:00 GMT)
        Calendar startOfWeek = DateConverter.getStartOfWeek(1354370400000L);

        // then
        assertEquals(1353888000000L, startOfWeek.getTimeInMillis());
    }

    @Test
    public void getStartOfWeek() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Mon, 10 Dec 2012 02:00:00 GMT)
        Calendar startOfWeek = DateConverter.getStartOfWeek(1355104800000L);

        // then
        assertEquals(1355097600000L, startOfWeek.getTimeInMillis());
    }

    @Test
    public void getEndOfWeek_endInNextMonth() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Mon, 26 Nov 2012 00:00:00 GMT)
        Calendar endOfWeek = DateConverter.getEndOfWeek(1353888000000L);

        // then
        assertEquals(1354492799999L, endOfWeek.getTimeInMillis());
    }

    @Test
    public void getEndOfWeek() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 13:38:45 GMT)
        Calendar endOfWeek = DateConverter.getEndOfWeek(1355492325000L);

        // then
        assertEquals(1355702399999L, endOfWeek.getTimeInMillis());
    }

    @Test
    public void getStartOfNextWeek_EndOfYear() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Mon, 31 Dec 2012 0:0:0 GMT)
        Calendar startOfNextWeek = DateConverter
                .getStartOfNextWeek(1356912000000L);

        // then (Mon, 7 Jan 2013 0:0:0 GMT)
        assertEquals(1357516800000L, startOfNextWeek.getTimeInMillis());
    }

    @Test
    public void getStartOfNextWeek_EndOfWeek() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Son, 6 Jan 2013 23:59:59 GMT)
        Calendar startOfNextWeek = DateConverter
                .getStartOfNextWeek(1357516799000L);

        // then (Mon, 7 Jan 2013 0:0:0 GMT)
        assertEquals(1357516800000L, startOfNextWeek.getTimeInMillis());
    }

    @Test
    public void getFirstDayOfMonth() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Fri, 14 Dec 2012 13:38:45 GMT)
        Calendar firstDayMonth = DateConverter.getFirstDayMonth(1355492325000L);

        // then
        assertEquals(1354320000000L, firstDayMonth.getTimeInMillis());
    }

    @Test
    public void getFirstDayOfNextMonth_EndOfYear() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (Son, 1 Dec 2012 0:0:0 GMT)
        Calendar firstDayMonth = DateConverter
                .getFirstDayOfNextMonth(1354320000000L);

        // then (TUE, 1 Jan 2013 0:0:0 GMT)
        assertEquals(1356998400000L, firstDayMonth.getTimeInMillis());
    }

    @Test
    public void getFirstDayOfNextMonth_EndOfMonth() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // when (THU, 31 Jan 2013 23:59:59 GMT)
        Calendar firstDayMonth = DateConverter
                .getFirstDayOfNextMonth(1357084799000L);

        // then (FRI, 1 Feb 2013 0:0:0 GMT)
        assertEquals(1359676800000L, firstDayMonth.getTimeInMillis());
    }

    @Test
    public void convertToUTCString() {
        // when
        String utcString = DateConverter
                .convertToUTCString(DateConverter.MILLISECONDS_PER_HOUR
                        + DateConverter.MILLISECONDS_PER_HOUR / 2);

        // then
        assertEquals("Wrong UTC String", "UTC+01:30", utcString);
    }

    @Test
    public void convertToUTCString_negative() {
        // when
        String utcString = DateConverter.convertToUTCString(-2
                * DateConverter.MILLISECONDS_PER_HOUR
                - DateConverter.MILLISECONDS_PER_MINUTE);

        // then
        assertEquals("Wrong UTC String", "UTC-02:01", utcString);
    }

    @Test
    public void getCurrentTimeZoneAsUTCString() {
        // given
        int currentTimeZoneOffset = Calendar.getInstance().getTimeZone()
                .getRawOffset();

        // when
        String utcString = DateConverter.getCurrentTimeZoneAsUTCString();

        // then
        assertEquals("Wrong UTC String",
                DateConverter.convertToUTCString(currentTimeZoneOffset),
                utcString);
    }

}
