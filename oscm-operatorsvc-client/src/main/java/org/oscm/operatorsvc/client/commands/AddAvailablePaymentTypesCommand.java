/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.vo.VOOrganization;

public class AddAvailablePaymentTypesCommand implements IOperatorCommand {

    private static final String ARG_ORGID = "orgid";

    private static final String ARG_PAYMENTTYPES = "paymenttypes";

    public String getName() {
        return "addavailablepaymenttypes";
    }

    public String getDescription() {
        return "Adds available payment types to the given organization.";
    }

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGID, ARG_PAYMENTTYPES);
    }

    public boolean run(CommandContext ctx) throws Exception {
        final VOOrganization organization = new VOOrganization();
        organization.setOrganizationId(ctx.getString(ARG_ORGID));

        final Set<String> types = new HashSet<String>(ctx
                .getList(ARG_PAYMENTTYPES));
        ctx.getService().addAvailablePaymentTypes(organization, types);
        ctx.out().println(
                "The following payment types were successfully enabled:");
        for (String t : types) {
            ctx.out().println(t);
        }
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
