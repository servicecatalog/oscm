/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

public class AddOrganizationToRoleCommand implements IOperatorCommand {

    private static final String ARG_ORGID = "orgid";

    private static final String ARG_ROLE = "role";

    public String getName() {
        return "addorganizationtorole";
    }

    public String getDescription() {
        return "Determines the organization belonging to the given "
                + "organization identifier and grants it the specified role, "
                + "if it does not already have it.";
    }

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGID, ARG_ROLE);
    }

    public boolean run(CommandContext ctx) throws Exception {
        final String orgid = ctx.getString(ARG_ORGID);
        Set<OrganizationRoleType> all = EnumSet
                .noneOf(OrganizationRoleType.class);
        all.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        all.add(OrganizationRoleType.SUPPLIER);
        all.add(OrganizationRoleType.BROKER);
        all.add(OrganizationRoleType.RESELLER);
        final OrganizationRoleType role = ctx.getEnum(ARG_ROLE, all);

        ctx.getService().addOrganizationToRole(orgid, role);

        ctx.out().printf(
                "Successfully granted role %s to organization '%s'.%n", role,
                orgid);
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
