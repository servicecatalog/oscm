/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: May 23, 2011                                                      
 *                                                                              
 *  Completion Time: June 6, 2011
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOService;

/**
 * Custom ANT task storing settings for WS-API calls in a project related
 * manner.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ServiceMigrationTask extends WebtestTask {

    private String migrationIds;
    private String serviceId;

    public void setMigrationTargetIds(String value) {
        migrationIds = value;
    }

    public void setServiceId(String value) {
        serviceId = value;
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {
        if (serviceId == null) {
            throwBuildException("No service ID specified - use the serviceId attribute to specifythe source service");
        }
        if (migrationIds == null) {
            throwBuildException("No target service IDs specified - use the migrationTargetIds attribute to specify one or more service IDsas migration target");
        }
        String[] split = migrationIds.split(",");
        List<String> targetServiceIds = new ArrayList<String>(split.length);
        for (int i = 0; i < split.length; i++) {
            targetServiceIds.add(split[i].trim());
        }

        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);

        ArrayList<VOService> targets = new ArrayList<VOService>();
        VOService source = null;
        ArrayList<String> missingIds = new ArrayList<String>(targetServiceIds);
        for (VOService service : spsSvc.getSuppliedServices()) {
            if (targetServiceIds.contains(service.getServiceId())) {
                targets.add(service);
                missingIds.remove(service.getServiceId());
            }
            if (serviceId.equals(service.getServiceId())) {
                source = service;
            }
        }
        if (missingIds.size() > 0) {
            throwBuildExceptionForIds(
                    "The following services were not found: ", missingIds);
        }
        if (source == null) {
            throwBuildException("Source service " + serviceId + " not found");
        }
        if (targets.size() > 0 && source != null) {
            spsSvc.setCompatibleServices(source, targets);
            log("Added " + targets.size() + " migration option(s) to service "
                    + serviceId);
        }
    }
}
