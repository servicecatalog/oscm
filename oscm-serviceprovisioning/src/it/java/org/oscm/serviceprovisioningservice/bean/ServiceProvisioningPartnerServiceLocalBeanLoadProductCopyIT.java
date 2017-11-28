/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class ServiceProvisioningPartnerServiceLocalBeanLoadProductCopyIT extends
        EJBTestBase {

    private static final String TECHPRODUCT_ID = "testTechnicalProduct";
    private static final String PRODUCT_ID = "testProduct";

    private DataService ds;
    private ServiceProvisioningPartnerServiceLocalBean sppslBean;

    private Organization supplier;
    private Organization broker;
    private Organization customer;
    private Product productTemplate;
    private Product resaleCopy;
    private Product subscrCopy;

    @Override
    public void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());

        sppslBean = new ServiceProvisioningPartnerServiceLocalBean();

        ds = container.get(DataService.class);
        sppslBean.dm = ds;

        createOrganizations();
        createProducts();
    }

    private void createOrganizations() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);

                customer = Organizations.createOrganization(ds);

                return null;
            }
        });
    }

    private void createProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create technical product and product without price model
                productTemplate = Products.createProduct(
                        supplier.getOrganizationId(), PRODUCT_ID,
                        TECHPRODUCT_ID, ds, ServiceAccessType.LOGIN);

                resaleCopy = Products.createProductResaleCopy(productTemplate,
                        broker, ds);

                subscrCopy = resaleCopy.copyForSubscription(customer, null);
                subscrCopy.setTemplate(resaleCopy);
                ds.persist(subscrCopy);

                return null;
            }
        });
    }

    @Test
    public void getProductCopyForVendor() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                Product productCopy = sppslBean.loadProductCopyForVendor(
                        broker, productTemplate);

                // then
                assertEquals("Wrong key in product copy", productCopy.getKey(),
                        resaleCopy.getKey());
                assertEquals("Wrong product copy ID",
                        productCopy.getProductId(), resaleCopy.getProductId());
                assertEquals("Wrong vendor key in product copy",
                        productCopy.getVendorKey(), broker.getKey());
                assertEquals("Wrong template reference in product copy",
                        productCopy.getTemplate().getKey(),
                        productTemplate.getKey());
                return null;
            }
        });
    }
}
