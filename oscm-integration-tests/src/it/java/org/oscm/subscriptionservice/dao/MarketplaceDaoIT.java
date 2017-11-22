/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.06.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
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
 * Unit tests for {@link MarketplaceDao} using the test EJB container.
 * 
 * @author Mao
 */
public class MarketplaceDaoIT extends EJBTestBase {

    protected static final String GLOBAL_MP_ID = "GLOBAL_MP";
    private DataService ds;
    private MarketplaceDao dao;
    private Product product;
    private Organization supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new MarketplaceDao(ds);
        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);
    }

    @Test
    public void getMarketplaceByService() throws Exception {
        // when
        List<Marketplace> result = runTX(new Callable<List<Marketplace>>() {
            @Override
            public List<Marketplace> call() throws Exception {
                return dao.getMarketplaceByService(product);
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
