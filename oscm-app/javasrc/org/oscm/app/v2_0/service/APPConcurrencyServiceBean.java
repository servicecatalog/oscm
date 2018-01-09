/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 03.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.exceptions.APPlatformException;

/**
 * Implementation for the concurrency handler.
 * 
 * @author soehnges
 * 
 */
@Stateless
public class APPConcurrencyServiceBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(APPConcurrencyServiceBean.class);

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean lockServiceInstance(String controllerId, String instanceId)
            throws APPlatformException {
        LOGGER.debug("try to lock service instance {}", instanceId);

        ServiceInstance lockedService = instanceDAO
                .getLockedInstanceForController(controllerId);
        if (lockedService != null) {
            if (!lockedService.getInstanceId().equals(instanceId)) {
                LOGGER.debug("other service is already locked ({}).",
                        lockedService.getInstanceId());
                return false;
            }

            LOGGER.debug("Service is already locked");
            return true;
        }

        ServiceInstance service = null;
        try {
            service = instanceDAO.getInstanceById(controllerId, instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            throw new APPlatformException(e.getMessage());
        }
        service.setLocked(true);
        em.flush();

        LOGGER.debug("Locked successfully.");
        return true;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void unlockServiceInstance(String controllerId, String instanceId)
            throws APPlatformException {
        ServiceInstance service = null;
        try {
            service = instanceDAO.getInstanceById(controllerId, instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            throw new APPlatformException(e.getMessage());
        }

        if (service.isLocked()) {
            LOGGER.debug("unlock service instance {}", instanceId);
            service.setLocked(false);
            em.flush();
        }
    }
}
