/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;

public class SetPSPAccountForOrganizationCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new SetPSPAccountForOrganizationCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("setpspaccountfororganization", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("orgid", "pspid", "pspidentifier"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("orgid", "est");
        args.put("pspid", "12345");
        args.put("pspidentifier", "67890");
        assertTrue(command.run(ctx));
        assertEquals("savePSPAccount", stubMethodName);
        assertTrue(stubCallArgs[0] instanceof VOOperatorOrganization);
        assertTrue(stubCallArgs[1] instanceof VOPSPAccount);
        assertEquals("67890",
                ((VOPSPAccount) stubCallArgs[1]).getPspIdentifier());
        assertOut("Organization 'est' has been updated successfully.%n");
        assertErr("");
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        stubMethodName = method.getName();
        stubCallArgs = args;
        if (stubMethodName.equals("getPSPs")) {
            final List<VOPSP> list = new ArrayList<VOPSP>();
            final VOPSP psp = new VOPSP();
            psp.setId("12345");
            list.add(psp);
            return list;
        }
        if (stubMethodName.equals("getOrganization")) {
            final VOOperatorOrganization org = new VOOperatorOrganization();
            org.setName("est");
            return org;
        }
        return stubCallReturn;
    }

}
