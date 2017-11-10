/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 15.05.15 10:18
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.interfaces;

import java.util.List;

import org.oscm.ui.model.ParameterValidationResult;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;

/**
 * Created by FlorekS on 2015-05-12.
 */
public interface ConfigParamValidateable {
    void setService(Service service);

    Service getService();

    void setServiceParameters(List<PricedParameterRow> serviceParameters);

    List<PricedParameterRow> getServiceParameters();

    boolean isReadOnlyParams();

    void setReadOnlyParams(boolean readOnlyParams);

    boolean isSubscriptionExisting();

    void setSubscriptionExisting(boolean subscriptionExisting);

    String getParameterConfigResponse();

    void setParameterConfigResponse(String parameterConfigResponse);

    ParameterValidationResult getParameterValidationResult();

    void setParameterValidationResult(ParameterValidationResult parameterValidationResult);

    public void setHideExternalConfigurator(boolean hideExternalConfigurator);
}
