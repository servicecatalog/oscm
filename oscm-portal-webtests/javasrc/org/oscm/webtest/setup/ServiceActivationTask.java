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

import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceActivation;

/**
 * Custom ANT task activating the service specified by service IDs.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ServiceActivationTask extends IterableTask {

    private String serviceIds;
    protected boolean active = true; // activate or deactivate?
    private boolean failOnMissing = true;

    public void setServiceIds(String value) {
        serviceIds = value;
    }

    public void setFailOnMissing(String value) {
        failOnMissing = Boolean.parseBoolean(value);
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {
        serviceIds = multiply(serviceIds);
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

        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);
        MarketplaceService mpSvc = getServiceInterface(MarketplaceService.class);

        ArrayList<VOServiceActivation> activations = new ArrayList<VOServiceActivation>();
        ArrayList<String> missingIds = new ArrayList<String>(serviceIds);
        List<VOCustomerService> csServices = spsSvc
                .getAllCustomerSpecificServices();
        boolean useAll = split.length == 1 && "*".equals(split[0]);
        for (VOService service : spsSvc.getSuppliedServices()) {
            if (useAll || serviceIds.contains(service.getServiceId())) {
                List<VOCatalogEntry> catEntries = mpSvc
                        .getMarketplacesForService(service);
                VOServiceActivation activation = new VOServiceActivation();
                activation.setService(service);
                activation.setCatalogEntries(catEntries);
                activation.setActive(active);
                activations.add(activation);
                activations.addAll(getCustomerServicesToDeactivate(service,
                        csServices));
                missingIds.remove(service.getServiceId());
            }
        }
        if (!useAll && missingIds.size() > 0) {
            if (failOnMissing) {
                throwBuildExceptionForIds(
                        "The following services were not found: ", missingIds);
            } else {
                logForIds("The following services were not found: ", missingIds);
            }
        }
        if (activations.size() > 0) {
            List<VOService> result = spsSvc.setActivationStates(activations);
            if (activations.size() > result.size()) {
                throwBuildException("Only " + result.size() + "/"
                        + activations.size()
                        + " (de-)activations were successful");
            }
            if (active) {
                log("Activated " + activations.size() + " service(s)");
            } else {
                log("De-activated " + activations.size() + " service(s)");
            }
        } else {
            throwBuildException("Nothing to do");
        }
    }

    /**
     * On deactivation, for all active customer specific services belonging to
     * the provided service, a {@link VOServiceActivation} to deactivate the
     * service will be created that is contained in the result list.
     * 
     * @param service
     *            the service to get customer specific one for
     * @param csServices
     *            all customer specific services
     * @return the {@link VOServiceActivation}s for the customer specific
     *         services to deactivate
     */
    private List<VOServiceActivation> getCustomerServicesToDeactivate(
            VOService service, List<VOCustomerService> csServices) {
        List<VOServiceActivation> result = new ArrayList<VOServiceActivation>();
        if (active) {
            return result;
        }
        for (VOCustomerService cs : csServices) {
            if (cs.getStatus() == ServiceStatus.ACTIVE
                    && cs.getServiceId().equals(service.getServiceId())) {
                VOServiceActivation activation = new VOServiceActivation();
                activation.setService(cs);
                activation.setCatalogEntries(new ArrayList<VOCatalogEntry>());
                activation.setActive(active);
                result.add(activation);
            }
        }
        return result;
    }
}
