/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 6, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tokoda
 * 
 */
public class PeriodTest {
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd hh:mm:ss";

    @Test
    public void getDuration_Day() {
        // given

        // when
        long duration = Period.DAY.getDuration(0);

        // then
        assertEquals(86400000L, duration);
    }

    @Test
    public void getDuration_MonthFeb2012() {
        // given
        long currentTime = calculateMillis("2012-03-01 11:11:11");

        // when
        long duration = Period.MONTH.getDuration(currentTime);

        // then
        long expected = 86400000L * 29L;
        assertEquals(expected, duration);
    }

    @Test
    public void getDuration_MonthJan() {
        // given
        long currentTime = calculateMillis("2012-02-01 11:11:11");

        // when
        long duration = Period.MONTH.getDuration(currentTime);

        // then
        long expected = 86400000L * 31L;
        assertEquals(expected, duration);
    }

    private long calculateMillis(String dateSource) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(DATE_FORMAT_PATTERN);
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(dateSource));
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
