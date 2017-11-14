/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.ServiceProvisioningService;

public class ServiceProvisioningServiceBeanNullArgumentTest extends
        NullArgumentTestBase<ServiceProvisioningService> {

    public ServiceProvisioningServiceBeanNullArgumentTest() {
        super(ServiceProvisioningService.class);
        addNullAllowed("createService", "imageResource");
        addNullAllowed("updateService", "imageResource");
        // the case serviceId == null is covered by the id validation
        addNullAllowed("copyService", "serviceId");
        addNullAllowed("setCatalogDetails", "entries");
        addNullAllowed("getRelatedServicesForMarketplace", "locale");
        addNullAllowed("getServiceForMarketplace", "locale");
        addNullAllowed("importService", "image");
    }

    @Override
    protected ServiceProvisioningService createInstance(TestContainer container)
            throws Exception {

        container.enableInterfaceMocking(true);
        final org.oscm.intf.ServiceProvisioningService service = new ServiceProvisioningServiceWS();
        ((ServiceProvisioningServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((ServiceProvisioningServiceWS) service).delegate = new ServiceProvisioningServiceBean();
        container.addBean(service);
        return service;
    }

}
