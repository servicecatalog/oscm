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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Test;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.test.data.Categories;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;

/**
 * Unit tests for the marketplace management. Only the "delete marketplace" and
 * "remove organization" methods will be tested in this class.
 * 
 * @author groch
 * 
 */
public class MarketplaceServiceBeanDeleteMarketplaceAndOrganizationIT extends
        MarketplaceServiceTestBase {

    /**
     * verify that marketplace is physically deleted from the database. also the
     * marketplace owner role has to be revoked from the owner if the owner does
     * not own further marketplaces
     * 
     * @throws Exception
     */
    @Test
    public void deleteMarketplace() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        supp1 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                return org;
            }
        });

        // create a marketplace
        final VOMarketplace marketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("MP1", "MP1", supp1));

        assertNotNull("Marketplace must not be null", marketplace);
        assertTrue("organization must have role MARKETPLACE_OWNER",
                hasSupp1MarketplaceOwnerRole());
        createLocalizedResourceEntries(marketplace.getKey());

        marketplaceService.deleteMarketplace(marketplace.getMarketplaceId());

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr, "MP1");
                return mp;
            }
        });
        assertNull("Marketplace must no exist", marketplaceFromDB);
        assertFalse("organization must NOT have role MARKETPLACE_OWNER",
                hasSupp1MarketplaceOwnerRole());

        List<VOLocalizedText> stageText = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() throws Exception {

                List<VOLocalizedText> texts = localizer.getLocalizedValues(
                        marketplace.getKey(),
                        LocalizedObjectTypes.MARKETPLACE_STAGE);

                return texts;
            }
        });
        List<VOLocalizedText> emptyList = new ArrayList<VOLocalizedText>();
        assertEquals("stage must be null", emptyList, stageText);

        List<VOLocalizedText> msgProp = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() throws Exception {

                List<VOLocalizedText> texts = localizer.getLocalizedValues(
                        marketplace.getKey(),
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);

                return texts;
            }
        });

        assertEquals("message properties must be null", emptyList, msgProp);
    }

    @Test
    public void deleteMarketplace_deleteCategory() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        createCategoriesForMarketplace();
        List<VOCategory> categoryListBefore = categorizationService
                .getCategories(mpGlobal1.getMarketplaceId(), "en");
        assertEquals(1, categoryListBefore.size());
        assertEquals("Pharma123", categoryListBefore.get(0).getCategoryId());
        marketplaceService.deleteMarketplace(mpGlobal1.getMarketplaceId());
        List<VOCategory> categoryListAfter = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        assertEquals(0, categoryListAfter.size());
    }

    private void createCategoriesForMarketplace() throws Exception {
        mpGlobal1 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner,
                        GLOBAL_MP_ID + 1, false, mgr);
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Category cat = Categories.create(mgr, "Pharma123", mpGlobal1);
                LocalizedResource loc = new LocalizedResource();
                loc.setLocale("en");
                loc.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                loc.setValue("english Pharma");
                loc.setObjectKey(cat.getKey());
                mgr.persist(loc);
                return null;
            }
        });
    }

    @Test
    public void deleteMarketplace_LocalMarketplaceOrgReference()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        final Organization newSupplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
            }
        });

        // create a marketplace
        final VOMarketplace marketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("MP1", "MP1",
                        newSupplier));
        assertNotNull("Marketplace must not be null", marketplace);

        // get actual organization from database
        Organization dbSupplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        newSupplier.getKey());
                return org;
            }
        });
        assertNotNull(dbSupplier);
        assertEquals(newSupplier.getKey(), dbSupplier.getKey());

        // delete marketplace, reference from organization to local marketplace
        // should be removed
        marketplaceService.deleteMarketplace(marketplace.getMarketplaceId());

        // get actual organization from database after deletion
        dbSupplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        newSupplier.getKey());
                return org;
            }
        });
        assertNotNull(dbSupplier);
        assertEquals(newSupplier.getKey(), dbSupplier.getKey());

        // create a marketplace
        final VOMarketplace newMarketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("MP1", "MP1",
                        newSupplier));
        assertNotNull("Marketplace must not be null", newMarketplace);
    }

    @Test
    public void deleteMarketplace_WithReferenceFromSubscription()
            throws Exception {

        createSubscription(LOCAL_MP_ID_SUPP1);
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        final long marketplaceKey = marketplaceService.getMarketplaceById(
                LOCAL_MP_ID_SUPP1).getKey();
        final Marketplace marketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return mgr.getReference(Marketplace.class, marketplaceKey);
            }
        });
        marketplaceService.deleteMarketplace(marketplace.getMarketplaceId());

        // verify the result
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    mgr.getReference(Marketplace.class, marketplaceKey);
                    fail();
                } catch (ObjectNotFoundException ex) {
                    // just check the object not exist (deleted)
                }
                List<DomainHistoryObject<?>> histories = mgr
                        .findHistory(marketplace);
                assertEquals(ModificationType.DELETE,
                        histories.get(histories.size() - 1).getModtype());
                return null;
            }
        });
    }

    /**
     * throw ObjectNotFoundException when trying to delete a non-existing
     * marketplace
     */
    @Test(expected = ObjectNotFoundException.class)
    public void deleteMarketplace_NonExistingMP()
            throws ObjectNotFoundException {

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplaceService.deleteMarketplace("BOGUS");
    }

    /**
     * delete is not allowed if active services on that marketplace exist. In
     * that case ServicesStillPublishedException is thrown
     * 
     * @throws Exception
     */
    @Test
    public void deleteMarketplace_DontDeleteMarketplaceWithActiveServices()
            throws Exception {
        // get local marketplace of supp1; this should have subscription from
        // setup
        VOMarketplace voMarketplace = runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr,
                        LOCAL_MP_ID_SUPP1);
                VOMarketplace m = MarketplaceAssembler.toVOMarketplace(mp,
                        localizerFacade);
                return m;
            }
        });
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertEquals(LOCAL_MP_ID_SUPP1, ce.getMarketplace()
                        .getMarketplaceId());
                assertEquals(ServiceStatus.ACTIVE, ce.getProduct().getStatus());
                return null;
            }
        });

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplaceService.deleteMarketplace(voMarketplace.getMarketplaceId());

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertNull(ce.getMarketplace());
                assertEquals(ServiceStatus.INACTIVE, ce.getProduct()
                        .getStatus());
                return null;
            }
        });

    }

    /**
     * test when a marketplace without active services is deleted all references
     * catalog-entries are set to null-marketplace
     * 
     * @throws Exception
     */
    @Test
    public void deleteMarketplace_RemoveCatalogEntryReferencesOnMarketplaceDelete()
            throws Exception {
        // prepare: set published products on marketplace LOCAL_MP_ID_SUPP1 to
        // inactive
        Marketplace mpToBeDeleted = setPublishedServicesToInactive();
        assertNotNull(mpToBeDeleted);

        final long mpKey = mpToBeDeleted.getKey();
        // save onwingOrganization for later comparision
        Organization owningOrganization = mpToBeDeleted.getOrganization();
        owningOrganizationId = owningOrganization.getOrganizationId();

        // delete marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplaceService.deleteMarketplace(LOCAL_MP_ID_SUPP1);

        // check that marketplace does not exist in db
        Marketplace mpDeleted = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr,
                        "LOCAL_MP_ID_SUPP1");
                return mp;
            }
        });

        assertNull(
                "marketplace LOCAL_MP_ID_SUPP1 should have been deleted but still exists ",
                mpDeleted);

        // verify that local marketplace id in owning organization has been
        // removed
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization template = new Organization();
                template.setOrganizationId(owningOrganizationId);
                Organization org = (Organization) mgr.find(template);
                return org;
            }
        });

        // check that no catalogEntries reference the deleted marketplace
        runTX(new Callable<Void>() {
            @Override
            @SuppressWarnings("boxing")
            public Void call() throws Exception {
                Query query = mgr
                        .createQuery("SELECT count(*) FROM CatalogEntry ce WHERE ce.marketplace.key= :marketplaceKey");
                query.setParameter("marketplaceKey", mpKey);
                Object result = query.getSingleResult();
                assertEquals(result, new Long(0));
                return null;
            }
        });

    }

    @Test
    public void deleteMarketplace_NoDeleteWhenCustomerSpecificServicesExist()
            throws Exception {
        // make product (published on local mp a customer specific product
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = (Product) mgr.find(p1_1);
                product.setTargetCustomer(supplier4);
                mgr.persist(product);
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertEquals(LOCAL_MP_ID_SUPP1, ce.getMarketplace()
                        .getMarketplaceId());
                assertEquals(ServiceStatus.ACTIVE, ce.getProduct().getStatus());
                return null;
            }
        });

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplaceService.deleteMarketplace(LOCAL_MP_ID_SUPP1);
        // service should have been deactivated and detached from the MP
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertNull(ce.getMarketplace());
                assertEquals(ServiceStatus.INACTIVE, ce.getProduct()
                        .getStatus());
                return null;
            }
        });

    }

    @Test
    public void removeOrganizationsFromMarketplace_ActiveServicesExist()
            throws Exception {
        grantSupplier1AccessToGlobalMp();
        container.login(mpOwnerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_MARKETPLACE_OWNER);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertEquals(mpGlobal.getMarketplaceId(), ce.getMarketplace()
                        .getMarketplaceId());
                assertEquals(ServiceStatus.ACTIVE, ce.getProduct().getStatus());
                return null;
            }
        });
        // supp1 has active services => they should be deactivated and detached
        // from the marketplace
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                mpGlobal.getMarketplaceId());
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                CatalogEntry ce = mgr.getReference(CatalogEntry.class,
                        ce_p1_1.getKey());
                assertNull(ce.getMarketplace());
                assertEquals(ServiceStatus.INACTIVE, ce.getProduct()
                        .getStatus());
                return null;
            }
        });
    }

    @Test(expected = ObjectNotFoundException.class)
    public void removeOrganizationsFromMarketplace_NonExistingSupplier()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        try {
            marketplaceService.removeOrganizationsFromMarketplace(
                    Collections.singletonList("non_existent_supplier"),
                    GLOBAL_MP_ID);
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.ORGANIZATION, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void removeOrganizationsFromMarketplace_NonExistingMarketplace()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        try {
            marketplaceService.removeOrganizationsFromMarketplace(
                    Collections.singletonList(supp1.getOrganizationId()),
                    "non_existent_marketplace");
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.MARKETPLACE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void removeOrganizationsFromMarketplace_NonSupplier()
            throws Exception {
        Organization customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr);
            }
        });
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(customer.getOrganizationId()),
                GLOBAL_MP_ID);
    }

    @Test
    public void removeOrganizationsFromMarketplace_removeBroker()
            throws Exception {
        // given a reseller and an existing relation to a closed marketplace
        // with granted publishing access
        broker = givenBroker();
        createRelationWithPublishingAccess(mpClosed, broker);
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        // when removing a broker from a closed marketplace
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                CLOSED_MP_ID);

        // verify that the relation has been removed.
        MarketplaceToOrganization mto = findMarketplaceToOrganization(mpClosed,
                broker);
        assertNull("Reference object must be removed", mto);
    }

    @Test
    public void removeOrganizationsFromMarketplace_removeReseller()
            throws Exception {
        // given a reseller and an existing relation to a closed marketplace
        // with granted publishing access
        reseller = givenReseller();
        createRelationWithPublishingAccess(mpClosed, reseller);
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        // when removing a reseller from a closed marketplace
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                CLOSED_MP_ID);

        // verify that relation has been removed.
        MarketplaceToOrganization mto = findMarketplaceToOrganization(mpClosed,
                reseller);
        assertNull("Reference object must be removed", mto);
    }

    @Test
    public void removeOrganizationsFromMarketplace_SupplierHasNeverBeenGrantedPublishingRights()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                GLOBAL_MP_ID);
        // no ref exists, however this should be ignored
    }

    @Test(expected = OperationNotPermittedException.class)
    public void removeOrganizationsFromMarketplace_WrongMarketplace()
            throws Exception {
        container.login(supplier1Key, ROLE_MARKETPLACE_OWNER);
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp2.getOrganizationId()),
                LOCAL_MP_ID_SUPP1);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void removeOrganizationsFromMarketplace_OpenMP() throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                OPEN_MP_ID);
    }

    /*
     * Use case: supp1 has previously been added as accepted supplier to MP,
     * thus a whitelist ref exists => now removing supp1 from accepted list of
     * should delete this ref
     */
    @Test
    public void removeOrganizationsFromMarketplace_goodCase() throws Throwable {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        MarketplaceToOrganization ref = runTX(new Callable<MarketplaceToOrganization>() {

            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();

                return mto;
            }
        });

        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                CLOSED_MP_ID);

        ref = runTX(new Callable<MarketplaceToOrganization>() {
            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1);
                return (MarketplaceToOrganization) mgr.find(mto);
            }
        });
        assertNull("Reference object must be removed", ref);
    }

    /*
     * Use case: supp1 was banned on previously open MP, blacklist ref remains
     * after switching MP to closed => now removing supp1 from accepted list of
     * MP should not be possible, but blacklist ref should remain
     */
    @Test
    public void removeOrganizationsFromMarketplace_SupplierHadBeenBannedOnOpenMpBefore()
            throws Throwable {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        MarketplaceToOrganization ref = runTX(new Callable<MarketplaceToOrganization>() {

            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();

                return mto;
            }
        });

        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                CLOSED_MP_ID);

        ref = runTX(new Callable<MarketplaceToOrganization>() {
            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1);
                return (MarketplaceToOrganization) mgr.find(mto);
            }
        });
        assertNotNull("Reference object expected", ref);
        assertEquals("Blacklist ref should still be there",
                PublishingAccess.PUBLISHING_ACCESS_DENIED,
                ref.getPublishingAccess());
        assertEquals("Not the same marketplace - ", mpClosed.getKey(),
                ref.getMarketplace_tkey());
        assertEquals("Not the same supplier - ", supp1.getKey(),
                ref.getOrganization_tkey());
    }

    @Test
    public void removeOrganizationsFromMarketplace_SupplierNotAssigned()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        List<VOOrganization> list = marketplaceService
                .getOrganizationsForMarketplace(GLOBAL_MP_ID);
        assertNotNull("List of suppliers expected - ", list);
        assertEquals("No supplier expected", 0, list.size());

        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp2.getOrganizationId()),
                GLOBAL_MP_ID);
        // expecting that nothing happens

        list = marketplaceService.getOrganizationsForMarketplace(GLOBAL_MP_ID);
        assertNotNull("List of suppliers expected - ", list);
        assertEquals("No supplier expected", 0, list.size());
    }

    /**
     * delete is only allowed for Role PLATFORM_OPERATOR if the user doesn't
     * have this role EJBAccessException is thrown
     * 
     * @throws Exception
     */
    @Test(expected = EJBAccessException.class)
    public void deleteMarketplace_RequiresPlatformOperatorRole()
            throws Exception {

        container.login(this.supplier3Key, ROLE_ORGANIZATION_ADMIN);

        try {
            marketplaceService.deleteMarketplace("");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

}
