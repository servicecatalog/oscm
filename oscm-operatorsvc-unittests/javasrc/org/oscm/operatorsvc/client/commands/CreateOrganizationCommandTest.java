/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Tenant;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class CreateOrganizationCommandTest extends CommandTestBase {

    private static final String FILE_NAME = "org.properties";

    private CreateOrganizationCommand coCommand;

    @Override
    protected IOperatorCommand createCommand() {
        coCommand = spy(new CreateOrganizationCommand());
        try {
            doReturn(new LdapProperties()).when(coCommand).loadPropertiesFile(
                    anyString());
        } catch (Exception e) {
            // Ignore because we fake returning an empty properties file anyway
        }
        return coCommand;
    }

    @Test
    public void testGetName() {
        assertEquals("createorganization", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        List<String> expected = Arrays.asList("organization.address",
                "organization.email", "organization.locale",
                "organization.name", "organization.phone", "organization.url",
                "organization.roles", "organization.domicile",
                "organization.description", "organization.operatorrevshare",
                "organization.settings", "user.additionalname", "user.address",
                "user.email", "user.firstname", "user.identifier",
                "user.lastname", "user.locale", "user.phone",
                "user.salutation", "marketplaceid");
        assertEquals(expected, command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", "SUPPLIER");
        args.put("organization.domicile", "DE");
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("organization.operatorrevshare", "15");
        args.put("user.additionalname", "ext1");
        args.put("user.address", "Vierter Stock");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.firstname", "Marc");
        args.put("user.identifier", "hoffmann");
        args.put("user.lastname", "Hoffmann");
        args.put("user.locale", "de");
        args.put("user.phone", "+49 89 360908-563");
        args.put("user.salutation", "MR");

        VOOrganization orgResult = new VOOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("registerOrganization", stubMethodName);

        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());
        assertEquals(new BigDecimal("15"), org.getOperatorRevenueShare());

        VOUserDetails user = (VOUserDetails) stubCallArgs[2];
        assertEquals("ext1", user.getAdditionalName());
        assertEquals("Vierter Stock", user.getAddress());
        assertEquals("marc.hoffmann@est.fujitsu.com", user.getEMail());
        assertEquals("Marc", user.getFirstName());
        assertEquals("hoffmann", user.getUserId());
        assertEquals("Hoffmann", user.getLastName());
        assertEquals("de", user.getLocale());
        assertEquals("+49 89 360908-563", user.getPhone());
        assertEquals(Salutation.MR, user.getSalutation());

        OrganizationRoleType[] roles = (OrganizationRoleType[]) stubCallArgs[5];
        assertArrayEquals(
                new OrganizationRoleType[] { OrganizationRoleType.SUPPLIER },
                roles);

        assertOut("Creation of organization '1000' was successful!%n");
        assertErr("");
    }

    @Test
    public void testCreateCustomerSuccess() throws Exception {
        // Prepare parameters
        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", "");
        args.put("organization.domicile", "DE");
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("user.additionalname", "ext1");
        args.put("user.address", "Vierter Stock");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.firstname", "Marc");
        args.put("user.identifier", "hoffmann");
        args.put("user.lastname", "Hoffmann");
        args.put("user.locale", "de");
        args.put("user.phone", "+49 89 360908-563");
        args.put("user.salutation", "MR");
        args.put("marketplaceid", "MarketPlace_1");

        VOOrganization orgResult = new VOOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("registerOrganization", stubMethodName);

        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());

        VOUserDetails user = (VOUserDetails) stubCallArgs[2];
        assertEquals("ext1", user.getAdditionalName());
        assertEquals("Vierter Stock", user.getAddress());
        assertEquals("marc.hoffmann@est.fujitsu.com", user.getEMail());
        assertEquals("Marc", user.getFirstName());
        assertEquals("hoffmann", user.getUserId());
        assertEquals("Hoffmann", user.getLastName());
        assertEquals("de", user.getLocale());
        assertEquals("+49 89 360908-563", user.getPhone());
        assertEquals(Salutation.MR, user.getSalutation());

        String marketPlaceID = (String) stubCallArgs[4];
        assertEquals("MarketPlace_1", marketPlaceID);

        OrganizationRoleType[] roles = (OrganizationRoleType[]) stubCallArgs[5];
        assertEquals(0, roles.length);

        assertOut("Creation of organization '1000' was successful!%n");
        assertErr("");
    }

    @Test
    public void testFailureNoRoleAndNoMarketPlaceID() throws Exception {
        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", ""); // No Role
        args.put("organization.domicile", "DE"); // No Role
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("user.additionalname", "ext1");
        args.put("user.address", "Vierter Stock");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.firstname", "Marc");
        args.put("user.identifier", "hoffmann");
        args.put("user.lastname", "Hoffmann");
        args.put("user.locale", "de");
        args.put("user.phone", "+49 89 360908-563");
        args.put("user.salutation", "MR");
        args.put("marketplaceid", "");

        assertFalse(command.run(ctx));
        assertOut("");
        assertErr("No organization was created, as no role or marketplace id has been specified%n");

    }

    @Test
    public void testMinimumSetUserAttributes() throws Exception {
        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", "SUPPLIER");
        args.put("organization.domicile", "DE");
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.identifier", "hoffmann");
        args.put("user.locale", "de");

        VOOrganization orgResult = new VOOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("registerOrganization", stubMethodName);

        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());

        VOUserDetails user = (VOUserDetails) stubCallArgs[2];
        assertEquals("marc.hoffmann@est.fujitsu.com", user.getEMail());
        assertEquals("hoffmann", user.getUserId());
        assertEquals("de", user.getLocale());

        OrganizationRoleType[] roles = (OrganizationRoleType[]) stubCallArgs[5];
        assertArrayEquals(
                new OrganizationRoleType[] { OrganizationRoleType.SUPPLIER },
                roles);

        assertOut("Creation of organization '1000' was successful!%n");
        assertErr("");
    }

    @Test
    public void testPropertiesFile() throws Exception {
        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", "SUPPLIER");
        args.put("organization.domicile", "DE");
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.identifier", "hoffmann");
        args.put("user.locale", "de");
        args.put("organization.properties.filename", "myPropsFile.properties");

        VOOrganization orgResult = new VOOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("registerOrganization", stubMethodName);

        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());

        VOUserDetails user = (VOUserDetails) stubCallArgs[2];
        assertEquals("marc.hoffmann@est.fujitsu.com", user.getEMail());
        assertEquals("hoffmann", user.getUserId());
        assertEquals("de", user.getLocale());

        OrganizationRoleType[] roles = (OrganizationRoleType[]) stubCallArgs[5];
        assertArrayEquals(
                new OrganizationRoleType[] { OrganizationRoleType.SUPPLIER },
                roles);

        assertOut("Creation of organization '1000' was successful!%n");
        assertErr("");
    }

    @Test
    public void testMarketplaceWithTenant() throws Exception {

        // Same test as the standard org creation, but with tenantId, assert no exceptions
        String mpId = "mpTenant1";

        args.put("organization.address", "St.-Pauls-Viertel");
        args.put("organization.email", "info@est.fujitsu.com");
        args.put("organization.locale", "by");
        args.put("organization.name", "Fujitsu EST");
        args.put("organization.phone", "+49 89 360908-0");
        args.put("organization.url", "http://www.test.com");
        args.put("organization.roles", "SUPPLIER");
        args.put("organization.domicile", "DE");
        args.put("organization.description",
                "Fujitsu Enabling Software Technology GmbH");
        args.put("organization.operatorrevshare", "15");
        args.put("user.additionalname", "ext1");
        args.put("user.address", "Vierter Stock");
        args.put("user.email", "marc.hoffmann@est.fujitsu.com");
        args.put("user.firstname", "Marc");
        args.put("user.identifier", "hoffmann");
        args.put("user.lastname", "Hoffmann");
        args.put("user.locale", "de");
        args.put("user.phone", "+49 89 360908-563");
        args.put("user.salutation", "MR");
        args.put("marketplaceid", mpId);

        Marketplace marketplaceWithTenant = new Marketplace();
        marketplaceWithTenant.setMarketplaceId(mpId);
        Tenant tenant = new Tenant();
        tenant.setKey(1234L);
        marketplaceWithTenant.setTenant(tenant);

        VOOrganization orgResult = new VOOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("registerOrganization", stubMethodName);

        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());
        assertEquals(new BigDecimal("15"), org.getOperatorRevenueShare());

        VOUserDetails user = (VOUserDetails) stubCallArgs[2];
        assertEquals("ext1", user.getAdditionalName());
        assertEquals("Vierter Stock", user.getAddress());
        assertEquals("marc.hoffmann@est.fujitsu.com", user.getEMail());
        assertEquals("Marc", user.getFirstName());
        assertEquals("hoffmann", user.getUserId());
        assertEquals("Hoffmann", user.getLastName());
        assertEquals("de", user.getLocale());
        assertEquals("+49 89 360908-563", user.getPhone());
        assertEquals(Salutation.MR, user.getSalutation());

        OrganizationRoleType[] roles = (OrganizationRoleType[]) stubCallArgs[5];
        assertArrayEquals(
                new OrganizationRoleType[] { OrganizationRoleType.SUPPLIER },
                roles);

        assertOut("Creation of organization '1000' was successful!%n");
        assertErr("");
    }

    /**
     * Tests whether all the arguments defined as "ARG_*" constants by
     * {@link CreateOrganizationCommand} are also contained by the properties
     * file 'org.properties'.
     */
    @Test
    public void testArgsAvailableInFile() throws Exception {
        checkArgsAvailableInFile(CreateOrganizationCommand.class, FILE_NAME);
    }

}
