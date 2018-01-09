/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Florian Walker                                                 
 *                                                                              
 *  Creation Date: Dec 12, 2011                                                      
 *                                                                              
 *  Completion Time: Dec 12, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Custom ANT task for granting a marketing for a technical product to a
 * supplier.
 * 
 * @author Florian Walker
 * 
 */
public class TechServiceAddSupplier extends WebtestTask {

    private String techServiceId;
    private String supplierId;

    public String getTechServiceId() {
        return techServiceId;
    }

    public void setTechServiceId(String techServiceId) {
        this.techServiceId = techServiceId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    @Override
    public void executeInternal() throws Exception {
        if (isEmpty(supplierId)) {
            throwBuildException("No supplier ID specified - use the supplierId attribute to specify the service ID");
        }
        if (isEmpty(techServiceId)) {
            throwBuildException("No technical service key specified - use the techServiceId attribute to specify the service key ");
        }

        long techServiceKey = findTechnicalServiceKey(techServiceId);
        if (techServiceKey < 0) {
            throwBuildException("Could not find technical service for passed service ID");
        }

        AccountService accountService = getServiceInterface(AccountService.class);

        VOTechnicalService voTechnicalService = new VOTechnicalService();
        voTechnicalService.setKey(techServiceKey);

        List<String> organizationIds = new ArrayList<String>();
        organizationIds.add(supplierId);

        accountService.addSuppliersForTechnicalService(voTechnicalService,
                organizationIds);

        log("Added supplier with ID '" + supplierId + "'to technical product'"
                + techServiceId + "'");

    }

    private long findTechnicalServiceKey(String id)
            throws OrganizationAuthoritiesException {
        ServiceProvisioningService provisioningService = getServiceInterface(ServiceProvisioningService.class);
        List<VOTechnicalService> technicalServices = provisioningService
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        if (technicalServices.isEmpty()) {
            return -1;
        }

        long techServiceKey = -1;
        for (VOTechnicalService voTechnicalService : technicalServices) {
            if (id.equals(voTechnicalService.getTechnicalServiceId())) {
                techServiceKey = voTechnicalService.getKey();
                break;
            }
        }
        return techServiceKey;
    }
}
