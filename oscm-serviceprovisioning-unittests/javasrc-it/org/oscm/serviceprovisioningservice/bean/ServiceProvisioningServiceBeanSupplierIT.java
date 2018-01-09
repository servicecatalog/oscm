/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Soehnges                                             
 *                                                                              
 *  Creation Date: 17.05.2011                                                      
 *                                                                              
 *  Completion Time: 17.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOOrganization;

public class ServiceProvisioningServiceBeanSupplierIT extends EJBTestBase {

    private ServiceProvisioningService provisioningService;
    private Organization supplier;
    private Product product;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        container.addBean(new DataServiceStub() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends DomainObject<?>> T getReference(
                    Class<T> objclass, long key) throws ObjectNotFoundException {
                if (objclass.equals(Product.class)) {
                    if (key == 1) {
                        return (T) product;
                    } else {
                        throw new ObjectNotFoundException();
                    }
                }
                return null;
            }
        });
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new ImageResourceServiceStub());
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (localeString.equals("en")) {
                    return "en";
                }
                return "";
            }
        });
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        provisioningService = container.get(ServiceProvisioningService.class);

        supplier = new Organization();
        supplier.setKey(11L);
        supplier.setName("SUPPLIER");

        product = new Product();
        product.setKey(1L);
        product.setVendor(supplier);
    }

    @Test
    public void getServiceSellerFallback_deEmpty()
            throws ObjectNotFoundException {
        VOOrganization sup = provisioningService.getServiceSellerFallback(1L,
                "de");
        assertEquals("SUPPLIER", sup.getName());
        assertEquals("en", sup.getDescription());
    }

    @Test
    public void testGetSupplierForService() throws ObjectNotFoundException {
        VOOrganization sup = provisioningService.getServiceSeller(1L, "en");
        assertEquals("SUPPLIER", sup.getName());
        assertEquals("en", sup.getDescription());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetSupplierForServiceError() throws ObjectNotFoundException {
        provisioningService.getServiceSeller(123L, "en");
    }

}
