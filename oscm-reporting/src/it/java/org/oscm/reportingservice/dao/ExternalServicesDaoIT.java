/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * Unit tests for {@link ExternalServicesDao} using the test EJB container.
 * 
 * @author barzu
 */
public class ExternalServicesDaoIT extends EJBTestBase {

    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
    }

    @Test
    public void executeQuery() throws Exception {
        // given
        final Organization supplier = createOrg();
        Product product = createProduct("serviceA", "techServiceA",
                supplier.getOrganizationId(), ServiceAccessType.EXTERNAL);
        createProduct("serviceB", "techServiceB", supplier.getOrganizationId(),
                ServiceAccessType.LOGIN);
        final ExternalServicesDao dao = new ExternalServicesDao(ds);

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                dao.executeQuery();
                return null;
            }
        });

        // then
        assertEquals(1, dao.getReportData().size());
        assertEquals("serviceA", dao.getReportData().get(0).getProductId());
        assertEquals(Long.valueOf(product.getKey()),
                dao.getReportData().get(0).getProductKey());
        assertEquals(product.getStatus().name(),
                dao.getReportData().get(0).getProductStatus());
        assertNotNull(dao.getReportData().get(0).getModdate());
        assertEquals(supplier.getAddress(),
                dao.getReportData().get(0).getAddress());
        assertEquals(
                supplier.getName() + " (" + supplier.getOrganizationId() + ")",
                dao.getReportData().get(0).getName());
        assertEquals(supplier.getPhone(),
                dao.getReportData().get(0).getPhone());
        assertEquals(supplier.getEmail(),
                dao.getReportData().get(0).getEmail());
        assertNotNull(dao.getReportData().get(0).getCountry());
    }

    private Organization createOrg() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
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
