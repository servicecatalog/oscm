/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 20, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Custom ANT task creating a technical services (PROXY) using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class TechServiceCreateProxyTask extends WebtestTask {

    private String serviceId;
    private String serviceUrl;
    private String provisioningUrl;
    private String tags = "";

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setId(String value) {
        serviceId = value;
    }

    public void setProvisioningUrl(String provisioningUrl) {
        this.provisioningUrl = provisioningUrl;
    }

    public void setServiceUrl(String value) {
        serviceUrl = value;
    }

    @Override
    public void executeInternal() throws Exception {
        if (serviceId == null || serviceId.trim().length() == 0) {
            throwBuildException("No service ID specified - use the serviceId attribute to specify the service ID");
        }
        if (serviceUrl == null || serviceUrl.trim().length() == 0) {
            throwBuildException("No service URL specified - use the serviceUrl attribute to specify the service URL");
        }
        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);
        VOTechnicalService technicalService = new VOTechnicalService();
        technicalService.setTechnicalServiceId(serviceId);
        technicalService.setBaseUrl(serviceUrl);
        technicalService.setProvisioningUrl(provisioningUrl);
        technicalService.setAccessType(ServiceAccessType.LOGIN);
        technicalService.setTags(getTagsFromString(tags));
        spsSvc.createTechnicalService(technicalService);
        log("Created technical service with ID '" + serviceId + "'");
    }

    public List<String> getTagsFromString(String tags) {
        // Convert comma separated list into list
        StringTokenizer tok = new StringTokenizer(tags, ",");
        List<String> list = new ArrayList<String>();
        while (tok.hasMoreElements()) {
            String tagValue = tok.nextElement().toString().trim();
            if (tagValue.length() > 0)
                list.add(tagValue.trim());
        }
        return list;
    }
}
