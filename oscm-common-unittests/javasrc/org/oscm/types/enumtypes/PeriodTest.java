/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 6, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.test.DateTimeHandling;

/**
 * @author tokoda
 * 
 */
public class PeriodTest {
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
        long currentTime = DateTimeHandling
                .calculateMillis("2012-03-01 11:11:11");

        // when
        long duration = Period.MONTH.getDuration(currentTime);

        // then
        long expected = 86400000L * 29L;
        assertEquals(expected, duration);
    }

    @Test
    public void getDuration_MonthJan() {
        // given
        long currentTime = DateTimeHandling
                .calculateMillis("2012-02-01 11:11:11");

        // when
        long duration = Period.MONTH.getDuration(currentTime);

        // then
        long expected = 86400000L * 31L;
        assertEquals(expected, duration);
    }
}
