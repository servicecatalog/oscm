/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 18, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author zhaoh
 * 
 */
public class TechnicalProductDaoIT extends EJBTestBase {

    private DataService ds;
    private TechnicalProductDao dao;

    private Organization techProvider1;
    private PlatformUser techProvider1User;
    private Organization techProvider3;
    private TechnicalProduct techProd1;
    private long techProdKey;
    private TechnicalProduct techProd2;
    private TechnicalProduct techProd3;
    private TechnicalProduct tp3TechProd1;
    private TechnicalProduct tp3TechProd2;
    private Organization supplier1;
    private Organization supplier2;
    private Organization supplier3;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new TechnicalProductDao());

        ds = container.get(DataService.class);
        dao = container.get(TechnicalProductDao.class);

        initData();

        container.login(techProvider1User.getKey(), ROLE_TECHNOLOGY_MANAGER);
    }

    @Test
    public void retrieveTechnicalProduct_NoResults() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<TechnicalProduct> result = dao
                        .retrieveTechnicalProduct(supplier1);
                assertNotNull(result);
                assertTrue(result.isEmpty());
                return null;
            }
        });
    }

    @Test
    public void retrieveTechnicalProduct_NoSupplier() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<TechnicalProduct> result = dao
                        .retrieveTechnicalProduct(techProvider1);
                assertNotNull(result);
                assertTrue(result.isEmpty());
                return null;
            }
        });
    }

    @Test
    public void retrieveTechnicalProduct_OneHit() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProdKey);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<TechnicalProduct> result = dao
                        .retrieveTechnicalProduct(supplier1);
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(techProdKey, result.get(0).getKey());
                return null;
            }
        });
    }

    @Test
    public void retrieveTechnicalProduct_SomeHits() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProdKey);
        createMarketingPermission(supplier1.getKey(), techProd3.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<TechnicalProduct> result = dao
                        .retrieveTechnicalProduct(supplier1);
                assertNotNull(result);
                assertEquals(2, result.size());
                assertTrue(result.contains(techProd1));
                assertTrue(result.contains(techProd3));
                return null;
            }
        });
    }

    @Test
    public void retrieveTechnicalProduct_SeveralProviders() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProdKey);
        createMarketingPermission(supplier1.getKey(), techProd3.getKey());
        createMarketingPermission(supplier1.getKey(), tp3TechProd1.getKey());
        createMarketingPermission(supplier2.getKey(), tp3TechProd1.getKey());
        createMarketingPermission(supplier3.getKey(), tp3TechProd2.getKey());
        createMarketingPermission(supplier3.getKey(), techProd2.getKey());
        createMarketingPermission(supplier3.getKey(), techProd3.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<TechnicalProduct> result = dao
                        .retrieveTechnicalProduct(supplier1);

                assertNotNull(result);
                assertEquals(3, result.size());
                assertTrue(result.contains(techProd1));
                assertTrue(result.contains(techProd3));
                assertTrue(result.contains(tp3TechProd1));

                result = dao.retrieveTechnicalProduct(supplier2);
                assertNotNull(result);
                assertEquals(1, result.size());
                assertTrue(result.contains(tp3TechProd1));

                result = dao.retrieveTechnicalProduct(supplier3);
                assertNotNull(result);
                assertEquals(3, result.size());
                assertTrue(result.contains(techProd2));
                assertTrue(result.contains(techProd3));
                assertTrue(result.contains(tp3TechProd2));
                return null;
            }
        });
    }

    private void initData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                techProvider1 = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                techProvider1User = Organizations.createUserForOrg(ds,
                        techProvider1, true, "admin1");
                techProd1 = TechnicalProducts.createTechnicalProduct(ds,
                        techProvider1, "techServ1", false,
                        ServiceAccessType.LOGIN);
                techProd2 = TechnicalProducts.createTechnicalProduct(ds,
                        techProvider1, "techServ2", false,
                        ServiceAccessType.LOGIN);
                techProd3 = TechnicalProducts.createTechnicalProduct(ds,
                        techProvider1, "techServ3", false,
                        ServiceAccessType.LOGIN);
                TechnicalProducts.createTechnicalProduct(ds, techProvider1,
                        "techServ4", false, ServiceAccessType.LOGIN);

                techProvider3 = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                tp3TechProd1 = TechnicalProducts.createTechnicalProduct(ds,
                        techProvider3, "tp3TechServ1", false,
                        ServiceAccessType.LOGIN);
                tp3TechProd2 = TechnicalProducts.createTechnicalProduct(ds,
                        techProvider3, "tp3TechServ2", false,
                        ServiceAccessType.LOGIN);

                supplier1 = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                supplier2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                supplier3 = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
        techProdKey = techProd1.getKey();
    }

    private MarketingPermission createMarketingPermission(
            final long supplierKey, final long technicalServiceKey)
            throws Exception {
        return runTX(new Callable<MarketingPermission>() {
            @Override
            public MarketingPermission call() throws Exception {
                Organization targetSupplier = ds
                        .getReference(Organization.class, supplierKey);
                TechnicalProduct targetTS = ds.getReference(
                        TechnicalProduct.class, technicalServiceKey);
                Organization targetTechProv = targetTS.getOrganization();

                List<OrganizationReference> tps = targetSupplier
                        .getSourcesForType(
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                List<Organization> technologyProviders = new ArrayList<>();
                for (OrganizationReference orgRef : tps) {
                    technologyProviders.add(orgRef.getSource());
                }
                if (!technologyProviders.contains(targetTechProv)) {
                    Organizations.createOrganizationReference(targetTechProv,
                            targetSupplier,
                            OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER,
                            ds);
                }
                ds.flush();
                ds.refresh(targetSupplier);
                MarketingPermission mp = new MarketingPermission();
                mp.setOrganizationReference(targetSupplier
                        .getSourcesForType(
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER)
                        .get(0));
                mp.setTechnicalProduct(targetTS);
                ds.persist(mp);
                return mp;
            }
        });
    }
}
