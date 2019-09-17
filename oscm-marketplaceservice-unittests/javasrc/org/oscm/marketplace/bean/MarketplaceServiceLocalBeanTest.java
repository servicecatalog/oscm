/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceAccess;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOCategory;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;

public class MarketplaceServiceLocalBeanTest {

    private MarketplaceServiceLocalBean service;
    private PlatformUser user;
    private Organization mpOwnerOrg;
    private final static String TrackcodeExpected = "<script>alert('hello world')</script>";
    private final static String TrackcodeChanged = "<script>alert('hello script')</script>";
    private Product product;
    private final List<VOCategory> voCategories = new ArrayList<>();

    private static final String SUPPLIER_ID = "MySupplier";
    private static final String BROKER_ID = "MyBroker";
    private static final String RESELLER_ID = "MyReseller";
    private static final String SERVICE_ID1 = "AGreatService";
    private static final String SERVICE_ID2 = "AnotherService";

    @Before
    public void setup() throws Exception {
        service = spy(new MarketplaceServiceLocalBean());
        service.ds = mock(DataService.class);
        service.sessionCtx = mock(SessionContext.class);
        service.audit = mock(MarketplaceAuditLogCollector.class);
        service.categorizationService = mock(CategorizationServiceBean.class);
        service.landingpageService = mock(LandingpageServiceBeanLocal.class);
        service.partnerSrvProv = mock(ServiceProvisioningPartnerServiceLocal.class);
        service.localizer = mock(LocalizerServiceLocal.class);
        service.marketplaceAccessDao = mock(MarketplaceAccessDao.class);
        service.marketplaceCache = mock(MarketplaceCacheService.class);
        service.accountService = mock(AccountServiceLocal.class);

        user = new PlatformUser();
        mpOwnerOrg = new Organization();
        mpOwnerOrg.setOrganizationId("org");
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(OrganizationRoleType.MARKETPLACE_OWNER);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(orgRole);
        orgToRole.setOrganization(mpOwnerOrg);
        mpOwnerOrg.getGrantedRoles().add(orgToRole);
        user.setOrganization(mpOwnerOrg);
        doReturn(user).when(service.ds).getCurrentUser();
        doReturn(mpOwnerOrg).when(service.ds).getReferenceByBusinessKey(
                any(Organization.class));
        doNothing().when(service.marketplaceCache).resetConfiguration(
                anyString());
        doReturn(Boolean.TRUE).when(service.localizer).storeLocalizedResource(
                anyString(), anyLong(), any(LocalizedObjectTypes.class),
                anyString());
        doReturn("localizedText").when(service.localizer)
                .getLocalizedTextFromDatabase(anyString(), anyLong(),
                        any(LocalizedObjectTypes.class));
        doReturn(Collections.EMPTY_LIST).when(service.accountService)
                .getOrganizationAdmins(anyLong());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateRevenueShare_versionMismatch() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = givenRevenueShareModel();
        revenueShareModelNew.setRevenueShare(BigDecimal.ONE);
        doReturn(givenRevenueShareModel()).when(service.ds).getReference(
                eq(RevenueShareModel.class), anyLong());

        // when
        service.updateRevenueShare(revenueShareModelNew, -1);
    }

    @Test
    public void updateRevenueShare() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = givenRevenueShareModel();
        revenueShareModelNew.setRevenueShare(BigDecimal.ONE);
        RevenueShareModel persistedRevenueShare = givenRevenueShareModel();
        doReturn(persistedRevenueShare).when(service.ds).getReference(
                eq(RevenueShareModel.class), anyLong());

        // when
        service.updateRevenueShare(revenueShareModelNew, 1);

