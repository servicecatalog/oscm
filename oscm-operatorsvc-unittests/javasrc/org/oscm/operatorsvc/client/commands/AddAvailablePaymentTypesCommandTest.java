/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.internal.vo.VOOrganization;

public class AddAvailablePaymentTypesCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new AddAvailablePaymentTypesCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("addavailablepaymenttypes", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("orgid", "paymenttypes"), command
                .getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("orgid", "est");
        args.put("paymenttypes", BaseAdmUmTest.CREDIT_CARD);
        assertTrue(command.run(ctx));
        assertEquals("addAvailablePaymentTypes", stubMethodName);
        VOOrganization org = (VOOrganization) stubCallArgs[0];
        assertEquals("est", org.getOrganizationId());
        Set<?> types = (Set<?>) stubCallArgs[1];
        assertEquals(new HashSet<Object>(Arrays
                .asList(BaseAdmUmTest.CREDIT_CARD)), types);
        assertOut("The following payment types were successfully enabled:%nCREDIT_CARD%n");
        assertErr("");
    }

}
