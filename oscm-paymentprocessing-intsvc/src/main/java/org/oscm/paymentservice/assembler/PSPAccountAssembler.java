/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.PSPAccount;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPSPAccount;

/**
 * Assembler for the PSP account entity.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPAccountAssembler extends BaseAssembler {

    public static PSPAccount updatePSPAccount(VOPSPAccount vopspAccount,
            PSPAccount account) throws ConcurrentModificationException,
            ValidationException {
        validate(vopspAccount);
        verifyVersionAndKey(account, vopspAccount);
        copyAttributes(account, vopspAccount);
        return account;
    }

    public static VOPSPAccount toVo(PSPAccount pspAccount, LocalizerFacade lf) {
        VOPSPAccount result = new VOPSPAccount();
        result.setPspIdentifier(pspAccount.getPspIdentifier());
        updateValueObject(result, pspAccount);
        result.setPsp(PSPAssembler.toVo(pspAccount.getPsp(), lf));
        return result;
    }

    public static List<VOPSPAccount> toVos(List<PSPAccount> pspAccounts,
            LocalizerFacade lf) {
        List<VOPSPAccount> result = new ArrayList<VOPSPAccount>();
        for (PSPAccount account : pspAccounts) {
            result.add(toVo(account, lf));
        }
        return result;
    }

    private static void copyAttributes(PSPAccount account,
            VOPSPAccount vopspAccount) {
        account.setPspIdentifier(vopspAccount.getPspIdentifier());
    }

    private static void validate(VOPSPAccount vopspAccount)
            throws ValidationException {
        BLValidator.isDescription("pspIdentifier",
                vopspAccount.getPspIdentifier(), true);
    }

}
