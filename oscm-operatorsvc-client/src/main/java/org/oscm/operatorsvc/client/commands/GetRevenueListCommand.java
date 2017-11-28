/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                             
 *                                                                              
 *  Creation Date: 04.08.2011                                                      
 *                                                                              
 *  Completion Time: 04.08.2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author tokoda
 * 
 */
public class GetRevenueListCommand implements IOperatorCommand {

    private static final String ARG_MONTH = "month";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_MONTH);
    }

    public String getDescription() {
        return "Returns the revenue data for suppliers and resellers in the specified month."
                + " The format for 'month' is yyyy-MM.";
    }

    public String getName() {
        return "getrevenuelist";
    }

    public boolean run(CommandContext ctx) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        String monthStr = ctx.getString(ARG_MONTH);
        long month = setTimeToZeroInCurrentTimeZone(sdf.parse(monthStr)
                .getTime());
        byte[] result = ctx.getService().getSupplierRevenueList(month);

        String csv = "";
        if (result != null) {
            csv = new String(result, "UTF-8");
        }
        ctx.out().print(csv);
        ctx.out().flush();
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return true;
    }

    /**
     * Sets hour, minute, second and millisecond of the provided time stamp to
     * zero and converts it to the server time zone.
     * 
     * @param timeStamp
     *            the time stamp to convert
     * @return the time stamp representing the day 00:00:00 000 passed in the
     *         time stamp in the current time zone
     */
    private static final long setTimeToZeroInCurrentTimeZone(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

}
