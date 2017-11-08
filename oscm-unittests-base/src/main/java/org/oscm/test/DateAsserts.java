/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: Feb 5, 2013                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

/**
 * @author afschar
 * 
 */
public class DateAsserts {
    public static void assertDates(long date1, long date2) {
        assertEquals(new Date(date1), new Date(date2));
    }

    public static void assertDates(String msg, long date1, long date2) {
        assertEquals(msg, new Date(date1), new Date(date2));
    }
}
