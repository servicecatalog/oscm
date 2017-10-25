/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.tenantprovisioningservice.bean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceResult;

/**
 * Session Bean implementation class TenantProvisioningServiceBean. The tenant
 * provisioning manages resource which are provide to the instances of the
 * technical products (currently we don't provide additional resource so the
 * implementation only forwards to the application management).
 */
@Stateless
@LocalBean
public class TenantProvisioningServiceBean {

    @EJB(beanInterface = ApplicationServiceLocal.class)
    ApplicationServiceLocal appManager;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public TenantProvisioningResult createProductInstance(
            Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        boolean asyncTenantProvisioning = isAsyncProvisioning(subscription);
        TenantProvisioningResult result = new TenantProvisioningResult();
        result.setAsyncProvisioning(asyncTenantProvisioning);
        if (asyncTenantProvisioning) {
            BaseResult baseResult = appManager.asyncCreateInstance(subscription);
            result.setResultMesage(baseResult.getDesc());
        } else {
            InstanceResult instanceResult = appManager.createInstance(subscription);
            InstanceInfo instanceInfo = instanceResult.getInstance();
            result.setProductInstanceId(instanceInfo.getInstanceId());
            result.setAccessInfo(instanceInfo.getAccessInfo());
            result.setBaseUrl(instanceInfo.getBaseUrl());
            result.setLoginPath(instanceInfo.getLoginPath());
            result.setResultMesage(instanceResult.getDesc());
        }
        return result;
    }

    /**
     * Check if the provisioning should be done synchronously or asynchronously.
     * 
     * @param subscription
     *            the subscription to get the technical product from
     * @return <code>true</code> in case of asynchronous provisioning otherwise
     *         <code>false</code>
     */
    private boolean isAsyncProvisioning(Subscription subscription) {
        TechnicalProduct product = subscription.getProduct()
                .getTechnicalProduct();
        return product.getProvisioningType() == ProvisioningType.ASYNCHRONOUS;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteProductInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        appManager.deleteInstance(subscription);
    }

}
