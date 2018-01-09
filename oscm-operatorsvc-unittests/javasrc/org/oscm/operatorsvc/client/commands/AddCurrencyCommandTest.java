/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 01.09.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

public class AddCurrencyCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new AddCurrencyCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("addcurrency", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("currencyISOCode"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        stubCallReturn = Boolean.TRUE;
        args.put("currencyISOCode", "EUR");
        assertTrue(command.run(ctx));
        assertEquals("addCurrency", stubMethodName);
        assertOut("Currency 'EUR' is now available.");
        assertErr("");
    }

}
