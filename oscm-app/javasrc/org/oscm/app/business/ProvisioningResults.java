/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 02.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import org.oscm.app.business.exceptions.ServiceInstanceInProcessingException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.provisioning.data.BaseResult;

/**
 * @author kulle
 * 
 */
public class ProvisioningResults {

    public <T extends BaseResult> T getOKResult(Class<T> type) {
        return getBaseResult(type, 0, "Ok");
    }

    public BaseResult newOkBaseResult() {
        return getBaseResult(BaseResult.class, 0, "Ok");
    }

    public <T extends BaseResult> T getSuccesfulResult(Class<T> type,
            String successMsg) {
        return getBaseResult(type, 0, successMsg);
    }

    /**
     * Returns a <code>BaseResult</code> object with the return code and
     * description message for the given exception.
     * 
     * @param type
     *            the type of <code>BaseResult</code> to return; this can be an
     *            <code>AbstractBaseResult</code>, an
     *            <code>InstanceResult</code>, or a <code>UserResult</code>
     * @param errorCode
     *            integer errorCode (0 - OK or 1 - error)
     * @param message
     *            description
     * @return a <code>BaseResult</code> object
     */
    public <T extends BaseResult> T getBaseResult(Class<T> type, int errorCode,
            String message) {
        try {
            T result = type.newInstance();
            result.setRc(errorCode);
            result.setDesc(message);
            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends BaseResult> T getErrorResult(Class<T> type, Exception e,
            String locale, ServiceInstance si, String instanceId) {

        si = getInstance(si, instanceId);

        if (e instanceof ServiceInstanceNotFoundException) {
            return getBaseResult(
                    type,
                    1,
                    Messages.get(locale, "error_instance_not_exists",
                            si.getInstanceId()));
        }

        if (e instanceof ServiceInstanceInProcessingException) {
            return getBaseResult(type, 1, Messages.get(locale,
                    "error_parallel_service_processing",
                    si.getSubscriptionId(), si.getInstanceId()));
        }

        if (e instanceof APPlatformException) {
            return getBaseResult(type, 1,
                    ((APPlatformException) e).getLocalizedMessage(locale));
        }
        return getBaseResult(type, 1, e.getMessage());
    }

    private ServiceInstance getInstance(ServiceInstance instance,
            String instanceId) {
        if (instance != null) {
            return instance;
        }

        ServiceInstance si = new ServiceInstance();
        si.setInstanceId(instanceId);
        return si;
    }

    public boolean isError(final BaseResult result) {
        return result.getRc() != 0;
    }

}
