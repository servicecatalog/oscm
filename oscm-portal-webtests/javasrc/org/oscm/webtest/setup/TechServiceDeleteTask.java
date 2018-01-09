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
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Custom ANT task deleting the technical services specified by service IDs.
 * 
 * @author Dirk Bernsau
 * 
 */
public class TechServiceDeleteTask extends IterableTask {

    private String serviceIds;

    public void setServiceIds(String value) {
        serviceIds = value;
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {
        serviceIds = multiply(serviceIds);
        if (serviceIds.trim().length() == 0) {
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
        for (VOTechnicalService service : spsSvc
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            if (deleteAll
                    || serviceIds.contains(service.getTechnicalServiceId())) {
                missingIds.remove(service.getTechnicalServiceId());
                spsSvc.deleteTechnicalService(service);
                log("Deleted technical service with ID "
                        + service.getTechnicalServiceId());
            }
        }

        if (!deleteAll && missingIds.size() > 0) {
            logForIds("The following services were not found: ", missingIds);
        }
    }
}
