/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2015-05-28
 *
 *******************************************************************************/

package org.oscm.ui.validator;

import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOSubscriptionDetails;

public class SubscriptionActivationValidator implements Validator {

    @Override
    public boolean supports(Class<?> objCls, Class<?> paramCls) {
        return SubscriptionService.class.isAssignableFrom(objCls) &&
                String.class.isAssignableFrom(paramCls);
    }

    @Override
    public boolean validate(Object obj, Object param) {
        SubscriptionService service = (SubscriptionService) obj;
        String subId = (String) param;

        try {
            VOSubscriptionDetails sub = service.getSubscriptionDetails(subId);
            if(SubscriptionStatus.DEACTIVATED == sub.getStatus()) {
                return false;
            }
        } catch (ObjectNotFoundException | OperationNotPermittedException e) {
            return false;
        }
        return true;
    }
}
