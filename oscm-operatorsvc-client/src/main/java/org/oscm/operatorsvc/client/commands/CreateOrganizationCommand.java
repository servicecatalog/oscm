/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import org.oscm.converter.PropertiesLoader;
import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class CreateOrganizationCommand implements IOperatorCommand {

    static final String ARG_ORGANIZATION_ADDRESS = "organization.address";
    static final String ARG_ORGANIZATION_EMAIL = "organization.email";
    static final String ARG_ORGANIZATION_LOCALE = "organization.locale";
    static final String ARG_ORGANIZATION_NAME = "organization.name";
    static final String ARG_ORGANIZATION_PHONE = "organization.phone";
    static final String ARG_ORGANIZATION_URL = "organization.url";
    static final String ARG_ORGANIZATION_ROLES = "organization.roles";
    static final String ARG_ORGANIZATION_DOMICILE = "organization.domicile";
    static final String ARG_ORGANIZATION_DESCRIPTION = "organization.description";
    static final String ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE = "organization.operatorrevshare";

    static final String ARG_ORGANIZATION_PROPERTIES_FILENAME = "organization.settings";

    static final String ARG_USER_ADDITIONALNAME = "user.additionalname";
    static final String ARG_USER_ADDRESS = "user.address";
    static final String ARG_USER_EMAIL = "user.email";
    static final String ARG_USER_FIRSTNAME = "user.firstname";
    static final String ARG_USER_IDENTIFIER = "user.identifier";
    static final String ARG_USER_LASTNAME = "user.lastname";
    static final String ARG_USER_LOCALE = "user.locale";
    static final String ARG_USER_PHONE = "user.phone";
    static final String ARG_USER_SALUTATION = "user.salutation";

    static final String MARKETPLACEID = "marketplaceid";

    @Override
    public String getName() {
        return "createorganization";
    }

    @Override
    public String getDescription() {
        return "Creates an organization with the given details as well as its "
                + "initial administrative user. The new organization will be "
                + "granted the authorities as requested.";
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGANIZATION_ADDRESS, ARG_ORGANIZATION_EMAIL,
                ARG_ORGANIZATION_LOCALE, ARG_ORGANIZATION_NAME,
                ARG_ORGANIZATION_PHONE, ARG_ORGANIZATION_URL,
                ARG_ORGANIZATION_ROLES, ARG_ORGANIZATION_DOMICILE,
                ARG_ORGANIZATION_DESCRIPTION,
                ARG_ORGANIZATION_OPERATOR_REVENUE_SHARE,
                ARG_ORGANIZATION_PROPERTIES_FILENAME, ARG_USER_ADDITIONALNAME,
                ARG_USER_ADDRESS, ARG_USER_EMAIL, ARG_USER_FIRSTNAME,
                ARG_USER_IDENTIFIER, ARG_USER_LASTNAME, ARG_USER_LOCALE,
                ARG_USER_PHONE, ARG_USER_SALUTATION, MARKETPLACEID);
    }

    @Override
    public boolean run(CommandContext ctx) throws Exception {
        final VOOrganization org = new VOOrganization();
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

        final VOUserDetails user = new VOUserDetails();
        user.setAdditionalName(ctx.getStringOptional(ARG_USER_ADDITIONALNAME));
        user.setAddress(ctx.getStringOptional(ARG_USER_ADDRESS));
        user.setEMail(ctx.getString(ARG_USER_EMAIL));
        user.setFirstName(ctx.getStringOptional(ARG_USER_FIRSTNAME));
        user.setUserId(ctx.getString(ARG_USER_IDENTIFIER));
        user.setLastName(ctx.getStringOptional(ARG_USER_LASTNAME));
        user.setLocale(ctx.getString(ARG_USER_LOCALE));
        user.setPhone(ctx.getStringOptional(ARG_USER_PHONE));
        final Salutation salut = ctx.getEnumOptional(ARG_USER_SALUTATION,
                EnumSet.allOf(Salutation.class));
        user.setSalutation(salut);

        final EnumSet<OrganizationRoleType> all = EnumSet
                .noneOf(OrganizationRoleType.class);
        all.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        all.add(OrganizationRoleType.SUPPLIER);
        all.add(OrganizationRoleType.BROKER);
        all.add(OrganizationRoleType.RESELLER);
        final OrganizationRoleType[] roles = ctx.getEnumList(
                ARG_ORGANIZATION_ROLES, all).toArray(
                new OrganizationRoleType[0]);

        String marketplaceID = ctx.getStringOptional(MARKETPLACEID);
        if (roles.length == 0) {
            if (marketplaceID == null || marketplaceID.trim().equals("")) {
                final String msg = "No organization was created, as no role or marketplace id has been specified";
                ctx.err().println(msg);
                return false;
            }
        }

        LdapProperties ldapProperties = null;
        String propertiesFilename = ctx
                .getStringOptional(ARG_ORGANIZATION_PROPERTIES_FILENAME);
        if (propertiesFilename != null) {
            ldapProperties = loadPropertiesFile(propertiesFilename);
        }

        final VOOrganization newOrg = ctx.getService().registerOrganization(
                org, null, user, ldapProperties, marketplaceID, roles);

        ctx.out().printf("Creation of organization '%s' was successful!%n",
                newOrg.getOrganizationId());

        return true;
    }

    protected LdapProperties loadPropertiesFile(String filename)
            throws FileNotFoundException, IOException {
        LdapProperties ldapProperties;
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(filename);
        properties = PropertiesLoader.loadProperties(fis);
        ldapProperties = new LdapProperties(properties);
        fis.close();
        return ldapProperties;
    }

    @Override
    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