        // then
        assertEquals(BigDecimal.ONE, persistedRevenueShare.getRevenueShare());
    }

    @Test
    public void updateRevenueShare_NullRevenueShareValue() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = new RevenueShareModel();
        revenueShareModelNew.setRevenueShare(null);

        // when
        try {
            service.updateRevenueShare(revenueShareModelNew, 1);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.REQUIRED, e.getReason());
        }
    }

    @Test
    public void updateRevenueShare_InvalidRevenueShare() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = givenRevenueShareModel();
        revenueShareModelNew.setRevenueShare(new BigDecimal("122.0"));
        doReturn(givenRevenueShareModel()).when(service.ds).getReference(
                eq(RevenueShareModel.class), anyLong());
        // when
        try {
            service.updateRevenueShare(revenueShareModelNew, 1);
            fail();
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
        }
    }

    @Test
    public void updateMarketplace_MpCache() throws Exception {
        // given
        addUserRole(UserRoleType.PLATFORM_OPERATOR);
        addUserRole(UserRoleType.MARKETPLACE_OWNER);
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("MP");
        user.getOrganization().getMarketplaces().add(mp);
        doReturn(Boolean.FALSE).when(service).updateOwningOrganization(
                any(Marketplace.class), anyString(), anyBoolean());

        // when
        service.updateMarketplace(mp, "MP", "ORG");

        // then
        verify(service.marketplaceCache, atLeastOnce()).resetConfiguration(
                anyString());
    }

    @Test
    public void updateMarketplace_RevenueSharesNotUpdated() throws Exception {
        // when
        // addUserRole(UserRoleType.PLATFORM_OPERATOR);
        addUserRole(UserRoleType.MARKETPLACE_OWNER);
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("MP");
        mp.setOrganization(mpOwnerOrg);
        mpOwnerOrg.getMarketplaces().add(mp);

        service.updateMarketplace(mp, mp, mp.getMarketplaceId(), user
                .getOrganization().getOrganizationId(), 100, 100, 100);

        // then
        verify(service, times(0)).updateRevenueShare(
                any(RevenueShareModel.class), anyInt());
        verify(service.marketplaceCache, atLeastOnce()).resetConfiguration(
                anyString());
    }

    @Test
    public void updateMarketplace_RevenueSharesUpdated() throws Exception {
        // given
        addUserRole(UserRoleType.PLATFORM_OPERATOR);
        addUserRole(UserRoleType.MARKETPLACE_OWNER);
        doReturn(null).when(service).updateRevenueShare(
                any(RevenueShareModel.class), anyInt());
        Marketplace newMarketplace = givenMarketplace();
        mpOwnerOrg.getMarketplaces().add(newMarketplace);

        // when
        service.updateMarketplace(newMarketplace, newMarketplace, "name", "id",
                1, 2, 3);

        // then
        verify(service, times(1)).updateRevenueShare(
                eq(newMarketplace.getPriceModel()), eq(1));
        verify(service, times(1)).updateRevenueShare(
                eq(newMarketplace.getResellerPriceModel()), eq(2));
        verify(service, times(1)).updateRevenueShare(
                eq(newMarketplace.getBrokerPriceModel()), eq(3));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateMarketplaceTrackingCode_ObjectNotFound() throws Exception {
        // given
        addUserRole(UserRoleType.MARKETPLACE_OWNER);
        Marketplace newMarketplace = givenMarketplace();
        doThrow(new ObjectNotFoundException()).when(service).getMarketplace(
                anyString());

        // when
        service.updateMarketplaceTrackingCode(
                newMarketplace.getMarketplaceId(), newMarketplace.getVersion(),
                TrackcodeChanged);
    }

    @Test
    public void updateMarketplaceTrackingCode() throws Exception {
        // given
        addUserRole(UserRoleType.MARKETPLACE_OWNER);
        Marketplace newMarketplace = givenMarketplace();
        Marketplace dbMarketplace = givenMarketplace();
        dbMarketplace.setTrackingCode("<script>another script</script>");
        doReturn(dbMarketplace).when(service).getMarketplace(anyString());

        // when
        service.updateMarketplaceTrackingCode(
                newMarketplace.getMarketplaceId(), newMarketplace.getVersion(),
                TrackcodeChanged);

        // then
        assertEquals(TrackcodeChanged, dbMarketplace.getTrackingCode());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateMarketplaceTrackingCode_ConcurrentModificationException()
            throws Exception {
        // given
        Marketplace marketplace = givenMarketplace();
        doReturn(marketplace).when(service).getMarketplace(anyString());
        int oldMarketplaceVersion = marketplace.getVersion() - 1;

        // when
        service.updateMarketplaceTrackingCode(marketplace.getMarketplaceId(),
                oldMarketplaceVersion, TrackcodeChanged);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getTrackingCodeFromMarketplace_ObjectNotFound()
            throws Exception {
        // given
        Marketplace newMarketplace = givenMarketplace();
        doThrow(new ObjectNotFoundException()).when(service).getMarketplace(
                anyString());
        // when
        service.getTrackingCodeFromMarketplace(newMarketplace
                .getMarketplaceId());
    }

    @Test
    public void getTrackingCodeFromMarketplace() throws Exception {
        // given
        Marketplace newMarketplace = givenMarketplace();
        doReturn(newMarketplace).when(service).getMarketplace(anyString());
        // when
        String tcode = service.getTrackingCodeFromMarketplace(newMarketplace
                .getMarketplaceId());
        // then
        assertEquals(TrackcodeExpected, tcode);
    }

    @Test
    public void addMarketplaceToOrganization()
            throws NonUniqueBusinessKeyException {
        // given
        Marketplace mp = givenMarketplace();
        mp.setKey(123L);
        Organization org = new Organization();
        org.setKey(567L);
        ArgumentCaptor<MarketplaceToOrganization> mto = ArgumentCaptor
                .forClass(MarketplaceToOrganization.class);

        // when
        service.addMarketplaceToOrganization(mp, org);

        // then
        verify(service.ds, times(1)).persist(mto.capture());
        assertEquals(mp.getKey(), mto.getValue().getMarketplace().getKey());
        assertEquals(org.getKey(), mto.getValue().getOrganization().getKey());
    }

    @Test
    public void addMarketplaceToOrganization_ExistingReference()
            throws NonUniqueBusinessKeyException {
        // given
        Marketplace mp = givenMarketplace();
        mp.setKey(123L);
        Organization org = new Organization();
        org.setKey(567L);
        MarketplaceToOrganization mto = new MarketplaceToOrganization(mp, org);
        doReturn(mto).when(service.ds).find(
                any(MarketplaceToOrganization.class));

        // when
        service.addMarketplaceToOrganization(mp, org);

        // then
        verify(service.ds, times(0)).persist(
                any(MarketplaceToOrganization.class));
    }

    @Test
    public void updateMarketplace_Rollback_ValidationException()
            throws Exception {
        // given
        doThrow(new ValidationException()).when(service).updateMarketplace(
                any(Marketplace.class), anyString(), anyString());
        // when
        try {
            service.updateMarketplace(any(Marketplace.class),
                    any(Marketplace.class), anyString(), anyString(), anyInt(),
                    anyInt(), anyInt());
            fail();
        } catch (ValidationException e) {
            // then
            verify(service.sessionCtx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void updateMarketplace_Rollback_ObjectNotFoundException()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(service).updateMarketplace(
                any(Marketplace.class), anyString(), anyString());
        // when
        try {
            service.updateMarketplace(any(Marketplace.class),
                    any(Marketplace.class), anyString(), anyString(), anyInt(),
                    anyInt(), anyInt());
            fail();
        } catch (ObjectNotFoundException e) {
            // then
            verify(service.sessionCtx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void updateMarketplace_Rollback_UserRoleAssignmentException()
            throws Exception {
        // given
        doThrow(new UserRoleAssignmentException()).when(service)
                .updateMarketplace(any(Marketplace.class), anyString(),
                        anyString());
        // when
        try {
            service.updateMarketplace(any(Marketplace.class),
                    any(Marketplace.class), anyString(), anyString(), anyInt(),
                    anyInt(), anyInt());
            fail();
        } catch (UserRoleAssignmentException e) {
            // then
            verify(service.sessionCtx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void publishService_noChanges() throws Exception {
        // given
        CatalogEntry ce = givenCatalogEntry(givenMarketplace(10000, null), true);
        givenProduct(ce);

        // when
        product = service.publishService(product.getKey(), ce, voCategories);

        // then
        verifyAudit(0, 0, 0);
    }

    @Test
    public void publishService_allChanged() throws Exception {
        // given
        Marketplace mpNew = givenMarketplace(10001, givenOrganization());
        CatalogEntry ce = givenCatalogEntry(givenMarketplace(10000, null), true);
        CatalogEntry ceNew = givenCatalogEntry(mpNew, false);
        givenProduct(ce);

        doReturn(mpNew).when(service.ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        doReturn(Boolean.TRUE).when(service.categorizationService)
                .updateAssignedCategories(any(CatalogEntry.class),
                        anyListOf(VOCategory.class));
        // when
        product = service.publishService(product.getKey(), ceNew, voCategories);
        // then
        assertEquals(Boolean.FALSE, new Boolean(product.getCatalogEntries()
                .get(0).isAnonymousVisible()));
    }

    @Test
    public void publishService_withoutMarketplace() throws Exception {
        // given
        CatalogEntry ce = givenCatalogEntry(null, true);
        givenProduct(ce);

        // when
        product = service.publishService(product.getKey(), ce,
                new ArrayList<VOCategory>());

        // then
        verifyAudit(0, 0, 0);
    }

    @Test
    public void publishServiceWithPermissionsTest() throws Exception {
        // given
        List<POResalePermissionDetails> permDetails = spy(new ArrayList<POResalePermissionDetails>());
        doReturn(null).when(service).publishService(anyLong(),
                any(CatalogEntry.class), anyListOf(VOCategory.class));
        RoleAssignment roleAssignment = new RoleAssignment();
        UserRole role = new UserRole();
        role.setRoleName(UserRoleType.SERVICE_MANAGER);
        roleAssignment.setRole(role);
        user.getAssignedRoles().add(roleAssignment);
        // when
        product = service.publishServiceWithPermissions(0, new CatalogEntry(),
                new ArrayList<VOCategory>(), permDetails, permDetails);

        // then
        verify(service, times(1)).publishService(anyLong(),
                any(CatalogEntry.class), anyListOf(VOCategory.class));
        verify(permDetails, times(2)).iterator();
    }

    private Marketplace givenMarketplace(int marketplaceId, Organization org) {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(String.valueOf(marketplaceId));
        mp.setKey(marketplaceId);

        if (org != null) {
            List<MarketplaceToOrganization> marketplaceToOrganizations = new ArrayList<MarketplaceToOrganization>();
            MarketplaceToOrganization marketplaceToOrganization = new MarketplaceToOrganization(
                    mp, org);
            marketplaceToOrganizations.add(marketplaceToOrganization);
            marketplaceToOrganization
                    .setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_GRANTED);
            mp.setMarketplaceToOrganizations(marketplaceToOrganizations);
        }
        return mp;
    }

    private Organization givenOrganization() {
        Organization supplier = new Organization();
        supplier.setOrganizationId("supplier");
        user.setOrganization(supplier);
        return supplier;
    }

    private CatalogEntry givenCatalogEntry(Marketplace mp,
            boolean isAnonymousVisible) {
        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(isAnonymousVisible);
        return ce;
    }

    private void givenProduct(CatalogEntry ce) throws Exception {
        product = new Product();
        List<CatalogEntry> ces = new ArrayList<CatalogEntry>();
        ces.add(ce);
        product.setCatalogEntries(ces);
        product.setTemplate(new Product());
        ce.setProduct(product);
        doReturn(product).when(service).loadProductAndVerifyOwner(anyLong(),
                any(Organization.class));
    }

    private void verifyAudit(int serviceAsPublicTimes,
            int assignToMarketPlaceTimes, int assignCategoriesTimes) {
        verify(service.audit, times(serviceAsPublicTimes)).setServiceAsPublic(
                any(DataService.class), any(Product.class), anyBoolean());
        verify(service.audit, times(assignToMarketPlaceTimes))
                .assignToMarketPlace(any(DataService.class),
                        any(Product.class), anyString(), anyString());
        verify(service.audit, times(assignCategoriesTimes)).assignCategories(
                any(DataService.class), any(Product.class),
                anyListOf(VOCategory.class));
    }

    private static RevenueShareModel givenRevenueShareModel() {
        RevenueShareModel model = new RevenueShareModel();
        model.setKey(1L);
        model.setRevenueShare(BigDecimal.ZERO);
        model.setRevenueShareModelType(RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        return model;
    }

    private static Marketplace givenMarketplace() {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("mId");
        mp.setOrganization(new Organization());
        mp.setPriceModel(new RevenueShareModel());
        mp.getPriceModel().setRevenueShare(BigDecimal.ZERO);
        mp.setResellerPriceModel(new RevenueShareModel());
        mp.getResellerPriceModel().setRevenueShare(BigDecimal.ZERO);
        mp.setBrokerPriceModel(new RevenueShareModel());
        mp.getBrokerPriceModel().setRevenueShare(BigDecimal.ZERO);
        mp.setTrackingCode(TrackcodeExpected);
        return mp;
    }

    private void addUserRole(UserRoleType userRole) {
        UserRole r = new UserRole();
        r.setRoleName(userRole);
        RoleAssignment ra = new RoleAssignment();
        ra.setRole(r);
        user.getAssignedRoles().addAll(Collections.singleton(ra));
        // user.setAssignedRoles(Collections.singleton(ra));
    }

    @Test
    public void grantResalePermissions() throws Exception {
        // given
        List<POResalePermissionDetails> resalePermDetails = new ArrayList<POResalePermissionDetails>();
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                BROKER_ID, SERVICE_ID1, OfferingType.BROKER));
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                RESELLER_ID, SERVICE_ID2, OfferingType.RESELLER));

        // when
        service.grantResalePermissions(resalePermDetails);

        // then
        verify(service.partnerSrvProv).grantResalePermission(SERVICE_ID1,
                SUPPLIER_ID, BROKER_ID, OfferingType.BROKER);
        verify(service.partnerSrvProv).grantResalePermission(SERVICE_ID2,
                SUPPLIER_ID, RESELLER_ID, OfferingType.RESELLER);
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void grantResalePermissions_NullArgument() throws Exception {
        // when
        service.grantResalePermissions(null);
    }

    @Test
    public void grantResalePermissions_WrongOfferingType() throws Exception {
        // given
        List<POResalePermissionDetails> resalePermDetails = new ArrayList<POResalePermissionDetails>();
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                BROKER_ID, SERVICE_ID1, OfferingType.BROKER));
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                RESELLER_ID, SERVICE_ID2, OfferingType.DIRECT));

        Product brokerResaleCopy = new Product();
        brokerResaleCopy.setProductId(SERVICE_ID1 + "#4711");

        doReturn(brokerResaleCopy).when(service.partnerSrvProv)
                .grantResalePermission(SERVICE_ID1, SUPPLIER_ID, BROKER_ID,
                        OfferingType.BROKER);
        doThrow(
                new IllegalArgumentException(
                        "Parameter resaleType has an illegal value.")).when(
                service.partnerSrvProv).grantResalePermission(SERVICE_ID2,
                SUPPLIER_ID, RESELLER_ID, OfferingType.DIRECT);

        try {
            // when
            service.grantResalePermissions(resalePermDetails);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // then
            verify(service.partnerSrvProv).grantResalePermission(SERVICE_ID2,
                    SUPPLIER_ID, RESELLER_ID, OfferingType.DIRECT);
        }
    }

    private POResalePermissionDetails createResalePermissionDetails(
            String grantorID, String granteeID, String serviceID,
            OfferingType offeringType) {
        POResalePermissionDetails resPermDetails = new POResalePermissionDetails();

        POOrganization grantor = new POOrganization();
        grantor.setOrganizationId(grantorID);
        resPermDetails.setGrantor(grantor);

        POOrganization grantee = new POOrganization();
        grantee.setOrganizationId(granteeID);
        resPermDetails.setGrantee(grantee);

        POServiceDetails service = new POServiceDetails();
        service.setServiceId(serviceID);
        resPermDetails.setService(service);

        resPermDetails.setOfferingType(offeringType);

        return resPermDetails;
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeResalePermissions_NullArgument() throws Exception {
        // when
        service.revokeResalePermissions(null);
    }

    @Test
    public void revokeResalePermissions() throws Exception {
        // given
        List<POResalePermissionDetails> resalePermDetails = new ArrayList<POResalePermissionDetails>();
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                BROKER_ID, SERVICE_ID1, null));
        resalePermDetails.add(createResalePermissionDetails(SUPPLIER_ID,
                RESELLER_ID, SERVICE_ID2, null));

        // when
        service.revokeResalePermissions(resalePermDetails);

        // then
        verify(service.partnerSrvProv).revokeResalePermission(SERVICE_ID1,
                SUPPLIER_ID, BROKER_ID);
        verify(service.partnerSrvProv).revokeResalePermission(SERVICE_ID2,
                SUPPLIER_ID, RESELLER_ID);
    }

    @Test
    public void testUpdateMarketplaceAccessType_notChangedAccessType()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        // given
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(1L);
        marketplace.setMarketplaceId("marketplaceId");
        marketplace.setRestricted(true);
        doReturn(marketplace).when(service.ds).getReferenceByBusinessKey(
                any(Marketplace.class));

        // when
        service.updateMarketplaceAccessType("marketplaceId", true);

        // then
        verify(service.ds, times(0)).persist(any(Marketplace.class));
        verify(service.marketplaceCache, times(0)).resetConfiguration(
                anyString());
    }

    @Test
    public void testUpdateMarketplaceAccessType()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        // given
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(1L);
        marketplace.setMarketplaceId("marketplaceId");
        marketplace.setRestricted(false);
        doReturn(marketplace).when(service.ds).getReferenceByBusinessKey(
                any(Marketplace.class));

        // when
        service.updateMarketplaceAccessType("marketplaceId", true);

        // then
        assertTrue(marketplace.isRestricted());
        verify(service.ds, times(1)).persist(any(Marketplace.class));
        verify(service.marketplaceCache, atLeastOnce()).resetConfiguration(
                anyString());
    }

    @Test
    public void testGrantAccessToMarketPlaceToOrganizations()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        // given
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(1L);
        marketplace.setMarketplaceId("marketplaceId");
        marketplace.setRestricted(false);

        Organization organization = new Organization();
        organization.setKey(1L);
        organization.setOrganizationId("organizationId");

        when(service.ds.getReferenceByBusinessKey(any(MarketplaceAccess.class)))
                .thenThrow(new ObjectNotFoundException());

        // when
        service.grantAccessToMarketPlaceToOrganization(marketplace,
                organization);

        // then
        verify(service.ds, times(1)).persist(any(MarketplaceAccess.class));
    }

    @Test
    public void testRemoveMarketplaceAccesses() {
        // given
        List<MarketplaceAccess> accesses = new ArrayList<>();
        MarketplaceAccess mp = new MarketplaceAccess();
        mp.setMarketplace_tkey(1L);
        mp.setOrganization_tkey(1L);
        accesses.add(mp);
        MarketplaceAccess mp1 = new MarketplaceAccess();
        mp1.setMarketplace_tkey(1L);
        mp1.setOrganization_tkey(2L);
        accesses.add(mp1);
        doReturn(accesses).when(service.marketplaceAccessDao)
                .getForMarketplaceKey(1L);

        // when
        service.removeMarketplaceAccesses(1L);

        // then
        verify(service.marketplaceAccessDao, times(1))
                .removeAccessForMarketplace(anyLong());
    }

    @Test
    public void testRemoveMarketplaceAccess() throws ObjectNotFoundException {
        // given

        MarketplaceAccess access = new MarketplaceAccess();
        access.setMarketplace_tkey(1L);
        access.setOrganization_tkey(1L);

        Marketplace mp = new Marketplace();
        mp.setKey(1L);
        mp.setMarketplaceId("id");

        Product prod = new Product();
        prod.setKey(1L);

        Organization org = new Organization();
        org.setKey(1L);
        org.setProducts(Arrays.asList(prod));

        prod.setVendor(org);

        CatalogEntry ce = new CatalogEntry();
        ce.setKey(1L);
        ce.setMarketplace(mp);

        prod.setCatalogEntries(Arrays.asList(ce));

        doReturn(access).when(service.ds).getReferenceByBusinessKey(
                any(MarketplaceAccess.class));
        doReturn(mp).when(service.ds).getReference(Marketplace.class, 1L);
        doReturn(org).when(service.ds).getReference(Organization.class, 1L);

        // when
        service.removeMarketplaceAccess(1L, 1L);

        // then
        verify(service.ds, times(1)).remove(any(MarketplaceAccess.class));
    }

    @Test
    public void testDoesAccessToMarketplaceExistForOrganization()
            throws ObjectNotFoundException {
        // given
        MarketplaceAccess access = new MarketplaceAccess();
        access.setMarketplace_tkey(1L);
        access.setOrganization_tkey(1L);

        doReturn(access).when(service.ds).getReferenceByBusinessKey(
                any(MarketplaceAccess.class));

        // when
        boolean result = service.doesAccessToMarketplaceExistForOrganization(
                1L, 1L);

        // then
        assertTrue(result);
    }

    @Test
    public void grantAccessToMarketplaceToOrganization_MpCache()
            throws NonUniqueBusinessKeyException {
        // given
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("MP1");
        mp.setKey(100L);
        mp.setOrganization(mpOwnerOrg);

        // when
        service.grantAccessToMarketPlaceToOrganization(mp, mpOwnerOrg);

        // then
        verify(service.marketplaceCache, atLeastOnce()).resetConfiguration(
                mp.getMarketplaceId());
    }
}
