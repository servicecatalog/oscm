/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                           
 *                                                                              
 *  Creation Date: 04.08.2011                                                      
 *                                                                              
 *  Completion Time: 04.08.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author tokoda
 * 
 */
public class GetRevenueListCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new GetRevenueListCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("getrevenuelist", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("month"), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        String month = "2011-02";
        args.put("month", month);
        stubCallReturn = "1272664800000,1273156744630,supplierA,10000,123000,EUR\n1272764800000,1273256744630,\"supplier,S\",10000,1230000,JPY\n"
                .getBytes();
        assertTrue(command.run(ctx));
        assertEquals("getSupplierRevenueList", stubMethodName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Long monthLong = Long.valueOf(sdf.parse(month).getTime());
        assertEquals(monthLong, stubCallArgs[0]);

        assertOut("1272664800000,1273156744630,supplierA,10000,123000,EUR\n1272764800000,1273256744630,\"supplier,S\",10000,1230000,JPY\n");
        assertErr("");
    }

}
