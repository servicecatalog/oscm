/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 26.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author malhotra
 * 
 */

public abstract class BaseProvisioningProcessor implements
        ProvisioningProcessor {

    private static final Logger logger = LoggerFactory
            .getLogger(BaseProvisioningProcessor.class);

    protected APPlatformService platformService;

    /**
     * Checks whether the given next status for this service is supported in the
     * current situation (concurrency handling!).
     * 
     * @param controllerId
     *            id of the controller
     * @param instanceId
     *            id of the instance
     * @param nextStatus
     *            the requested new status
     * @param paramHandler
     *            entity which holds all properties of the instance
     * @return true if next status can be defined, false otherwise
     */
    public boolean checkNextStatus(String controllerId, String instanceId,
            FlowState nextStatus, PropertyHandler paramHandler)
            throws APPlatformException {
        if (paramHandler.isParallelProvisioningEnabled()) {
            // currently testing only:
            // disable locking and therefore allow parallel handling of
            // resources
            return true;
        }

        EnumSet<FlowState> CONFLICT_STATES = EnumSet.of(
                FlowState.VSERVER_CREATING, FlowState.VSERVER_CREATED,
                FlowState.VSDISK_CREATING, FlowState.VSDISK_CREATED,
                FlowState.VSDISK_ATTACHING, FlowState.VSDISK_ATTACHED,
                FlowState.VSDISK_DETACHING, FlowState.VSDISK_DETACHED,
                FlowState.VSDISK_DELETING, FlowState.VSDISK_DESTROYED,
                FlowState.VSERVER_UPDATING, FlowState.VSERVER_UPDATED,
                FlowState.VSERVER_DELETING, FlowState.VSYSTEM_CREATING,
                FlowState.VSYSTEM_DELETING, FlowState.VSYSTEM_SCALE_UP,
                FlowState.VSYSTEM_SCALE_DOWN, FlowState.VNET_DELETING);

        if (CONFLICT_STATES.contains(nextStatus)) {
            return enableExclusiveProcessing(controllerId, instanceId,
                    paramHandler);
        }

        // Safe (non-conflicting) operation (e.g. start/stop)
        // => release the exclusive token
        disableExclusiveProcessing(controllerId, instanceId, paramHandler);

        return true;
    }

    /**
     * Enables exclusive processing.
     * 
     * @param controllerId
     *            id of the controller
     * @param instanceId
     *            id of the instance
     * @param paramHandler
     *            entity which holds all properties of the instance
     */
    private synchronized boolean enableExclusiveProcessing(String controllerId,
            String instanceId, PropertyHandler paramHandler)
            throws APPlatformException {
        logger.debug("enableExclusiveProcessing('{}')", instanceId);
        return platformService.lockServiceInstance(controllerId, instanceId,
                paramHandler.getTPAuthentication());
    }

    /**
     * Disables exclusive processing.
     * 
     * @param controllerId
     *            id of the controller
     * @param instanceId
     *            id of the instance
     * @param paramHandler
     *            entity which holds all properties of the instance
     */
    public synchronized void disableExclusiveProcessing(String controllerId,
            String instanceId, PropertyHandler paramHandler)
            throws APPlatformException {
        logger.debug("disableExclusiveProcessing('{}')", instanceId);
        platformService.unlockServiceInstance(controllerId, instanceId,
                paramHandler.getTPAuthentication());

    }

    public void setPlatformService(APPlatformService platformService) {
        this.platformService = platformService;
    }

    public String getTechnicalProviderLocale(String controllerId,
            PropertyHandler paramHandler) throws AuthenticationException,
            ConfigurationException, APPlatformException {
        User user = platformService.authenticate(controllerId,
                paramHandler.getTPAuthentication());
        return user.getLocale();
    }
}
