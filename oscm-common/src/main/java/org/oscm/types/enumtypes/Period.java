/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import java.util.Calendar;

import org.oscm.converter.DateConverter;

/**
 * Represents periods that can be used by the services to indicate time spans,
 * whereas they are not based on milliseconds but represent periods like e.g. a
 * month.
 * 
 * @author Mike J&auml;ger
 * 
 */
public enum Period {
    /**
     * A period of one day.
     */
    DAY,
    /**
     * A period of one month, irrespective of it's concrete duration.
     */
    MONTH;

    /**
     * Returns the duration in millisecond of the period. For month, the
     * duration of one month before of the input time.
     * 
     * @param currentTime
     * @return
     */
    public long getDuration(long currentTime) {
        long length = 0;

        switch (this) {
        case MONTH:
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(currentTime);
            cal.add(Calendar.MONTH, -1);
            long oneMonthAgo = cal.getTimeInMillis();
            length = currentTime - oneMonthAgo;
            break;
        default:
            length = DateConverter.MILLISECONDS_PER_DAY;
            break;
        }
        return length;
    }
}
