/*******************************************************************************
 *                                                                              
7*  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                 
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
//import org.oscm.accountservice.bean.UserLicenseServiceLocalBean;
//import org.oscm.accountservice.dao.PaymentTypeDao;
//import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAlreadyBannedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CategorizationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.EmailType;

/**
 * Unit tests for the marketplace management with focus on an open marketplace.
 * 
 * @author groch
 * 
 */
@Ignore
public class MarketplaceServiceBeanOpenMPIT extends EJBTestBase {

    private static final String GLOBAL_NONOPEN_MP_ID = "GLOBAL_NONOPEN_MP";
    private static final String GLOBAL_OPEN_MP_ID = "GLOBAL_OPEN_MP";

    private static final String mpOwner_orgId = "mpOwnerId";
    private static final String mpOwner_userId = "mpOwnerUserId";

    private static final String supplier1_orgId = "supp1OrgId";
    private static final String supplier1_userId = "supp1UserId";
    private static final String supplier2_orgId = "supp2OrgId";

    private static final String supplierBanned_orgId = "existingSuppBannedOrgId";
    private static final String supplierPublishingGranted_orgId = "existingSuppPublishingGrantedOrgId";

    private Marketplace mpGlobalOpen;

    private MarketplaceService marketplaceService;
    private DataService ds;
    private CommunicationServiceLocal commSvcMock;

    private Organization mpOwner;
    private Organization supplier1;
    private Organization supplier2;
    private Organization supplierBanned;
    private Organization supplierPublishingGranted;

    private PlatformUser adminUserMpOwner;
    private PlatformUser adminUserSupp1;

    private EmailType sentMailType;
    private String sentMailMarketplaceId;
    private List<String> sentMailOrgIds;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        commSvcMock = mock(CommunicationServiceLocal.class);
        container.addBean(commSvcMock);
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(mock(MarketplaceAccessDao.class));
        container.addBean(mock(MarketplaceCacheService.class));
        container.addBean(mock(MarketplaceService.class));
        container.addBean(mock(MarketplaceServiceLocal.class));
//        container.addBean(new PaymentTypeDao());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(mock(LdapAccessServiceLocal.class));
        container.addBean(mock(PaymentServiceLocal.class));
        container.addBean(mock(TriggerQueueServiceLocal.class));
        container.addBean(mock(ApplicationServiceLocal.class));
        container.addBean(mock(ConfigurationServiceLocal.class));
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));
        container.addBean(mock(ServiceProvisioningPartnerServiceLocal.class));
        container.addBean(new SubscriptionAuditLogCollector());
