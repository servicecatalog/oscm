/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateOrganizationCommandTest extends CommandTestBase {

    private static final String FILE_NAME = "update_org.properties";

    @Override
    protected IOperatorCommand createCommand() {
        return new UpdateOrganizationCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("updateorganization", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        List<String> expected = Arrays.asList("organization.id",
                "organization.address", "organization.email",
                "organization.locale", "organization.name",
                "organization.phone", "organization.url", "organization.roles",
                "organization.domicile", "organization.description",
                "organization.operatorrevshare");
        assertEquals(expected, command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("organization.id", "1000");
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

        VOOperatorOrganization orgResult = new VOOperatorOrganization();
        orgResult.setOrganizationId("1000");
        stubCallReturn = orgResult;

        assertTrue(command.run(ctx));

        assertEquals("updateOrganization", stubMethodName);

        VOOperatorOrganization org = (VOOperatorOrganization) stubCallArgs[0];
        assertEquals("1000", org.getOrganizationId());
        assertEquals("St.-Pauls-Viertel", org.getAddress());
        assertEquals("info@est.fujitsu.com", org.getEmail());
        assertEquals("by", org.getLocale());
        assertEquals("Fujitsu EST", org.getName());
        assertEquals("+49 89 360908-0", org.getPhone());
        assertEquals("http://www.test.com", org.getUrl());
        assertEquals("DE", org.getDomicileCountry());
        assertEquals("SUPPLIER", org.getOrganizationRoles().iterator().next()
                .name());
        assertEquals("Fujitsu Enabling Software Technology GmbH",
                org.getDescription());
        assertEquals(new BigDecimal("15"), org.getOperatorRevenueShare());

        assertOut("Update of organization '1000' was successful!%n");
        assertErr("");
    }

    /**
     * Tests whether all the arguments defined as "ARG_*" constants by
     * {@link UpdateOrganizationCommand} are also contained by the properties
     * file 'update_org.properties'.
     */
    @Test
    public void testArgsAvailableInFile() throws Exception {
        checkArgsAvailableInFile(UpdateOrganizationCommand.class, FILE_NAME);
    }

}
