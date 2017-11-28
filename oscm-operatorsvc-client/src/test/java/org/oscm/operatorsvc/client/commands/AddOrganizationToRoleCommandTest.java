/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddOrganizationToRoleCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new AddOrganizationToRoleCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("addorganizationtorole", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("orgid", "role"), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("orgid", "est");
        args.put("role", "SUPPLIER");

        assertTrue(command.run(ctx));

        assertEquals("addOrganizationToRole", stubMethodName);
        assertEquals("est", stubCallArgs[0]);
        assertEquals(OrganizationRoleType.SUPPLIER, stubCallArgs[1]);
        assertOut("Successfully granted role SUPPLIER to organization 'est'.%n");
        assertErr("");
    }

}
