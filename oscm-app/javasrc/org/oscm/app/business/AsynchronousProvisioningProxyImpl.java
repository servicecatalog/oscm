/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 02.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * @author kulle
 * 
 */
public class AsynchronousProvisioningProxyImpl {

    @Inject
    protected transient Logger logger;

    @Inject
    protected ProvisioningResults provResultHelper;

    @Inject
    protected ServiceInstanceDAO instanceDAO;

    @Inject
    protected APPConfigurationServiceBean configService;

    @Inject
    protected APPTimerServiceBean timerService;

    @Inject
    protected ProductProvisioningServiceFactoryBean provisioningFactory;

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    public BaseResult deleteInstance(ServiceInstance instance,
            User requestingUser) throws BadResultException, APPlatformException {

        String oldSubscriptionId = instance.getSubscriptionId();
        instance.markForDeletion();

        instance.setRunWithTimer(true);
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);

        final APPlatformController controller = APPlatformControllerFactory
                .getInstance(instance.getControllerId());
        final ProvisioningSettings settings = configService
                .getProvisioningSettings(instance,
                        UserMapper.toServiceUser(requestingUser));
        final InstanceStatus status = controller.deleteInstance(
                instance.getInstanceId(), settings);
        if (status != null) {
            // forward call to provisioning service on application instance
            if (status.isInstanceProvisioningRequested()) {
                final ProvisioningService provisioning = provisioningFactory
                        .getInstance(instance);
                String instanceId = instance.getInstanceId();
                String organizationId = instance.getOrganizationId();
                final BaseResult result = provisioning.deleteInstance(
                        instanceId, organizationId, oldSubscriptionId,
                        requestingUser);
                if (provResultHelper.isError(result)) {
                    return result;
                }
            }

            // If everything worked well we will save all changed parameters
            instance.setInstanceParameters(status.getChangedParameters());
        }

        timerService.initTimers();
        return provResultHelper.getOKResult(BaseResult.class);
    }

    /**
     * Deletes the APP service instance and controller back-end resources.
     * <p>
     * Note: If the APP instance is not found within the APP database or if it
     * is already marked as deleted this method will return successfully without
     * deleting controller back-end resources!
     */
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {

        ServiceInstance instance = null;

        try {
            instance = instanceDAO.getInstance(instanceId, subscriptionId,
                    organizationId);
        } catch (ServiceInstanceNotFoundException e) {
            logger.info("Instance to be deleted '{}' doesn't exist any more.",
                    instanceId);
            return provResultHelper.getOKResult(BaseResult.class);
        }

        if (instance.isDeleted()) {
            logger.info(
                    "Instance to be deleted '{}' is already marked for deletion.",
                    instanceId);
            return provResultHelper.getOKResult(BaseResult.class);
        }

        try {
            return deleteInstance(instance, requestingUser);
        } catch (Exception e) {
            return provResultHelper.getErrorResult(BaseResult.class, e,
                    getLocale(requestingUser), instance, instanceId);
        }
    }

    String getLocale(User requestingUser) {
        String locale = null;
        if (requestingUser != null) {
            locale = requestingUser.getLocale();
        }
        return locale;
    }

}
