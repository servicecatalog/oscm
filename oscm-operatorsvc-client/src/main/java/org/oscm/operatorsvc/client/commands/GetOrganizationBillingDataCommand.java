/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                             
 *                                                                              
 *  Creation Date: 26.08.2010                                                      
 *                                                                              
 *  Completion Time: 26.08.2010                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.oscm.converter.DateConverter;
import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author weiser
 * 
 */
public class GetOrganizationBillingDataCommand implements IOperatorCommand {

    private static final String ARG_ORGID = "orgid";
    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGID, ARG_FROM, ARG_TO);
    }

    public String getDescription() {
        return "Returns the billing data for an organization in the specified time frame."
                + " The format for 'from' and 'to' is yyyy-MM-dd.";
    }

    public String getName() {
        return "getbillingdata";
    }

    public boolean run(CommandContext ctx) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String orgId = ctx.getString(ARG_ORGID);

        String fromStr = ctx.getString(ARG_FROM);
        long from = DateConverter.getBeginningOfDayInCurrentTimeZone(sdf.parse(
                fromStr).getTime());

        String toStr = ctx.getString(ARG_TO);
        long to = DateConverter.getBeginningOfNextDayInCurrentTimeZone(sdf
                .parse(toStr).getTime());

        byte[] result = ctx.getService().getOrganizationBillingData(from, to,
                orgId);
        String xml = "";
        if (result != null) {
            xml = new String(result, "UTF-8");
        }
        ctx.out().print(xml);
        ctx.out().flush();
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return true;
    }

}
