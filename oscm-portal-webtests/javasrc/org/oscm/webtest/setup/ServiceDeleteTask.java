/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: May 23, 2011                                                      
 *                                                                              
 *  Completion Time: June 8, 2011
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
 * Custom ANT task deleting the services specified by service IDs.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ServiceDeleteTask extends IterableTask {

    private String serviceIds;
    private boolean deactivate = false;

    public void setServiceIds(String value) {
        serviceIds = value;
    }

    public void setDeactivate(String value) {
        deactivate = Boolean.parseBoolean(value);
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException,
            org.oscm.internal.types.exception.SaaSApplicationException {
        serviceIds = multiply(serviceIds);
        if (serviceIds.trim().length() == 0) {
            throwBuildException("No service IDs specified - use the serviceIds attribute to specify one or more service IDs");
        }
        if (deactivate) {
            ServiceDeactivationTask task = new ServiceDeactivationTask();
            task.setProject(getProject());
            task.setDescription("Deactivation by " + getDescription());
            task.setServiceIds(serviceIds);
            task.setFailOnMissing("false"); // best effort
            task.executeInternal();
        }
        if (serviceIds == null || serviceIds.trim().length() == 0) {
            throwBuildException("No service IDs specified - use the serviceIds attribute to specify one or more service IDs");
        }
        String[] split = serviceIds.split(",");
        List<String> serviceIds = new ArrayList<String>(split.length);
        for (int i = 0; i < split.length; i++) {
            if (split[i].trim().length() > 0) {
                serviceIds.add(split[i].trim());
            }
        }
        boolean deleteAll = split.length == 1 && "*".equals(split[0]);

        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);
        ArrayList<String> missingIds = new ArrayList<String>(serviceIds);
        for (VOService service : spsSvc.getSuppliedServices()) {
            if (deleteAll || serviceIds.contains(service.getServiceId())) {
                missingIds.remove(service.getServiceId());
                spsSvc.deleteService(service);
                log("Deleted service with ID " + service.getServiceId());
            }
        }

        if (!deleteAll && missingIds.size() > 0) {
            logForIds("The following services were not found: ", missingIds);
        }
    }
}
