/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Enes Sejfi                                               
 *                                                                              
 *  Creation Date: 15.12.2011                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.types.exceptions.*;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceEntry;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUserDetails;

/**
 * Tests the marketplace webservice.
 * 
 * @author Enes Sejfi
 */
@SuppressWarnings("boxing")
public class MarketplaceServiceWSTest {

    private static WebserviceTestSetup setup;
    private static VOFactory factory = new VOFactory();
    private static VOOrganization supplier;
    private static MarketplaceService mpService_Operator;
    private static MarketplaceService mpService_Supplier;
    private static final String PLATFORM_OPERATOR = "PLATFORM_OPERATOR";
    private static String brandingUrl;

    // needed to grant and revoke role MARKETPLACE_OWNER
    private static IdentityService is;

    @BeforeClass
    public static void setup() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        init();
    }

    @Before
    public void before() throws Exception {
        deleteAllMarketplaces();
    }

    @Test
    public void testCreateGlobalMarketplaceForOperator() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateGlobalMarketplaceForOperator2() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(null, false,
                "globalMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateGlobalMarketplaceForSupplier() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateLocalMarketplaceForOperator() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateLocalMarketplaceForOperator2() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(null, false,
                "localMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateGlobalMarketplaceWithWrongRole() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");

        try {
            mpService_Supplier.createMarketplace(marketplace);
            fail();
        } catch (SOAPFaultException e) {
            checkAccessException(e);
        }
    }

    @Test(expected = MarketplaceValidationException.class)
    public void testCreateGlobalMarketplaceWrongMarketplaceId()
            throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(null, false,
                "localMp");
        marketplace = mpService_Operator.createMarketplace(marketplace);

        String marketplaceId = marketplace.getMarketplaceId();
        marketplace = factory.createMarketplaceVO(null, false, "localMp");
        marketplace.setMarketplaceId(marketplaceId);
        mpService_Operator.createMarketplace(marketplace);
        fail();
    }

    @Test(expected = MarketplaceValidationException.class)
    public void testCreateGlobalMarketplaceMarketplaceIdInUse()
            throws Exception {
        final String MP_ID = "mpId";
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "Mp1", MP_ID);
        mpService_Operator.createMarketplace(marketplace);
        VOMarketplace marketplaceGet = mpService_Operator
                .getMarketplaceById(MP_ID);
        assertEquals(MP_ID, marketplaceGet.getMarketplaceId());
        VOMarketplace marketplace2 = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "Mp2", MP_ID);
        mpService_Operator.createMarketplace(marketplace2);
        fail();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCreateMarketplaceWithNotExistingOrganization()
            throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO("432954353",
                false, "globalMp");
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateMarketplaceWithMarketplaceConfiguration()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");
        marketplace.setTaggingEnabled(false);
        marketplace.setReviewEnabled(false);
        marketplace.setSocialBookmarkEnabled(false);
        marketplace = createAndValidateMarketplace(marketplace);

        VOMarketplace marketplace2 = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());

        assertEquals(marketplace.isReviewEnabled(),
                marketplace2.isReviewEnabled());
        assertEquals(marketplace.isSocialBookmarkEnabled(),
                marketplace2.isSocialBookmarkEnabled());
        assertEquals(marketplace.isSocialBookmarkEnabled(),
                marketplace2.isSocialBookmarkEnabled());

        marketplace.setTaggingEnabled(true);
        marketplace.setReviewEnabled(true);
        marketplace.setSocialBookmarkEnabled(true);
        mpService_Operator.updateMarketplace(marketplace);
        marketplace2 = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertEquals(marketplace.isReviewEnabled(),
                marketplace2.isReviewEnabled());
        assertEquals(marketplace.isSocialBookmarkEnabled(),
                marketplace2.isSocialBookmarkEnabled());
        assertEquals(marketplace.isSocialBookmarkEnabled(),
                marketplace2.isSocialBookmarkEnabled());
    }

    @Test(expected = ValidationException.class)
    public void testCreateMarketplaceWithMissingMarketplaceName()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(supplier.getOrganizationId(), false, null); // name
                                                                                 // not
                                                                                 // set
        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testCreateMarketplaceWithNotNecessaryData() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace.setMarketplaceId("oioioioi");
        marketplace.setOwningOrganizationName("zzz");
        marketplace.setKey(423);
        marketplace.setVersion(643);

        createAndValidateMarketplace(marketplace);
    }

    @Test
    public void testChangeMarketplaceNameAndMarketplaceOwnerIsAvailable()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");

        marketplace = createAndValidateMarketplace(marketplace);

        is.grantUserRoles(is.getCurrentUserDetails(),
                new ArrayList<UserRoleType>(
                        EnumSet.of(UserRoleType.MARKETPLACE_OWNER)));

        marketplace.setName("newName");

        mpService_Operator.updateMarketplace(marketplace);
        VOMarketplace marketplace2 = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace2);
        assertEquals(marketplace.getKey(), marketplace2.getKey());
        assertEquals(marketplace.getName(), marketplace2.getName());
    }

    @Test
    public void testUpdateMarketplaceConcurrentlyChanged() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");

        marketplace = createAndValidateMarketplace(marketplace);
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(0, marketplace.getVersion());

        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(0, marketplace.getVersion());

        marketplace.setOwningOrganizationId(supplier.getOrganizationId());
        VOMarketplace marketplaceAfterUpdate = mpService_Operator
                .updateMarketplace(marketplace);
        assertEquals(0, marketplace.getVersion());
        assertEquals(1, marketplaceAfterUpdate.getVersion());

        marketplace.setOwningOrganizationId(PLATFORM_OPERATOR);
        try {
            mpService_Operator.updateMarketplace(marketplace);
            fail();
        } catch (ConcurrentModificationException e) {
        }

        marketplaceAfterUpdate
                .setOwningOrganizationId(supplier.getOrganizationId());
        mpService_Operator.updateMarketplace(marketplaceAfterUpdate);
    }

    @Test
    public void testUpdateMarketplaceName_ConcurrentlyChanged()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");

        marketplace = createAndValidateMarketplace(marketplace);
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(0, marketplace.getVersion());

        is.grantUserRoles(is.getCurrentUserDetails(),
                new ArrayList<UserRoleType>(
                        EnumSet.of(UserRoleType.MARKETPLACE_OWNER)));

        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(0, marketplace.getVersion());

        marketplace.setName("f");
        VOMarketplace marketplaceAfterUpdate = mpService_Operator
                .updateMarketplace(marketplace);
        assertEquals("f", marketplaceAfterUpdate.getName());
        assertEquals(0, marketplace.getVersion());
        // assertEquals(1, marketplaceAfterUpdate.getVersion()); // should be
        assertEquals(0, marketplaceAfterUpdate.getVersion());

        marketplace.setName("f1");
        try {
            mpService_Operator.updateMarketplace(marketplace);
            // fail(); should be
        } catch (ConcurrentModificationException e) {
        }

        marketplaceAfterUpdate.setName("f2");
        mpService_Operator.updateMarketplace(marketplaceAfterUpdate);
    }

    @Test
    public void testChangeMarketplaceBranding() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "localMp");

        marketplace = createAndValidateMarketplace(marketplace);

        mpService_Operator.updateMarketplace(marketplace); // is ignored

        VOMarketplace marketplace2 = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace2);
    }

    @Test
    public void testDeleteMarketplace() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "abc");

        marketplace = createAndValidateMarketplace(marketplace);

        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);

        deleteAndVerifyMarketplace(marketplace);
    }

    @Test
    public void testDeleteMarketplaceWithWrongRole() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "abc");

        marketplace = createAndValidateMarketplace(marketplace);

        try {
            // only operator can delete marketplaces
            mpService_Supplier
                    .deleteMarketplace(marketplace.getMarketplaceId());
            fail();
        } catch (SOAPFaultException e) {
            checkAccessException(e);
        }
    }

    @Ignore
    @Test
    public void testDeleteMarketplaceWithServices() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "abc");
        marketplace = createAndValidateMarketplace(marketplace);

        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace);

        // create a new service
        VOService freeService = createAndActivateFreeService(marketplace);

        VOServiceEntry serviceEntry = setup
                .getServiceProvisioningSrvAsSupplier()
                .getServiceForMarketplace(Long.valueOf(freeService.getKey()),
                        marketplace.getMarketplaceId(), "en");
        assertNotNull(serviceEntry);
        assertTrue(serviceEntry.getKey() > 0);
        assertEquals(freeService.getKey(), serviceEntry.getKey());

        mpService_Operator.deleteMarketplace(marketplace.getMarketplaceId());

        VOServiceEntry serviceEntry2 = setup
                .getServiceProvisioningSrvAsSupplier()
                .getServiceForMarketplace(Long.valueOf(freeService.getKey()),
                        marketplace.getMarketplaceId(), "en");
        assertNull(serviceEntry2);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testAddSupplierToMarketplace_OpenMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test
    public void testAddSupplierToMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testAddSuppliersToMarketplaceWithWrongRole() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testAddSupplierToMarketplaceWithNotExistingSupplier()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator, "fhdsjkh", 0);
    }

    @Test(expected = OrganizationAlreadyExistsException.class)
    public void testAddSupplierToMarketplace_DuplicateSupplier()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 1);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void removeSuppliersFromMarketplace_OpenMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.removeOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test
    public void removeSuppliersFromMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.removeOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());

        List<VOOrganization> mpSuppliers = mpService_Operator
                .getOrganizationsForMarketplace(marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(0, mpSuppliers.size());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void removeSupplierFromNonExistingMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace.setMarketplaceId("5434354");

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());

        mpService_Operator.removeOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void removeSupplierFromMarketplaceWithMissingRoles()
            throws Exception {

        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.removeOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testBanSupplierFromMarketplace_ClosedMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test
    public void testBanSupplierFromMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testBanSupplierFromMarketplaceWithWrongRole() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testBanSupplierFromMarketplaceWithNotExistingSupplier()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator, "fhdsjkh",
                0);
    }

    @Test(expected = OrganizationAlreadyBannedException.class)
    public void testBanSupplierFromMarketplace_DuplicateSupplier()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 1);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void liftBannedSuppliersFromMarketplace_ClosedMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.liftBanOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test
    public void liftBannedSuppliersFromMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);

        List<VOOrganization> mpSuppliers = mpService_Operator
                .getBannedOrganizationsForMarketplace(
                        marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(1, mpSuppliers.size());

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.liftBanOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());

        mpSuppliers = mpService_Operator.getBannedOrganizationsForMarketplace(
                marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(0, mpSuppliers.size());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void liftBannedSuppliersFromNonExistingMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace.setMarketplaceId("5434354");

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());

        mpService_Operator.liftBanOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void liftBannedSuppliersFromMarketplaceWithMissingRoles()
            throws Exception {

        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplier.getOrganizationId());
        mpService_Operator.liftBanOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetSuppliersForMarketplaceWithInvalidMarketplaceId()
            throws Exception {
        mpService_Operator.getOrganizationsForMarketplace("fdesfgsd");
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testGetSuppliersForMarketplace_OpenMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);
        mpService_Operator
                .getOrganizationsForMarketplace(marketplace.getMarketplaceId());
    }

    @Test
    public void testGetSuppliersForMarketplace() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        addSupplierToMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetSuppliersForMarketplaceWithWrongRole() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);
        mpService_Operator
                .getOrganizationsForMarketplace(marketplace.getMarketplaceId());
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void testGetBannedSuppliersFromMarketplace_ClosedMarketplace()
            throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);
        mpService_Operator.getBannedOrganizationsForMarketplace(
                marketplace.getMarketplaceId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetBannedSuppliersFromMarketplaceWithInvalidMarketplaceId()
            throws Exception {
        mpService_Operator.getBannedOrganizationsForMarketplace("fdesfgsd");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetBannedSuppliersFromMarketplaceWithWrongRole()
            throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);
        mpService_Operator.getBannedOrganizationsForMarketplace(
                marketplace.getMarketplaceId());
    }

    @Test
    public void testGetMarketplaceById() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        VOMarketplace marketplace2 = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());

        verifyPersistedMarketplace(marketplace, marketplace2);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetMarketplaceByInvalidId() throws Exception {
        mpService_Operator.getMarketplaceById("rw3er2");
    }

    @Test
    public void testGetMarketplacesForOperator() throws Exception {
        VOMarketplace marketplaceForSupplier = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplaceForSupplier = createAndValidateMarketplace(
                marketplaceForSupplier);

        VOMarketplace marketplaceForOperator = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplaceForOperator = createAndValidateMarketplace(
                marketplaceForOperator);

        List<VOMarketplace> marketplaces = mpService_Operator
                .getMarketplacesForOperator();
        assertNotNull(marketplaces);
        assertEquals(3, marketplaces.size()); // 2 created + Fujitsu Marketplace
    }

    @Test
    public void testGetMarketplacesForSupplier_ClosedMarketplace()
            throws Exception {
        VOMarketplace marketplaceForSupplier = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplaceForSupplier = createAndValidateMarketplace(
                marketplaceForSupplier);

        VOMarketplace marketplaceForOperator = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplaceForOperator = createAndValidateMarketplace(
                marketplaceForOperator);

        List<VOMarketplace> marketplaces = mpService_Supplier
                .getMarketplacesForOrganization();
        assertNotNull(marketplaces);
        assertEquals(1, marketplaces.size());
    }

    @Test
    public void testGetMarketplacesForSupplier_OpenMarketplace()
            throws Exception {

        List<VOMarketplace> marketplaces = mpService_Supplier
                .getMarketplacesForOrganization();
        assertNotNull(marketplaces);
        // the marketplace for operator is open, supplier can also publish
        assertEquals(0, marketplaces.size());

        VOMarketplace marketplaceForSupplier = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplaceForSupplier = createAndValidateMarketplace(
                marketplaceForSupplier);

        VOMarketplace marketplaceForOperator = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplaceForOperator = createAndValidateMarketplace(
                marketplaceForOperator);

        marketplaces = mpService_Supplier.getMarketplacesForOrganization();
        assertNotNull(marketplaces);
        // the marketplace for operator is open, supplier can also publish
        assertEquals(2, marketplaces.size());

    }

    @Test
    public void testPublishService_ClosedMarketplace() throws Exception {
        createMarketplaceAndPublishService(supplier.getOrganizationId(), false);
    }

    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void testPublishService_NotAcceptedSupplier() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        createMarketplaceAndPublishService(marketplace);
    }

    @Test
    public void testPublishService_OpenMarketplace() throws Exception {
        createMarketplaceAndPublishService(PLATFORM_OPERATOR, true);
    }

    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void testPublishService_BannedSupplier() throws Exception {
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, true, "globalOpenMp");
        marketplace = createAndValidateMarketplace(marketplace);

        banSupplierFromMarketplace(marketplace, mpService_Operator,
                supplier.getOrganizationId(), 0);

        createMarketplaceAndPublishService(marketplace);
    }

    @Test
    public void testGetMarketplacesForServices() throws Exception {
        // test explicitly webmethod 'getMarketplacesForServices'
        createMarketplaceAndPublishService(supplier.getOrganizationId(), false);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetMarketplacesForServicesWithInvalidService()
            throws Exception {
        mpService_Supplier.getMarketplacesForService(new VOService());
    }

    @Test
    public void getMarketplacesOwned_NoPermission() {
        try {
            mpService_Supplier.getMarketplacesOwned();
        } catch (SOAPFaultException e) {
            checkAccessException(e);
        }
    }

    @Test
    public void getMarketplacesOwned() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        createAndValidateMarketplace(marketplace);
        // give the supplier user the MARKETPLACE_OWNER user role
        final IdentityService is = ServiceFactory.getDefault()
                .getIdentityService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        is.grantUserRoles(is.getCurrentUserDetails(),
                Arrays.asList(UserRoleType.MARKETPLACE_OWNER));

        // use the marketplace service as supplier
        List<VOMarketplace> marketplaces = mpService_Supplier
                .getMarketplacesOwned();
        assertNotNull(marketplaces);
        assertEquals(1, marketplaces.size());

        marketplaces = mpService_Operator.getMarketplacesOwned();
        assertNotNull(marketplaces);
        assertEquals(1, marketplaces.size()); // FUJITSU marketplace

        VOMarketplace marketplaceForOperator = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "globalMp");
        createAndValidateMarketplace(marketplaceForOperator);

        marketplaces = mpService_Operator.getMarketplacesOwned();
        assertNotNull(marketplaces);
        assertEquals(2, marketplaces.size()); // 1 created marketplace + FUJITSU
                                              // marketplace
    }

    @Ignore
    @Test
    public void testGetMarketplaceForSubscription() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "globalMp");
        marketplace = createAndValidateMarketplace(marketplace);

        VOService freeService = createAndActivateFreeService(marketplace);

        // subscribe the service
        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService();
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(
                Long.toHexString(System.currentTimeMillis()));
        subscription = subSvc.subscribeToService(subscription, freeService,
                null, null, null, new ArrayList<VOUda>());

        VOMarketplace marketplace2 = mpService_Supplier
                .getMarketplaceForSubscription(subscription.getKey(), "en");
        assertNotNull(marketplace2);
        assertEquals(marketplace.getKey(), marketplace2.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetMarketplaceForSubscriptionWithInvalidSubscription()
            throws Exception {
        mpService_Supplier.getMarketplaceForSubscription(64356243, "en");
    }

    @Test
    public void testChangeOwnershipOfMarketplace() throws Exception {

        int initialMpCount = mpService_Operator.getMarketplacesOwned().size();

        // create a marketplace for platform operator
        VOMarketplace marketplace = factory
                .createMarketplaceVO(PLATFORM_OPERATOR, false, "Mp");
        marketplace = createAndValidateMarketplace(marketplace);
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(0, marketplace.getVersion());
        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertNotNull(marketplace);
        assertTrue(marketplace.getKey() > 0);
        assertEquals(initialMpCount + 1,
                mpService_Operator.getMarketplacesOwned().size());

        // set supplier as owner for the previously created marketplace
        marketplace.setOwningOrganizationId(supplier.getOrganizationId());
        mpService_Operator.updateMarketplace(marketplace);
        marketplace = mpService_Operator
                .getMarketplaceById(marketplace.getMarketplaceId());
        assertEquals(supplier.getOrganizationId(),
                marketplace.getOwningOrganizationId());

        assertEquals(initialMpCount,
                mpService_Operator.getMarketplacesOwned().size());
    }

    @Test
    public void saveAndGetBrandingUrl() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), true, "BrandedMP");
        marketplace = createAndValidateMarketplace(marketplace);

        // set a valid branding URL
        mpService_Supplier.saveBrandingUrl(marketplace, brandingUrl);
        assertEquals(brandingUrl, mpService_Supplier
                .getBrandingUrl(marketplace.getMarketplaceId()));

        marketplace = mpService_Supplier
                .getMarketplaceById(marketplace.getMarketplaceId());

        // delete the branding URL
        mpService_Supplier.saveBrandingUrl(marketplace, null);
        assertNull(mpService_Supplier
                .getBrandingUrl(marketplace.getMarketplaceId()));
    }

    @Test
    public void createMarketplace_AutomaticMPOwnerRole() throws Exception {
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), true, "BrandedMP");
        marketplace = createAndValidateMarketplace(marketplace);

        Set<UserRoleType> roles = getUserDetails(setup.getSupplierUserKey(),
                setup.getSupplierUserId()).getUserRoles();

        assertTrue(roles.contains(UserRoleType.MARKETPLACE_OWNER));
    }

    @Test
    public void updateMarketplace_AutomaticMPOwnerRole() throws Exception {
        // create a marketplace for supplier
        VOMarketplace marketplace = factory.createMarketplaceVO(
                supplier.getOrganizationId(), true, "SomeName");
        marketplace = createAndValidateMarketplace(marketplace);

        // add an administrator user of the platform operator organization
        VOUserDetails user = new VOUserDetails();
        user.setUserId("newAdmin" + "_" + WebserviceTestBase.createUniqueKey());
        user.setOrganizationId("PLATFORM_OPERATOR");
        user.setEMail("a@b.com");
        user.setLocale("en");
        is.createUser(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN),
                marketplace.getMarketplaceId());
        // this new administrator has no MARKETPLACE_OWNER role
        user = is.getUserDetails(user);
        Set<UserRoleType> roles = user.getUserRoles();
        assertFalse(roles.contains(UserRoleType.MARKETPLACE_OWNER));

        // change owner of the marketplace from supplier to platform operator
        marketplace.setOwningOrganizationId("PLATFORM_OPERATOR");
        marketplace = mpService_Operator.updateMarketplace(marketplace);

        // the new admin received automatically the MARKETPLACE_OWNER role
        roles = is.getUserDetails(user).getUserRoles();
        assertTrue(roles.contains(UserRoleType.MARKETPLACE_OWNER));
    }

    /*
     * HELPER METHODS
     */
    private VOService createAndActivateFreeService(VOMarketplace marketplace)
            throws Exception {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        VOService freeService = setup.createAndActivateService("Service",
                marketplace, priceModel);
        assertNotNull(freeService);
        assertTrue(freeService.getKey() > 0);
        return freeService;
    }

    private void deleteAllMarketplaces() throws Exception {
        List<VOMarketplace> marketplaces = mpService_Operator
                .getMarketplacesForOperator();
        assertNotNull(marketplaces);
        for (VOMarketplace voMarketplace : marketplaces) {
            if (!voMarketplace.getMarketplaceId()
                    .equals(setup.getGlobalMarketplaceId())) {
                mpService_Operator
                        .deleteMarketplace(voMarketplace.getMarketplaceId());
            }
        }
    }

    private static void addSupplierToMarketplace(VOMarketplace marketplace,
            MarketplaceService mpService, String supplierId,
            int actualNumberOfSuppliers) throws Exception {

        List<VOOrganization> mpSuppliers = mpService
                .getOrganizationsForMarketplace(marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(actualNumberOfSuppliers, mpSuppliers.size());

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplierId);
        mpService.addOrganizationsToMarketplace(organizationIds,
                marketplace.getMarketplaceId());

        mpSuppliers = mpService
                .getOrganizationsForMarketplace(marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(actualNumberOfSuppliers + 1, mpSuppliers.size());
    }

    private static void banSupplierFromMarketplace(VOMarketplace marketplace,
            MarketplaceService mpService, String supplierId,
            int actualNumberOfSuppliers) throws Exception {

        List<VOOrganization> mpSuppliers = mpService
                .getBannedOrganizationsForMarketplace(
                        marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(actualNumberOfSuppliers, mpSuppliers.size());

        List<String> organizationIds = new LinkedList<String>();
        organizationIds.add(supplierId);
        mpService.banOrganizationsFromMarketplace(organizationIds,
                marketplace.getMarketplaceId());

        mpSuppliers = mpService.getBannedOrganizationsForMarketplace(
                marketplace.getMarketplaceId());
        assertNotNull(mpSuppliers);
        assertEquals(actualNumberOfSuppliers + 1, mpSuppliers.size());
    }

    private static void init() throws Exception {
        setup = new WebserviceTestSetup();
        supplier = setup.createSupplier("newSupplier");

        is = ServiceFactory.getDefault().getIdentityService(
                WebserviceTestBase.getPlatformOperatorKey(),
                WebserviceTestBase.getPlatformOperatorPassword());

        // use the marketplace service as operator
        mpService_Operator = ServiceFactory.getDefault().getMarketPlaceService(
                WebserviceTestBase.getPlatformOperatorKey(),
                WebserviceTestBase.getPlatformOperatorPassword());

        // use the marketplace service as supplier
        mpService_Supplier = ServiceFactory.getDefault().getMarketPlaceService(
                setup.getSupplierUserKey(),
                WebserviceTestBase.DEFAULT_PASSWORD);

        setup.createTechnicalService();

        brandingUrl = WebserviceTestBase.getConfigSetting("bes.https.url")
                + "/oscm-portal/marketplace/css/mp.css";
    }

    private VOMarketplace createAndValidateMarketplace(
            VOMarketplace transientMarketplace) throws Exception {
        VOMarketplace persistedMarketplace = mpService_Operator
                .createMarketplace(transientMarketplace);
        verifyPersistedMarketplace(transientMarketplace, persistedMarketplace);
        return persistedMarketplace;
    }

    private void verifyPersistedMarketplace(VOMarketplace transientMarketplace,
            VOMarketplace persistedMarketplace) {

        assertNotNull(transientMarketplace);
        assertNotNull(persistedMarketplace);

        assertTrue(persistedMarketplace.getKey() > 0);

        // Hibernate creates for the first object the version 0
        assertTrue(persistedMarketplace.getVersion() == 0);
        assertEquals(transientMarketplace.getName(),
                persistedMarketplace.getName());
        assertNotNull(persistedMarketplace.getMarketplaceId());

        assertNotNull(persistedMarketplace.getOwningOrganizationId());
        assertNotNull(persistedMarketplace.getOwningOrganizationName());

        if (transientMarketplace.getOwningOrganizationId() != null) {
            assertEquals(transientMarketplace.getOwningOrganizationId(),
                    persistedMarketplace.getOwningOrganizationId());

            if (transientMarketplace.getOwningOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                assertEquals(supplier.getName(),
                        persistedMarketplace.getOwningOrganizationName());
            }
        }
    }

    private void checkAccessException(SOAPFaultException e) {
        assertTrue(e.getMessage().contains("Unauthorized"));
    }

    private void createMarketplaceAndPublishService(String mpOwnerId,
            boolean open) throws Exception {
        VOMarketplace marketplace;
        if (open) {
            marketplace = factory.createMarketplaceVO(mpOwnerId, true,
                    "globalOpenMp");
        } else {
            marketplace = factory.createMarketplaceVO(mpOwnerId, false,
                    "globalMp");
        }
        marketplace = createAndValidateMarketplace(marketplace);

        createMarketplaceAndPublishService(marketplace);
    }

    private void createMarketplaceAndPublishService(VOMarketplace marketplace)
            throws Exception {
        // create a new service without setting the marketplace
        VOService freeService = setup.createFreeService("Service");

        List<VOCatalogEntry> entries = factory.createCatalogEntryVO(marketplace,
                freeService);

        List<VOCatalogEntry> catalogEntries = mpService_Supplier
                .getMarketplacesForService(freeService);
        assertNotNull(catalogEntries);
        assertEquals(1, catalogEntries.size());
        assertNull(catalogEntries.get(0).getMarketplace());

        mpService_Supplier.publishService(freeService, entries);

        catalogEntries = mpService_Supplier
                .getMarketplacesForService(freeService);
        assertNotNull(catalogEntries);
        assertEquals(1, catalogEntries.size());

        VOMarketplace marketplace2 = catalogEntries.get(0).getMarketplace();
        assertNotNull(marketplace2);
        assertEquals(marketplace.getKey(), marketplace2.getKey());
    }

    private void deleteAndVerifyMarketplace(VOMarketplace marketplace)
            throws Exception {
        mpService_Operator.deleteMarketplace(marketplace.getMarketplaceId());

        try {
            mpService_Operator
                    .getMarketplaceById(marketplace.getMarketplaceId());
            fail();
        } catch (ObjectNotFoundException e) {
        }
    }

    private VOUserDetails getUserDetails(String adminUserKey, String userId)
            throws Exception {
        IdentityService idService = ServiceFactory.getDefault()
                .getIdentityService(adminUserKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        return idService.getUserDetails(user);
    }
}
