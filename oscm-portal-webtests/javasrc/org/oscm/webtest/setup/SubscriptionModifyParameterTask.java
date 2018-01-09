/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;

/**
 * @author weiser
 * 
 */
public class SubscriptionModifyParameterTask extends WebtestTask {

    private String subId;
    private String param;
    private String paramValue;

    @Override
    public void executeInternal() throws Exception {
        if (!isEmpty(param)) {
            SubscriptionService ss = getServiceInterface(SubscriptionService.class);
            VOSubscriptionDetails sub = ss.getSubscriptionDetails(subId);
            ss.modifySubscription(sub, getParametersToModify(sub),
                    new ArrayList<VOUda>());
        } else {
            log("No parameter set - noting modified.", 0);
        }
    }

    private List<VOParameter> getParametersToModify(VOSubscriptionDetails sub) {
        List<VOParameter> result = new ArrayList<VOParameter>();
        List<VOParameter> list = sub.getSubscribedService().getParameters();
        for (VOParameter p : list) {
            if (p.getParameterDefinition().getParameterId().equals(param)) {
                p.setValue(paramValue);
                result.add(p);
            }
        }
        if (result.isEmpty()) {
            log(String.format("Parameter '%s' not found.", param), 0);
        }
        return result;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