//        container.addBean(mock(UserLicenseDao.class));
//        container.addBean(mock(UserLicenseServiceLocalBean.class));
        container.addBean(mock(AccountService.class));
        container.addBean(mock(ServiceProvisioningServiceLocal.class));
        container.addBean(new CategorizationServiceStub());
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(new MarketplaceAuditLogCollector());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        marketplaceService = container.get(MarketplaceService.class);
        ds = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(ds, mpOwner_orgId,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                adminUserMpOwner = PlatformUsers.createAdmin(ds, mpOwner_userId,
                        mpOwner);

                mpGlobalOpen = Marketplaces.createMarketplace(mpOwner,
                        GLOBAL_OPEN_MP_ID, true, ds);

                // create non-open mp
                Marketplaces.createMarketplace(mpOwner, GLOBAL_NONOPEN_MP_ID,
                        false, ds);

                // create suppliers with corresponding admin users
                supplier1 = Organizations.createOrganization(ds,
                        supplier1_orgId, OrganizationRoleType.SUPPLIER);
                adminUserSupp1 = PlatformUsers.createAdmin(ds, supplier1_userId,
                        supplier1);

                supplier2 = Organizations.createOrganization(ds,
                        supplier2_orgId, OrganizationRoleType.SUPPLIER);
                PlatformUsers.createAdmin(ds, "supplier2_userId", supplier2);

                supplierBanned = Organizations.createOrganization(ds,
                        supplierBanned_orgId, OrganizationRoleType.SUPPLIER);
                PlatformUsers.createAdmin(ds, "supplierBanned_userId",
                        supplierBanned);

                supplierPublishingGranted = Organizations.createOrganization(ds,
                        supplierPublishingGranted_orgId,
                        OrganizationRoleType.SUPPLIER);
                PlatformUsers.createAdmin(ds,
                        "supplierPublishingGranted_userId",
                        supplierPublishingGranted);

                // create customer org
                Organization customer = Organizations.createOrganization(ds,
                        "customer_orgId", OrganizationRoleType.CUSTOMER);
                PlatformUsers.createUser(ds, "customer_userId", customer);

                ds.flush();
                return null;
            }
        });

        // supplier is banned on the open mp
        createMTORef(mpGlobalOpen, supplierBanned,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        // supplier has already published a service on open mp
        createMTORef(mpGlobalOpen, supplierPublishingGranted,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        createAndPublishService("SvcOfSupplierPublishingGranted", mpGlobalOpen,
                supplierPublishingGranted);

        // prepare email setup
        sentMailType = null;
        sentMailMarketplaceId = null;
        sentMailOrgIds = new ArrayList<>();
        doAnswer((new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Assert.assertEquals(4, args.length);
                sentMailType = (EmailType) args[1];
                sentMailMarketplaceId = ((Marketplace) args[3])
                        .getMarketplaceId();
                sentMailOrgIds.add(((PlatformUser) args[0]).getOrganization()
                        .getOrganizationId());
                return null;
            }
        })).when(commSvcMock).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));

        // by default, login as admin user of open mp owner organization
        container.login(adminUserMpOwner.getKey(), ROLE_MARKETPLACE_OWNER);
    }

    private Product createAndPublishService(final String svcId,
            final Marketplace mp, final Organization supp) throws Exception {
        return runTX(new Callable<Product>() {

            @Override
            public Product call() throws Exception {
                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(ds, supp, svcId, false,
                                ServiceAccessType.LOGIN);
                return Products.createProduct(supp, tProd, false, svcId, null,
                        mp, ds);
            }
        });
    }

    private void assertServicePublishingStatus(final String svcId,
            final Organization supp, final ServiceStatus expectedStatus,
            final String mpId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product prod = new Product();
                prod.setProductId(svcId);
                prod.setVendorKey(supp.getKey());
                prod = (Product) ds.find(prod);

                assertNotNull("Product not found", prod);

                assertEquals(
                        "Product with id " + svcId + " has unexpected status",
                        expectedStatus, prod.getStatus());
                assertEquals(
                        "Catalog entry for product with id " + svcId
                                + " expected",
                        1, prod.getCatalogEntries().size());
                if (expectedStatus.equals(ServiceStatus.ACTIVE)) {
                    assertEquals(
                            "Wrong marketplace set at catalog entry for product with id "
                                    + svcId,
                            mpId, prod.getCatalogEntries().get(0)
                                    .getMarketplace().getMarketplaceId());
                } else if (expectedStatus.equals(ServiceStatus.INACTIVE)) {
                    assertNull(
                            "Wrong marketplace set at catalog entry for product with id "
                                    + svcId,
                            prod.getCatalogEntries().get(0).getMarketplace());
                } else
                    throw new RuntimeException(
                            "No handling for expected service status defined - revise test!");
                return null;
            }
        });
    }

    private void createMTORef(final Marketplace mp, final Organization org,
            final PublishingAccess pa) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mp, org, pa);
                ds.persist(mto);
                return null;
            }
        });
    }

    private void updateMTORef(final Marketplace mp, final Organization org,
            final PublishingAccess pa) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mp, org);
                mto = (MarketplaceToOrganization) ds.find(mto);
                mto.setPublishingAccess(pa);
                return null;
            }
        });
    }

    private void checkNonExistenceMTORef(final Marketplace mp,
            final Organization org) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mp, org);
                mto = (MarketplaceToOrganization) ds.find(mto);
                assertNull("No ref object expected", mto);
                return null;
            }
        });
    }

    private void checkExistenceMTORef(final Marketplace mp,
            final Organization org, final PublishingAccess pa)
            throws Exception {
        MarketplaceToOrganization mto = runTX(
                new Callable<MarketplaceToOrganization>() {
                    @Override
                    public MarketplaceToOrganization call() throws Exception {
                        MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                mp, org);
                        return (MarketplaceToOrganization) ds.find(mto);
                    }
                });
        assertNotNull("Relation object expected", mto);
        assertEquals("Wrong publishing access", pa, mto.getPublishingAccess());
        assertEquals("Wrong marketplace", mp.getKey(),
                mto.getMarketplace_tkey());
        assertEquals("Wrong supplier", org.getKey(),
                mto.getOrganization_tkey());
    }

    private void checkExpectedMailHasBeenSent(EmailType type, Marketplace mp,
            List<Organization> orgs) throws MailOperationException {
        assertNotNull("No mail has been sent", sentMailType);
        assertNotNull("No mail has been sent", sentMailMarketplaceId);
        assertNotNull("No mail has been sent", sentMailOrgIds);

        verify(commSvcMock, Mockito.times(orgs.size())).sendMail(
                any(PlatformUser.class), any(EmailType.class),
                any(Object[].class), any(Marketplace.class));

        assertEquals("Send email of wrong type", type, sentMailType);
        assertEquals("Wrong marketplace id used in sent email",
                mp.getMarketplaceId(), sentMailMarketplaceId);
        for (int i = 0; i < orgs.size(); i++) {
            assertEquals("Send email to wrong organization admin",
                    orgs.get(i).getOrganizationId(), sentMailOrgIds.get(i));
        }
    }

    private void assertExpectedSuppliersList(List<String> expectedOrgIds,
            List<VOOrganization> actualOrgs) {
        if (actualOrgs.size() > 0) {
            assertEquals("More or less suppliers expected",
                    expectedOrgIds.size(), actualOrgs.size());
            for (VOOrganization org : actualOrgs) {
                assertTrue("Unexpected supplier found",
                        expectedOrgIds.contains(org.getOrganizationId()));
            }
        }
    }

    private void checkBannedSuppliers(final PlatformUser mpOwnerAdminUser,
            final String mpId, final List<String> expectedBannedSuppliers)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        container.login(mpOwnerAdminUser.getKey(), ROLE_MARKETPLACE_OWNER);
        List<VOOrganization> bannedSuppList = marketplaceService
                .getBannedOrganizationsForMarketplace(mpId);
        assertExpectedSuppliersList(expectedBannedSuppliers, bannedSuppList);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testBanSuppliersFromMarketplace_nonOpenMp() throws Exception {
        marketplaceService.banOrganizationsFromMarketplace(
                Arrays.asList(supplier1_orgId), GLOBAL_NONOPEN_MP_ID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testBanSupplier_NonExistingSupplier() throws Exception {
        try {
            marketplaceService.banOrganizationsFromMarketplace(
                    Collections.singletonList("non_existing_supplier"),
                    GLOBAL_OPEN_MP_ID);
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.ORGANIZATION, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testBanSupplier_NonExistingMarketplace() throws Exception {
        try {
            marketplaceService.banOrganizationsFromMarketplace(
                    Collections.singletonList(supplier1_orgId),
                    "non_existing_marketplace");
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.MARKETPLACE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testBanSupplier_callingOrghasWrongRole() throws Throwable {
        container.login(adminUserSupp1.getKey(), ROLE_SERVICE_MANAGER);
        try {
            marketplaceService.banOrganizationsFromMarketplace(
                    Collections.singletonList(supplier1_orgId),
                    GLOBAL_OPEN_MP_ID);
        } catch (EJBException e) {
            assertTrue(e.getMessage()
                    .contains("Allowed roles are: [MARKETPLACE_OWNER]"));
            throw e.getCause();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testBanSupplier_callingOrgDoesNotOwnMp() throws Exception {
        PlatformUser otherMpOwner = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization mpOwner2 = Organizations.createOrganization(ds,
                        "otherMpOwner", OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser userMpOwner2 = PlatformUsers.createUser(ds,
                        "otherMpOwnerUser", mpOwner2);
                return userMpOwner2;
            }
        });

        container.login(otherMpOwner.getKey(), ROLE_MARKETPLACE_OWNER);
        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void testBanSupplier_banningNonSupplierOrg() throws Exception {
        Organization customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, "customerOrg");
            }
        });

        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(customer.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);
    }

    @Test
    public void testBanSupplier_BanOneSupplierWhoHasNeverPublishedOnMpBefore()
            throws Exception {

        checkNonExistenceMTORef(mpGlobalOpen, supplier1);

        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);

        checkExistenceMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(supplier1));
    }

    @Test
    public void testBanSupplier_BanOneSupplierWhoHasPublishedOnMpBefore()
            throws Exception {
        final String SVC_ID = "MySvc";

        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        createAndPublishService(SVC_ID, mpGlobalOpen, supplier1);
        assertServicePublishingStatus(SVC_ID, supplier1, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);

        checkExistenceMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        assertServicePublishingStatus(SVC_ID, supplier1, ServiceStatus.INACTIVE,
                null);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(supplier1));
    }

    @Test(expected = OrganizationAlreadyBannedException.class)
    public void testBanSupplier_BanSupplierAlreadyBanned() throws Exception {
        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        try {
            marketplaceService.banOrganizationsFromMarketplace(
                    Collections.singletonList(supplier1_orgId),
                    GLOBAL_OPEN_MP_ID);
        } catch (Exception e) {
            checkExistenceMTORef(mpGlobalOpen, supplier1,
                    PublishingAccess.PUBLISHING_ACCESS_DENIED);
            // verify no mail has been sent
            verify(commSvcMock, Mockito.never()).sendMail(
                    any(PlatformUser.class), any(EmailType.class),
                    any(Object[].class), any(Marketplace.class));
            throw e;
        }
    }

    @Test
    public void testBanSupplier_BanMultipleSuppliers() throws Exception {
        // Supplier1 has published before, supplier2 not
        final String SVC_ID_SUPP1 = "MySvcSupp1";

        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        createAndPublishService(SVC_ID_SUPP1, mpGlobalOpen, supplier1);
        assertServicePublishingStatus(SVC_ID_SUPP1, supplier1,
                ServiceStatus.ACTIVE, GLOBAL_OPEN_MP_ID);
        checkNonExistenceMTORef(mpGlobalOpen, supplier2);

        marketplaceService.banOrganizationsFromMarketplace(
                Arrays.asList(supplier1_orgId, supplier2_orgId),
                GLOBAL_OPEN_MP_ID);

        checkExistenceMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        assertServicePublishingStatus(SVC_ID_SUPP1, supplier1,
                ServiceStatus.INACTIVE, null);
        checkExistenceMTORef(mpGlobalOpen, supplier2,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Arrays.asList(supplier1, supplier2));
    }

    @Test
    public void banSupplier_BanOneBrokerWhoHasNeverPublishedOnMpBefore()
            throws Exception {
        // given a broker who has never published on the marketplace before
        Organization broker = givenBroker();
        checkNonExistenceMTORef(mpGlobalOpen, broker);

        // when banning the broker
        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the publishing access for the broker has been denied
        // and the expected e-mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(broker));
    }

    @Test
    public void banSupplier_BanOneBrokerWhoHasPublishedOnMpBefore()
            throws Exception {
        // given a broker who has published on the marketplace and has
        // publishing access granted
        Organization broker = givenBroker();
        createMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        final String SVC_ID = "MySvc";
        createAndPublishService(SVC_ID, mpGlobalOpen, broker);
        assertServicePublishingStatus(SVC_ID, broker, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        // when banning the broker
        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the publishing access for the broker has been denied,
        // that the service is inactive
        // and the expected mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        assertServicePublishingStatus(SVC_ID, broker, ServiceStatus.INACTIVE,
                null);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(broker));
    }

    @Test
    public void banSupplier_BanOneResellerWhoHasNeverPublishedOnMpBefore()
            throws Exception {
        // given a reseller who has never published on the marketplace before
        Organization reseller = givenReseller();
        checkNonExistenceMTORef(mpGlobalOpen, reseller);

        // when banning the reseller
        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the publishing access for the reseller has been denied
        // and the expected e-mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(reseller));
    }

    @Test
    public void banSupplier_BanOneResellerWhoHasPublishedOnMpBefore()
            throws Exception {
        // given a reseller who has published on the marketplace and has
        // publishing access granted
        Organization reseller = givenReseller();
        createMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        final String SVC_ID = "MySvc";
        createAndPublishService(SVC_ID, mpGlobalOpen, reseller);
        assertServicePublishingStatus(SVC_ID, reseller, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        // when banning the reseller
        marketplaceService.banOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the publishing access for the reseller has been denied,
        // that the service is inactive
        // and the expected e-mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        assertServicePublishingStatus(SVC_ID, reseller, ServiceStatus.INACTIVE,
                null);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_BANNED,
                mpGlobalOpen, Collections.singletonList(reseller));
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testLiftBanSuppliersFromMarketplace_nonOpenMp()
            throws Exception {
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Arrays.asList(supplier1_orgId), GLOBAL_NONOPEN_MP_ID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testLiftBanSupplier_NonExistingSupplier() throws Exception {
        try {
            marketplaceService.liftBanOrganizationsFromMarketplace(
                    Collections.singletonList("non_existing_supplier"),
                    GLOBAL_OPEN_MP_ID);
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.ORGANIZATION, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testLiftBanSupplier_NonExistingMarketplace() throws Exception {
        try {
            marketplaceService.liftBanOrganizationsFromMarketplace(
                    Collections.singletonList(supplier1_orgId),
                    "non_existing_marketplace");
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.MARKETPLACE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testLiftBanSupplier_callingOrghasWrongRole() throws Throwable {
        container.login(adminUserSupp1.getKey(), ROLE_SERVICE_MANAGER);
        try {
            marketplaceService.liftBanOrganizationsFromMarketplace(
                    Collections.singletonList(supplier1_orgId),
                    GLOBAL_OPEN_MP_ID);
        } catch (EJBException e) {
            assertTrue(e.getMessage()
                    .contains("Allowed roles are: [MARKETPLACE_OWNER]"));
            throw e.getCause();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testLiftBanSupplier_callingOrgDoesNotOwnMp() throws Exception {
        PlatformUser otherMpOwner = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization mpOwner2 = Organizations.createOrganization(ds,
                        "otherMpOwner", OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser userMpOwner2 = PlatformUsers.createUser(ds,
                        "otherMpOwnerUser", mpOwner2);
                return userMpOwner2;
            }
        });

        container.login(otherMpOwner.getKey(), ROLE_MARKETPLACE_OWNER);
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void testLiftBanSupplier_LiftBanningNonSupplierOrg()
            throws Exception {
        Organization customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, "customerOrg");
            }
        });

        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(customer.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);
    }

    @Test
    public void testLiftBanSupplier_LiftBanOneUnbannedSupplierWhoHasNeverPublishedOnMpBefore()
            throws Exception {

        checkNonExistenceMTORef(mpGlobalOpen, supplier1);

        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);

        checkNonExistenceMTORef(mpGlobalOpen, supplier1);
        // verify no mail has been sent
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void testLiftBanSupplier_LiftBanOneUnbannedSupplierWhoHasPublishedOnMpBefore()
            throws Exception {
        final String SVC_ID = "MySvc";

        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        createAndPublishService(SVC_ID, mpGlobalOpen, supplier1);
        assertServicePublishingStatus(SVC_ID, supplier1, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);

        checkExistenceMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        assertServicePublishingStatus(SVC_ID, supplier1, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);
        // verify no mail has been sent
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void testLiftBanSupplier_LiftBanSupplierBanned() throws Exception {
        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(supplier1_orgId), GLOBAL_OPEN_MP_ID);

        checkNonExistenceMTORef(mpGlobalOpen, supplier1);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_LIFTED_BAN,
                mpGlobalOpen, Arrays.asList(supplier1));
    }

    @Test
    public void testLiftBanSupplier_LiftBanMultipleSuppliers()
            throws Exception {
        // Supplier2 + SupplierBanned have been banned before, Supplier1 has not
        // been banned before (but published a svc)
        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        createMTORef(mpGlobalOpen, supplier2,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        marketplaceService.liftBanOrganizationsFromMarketplace(Arrays
                .asList(supplier1_orgId, supplier2_orgId, supplierBanned_orgId),
                GLOBAL_OPEN_MP_ID);

        checkExistenceMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        checkNonExistenceMTORef(mpGlobalOpen, supplier2);
        checkNonExistenceMTORef(mpGlobalOpen, supplierBanned);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_LIFTED_BAN,
                mpGlobalOpen, Arrays.asList(supplier2, supplierBanned));
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testGetBannedSuppliersForMarketplace_nonOpenMp()
            throws Exception {
        marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_NONOPEN_MP_ID);
    }

    @Test
    public void liftBanSupplier_LiftBanOneUnbannedBrokerWhoHasNeverPublishedOnMpBefore()
            throws Exception {
        // given an unbanned broker who has never published on the marketplace
        // before
        Organization broker = givenBroker();
        checkNonExistenceMTORef(mpGlobalOpen, broker);

        // when lifting the ban of the broker
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // lifting the ban has no effect and that no mail has been sent
        checkNonExistenceMTORef(mpGlobalOpen, broker);
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void liftBanSupplier_LiftBanOneUnbannedBrokerWhoHasPublishedOnMpBefore()
            throws Exception {
        // given an unbanned broker who has published on the marketplace before
        Organization broker = givenBroker();

        createMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        final String SVC_ID = "MySvc";
        createAndPublishService(SVC_ID, mpGlobalOpen, broker);
        assertServicePublishingStatus(SVC_ID, broker, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        // when lifting the ban of the broker
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the broker has still publishing access
        // that the service is active and that no mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        assertServicePublishingStatus(SVC_ID, broker, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void liftBanSupplier_LiftBanBrokerBanned() throws Exception {
        // given a banned broker
        Organization broker = givenBroker();
        createMTORef(mpGlobalOpen, broker,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        // when lifting the ban
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the broker-marketplace relation has been removed
        // and that the expected mail has been sent
        checkNonExistenceMTORef(mpGlobalOpen, broker);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_LIFTED_BAN,
                mpGlobalOpen, Arrays.asList(broker));
    }

    @Test
    public void liftBanSupplier_LiftBanOneUnbannedResellerWhoHasNeverPublishedOnMpBefore()
            throws Exception {
        // given an unbanned reseller who has never published on the marketplace
        // before
        Organization reseller = givenReseller();
        checkNonExistenceMTORef(mpGlobalOpen, reseller);

        // when lifting the ban of the reseller
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // lifting the ban has no effect and that no mail has been sent
        checkNonExistenceMTORef(mpGlobalOpen, reseller);
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void liftBanSupplier_LiftBanOneUnbannedResellerWhoHasPublishedOnMpBefore()
            throws Exception {
        // given an unbanned reseller who has published on the marketplace
        // before
        Organization reseller = givenReseller();

        createMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        final String SVC_ID = "MySvc";
        createAndPublishService(SVC_ID, mpGlobalOpen, reseller);
        assertServicePublishingStatus(SVC_ID, reseller, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);

        // when lifting the ban of the reseller
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the broker still has publishing access
        // that the service is active and that no mail has been sent.
        checkExistenceMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        assertServicePublishingStatus(SVC_ID, reseller, ServiceStatus.ACTIVE,
                GLOBAL_OPEN_MP_ID);
        verify(commSvcMock, Mockito.never()).sendMail(any(PlatformUser.class),
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class));
    }

    @Test
    public void liftBanSupplier_LiftBanResellerBanned() throws Exception {
        // given a banned reseller
        Organization reseller = givenReseller();
        createMTORef(mpGlobalOpen, reseller,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        // when lifting the ban of the reseller
        marketplaceService.liftBanOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_OPEN_MP_ID);

        // verify that the reseller-marketplace relation has been removed
        // and that the expected mail has been sent.
        checkNonExistenceMTORef(mpGlobalOpen, reseller);

        checkExpectedMailHasBeenSent(EmailType.MARKETPLACE_SUPPLIER_LIFTED_BAN,
                mpGlobalOpen, Arrays.asList(reseller));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetBannedSuppliers_NonExistingMarketplace()
            throws Exception {
        try {
            marketplaceService.getBannedOrganizationsForMarketplace(
                    "non_existing_marketplace");
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.MARKETPLACE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testGetBannedSuppliers_callingOrghasWrongRole()
            throws Throwable {
        container.login(adminUserSupp1.getKey(), ROLE_SERVICE_MANAGER);
        try {
            marketplaceService
                    .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
        } catch (EJBException e) {
            assertTrue(e.getMessage()
                    .contains("Allowed roles are: [MARKETPLACE_OWNER]"));
            throw e.getCause();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetBannedSuppliers_callingOrgDoesNotOwnMp()
            throws Exception {
        PlatformUser otherMpOwner = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization mpOwner2 = Organizations.createOrganization(ds,
                        "otherMpOwner", OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser userMpOwner2 = PlatformUsers.createUser(ds,
                        "otherMpOwnerUser", mpOwner2);
                return userMpOwner2;
            }
        });

        container.login(otherMpOwner.getKey(), ROLE_MARKETPLACE_OWNER);
        marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
    }

    @Test
    public void testGetBannedSuppliers_default() throws Exception {
        // this test also verifies that orgs where no MTO ref exists are not
        // contained in result list, e.g. supplier1
        checkNonExistenceMTORef(mpGlobalOpen, supplier1);
        List<VOOrganization> bannedSuppList = marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
        assertExpectedSuppliersList(Arrays.asList(supplierBanned_orgId),
                bannedSuppList);
    }

    @Test
    public void testGetBannedSuppliers_BanOneSupplierWhoHasNeverPublishedOnMpBefore()
            throws Exception {
        // ban supplier1
        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        List<VOOrganization> bannedSuppList = marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
        assertExpectedSuppliersList(
                Arrays.asList(supplierBanned_orgId, supplier1_orgId),
                bannedSuppList);
    }

    @Test
    public void testGetBannedSuppliers_BanOneSupplierWhoHasPublishedOnMpBefore()
            throws Exception {
        // ban supplier
        updateMTORef(mpGlobalOpen, supplierPublishingGranted,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        List<VOOrganization> bannedSuppList = marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
        assertExpectedSuppliersList(Arrays.asList(supplierBanned_orgId,
                supplierPublishingGranted_orgId), bannedSuppList);
    }

    @Test
    public void testGetBannedSuppliers_liftBanOnSupplier() throws Exception {
        // lift ban supplier
        updateMTORef(mpGlobalOpen, supplierBanned,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        List<VOOrganization> bannedSuppList = marketplaceService
                .getBannedOrganizationsForMarketplace(GLOBAL_OPEN_MP_ID);
        assertEquals("No supplier must be contained in list", 0,
                bannedSuppList.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetBannedSuppliers_BanSupplierWhoHasPublishedOnMultipleMps_verifyNoInteraction()
            throws Exception {

        final String global_open_mp_id_2 = "OPEN_MP_2";
        final Organization otherMpOwner = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization mpOwner2 = Organizations.createOrganization(ds,
                        "otherMpOwner", OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUsers.createUser(ds, "otherMpOwnerUser", mpOwner2);
                return mpOwner2;
            }
        });
        final PlatformUser adminUserMpOwner2 = runTX(
                new Callable<PlatformUser>() {
                    @Override
                    public PlatformUser call() throws Exception {
                        return PlatformUsers.createAdmin(ds,
                                "otherMpOwnerAdminUser", otherMpOwner);
                    }
                });
        Marketplace mpGlobalOpen2 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(otherMpOwner,
                        global_open_mp_id_2, true, ds);
            }
        });

        // supplier1 must neither be banned from openMp1 nor openMp2
        checkBannedSuppliers(adminUserMpOwner, GLOBAL_OPEN_MP_ID,
                Arrays.asList(supplierBanned_orgId));
        checkBannedSuppliers(adminUserMpOwner2, global_open_mp_id_2,
                Collections.EMPTY_LIST);

        // ban supplier1 from openMp1 (must not be banned from openMp2)
        createMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        checkBannedSuppliers(adminUserMpOwner, GLOBAL_OPEN_MP_ID,
                Arrays.asList(supplierBanned_orgId, supplier1_orgId));
        checkBannedSuppliers(adminUserMpOwner2, global_open_mp_id_2,
                Collections.EMPTY_LIST);

        // ban supplier1 from openMp2 (must be banned from openMp1 + openMp2)
        createMTORef(mpGlobalOpen2, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_DENIED);
        checkBannedSuppliers(adminUserMpOwner, GLOBAL_OPEN_MP_ID,
                Arrays.asList(supplierBanned_orgId, supplier1_orgId));
        checkBannedSuppliers(adminUserMpOwner2, global_open_mp_id_2,
                Arrays.asList(supplier1_orgId));

        // lift ban supplier1 from openMp1 (must be banned from openMp2)
        updateMTORef(mpGlobalOpen, supplier1,
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        checkBannedSuppliers(adminUserMpOwner, GLOBAL_OPEN_MP_ID,
                Arrays.asList(supplierBanned_orgId));
        checkBannedSuppliers(adminUserMpOwner2, global_open_mp_id_2,
                Arrays.asList(supplier1_orgId));
    }

    private Organization givenBroker() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                PlatformUsers.createAdmin(ds, "brokerAdmin", broker);

                return broker;
            }
        });
    }

    private Organization givenReseller() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                PlatformUsers.createAdmin(ds, "resellerAdmin", reseller);
                return reseller;
            }
        });
    }

}
