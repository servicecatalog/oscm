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

import java.util.Arrays;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author weiser
 * 
 */
public class SetDistinguishedNameCommand implements IOperatorCommand {

    private static final String ARG_ORGID = "orgid";
    private static final String ARG_DISTINGUISHEDNAME = "distinguishedname";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGID, ARG_DISTINGUISHEDNAME);
    }

    public String getDescription() {
        return "Sets the distinguished name for an organization"
                + " which is required to use secure certificate based communication";
    }

    public String getName() {
        return "setdistinguishedname";
    }

    public boolean run(CommandContext ctx) throws Exception {
        final String orgId = ctx.getString(ARG_ORGID);
        final String distinguishedName = ctx.getString(ARG_DISTINGUISHEDNAME);
        ctx.getService().setDistinguishedName(orgId, distinguishedName);
        ctx
                .out()
                .printf(
                        "Successfully saved distinguished name '%s' for organization '%s'.",
                        distinguishedName, orgId);
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
