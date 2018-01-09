/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * Tests for the saveconfigurationsetting comman.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class StartPaymentProcessingCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new StartPaymentProcessingCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("startpaymentprocessing", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Collections.emptyList(), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        stubCallReturn = Boolean.TRUE;
        assertTrue(command.run(ctx));
        assertEquals("startPaymentProcessing", stubMethodName);
        assertOut("Payment processing completed successfully.");
        assertErr("");
    }

    @Test
    public void testFail() throws Exception {
        stubCallReturn = Boolean.FALSE;
        assertFalse(command.run(ctx));
        assertEquals("startPaymentProcessing", stubMethodName);
        assertOut("");
        assertErr("Payment processing failed.");
    }
}
