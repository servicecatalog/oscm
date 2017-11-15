/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-5                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

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
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Unit tests for {@link ProductDao} using the test EJB container.
 * 
 * @author Mao
 */
public class ProductDaoIT extends EJBTestBase {

    private DataService ds;
    private ProductDao dao;
    private Product product;
    private Organization supplier;
    private Organization supplierCustomer;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new ProductDao(ds);
        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierCustomer = registerCustomer("supplierCustomer", supplier);

        product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);
    }

    @Test
    public void getCopyForCustomer() throws Exception {
        // when
        List<Product> result = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() throws Exception {
                return dao.getCopyForCustomer(product, supplierCustomer);
            }
        });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void getProductForCustomerOnly() throws Exception {

        // when
        List<Product> result = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() throws Exception {
                return dao.getProductForCustomerOnly(supplierCustomer.getKey(),
                        supplierCustomer);
            }
        });

        // then
        assertEquals(0, result.size());
    }

    private Organization createOrg(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private Organization registerCustomer(final String customerId,
            final Organization vendor) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createCustomer(ds, vendor, customerId,
                        false);
            }
        });
    }

    private Product createProduct(final String productId,
            final String techProductId, final String organizationId,
            final ServiceAccessType type) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(organizationId, productId,
                        techProductId, ds, type);
            }
        });
    }
}
