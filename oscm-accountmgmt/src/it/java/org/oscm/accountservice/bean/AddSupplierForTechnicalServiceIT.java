/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                   
 *                                                                              
 *  Creation Date: 02.12.2011                                                      
 *                                                                              
 *  Completion Time: 02.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.bean.LdapAccessStub;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;

/**
 * @author kulle
 * 
 */
public class AddSupplierForTechnicalServiceIT extends EJBTestBase {

    private DataService dataMgr;
    private AccountService accountMgr;

    private Organization techProvider;
    private Organization supplier;
    private PlatformUser techProviderAdmin;
    private TechnicalProduct technicalProduct;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(mock(TriggerQueueServiceLocal.class));
        container.addBean(mock(ReviewServiceLocalBean.class));
        container.addBean(new LocalizerServiceStub());
        container.addBean(new ImageResourceServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new TaskQueueServiceStub());
        container.addBean(new IdentityServiceBean());
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new AccountServiceBean());

        dataMgr = container.get(DataService.class);
        accountMgr = container.get(AccountService.class);

        techProvider = registerTechnologyProvider();
        techProviderAdmin = registerAdminUser(techProvider, "admin");
        technicalProduct = createTechnicalProduct(techProvider);
        supplier = registerSupplier();
    }

    @Test
    public void testAddSupplierToProduct() throws Exception {
        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));

        // assert
        assertEquals(1, getMarketingPermissionCount().longValue());
        assertEquals(1, getOrganizationReferences().size());
        OrganizationReference reference = getOrganizationReferences().get(0);
        assertEquals(techProvider.getKey(), reference.getSourceKey());
        assertEquals(supplier.getKey(), reference.getTargetKey());
    }

    @Test
    public void testAddSupplierToProduct_OrgRefPresent() throws Exception {
        OrganizationReference reference = createOrganizationReference(
                techProvider, supplier);
        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));

        // assert
        List<MarketingPermission> marketingPermissions = getMarketingPermissions();
        assertEquals(1, marketingPermissions.size());
        assertEquals(technicalProduct.getKey(), marketingPermissions.get(0)
                .getTechnicalProductKey());
        assertEquals(reference.getKey(), marketingPermissions.get(0)
                .getOrganizationReferenceKey());
    }

    @Test
    public void testAddSuppliersToProduct_NoOrgRef() throws Exception {
        Organization anotherSupplier1 = registerSupplier();
        Organization anotherSupplier2 = registerSupplier();

        List<String> orgIds = new ArrayList<String>();
        orgIds.add(supplier.getOrganizationId());
        orgIds.add(anotherSupplier1.getOrganizationId());
        orgIds.add(anotherSupplier2.getOrganizationId());

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct), orgIds);

        // assert
        assertEquals(3, getMarketingPermissionCount().longValue());
        assertEquals(3, getOrganizationReferences().size());
    }

    @Test
    public void testAddAnotherSupplier() throws Exception {
        Organization anotherSupplier = registerSupplier();

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(anotherSupplier.getOrganizationId()));

        // assert
        assertEquals(2, getMarketingPermissionCount().longValue());
        assertEquals(2, getOrganizationReferences().size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testNotOwnerOfProduct() throws Exception {
        Organization anotherProvider = registerTechnologyProvider();
        PlatformUser adminUser = registerAdminUser(anotherProvider, "admin");

        container.login(String.valueOf(adminUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));
    }

    @Test(expected = AddMarketingPermissionException.class)
    public void testNotSupplier() throws Exception {
        Organization anotherProvider = registerTechnologyProvider();

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(anotherProvider.getOrganizationId()));
    }

    @Test
    public void testOneSupplierIsMissingSupplierRole() throws Exception {
        Organization anotherSupplier = registerSupplier();
        Organization anotherProvider = registerTechnologyProvider();

        List<String> orgIds = new ArrayList<String>();
        orgIds.add(supplier.getOrganizationId());
        orgIds.add(anotherSupplier.getOrganizationId());
        orgIds.add(anotherProvider.getOrganizationId());

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);

        try {
            accountMgr.addSuppliersForTechnicalService(
                    convertTechnicalProduct(technicalProduct), orgIds);
            fail();
        } catch (AddMarketingPermissionException e) {
            assertTrue(e.getMessage().contains(
                    "MarketingPermission for technical service '"
                            + technicalProduct.getKey()
                            + "' and supplier ids '"
                            + anotherProvider.getOrganizationId()
                            + "' could not be added."));
        }
        assertEquals(2, getMarketingPermissionCount().longValue());
        assertEquals(2, getOrganizationReferences().size());
    }

    @Test
    public void testSupplierAreMissingSupplierRole() throws Exception {
        Organization anotherProvider1 = registerTechnologyProvider();
        Organization anotherProvider2 = registerTechnologyProvider();

        List<String> orgIds = new ArrayList<String>();
        orgIds.add(supplier.getOrganizationId());
        orgIds.add(anotherProvider1.getOrganizationId());
        orgIds.add(anotherProvider2.getOrganizationId());

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);

        try {
            accountMgr.addSuppliersForTechnicalService(
                    convertTechnicalProduct(technicalProduct), orgIds);
            fail();
        } catch (AddMarketingPermissionException e) {
            assertTrue(e.getMessage().contains(
                    "MarketingPermission for technical service '"
                            + technicalProduct.getKey()
                            + "' and supplier ids '"
                            + anotherProvider1.getOrganizationId() + ", "
                            + anotherProvider2.getOrganizationId()
                            + "' could not be added."));
        }
        assertEquals(1, getMarketingPermissionCount().longValue());
        assertEquals(1, getOrganizationReferences().size());
    }

    @Test
    public void testSupplierListEmpty() throws Exception {
        List<String> emptyList = new ArrayList<String>();
        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            accountMgr.addSuppliersForTechnicalService(
                    convertTechnicalProduct(technicalProduct), emptyList);
            fail();
        } catch (EJBException e) {
            assertTrue(e.getCausedByException() instanceof org.oscm.internal.types.exception.IllegalArgumentException);
        }
    }

    @Test
    public void testAddSupplier2Times() throws Exception {
        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));

        assertEquals(1, getMarketingPermissionCount().longValue());
        assertEquals(1, getOrganizationReferences().size());
    }

    @Test
    public void testServiceStatusSupplier() throws Exception {
        final Product product1 = createProduct(supplier.getOrganizationId(),
                technicalProduct.getOrganization().getOrganizationId(),
                "productId1", technicalProduct.getTechnicalProductId());
        final Product product2 = createProduct(supplier.getOrganizationId(),
                technicalProduct.getOrganization().getOrganizationId(),
                "productId2", technicalProduct.getTechnicalProductId());

        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct),
                Collections.singletonList(supplier.getOrganizationId()));

        // assert
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p1 = dataMgr.find(Product.class, product1.getKey());
                Product p2 = dataMgr.find(Product.class, product2.getKey());
                assertEquals(ServiceStatus.INACTIVE, p1.getStatus());
                assertEquals(ServiceStatus.INACTIVE, p2.getStatus());
                return null;
            }
        });
    }

    @Test
    public void testServiceStatusSuppliers() throws Exception {
        final Product product1 = createProduct(supplier.getOrganizationId(),
                technicalProduct.getOrganization().getOrganizationId(),
                "productId1", technicalProduct.getTechnicalProductId());
        final Product product2 = createProduct(supplier.getOrganizationId(),
                technicalProduct.getOrganization().getOrganizationId(),
                "productId2", technicalProduct.getTechnicalProductId());

        // another supplier
        Organization anotherSupplier = registerSupplier();
        final Product product3 = createProduct(
                anotherSupplier.getOrganizationId(), technicalProduct
                        .getOrganization().getOrganizationId(), "productId1",
                technicalProduct.getTechnicalProductId());

        List<String> orgIds = new ArrayList<String>();
        orgIds.add(supplier.getOrganizationId());
        orgIds.add(anotherSupplier.getOrganizationId());
        container.login(String.valueOf(techProviderAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgr.addSuppliersForTechnicalService(
                convertTechnicalProduct(technicalProduct), orgIds);

        // assert
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p1 = dataMgr.find(Product.class, product1.getKey());
                Product p2 = dataMgr.find(Product.class, product2.getKey());
                Product p3 = dataMgr.find(Product.class, product3.getKey());
                assertEquals(ServiceStatus.INACTIVE, p1.getStatus());
                assertEquals(ServiceStatus.INACTIVE, p2.getStatus());
                assertEquals(ServiceStatus.INACTIVE, p3.getStatus());
                return null;
            }
        });
    }

    private Organization registerTechnologyProvider() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        dataMgr, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return organization;
            }
        });
    }

    private PlatformUser registerAdminUser(final Organization org,
            final String adminId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(dataMgr, org, true,
                        adminId);
            }
        });
    }

    private TechnicalProduct createTechnicalProduct(
            final Organization techProvider) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct product = TechnicalProducts.createTestData(
                        dataMgr, techProvider, 1).get(0);
                return product;
            }
        });
    }

    private Organization registerSupplier() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        dataMgr, OrganizationRoleType.SUPPLIER);
                return organization;
            }
        });
    }

    private OrganizationReference createOrganizationReference(
            final Organization techProvider, final Organization supplier)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                OrganizationReference reference = Organizations
                        .createOrganizationReference(
                                techProvider,
                                supplier,
                                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER,
                                dataMgr);
                return reference;
            }
        });
    }

    private VOTechnicalService convertTechnicalProduct(
            TechnicalProduct technicalProduct) {
        VOTechnicalService techSrv = new VOTechnicalService();
        techSrv.setKey(technicalProduct.getKey());
        return techSrv;
    }

    private Long getMarketingPermissionCount() throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Query query = dataMgr
                        .createQuery("SELECT count(permission) FROM MarketingPermission permission");
                return (Long) query.getSingleResult();
            }
        });
    }

    private List<MarketingPermission> getMarketingPermissions()
            throws Exception {
        return runTX(new Callable<List<MarketingPermission>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<MarketingPermission> call() throws Exception {
                Query query = dataMgr
                        .createQuery("SELECT permission FROM MarketingPermission permission");
                return query.getResultList();
            }
        });
    }

    private List<OrganizationReference> getOrganizationReferences()
            throws Exception {
        return runTX(new Callable<List<OrganizationReference>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<OrganizationReference> call() throws Exception {
                Query query = dataMgr
                        .createQuery("SELECT ref FROM OrganizationReference ref WHERE ref.dataContainer.referenceType=:param1");
                query.setParameter("param1", OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                return query.getResultList();
            }
        });
    }

    private Product createProduct(final String supplierId,
            final String technicalProviderId, final String productId,
            final String techPrdId) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProduct(supplierId,
                        technicalProviderId, productId, techPrdId, dataMgr,
                        ServiceAccessType.LOGIN);
                product.setStatus(ServiceStatus.OBSOLETE);
                dataMgr.persist(product);
                return product;
            }
        });
    }
}
