/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 01.12.2011                                                      
 *                                                                              
 *  Completion Time: 05.12.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.accountservice.dao.TechnicalProductDao;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.MarketingPermissionNotFoundException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Tests of the Account service that are related to the managing of suppliers
 * their rights to use technical services.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class MarketingPermissionServiceBeanIT extends EJBTestBase {

    private MarketingPermissionServiceLocal ms;
    private DataService ds;

    private Organization techProvider1;
    private PlatformUser techProvider1User;
    private Organization techProvider2;
    private PlatformUser techProvider2User;
    private TechnicalProduct techProd1;
    private long techProdKey;
    private TechnicalProduct techProd2;
    private TechnicalProduct techProd3;
    private Organization supplier1;
    private Organization supplier2;
    private Organization supplier3;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new TechnicalProductDao());
        container.addBean(new MarketingPermissionServiceBean());

        ms = container.get(MarketingPermissionServiceLocal.class);
        ds = container.get(DataService.class);

        initData();

        container.login(techProvider1User.getKey(), ROLE_TECHNOLOGY_MANAGER);
    }

    @Test
    public void removeMarketingPermission_NonExistingTechnicalService()
            throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ms.removeMarketingPermission(0, new ArrayList<String>());
                    return null;
                }
            });
            fail("Operation should have failed");
        } catch (ObjectNotFoundException e) {
            assertEquals("ex.ObjectNotFoundException.TECHNICAL_SERVICE",
                    e.getMessageKey());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void removeMarketingPermission_ForeignTechnicalService()
            throws Exception {
        container.login(techProvider2User.getKey(), ROLE_TECHNOLOGY_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        new ArrayList<String>());
                return null;
            }
        });
    }

    @Test
    public void removeMarketingPermission_SupplierWithNoRelation()
            throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ms.removeMarketingPermission(techProdKey,
                            Arrays.asList(supplier1.getOrganizationId()));
                    return null;
                }
            });
            fail();
        } catch (MarketingPermissionNotFoundException e) {
            assertEquals(supplier1.getOrganizationId(),
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void removeMarketingPermission_SuppliersWithNoRelation()
            throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ms.removeMarketingPermission(techProdKey,
                            Arrays.asList(supplier1.getOrganizationId(),
                                    supplier2.getOrganizationId()));
                    return null;
                }
            });
            fail();
        } catch (MarketingPermissionNotFoundException e) {
            assertEquals(
                    supplier1.getOrganizationId() + ", "
                            + supplier2.getOrganizationId(),
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void removeMarketingPermission_SupplierWithRelation()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier1.getOrganizationId()));
                return null;
            }
        });
        assertMarketingPermissionCount(0, techProd1.getKey());
    }

    @Test
    public void removeMarketingPermission_CheckServiceStateChangeTemplate()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        Product product = createMarketableService(supplier1, techProd1, false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier1.getOrganizationId()));
                return null;
            }
        });
        validateServiceState(product.getKey(), ServiceStatus.OBSOLETE);
    }

    @Test
    public void removeMarketingPermission_CheckServiceStateChangeCustomerCopy()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        Product product = createMarketableService(supplier1, techProd1, true);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier1.getOrganizationId()));
                return null;
            }
        });
        validateServiceState(product.getKey(), ServiceStatus.OBSOLETE);
        // a bit risky to do key guessing, but it is almost impossible that
        // another product has been created in the meantime
        validateServiceState(product.getKey() - 1, ServiceStatus.OBSOLETE);
    }

    @Test
    public void removeMarketingPermission_LastProdForSupplier()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier1.getOrganizationId()));
                return null;
            }
        });
        assertOrganizationReferenceCount(0, techProd1.getOrganizationKey(),
                supplier1.getKey(),
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
    }

    @Test
    public void removeMarketingPermission_multipleSuppliersOneInInput()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier2.getOrganizationId()));
                return null;
            }
        });
        List<OrganizationReference> refs = assertMarketingPermissionCount(1,
                techProd1.getKey());
        // only the permission for supplier 1 must remain
        assertEquals(supplier1.getKey(), refs.get(0).getTargetKey());
    }

    @Test
    public void removeMarketingPermission_multipleSuppliersSubsetInInput()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier2.getOrganizationId(),
                                supplier1.getOrganizationId()));
                return null;
            }
        });
        List<OrganizationReference> refs = assertMarketingPermissionCount(1,
                techProd1.getKey());
        // only the permission for supplier 1 must remain
        assertEquals(supplier3.getKey(), refs.get(0).getTargetKey());
    }

    @Test
    public void removeMarketingPermission_multipleSuppliersAllInInput()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermission(techProdKey,
                        Arrays.asList(supplier2.getOrganizationId(),
                                supplier1.getOrganizationId(),
                                supplier3.getOrganizationId()));
                return null;
            }
        });
        assertMarketingPermissionCount(0, techProd1.getKey());
        assertOrgRefHistoryNotDeletedCount(0, techProd1.getKey());
    }

    @Test
    public void removeMarketingPermission_multipleSuppliersTxnBehavior()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd1.getKey());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ms.removeMarketingPermission(techProdKey,
                            Arrays.asList(supplier2.getOrganizationId(),
                                    supplier3.getOrganizationId()));
                    return null;
                }
            });
            fail();
        } catch (MarketingPermissionNotFoundException e) {
            assertEquals(supplier2.getOrganizationId(),
                    e.getMessageParams()[0]);
        }

        List<OrganizationReference> refs = assertMarketingPermissionCount(1,
                techProd1.getKey());
        // only the permission for supplier 1 must remain
        assertEquals(supplier1.getKey(), refs.get(0).getTargetKey());
    }

    /**
     * This test creates the following setup:
     * <p>
     * There are 2 technology providers TP1 and TP2. TP1 has 4 technical
     * services TS1 to TS4.
     * </p>
     * <p>
     * TS1 exists with 2 permissions, one for each of the Suppliers S1, and S3.
     * TS2 has just one supplier S2 that is allowed to use it, whereas TS3 is
     * only useable by supplier S1. TS4 has no permissions granted to any
     * supplier so far.
     * </p>
     * <p>
     * Now a call by TP1 is made to the server to remove sup2 and sup3 from TS1.
     * </p>
     * <p>
     * <b>Expected result:</b> sup1 must still be allowed to use TS1, sup3 is
     * not allowed to do so anymore, the corresponding marketing permission,
     * and, as sup3 had just one usable technical service, also the organization
     * reference must be removed. Furthermore an exception must be thrown
     * containing the information that the operation failed for sup2.
     * </p>
     * 
     */
    @Test
    public void removeMarketingPermission_complexScenario() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd2.getKey());
        createMarketingPermission(supplier1.getKey(), techProd3.getKey());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ms.removeMarketingPermission(techProdKey,
                            Arrays.asList(supplier2.getOrganizationId(),
                                    supplier3.getOrganizationId()));
                    return null;
                }
            });
            fail();
        } catch (MarketingPermissionNotFoundException e) {
            assertEquals(supplier2.getOrganizationId(),
                    e.getMessageParams()[0]);
        }

        List<OrganizationReference> refs = assertMarketingPermissionCount(1,
                techProd1.getKey());
        // only the permission for supplier 1 must remain
        assertEquals(supplier1.getKey(), refs.get(0).getTargetKey());
        assertOrganizationReferenceCount(0, techProvider1.getKey(),
                supplier3.getKey(),
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getSuppliersForTechnicalService_NonExistingService()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.getSuppliersForTechnicalService(-1);
                return null;
            }
        });
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getSuppliersForTechnicalService_ForeignService()
            throws Exception {
        container.login(techProvider2User.getKey(), ROLE_TECHNOLOGY_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.getSuppliersForTechnicalService(techProdKey);
                return null;
            }
        });
    }

    @Test
    public void getSuppliersForTechnicalService_EmptyResult() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Organization> result = ms
                        .getSuppliersForTechnicalService(techProdKey);
                assertNotNull(result);
                assertTrue(result.isEmpty());
                return null;
            }
        });
    }

    @Test
    public void getSuppliersForTechnicalService_OneResult() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProdKey);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Organization> result = ms
                        .getSuppliersForTechnicalService(techProdKey);
                assertEquals(1, result.size());
                Organization organization = result.get(0);
                assertEquals(supplier1.getKey(), organization.getKey());
                return null;
            }
        });
    }

    @Test
    public void getSuppliersForTechnicalService_SeveralResults()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProdKey);
        createMarketingPermission(supplier3.getKey(), techProdKey);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Organization> result = ms
                        .getSuppliersForTechnicalService(techProdKey);
                assertEquals(2, result.size());
                assertTrue(result.contains(supplier1));
                assertTrue(result.contains(supplier3));
                return null;
            }
        });
    }

    @Test
    public void addMarketingPermission_again() throws Exception {
        Product product = createMarketableService(supplier1, techProd1, false);
        final long prdKey = product.getKey();
        createMarketingPermission(supplier1.getKey(), techProdKey);

        setProductStatus(prdKey, ServiceStatus.ACTIVE);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = ds.find(Product.class, prdKey);
                assertEquals(ServiceStatus.ACTIVE, product.getStatus());
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.addMarketingPermission(techProvider1, techProdKey,
                        Collections
                                .singletonList(supplier1.getOrganizationId()));
                Product product = ds.find(Product.class, prdKey);
                assertEquals(ServiceStatus.ACTIVE, product.getStatus());
                return null;
            }
        });
    }

    @Test
    public void removeMarketingPermissions_VerifyOrgRef() throws Exception {

        MarketingPermission mp = createMarketingPermission(supplier1.getKey(),
                techProd1.getKey());

        final long orgRefKey = mp.getOrganizationReferenceKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermissions(techProd1);
                List<TechnicalProduct> result = ms
                        .getTechnicalServicesForSupplier(supplier1);
                assertEquals(0, result.size());
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OrganizationReference organizationReference = ds
                        .find(OrganizationReference.class, orgRefKey);
                assertNull(organizationReference);
                return null;
            }
        });

    }

    @Test
    public void removeMarketingPermissions_OneSupplier() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermissions(techProd1);
                List<TechnicalProduct> result = ms
                        .getTechnicalServicesForSupplier(supplier1);
                assertEquals(0, result.size());
                return null;
            }
        });
    }

    @Test
    public void removeMarketingPermissions() throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd1.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermissions(techProd1);
                assertEquals(0,
                        ms.getTechnicalServicesForSupplier(supplier1).size());
                assertEquals(0,
                        ms.getTechnicalServicesForSupplier(supplier2).size());
                assertEquals(0,
                        ms.getTechnicalServicesForSupplier(supplier3).size());
                return null;
            }
        });
    }

    @Test
    public void removeMarketingPermissions_OtherTechnicalProducts()
            throws Exception {
        createMarketingPermission(supplier1.getKey(), techProd1.getKey());
        createMarketingPermission(supplier2.getKey(), techProd1.getKey());
        createMarketingPermission(supplier3.getKey(), techProd2.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ms.removeMarketingPermissions(techProd1);
                assertEquals(0,
                        ms.getTechnicalServicesForSupplier(supplier1).size());
                assertEquals(0,
                        ms.getTechnicalServicesForSupplier(supplier2).size());
                assertEquals(1,
                        ms.getTechnicalServicesForSupplier(supplier3).size());
                return null;
            }
        });
    }

    private void assertOrganizationReferenceCount(final int expectedCount,
            final long tpKey, final long supplierKey,
            final OrganizationReferenceType type) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds.createQuery(
                        "SELECT COUNT(*) FROM OrganizationReference orgRef WHERE orgRef.source.key = :tpkey AND orgRef.target.key = :supplierKey AND orgRef.dataContainer.referenceType = :type");
                query.setParameter("tpkey", Long.valueOf(tpKey));
                query.setParameter("supplierKey", Long.valueOf(supplierKey));
                query.setParameter("type", type);
                Long count = (Long) query.getSingleResult();
                assertEquals(expectedCount, count.intValue());
                return null;
            }
        });
    }

    private List<OrganizationReference> assertMarketingPermissionCount(
            final int expectedCount, final long technicalServiceKey)
            throws Exception {
        return runTX(new Callable<List<OrganizationReference>>() {
            @Override
            public List<OrganizationReference> call() throws Exception {
                Query query = ds.createQuery(
                        "SELECT mp.organizationReference FROM MarketingPermission mp WHERE mp.technicalProduct.key = :tpKey");
                query.setParameter("tpKey", Long.valueOf(technicalServiceKey));
                List<OrganizationReference> keys = ParameterizedTypes.list(
                        query.getResultList(), OrganizationReference.class);
                assertEquals(expectedCount, keys.size());
                return keys;
            }
        });
    }

    private void assertOrgRefHistoryNotDeletedCount(final int expectedCount,
            final long technicalServiceKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds.createQuery(
                        "SELECT COUNT(orgRefHist) FROM OrganizationReferenceHistory orgRefHist, MarketingPermission mp WHERE mp.technicalProductKey = :tpKey AND mp.organizationReferenceKey = orgRefHist.objKey AND orgRefHist.modType <> :modType AND orgRefHist.objVersion = (SELECT MAX(innerOrgRefHist.objVersion) FROM OrganizationReferenceHistory innerOrgRefHist WHERE orgRefHist.objKey = innerOrgRefHist.objKey)");
                query.setParameter("tpKey", Long.valueOf(technicalServiceKey));
                query.setParameter("modType", ModificationType.DELETE);
                Long count = (Long) query.getSingleResult();
                assertEquals(expectedCount, count.longValue());
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

                techProvider2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                techProvider2User = Organizations.createUserForOrg(ds,
                        techProvider2, true, "admin2");

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

    private Product createMarketableService(final Organization supplier,
            final TechnicalProduct techProd, final boolean createCustomerCopy)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Marketplaces.createGlobalMarketplace(supplier, "mid", ds);
                Product template = Products.createProduct(supplier, techProd,
                        false, "product1", "pm1", ds);
                if (createCustomerCopy) {
                    return Products.createCustomerSpecifcProduct(ds, supplier,
                            template, ServiceStatus.INACTIVE);
                }
                return template;
            }
        });
    }

    private void validateServiceState(final long key,
            final ServiceStatus status) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product prodRef = ds.getReference(Product.class, key);
                assertEquals(status, prodRef.getStatus());
                return null;
            }
        });
    }

    private void setProductStatus(final long productKey,
            final ServiceStatus status) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = ds.find(Product.class, productKey);
                product.setStatus(status);
                return null;
            }
        });
    }
}
