/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Ronny Weiser                                                      
 *                                                                              
 *  Creation Date: 28.05.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author weiser
 * 
 */
public class SetDistinguishedNameCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new SetDistinguishedNameCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("setdistinguishedname", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("orgid", "distinguishedname"), command
                .getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        args.put("orgid", "est");
        args.put("distinguishedname", "distinguishedname");
        assertTrue(command.run(ctx));
        assertEquals("setDistinguishedName", stubMethodName);

        assertEquals("est", stubCallArgs[0]);
        assertEquals("distinguishedname", stubCallArgs[1]);

        assertOut("Successfully saved distinguished name 'distinguishedname' for organization 'est'.");
        assertErr("");
    }
}
