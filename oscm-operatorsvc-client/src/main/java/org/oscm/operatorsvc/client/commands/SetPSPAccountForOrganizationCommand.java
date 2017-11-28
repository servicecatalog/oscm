/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;

public class SetPSPAccountForOrganizationCommand implements IOperatorCommand {

    private static final String ARG_ORGID = "orgid";

    private static final String ARG_PSP_IDENTIFIER = "pspidentifier";

    private static final String ARG_PSPID = "pspid";

    public String getName() {
        return "setpspaccountfororganization";
    }

    public String getDescription() {
        return "Sets the PSP identifier for an organization.";
    }

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_ORGID, ARG_PSPID, ARG_PSP_IDENTIFIER);
    }

    public boolean run(CommandContext ctx) throws Exception {
        final String orgid = ctx.getString(ARG_ORGID);
        final String pspidentifier = ctx.getString(ARG_PSP_IDENTIFIER);
        final String pspid = ctx.getString(ARG_PSPID);

        final VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier(pspidentifier);
        final List<VOPSP> psps = ctx.getService().getPSPs();
        if (psps != null) {
            for (VOPSP psp : psps) {
                if (pspid.equals(psp.getId())) {
                    account.setPsp(psp);
                    break;
                }
            }
        }
        if (account.getPsp() == null) {
            throw new ObjectNotFoundException("PSP with id '" + pspid
                    + "' not found!");
        }
        final VOOrganization org = ctx.getService().getOrganization(orgid);
        final List<VOPSPAccount> pspAccounts = ctx.getService().getPSPAccounts(
                org);
        if (pspAccounts != null) {
            for (VOPSPAccount pspAccount : pspAccounts) {
                if (account.getPsp().getId()
                        .equals(pspAccount.getPsp().getId())) {
                    return true;
                }
            }
        }
        ctx.getService().savePSPAccount(org, account);
        ctx.out().printf("Organization '%s' has been updated successfully.%n",
                orgid);

        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
