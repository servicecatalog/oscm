/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;

public class UpdateOrganizationCommand implements IOperatorCommand {

    static final String ARG_ORGANIZATION_ID = "organization.id";

    static final String ARG_ORGANIZATION_ADDRESS = CreateOrganizationCommand.ARG_ORGANIZATION_ADDRESS;
    static final String ARG_ORGANIZATION_EMAIL = CreateOrganizationCommand.ARG_ORGANIZATION_EMAIL;
    static final String ARG_ORGANIZATION_LOCALE = CreateOrganizationCommand.ARG_ORGANIZATION_LOCALE;
    static final String ARG_ORGANIZATION_NAME = CreateOrganizationCommand.ARG_ORGANIZATION_NAME;
    static final String ARG_ORGANIZATION_PHONE = CreateOrganizationCommand.ARG_ORGANIZATION_PHONE;
    static final String ARG_ORGANIZATION_URL = CreateOrganizationCommand.ARG_ORGANIZATION_URL;
    static final String ARG_ORGANIZATION_ROLES = CreateOrganizationCommand.ARG_ORGANIZATION_ROLES;
    static final String ARG_ORGANIZATION_DOMICILE = CreateOrganizationCommand.ARG_ORGANIZATION_DOMICILE;
    static final String ARG_ORGANIZATION_DESCRIPTION = CreateOrganizationCommand.ARG_ORGANIZATION_DESCRIPTION;
    static final String ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE = CreateOrganizationCommand.ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE;

    public String getName() {
        return "updateorganization";
    }

    public String getDescription() {
        return "Updates an organization with the given details.";
    }

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGANIZATION_ID, ARG_ORGANIZATION_ADDRESS,
                ARG_ORGANIZATION_EMAIL, ARG_ORGANIZATION_LOCALE,
                ARG_ORGANIZATION_NAME, ARG_ORGANIZATION_PHONE,
                ARG_ORGANIZATION_URL, ARG_ORGANIZATION_ROLES,
                ARG_ORGANIZATION_DOMICILE, ARG_ORGANIZATION_DESCRIPTION,
                ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE);
    }

    public boolean run(CommandContext ctx) throws Exception {

        final VOOperatorOrganization org = ctx.getService().getOrganization(
                ctx.getString(ARG_ORGANIZATION_ID));

        org.setAddress(ctx.getString(ARG_ORGANIZATION_ADDRESS));
        org.setEmail(ctx.getString(ARG_ORGANIZATION_EMAIL));
        org.setLocale(ctx.getString(ARG_ORGANIZATION_LOCALE));
        org.setName(ctx.getString(ARG_ORGANIZATION_NAME));
        org.setPhone(ctx.getString(ARG_ORGANIZATION_PHONE));
        org.setUrl(ctx.getString(ARG_ORGANIZATION_URL));
        org.setDomicileCountry(ctx.getString(ARG_ORGANIZATION_DOMICILE));
        org.setDescription(ctx.getString(ARG_ORGANIZATION_DESCRIPTION));

        String operatorRevShare = ctx
                .getStringOptional(ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE);
        if (operatorRevShare != null) {
            org.setOperatorRevenueShare(new BigDecimal(operatorRevShare));
        }

        final EnumSet<OrganizationRoleType> all = EnumSet
                .noneOf(OrganizationRoleType.class);
        all.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        all.add(OrganizationRoleType.SUPPLIER);
        all.add(OrganizationRoleType.BROKER);
        all.add(OrganizationRoleType.RESELLER);
        final Set<OrganizationRoleType> roles = ctx.getEnumList(
                ARG_ORGANIZATION_ROLES, all);
        org.setOrganizationRoles(new ArrayList<OrganizationRoleType>(roles));

        final VOOrganization newOrg = ctx.getService().updateOrganization(org,
                null);

        ctx.out().printf("Update of organization '%s' was successful!%n",
                newOrg.getOrganizationId());

        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
