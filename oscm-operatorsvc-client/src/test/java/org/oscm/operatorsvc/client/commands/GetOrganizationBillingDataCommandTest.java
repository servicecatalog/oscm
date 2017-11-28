/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                           
 *                                                                              
 *  Creation Date: 26.08.2010                                                      
 *                                                                              
 *  Completion Time: 26.08.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author weiser
 * 
 */
public class GetOrganizationBillingDataCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new GetOrganizationBillingDataCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("getbillingdata", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("orgid", "from", "to"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("orgid", "est");
        String from = "1991-02-14";
        args.put("from", from);
        String to = "2003-07-21";
        args.put("to", to);
        assertTrue(command.run(ctx));
        assertEquals("getOrganizationBillingData", stubMethodName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long fromLong = Long.valueOf(sdf.parse(from).getTime());
        Long toLong = Long.valueOf(sdf.parse(to).getTime() + 86400000L);
        assertEquals(fromLong, stubCallArgs[0]);
        assertEquals(toLong, stubCallArgs[1]);
        assertEquals("est", stubCallArgs[2]);

        assertOut("");
        assertErr("");
    }

}
