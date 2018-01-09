/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

public class RetryFailedPaymentProcessesCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new RetryFailedPaymentProcessesCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("retryfailedpaymentprocesses", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList(), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        stubCallReturn = Boolean.TRUE;
        assertTrue(command.run(ctx));
        assertEquals("retryFailedPaymentProcesses", stubMethodName);
        assertOut("All payment processes to be retried were handled successfully.%n");
        assertErr("");
    }

    @Test
    public void testFailure() throws Exception {
        stubCallReturn = Boolean.FALSE;
        assertFalse(command.run(ctx));
        assertEquals("retryFailedPaymentProcesses", stubMethodName);
        assertOut("");
        assertErr("Retrying the payment processes failed for at least one entry.%n");
    }

}
