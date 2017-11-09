/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *  <p/>
 *  Creation Date: 2015-06-02
 *******************************************************************************/
package org.oscm.ui.validator;

import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOSubscriptionDetails;

public class MySubscriptionActivationValidator implements Validator {

    @Override
    public boolean supports(Class<?> objCls, Class<?> paramCls) {
        return SubscriptionsService.class.isAssignableFrom(objCls)
                && Long.class.isAssignableFrom(paramCls);
    }

    @Override
    public boolean validate(Object obj, Object param) {
        SubscriptionsService service = (SubscriptionsService) obj;
        Long subId = (Long) param;

        try {
            VOSubscriptionDetails sub = service.getSubscriptionDetails(subId.longValue());
            if (SubscriptionStatus.DEACTIVATED == sub.getStatus()) {
                return false;
            }
        } catch (ObjectNotFoundException e) {
            return false;
        }
        return true;
    }
}
