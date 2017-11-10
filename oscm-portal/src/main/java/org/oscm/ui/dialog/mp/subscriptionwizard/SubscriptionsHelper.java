/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 17.02.15 09:42
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionwizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.model.UdaRow;

public class SubscriptionsHelper implements Serializable {
    private static final long serialVersionUID = 805145414615881714L;

    public SubscriptionsHelper() {
    }

    /**
     * get VOUdas from subscriptionUdaRows
     *
     * @param subscriptionUdaRows
     * @return list of all VOUdas
     */
    public List<VOUda> getVoUdaFromUdaRows(List<UdaRow> subscriptionUdaRows) {
        List<VOUda> voUdas = new ArrayList<>();
        if (subscriptionUdaRows != null) {
            for (UdaRow row : subscriptionUdaRows) {
                voUdas.add(row.getUda());
            }
        }
        return voUdas;
    }

    public void setUdas(final POSubscriptionDetails subscriptionDetails,
            List<VOUdaDefinition> subUdaDefinitions,
            List<VOUdaDefinition> orgUdaDefinitions,
            VOSubscriptionDetails subscription) {
        if (subscription != null) {
            // get the Udas for current organization and subscription
            for (VOUdaDefinition def : subscriptionDetails
                    .getUdasDefinitions()) {
                if (def.getTargetType().equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                    subUdaDefinitions.add(def);
                } else if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                    orgUdaDefinitions.add(def);
                }
            }
        }
    }

    public Integer setMaximumNamedUsers(VOSubscriptionDetails subscription) {
        Integer maxNamedUsers = null;
        final List<VOParameter> parameters = subscription.getSubscribedService()
                .getParameters();
        if (parameters != null) {
            String paramDef, paramVal;
            for (VOParameter param : parameters) {
                paramDef = param.getParameterDefinition().getParameterId();
                if (PlatformParameterIdentifiers.NAMED_USER.equals(paramDef)) {
                    paramVal = param.getValue();
                    if (paramVal != null && paramVal.trim().length() > 0) {
                        maxNamedUsers = new Integer(paramVal.trim());
                        break;
                    }
                }
            }
        }
        return maxNamedUsers;
    }

    public void setUsageLicenses(VOSubscriptionDetails subscription,
            Map<String, VOUsageLicense> usageLicenseMap) {
        if (subscription == null) {
            return;
        }
        if (subscription.getUsageLicenses() == null) {
            return;
        }

        for (VOUsageLicense voUsageLicense : subscription.getUsageLicenses()) {
            usageLicenseMap.put(voUsageLicense.getUser().getUserId(),
                    voUsageLicense);
        }
    }

    public boolean validateSubscriptionStatus(
            VOSubscriptionDetails subscription,
            SubscriptionDetailsService service) {
        try {
            SubscriptionStatus status = service
                    .loadSubscriptionStatus(subscription.getKey())
                    .getResult(SubscriptionStatus.class);
            return status.isInvalidOrDeactive();
        } catch (ObjectNotFoundException e) {
            return true;
        }
    }
}
