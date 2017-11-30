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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Ignore;
import org.junit.Test;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAlreadyExistsException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Unit tests for the marketplace management. Only the "create/add/update"
 * method will be tested in this class.
 * 
 * @author groch
 * 
 */
@Ignore
public class MarketplaceServiceBeanIT extends MarketplaceServiceTestBase {

    @Test
    public void updateMarketplace_NameAndOrg() throws Exception {
        container.login(mpOwnerUserKey, ROLE_PLATFORM_OPERATOR,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        VOMarketplace marketplace = marketplaceService.createMarketplace(
                this.buildMarketplace("ORIGINAL_NAME", "MPL", mpOwner));

        marketplace.setName("NEW_NAME");
        marketplace.setOwningOrganizationId(supp1.getOrganizationId());

        VOMarketplace updatedMp = marketplaceService
                .updateMarketplace(marketplace);
        assertNotNull("updatedMarketplace must not be null", updatedMp);
        assertEquals("unchanged organizationId", supp1.getOrganizationId(),
                updatedMp.getOwningOrganizationId());
        assertNotNull("new name must not be null", updatedMp.getName());
        assertEquals("renamed marketplace", "NEW_NAME", updatedMp.getName());
    }

    /**
     * test creation of a global marketplace for a supplier verify that this
     * supplier gets granted the organization Role MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void createMarketplace_checkSupplierRole() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mplOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });

        VOMarketplace marketplace = buildMarketplace("GLOBAL_CREATED",
                "GLOBAL_NO_OWNER", mplOwner);
        marketplace.setTaggingEnabled(false);
        marketplace.setReviewEnabled(false);
        marketplace.setSocialBookmarkEnabled(true);

        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);

        assertFalse(createdMpl.isTaggingEnabled());
        assertFalse(createdMpl.isReviewEnabled());
        assertTrue(createdMpl.isSocialBookmarkEnabled());

        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that organization has default value
        assertNotNull("Marketplace owner not defined",
                createdMpl.getOwningOrganizationId());

        String ownerOrganizationId = createdMpl.getOwningOrganizationId();
        assertEquals("default marketplace owner ", ownerOrganizationId,
                mplOwner.getOrganizationId());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        mplOwner.getOrganizationId());
                Marketplace mp = mgr.getReference(Marketplace.class,
                        createdMpl.getKey());

                assertFalse(mp.isTaggingEnabled());
                assertFalse(mp.isReviewEnabled());
                assertTrue(mp.isSocialBookmarkEnabled());

                MarketplaceToOrganization ref = new MarketplaceToOrganization(
                        mp, org);
                mgr.getReferenceByBusinessKey(ref);
                assertTrue(
                        "organization has not been given role MARKETPLACE_OWNER",
                        org.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));

                return null;
            }
        });
    }

    /**
     * Test creation of a global marketplace for a broker. Verify that this
     * broker gets granted the organization Role MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void createMarketplace_checkBrokerRole() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mplOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
                return null;
            }
        });

        VOMarketplace marketplace = buildMarketplace("GLOBAL_CREATED",
                "GLOBAL_NO_OWNER", mplOwner);
        marketplace.setTaggingEnabled(false);
        marketplace.setReviewEnabled(false);
        marketplace.setSocialBookmarkEnabled(true);

        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);

        assertFalse(createdMpl.isTaggingEnabled());
        assertFalse(createdMpl.isReviewEnabled());
        assertTrue(createdMpl.isSocialBookmarkEnabled());

        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that organization has default value
        assertNotNull("Marketplace owner not defined",
                createdMpl.getOwningOrganizationId());

        String ownerOrganizationId = createdMpl.getOwningOrganizationId();
        assertEquals("default marketplace owner ", ownerOrganizationId,
                mplOwner.getOrganizationId());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        mplOwner.getOrganizationId());
                Marketplace mp = mgr.getReference(Marketplace.class,
                        createdMpl.getKey());

                assertFalse(mp.isTaggingEnabled());
                assertFalse(mp.isReviewEnabled());
                assertTrue(mp.isSocialBookmarkEnabled());

                MarketplaceToOrganization ref = new MarketplaceToOrganization(
                        mp, org);
                mgr.getReferenceByBusinessKey(ref);
                assertTrue(
                        "organization has not been given role MARKETPLACE_OWNER",
                        org.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));

                return null;
            }
        });
    }

    /**
     * test creation of a global marketplace for a reseller verify that this
     * reseller gets granted the organization Role MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void createMarketplace_checkResellerRole() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mplOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.RESELLER);
                return null;
            }
        });

        VOMarketplace marketplace = buildMarketplace("GLOBAL_CREATED",
                "GLOBAL_NO_OWNER", mplOwner);
        marketplace.setTaggingEnabled(false);
        marketplace.setReviewEnabled(false);
        marketplace.setSocialBookmarkEnabled(true);

        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);

        assertFalse(createdMpl.isTaggingEnabled());
        assertFalse(createdMpl.isReviewEnabled());
        assertTrue(createdMpl.isSocialBookmarkEnabled());

        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that organization has default value
        assertNotNull("Marketplace owner not defined",
                createdMpl.getOwningOrganizationId());

        String ownerOrganizationId = createdMpl.getOwningOrganizationId();
        assertEquals("default marketplace owner ", ownerOrganizationId,
                mplOwner.getOrganizationId());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        mplOwner.getOrganizationId());
                Marketplace mp = mgr.getReference(Marketplace.class,
                        createdMpl.getKey());

                assertFalse(mp.isTaggingEnabled());
                assertFalse(mp.isReviewEnabled());
                assertTrue(mp.isSocialBookmarkEnabled());

                MarketplaceToOrganization ref = new MarketplaceToOrganization(
                        mp, org);
                mgr.getReferenceByBusinessKey(ref);
                assertTrue(
                        "organization has not been given role MARKETPLACE_OWNER",
                        org.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));

                return null;
            }
        });
    }

    /**
     * Services displayed on landing page - use case 1:<br>
     * 
     * Test:<br>
     * create marketplace<br>
     * 
     * Check:<br>
     * A landing page must be created and the default values must be set
     * 
     * @throws Exception
     */
    @Test
    public void createMarketplace_checkDefaultLandingPage() throws Exception {
        // create marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketplace = buildMarketplace("TEST_MP_NAME",
                "TEST_MP_ID", null);
        final VOMarketplace createdMp = marketplaceService
                .createMarketplace(marketplace);
        assertNotNull(createdMp);
        container.logout();

        // verify default settings
        checkDefaultLandingPage(createdMp.getKey());
    }

    @Test
    public void createMarketplace_checkMail_supplierSpecific()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        VOMarketplace marketplace = buildMarketplace("LOCAL_MP_MAIL_TEST",
                "LOCAL_MP_MAIL_TEST", null);
        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);
        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that the email sent contains the correct links)
        assertTrue(publicAccessUrl.contains(
                org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
        assertNotNull(adminUrl);
    }

    @Test
    public void createMarketplace_checkMail_FujitsuBranded() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        VOMarketplace marketplace = buildMarketplace("GLOBAL_MP_MAIL_TEST",
                "GLOBAL_MP_MAIL_TEST", null);
        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);
        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that the email sent contains the correct links)
        assertTrue(publicAccessUrl.contains(
                org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
        assertNotNull(adminUrl);
    }

    /**
     * test when no owner is given it should be set to platform operator
     * organization
     * 
     */
    @Test
    public void createMarketplace_defaultOwner() throws Exception {

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        VOMarketplace marketplace = buildMarketplace("GLOBAL_CREATED",
                "GLOBAL_DEFAULT_OWNER", null);
        final VOMarketplace createdMpl = marketplaceService
                .createMarketplace(marketplace);
        // check that marketplace is created
        assertNotNull("created marketplace is null but was expected not null",
                createdMpl);
        // check that organization has default value
        assertNotNull("Marketplace owner not defined",
                createdMpl.getOwningOrganizationId());

        assertEquals("default marketplace owner ",
                this.platformOperatorOrg.getOrganizationId(),
                createdMpl.getOwningOrganizationId());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        platformOperatorOrg.getOrganizationId());
                Marketplace mp = mgr.getReference(Marketplace.class,
                        createdMpl.getKey());
                MarketplaceToOrganization ref = new MarketplaceToOrganization(
                        mp, org);
                try {
                    mgr.getReferenceByBusinessKey(ref);
                    fail("MarketplaceToOrganization created.");
                } catch (ObjectNotFoundException e) {
                    // expected because platform operator doesn't have the
                    // supplier role
                }
                assertTrue(
                        "organization has not been given role MARKETPLACE_OWNER",
                        org.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));

                return null;
            }
        });
    }

    /**
     * verify that marketplace without name is NOT created
     */
    @Test(expected = ValidationException.class)
    public void createMarketplace_missingProperties() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        // name must not be missing
        VOMarketplace mplWithoutName = buildMarketplace(null,
                "GLOBAL_WITHOUT_NAME", mpOwner);

        // marketplace owner must not be missing
        marketplaceService.createMarketplace(mplWithoutName);

    }

    /**
     * verify creation of marketplace is not allowed to MARKETPLACE_OWNER
     */
    @Test
    public void createMarketplace_notPermitted() throws Exception {

        VOMarketplace marketplace = buildMarketplace("UNALLOWED_CREATED",
                "UNALLOWED_CREATED", null);

        // operation not permitted to roles not equal PLATFORM_OPERATOR
        container.login(supplier1Key,
                OrganizationRoleType.MARKETPLACE_OWNER.name());

        try {
            marketplaceService.createMarketplace(marketplace);
        } catch (EJBException e) {
            assertTrue(e.getCause().getClass() == EJBAccessException.class);
        }

    }

    /**
     * verify that all admins users of the assigned marketplace were notified by
     * email
     * 
     * @throws Exception
     */
    @Test
    public void createMarketplace_EmailNotifiationsOnCreation()
            throws Exception {
        resetEmailNotificationResults();
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        marketplaceService.createMarketplace(this
                .buildMarketplace("GLOBAL_SUPP1", "GLOBAL_SUPP1", supplier4));
        verifyEmaiRecipients();
    }

    /**
     * verify ObjectNotFoundException when update on non existing Marketplace is
     * issued
     * 
     * @throws ValidationException
     * @throws ConcurrentModificationException
     */
    @Test(expected = ObjectNotFoundException.class)
    public void updateMarketplace_NotFound() throws Exception {
        VOMarketplace notExistingMarketplace = buildMarketplace(
                "GLOBAL NON EXISTING", "NOT EXISTING", mpOwner);
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplaceService.updateMarketplace(notExistingMarketplace);
    }

    /**
     * update only marketplaceName by owning organization owner organization
     * must be unchanged.
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_NameAndConfiguration() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketplace = marketplaceService.createMarketplace(
                this.buildMarketplace("ORIGINAL_NAME", "MPL", mpOwner));

        marketplace.setTaggingEnabled(false);
        marketplace.setReviewEnabled(false);
        marketplace.setSocialBookmarkEnabled(false);

        marketplace.setName("NEW_NAME");
        marketplace.setOwningOrganizationId(null);
        int firstVers = marketplace.getVersion();
        container.login(mpOwnerUserKey,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        VOMarketplace updatedMp = marketplaceService
                .updateMarketplace(marketplace);

        assertFalse(updatedMp.isTaggingEnabled());
        assertFalse(updatedMp.isReviewEnabled());
        assertFalse(updatedMp.isSocialBookmarkEnabled());
        assertEquals(firstVers + 1, updatedMp.getVersion());
        assertNotNull("updatedMarketplace must not be null", updatedMp);
        assertEquals("unchanged organizationId", mpOwner.getOrganizationId(),
                updatedMp.getOwningOrganizationId());
        assertNotNull("new name must not be null", updatedMp.getName());
        assertEquals("renamed marketplace", "NEW_NAME", updatedMp.getName());

    }

    /**
     * test that new owner is assigned
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_Org() throws Exception {
        // setup: create a global marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketplace4supplier4 = marketplaceService
                .createMarketplace(this.buildMarketplace("REASSIGN_TEST",
                        "REASSIGN_TEST", supplier4));

        voMarketplace = marketplace4supplier4;
        // modify owner--> supp1 becomes owner
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplace4supplier4
                .setOwningOrganizationId(supp1.getOrganizationId());
        marketplaceService.updateMarketplace(marketplace4supplier4);

        // verify marketplace has new ownership
        VOMarketplace updatedMarketplace = runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr,
                        voMarketplace.getMarketplaceId());

                // check that the reference is added to the new owner
                Organization org = mgr.getReference(Organization.class,
                        supplier4.getKey());
                MarketplaceToOrganization rel = new MarketplaceToOrganization(
                        mp, org);
                mgr.getReferenceByBusinessKey(rel);

                voMarketplace = MarketplaceAssembler.toVOMarketplace(mp,
                        localizerFacade);
                return voMarketplace;
            }
        });

        assertEquals("updated marketplace owner ", supp1.getOrganizationId(),
                updatedMarketplace.getOwningOrganizationId());
    }

    /**
     * Services displayed on landing page - use case 7:<br>
     * 
     * Test:<br>
     * create marketplace<br>
     * set "best ratings first" on landingpage<br>
     * marketplace disable ratings<br>
     * marketplace update<br>
     * 
     * Check:<br>
     * the fillin criterion on the landing page must be "most recent first"
     */
    @Test
    public void updateMarketplace_disableRatings() throws Exception {
        // create marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketplace = buildMarketplace("TEST_MP_NAME",
                "TEST_MP_ID", null);
        final VOMarketplace voMarketplace = marketplaceService
                .createMarketplace(marketplace);
        assertNotNull(voMarketplace);

        // set "best ratings first" on landingpage
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace marketplace = mgr.getReference(Marketplace.class,
                        voMarketplace.getKey());
                PublicLandingpage landingpage = marketplace
                        .getPublicLandingpage();
                landingpage
                        .setFillinCriterion(FillinCriterion.RATING_DESCENDING);
                mgr.persist(landingpage);
                return null;
            }
        });

        // disable ratings
        voMarketplace.setReviewEnabled(false);

        // update marketplace
        marketplaceService.updateMarketplace(voMarketplace);

        // verify settings
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace marketplace = mgr.getReference(Marketplace.class,
                        voMarketplace.getKey());
                PublicLandingpage landingpage = marketplace
                        .getPublicLandingpage();

                // verify default setting
                assertEquals(FillinCriterion.ACTIVATION_DESCENDING,
                        landingpage.getFillinCriterion());
                return null;
            }
        });
    }

    /**
     * Services displayed on landing pag<br>
     * 
     * Test:<br>
     * create marketplace<br>
     * witch to enterprise landingpage<br>
     * marketplace disable ratings<br>
     * marketplace update<br>
     * 
     * Check:<br>
     * the review is disabled. No Exception should be thrown
     * 
     * BUG 10919
     */
    @Test
    public void updateMarketplace_disableRatings_EnterpriseLandingpage()
            throws Exception {
        // given marketplace and enterprise landingpage selected
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        final VOMarketplace enterpriseMarketplace = givenMarketplaceWithEnterpriseLandingpage();

        // when
        enterpriseMarketplace.setReviewEnabled(false);
        marketplaceService.updateMarketplace(enterpriseMarketplace);

        // than
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace marketplace = mgr.getReference(Marketplace.class,
                        enterpriseMarketplace.getKey());

                // verify default setting
                assertFalse(marketplace.isReviewEnabled());

                return null;
            }
        });
    }

    /**
     * Test concurrent owner assignment leads to
     * ConcurrentModificationException.
     */
    @Test
    public void updateMarketplace_Concurrently() throws Exception {
        // setup: create a global marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketplace4supplier4 = marketplaceService
                .createMarketplace(this.buildMarketplace("CONCURRENT_TEST",
                        "CONCURRENT_TEST", supplier4));
        voMarketplace = (VOMarketplace) ReflectiveClone
                .clone(marketplace4supplier4);

        // modify owner--> creates new version
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        marketplace4supplier4
                .setOwningOrganizationId(supp1.getOrganizationId());
        marketplaceService.updateMarketplace(marketplace4supplier4);

        // Set older version and check if ConcurrentModificationException is
        // thrown!

        boolean expectedException = false;
        try {
            voMarketplace
                    .setOwningOrganizationId(supplier4.getOrganizationId());
            marketplaceService.updateMarketplace(voMarketplace);
        } catch (ConcurrentModificationException cm) {
            expectedException = true;
        }
        assertTrue(expectedException);
    }

    /**
     * verify that change of marketplace owner throws NO Exception
     * 
     */
    @Test
    public void updateMarketplace_OwnerChangeForLocalMarketplace()
            throws Exception {
        supp1 = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization newOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, newOrg, true, "admin");
                mpOwnerUserKey = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);

                return newOrg;
            }
        });
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        // setup: create a local marketplace for supp1
        VOMarketplace marketplace = this.buildMarketplace("LOCAL",
                "LOCAL_SUPP1", supp1);
        VOMarketplace localMarketPlace = marketplaceService
                .createMarketplace(marketplace);

        Organization suppX = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
            }
        });

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        localMarketPlace.setOwningOrganizationId(suppX.getOrganizationId());
        // this should NOT throw MarketplaceAlreadyExistsException: owner of a
        // local mp can be changed (as well as owner of global mp, see bug 8168)
        localMarketPlace = marketplaceService
                .updateMarketplace(localMarketPlace);
    }

    /**
     * verify that all admins of a changed assignee are notified by mail
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_EmailNotifiationsOnUpdate() throws Exception {
        resetEmailNotificationResults();

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        VOMarketplace defaultOwnedMarketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("GLOBAL_SUPP1",
                        "GLOBAL_SUPP1", null));

        // reset counter (one mail has already been sent by createMarketplace)
        resetEmailNotificationResults();

        defaultOwnedMarketplace
                .setOwningOrganizationId(supplier4.getOrganizationId());
        marketplaceService.updateMarketplace(defaultOwnedMarketplace);
        verifyEmaiRecipients();

    }

    @Test
    public void updateMarketplace_ChangeLocalMarketplaceOwnership_Bug8706()
            throws Exception {
        // setup: create a global marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        Organization oldSupplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
            }
        });

        Organization newSupplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
            }
        });

        final VOMarketplace marketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("changeOwnership",
                        "changeOwnership", oldSupplier));

        String oldOwnerId = marketplace.getOwningOrganizationId();
        assertEquals(oldSupplier.getOrganizationId(), oldOwnerId);

        // set a new owner for the marketplace
        String newOwnerId = newSupplier.getOrganizationId();
        marketplace.setOwningOrganizationId(newOwnerId);
        VOMarketplace marketplaceAfterUpdate = marketplaceService
                .updateMarketplace(marketplace);

        // new owner must have a reference to the marketplace
        assertEquals(marketplaceAfterUpdate.getOwningOrganizationId(),
                newOwnerId);

    }

    /**
     * test that OrganizationRole is NOT revoked if organization has remaining
     * marketplace(s) after delete
     */
    @Test
    public void createMarketplace_DoNotRevokeRoleIfMarketplacesRemain()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        // create a marketplace
        VOMarketplace marketplace = marketplaceService
                .createMarketplace(this.buildMarketplace("MP1", "MP1", supp1));
        // second marketplace for same supplier
        marketplaceService
                .createMarketplace(this.buildMarketplace("MP2", "MP2", supp1));
        assertTrue("organization must have role MARKETPLACE_OWNER",
                hasSupp1MarketplaceOwnerRole());

        marketplaceService.deleteMarketplace(marketplace.getMarketplaceId());

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr, "MP1");
                return mp;
            }
        });
        assertNull("Marketplace must no exist", marketplaceFromDB);
        assertTrue("organization must have role MARKETPLACE_OWNER",
                hasSupp1MarketplaceOwnerRole());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void addOrganizationsToMarketplace_NonExistingSupplier()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList("not_existent"), GLOBAL_MP_ID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void addOrganizationsToMarketplace_NonExistingMarketplace()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                "not_existent");
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void addOrganizationsToMarketplace_NonSupplier() throws Exception {
        Organization customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr);
            }
        });
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(customer.getOrganizationId()),
                GLOBAL_MP_ID);
    }

    @Test
    public void addOrganizationsToMarketplace_addBroker() throws Exception {
        // given a broker
        broker = givenBroker();
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);

        // when assigning a broker to a marketplace
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(broker.getOrganizationId()),
                GLOBAL_MP_ID);

        // verify that a relation between the broker and the marketplace
        // has been created.
        MarketplaceToOrganization mto = findMarketplaceToOrganization(mpGlobal,
                broker);
        assertNotNull("Relation object expected", mto);
    }

    @Test
    public void addOrganizationsToMarketplace_addReseller() throws Exception {
        // given a reseller.
        reseller = givenReseller();
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);

        // when assigning a reseller to a marketplace
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(reseller.getOrganizationId()),
                GLOBAL_MP_ID);

        // verify that a relation between the broker and the marketplace
        // has been created.
        MarketplaceToOrganization mto = findMarketplaceToOrganization(mpGlobal,
                reseller);
        assertNotNull("Relation object expected", mto);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void addOrganizationsToMarketplace_WrongMarketplace()
            throws Exception {
        container.login(supplier1Key, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp2.getOrganizationId()),
                LOCAL_MP_ID_SUPP1);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void addOrganizationsToMarketplace_OpenMP() throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                OPEN_MP_ID);
    }

    @Test
    public void addOrganizationsToMarketplace_AddOneSupplier()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpGlobal, supp1);
                mto = (MarketplaceToOrganization) mgr.find(mto);
                assertNull("No ref object expected", mto);
                return null;
            }
        });

        mailCounter = 0;
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                GLOBAL_MP_ID);

        assertEquals(EmailType.MARKETPLACE_SUPPLIER_ASSIGNED, emailType1);
        assertTrue(publicAccessUrl.contains(
                org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
        assertNull(adminUrl);
        assertEquals(1, mailCounter);

        MarketplaceToOrganization mto = runTX(
                new Callable<MarketplaceToOrganization>() {
                    @Override
                    public MarketplaceToOrganization call() throws Exception {
                        MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                mpGlobal, supp1);
                        return (MarketplaceToOrganization) mgr.find(mto);
                    }
                });
        assertNotNull("Relation object expected", mto);
        assertEquals("Not the same marketplace - ", mpGlobal.getKey(),
                mto.getMarketplace_tkey());
        assertEquals("Not the same supplier - ", supp1.getKey(),
                mto.getOrganization_tkey());
    }

    @Test
    public void addOrganizationsToMarketplace_supplierOwnsMarketplace()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        // given a supplier that owns the marketplace
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpGlobal, mpOwner);
                mto = (MarketplaceToOrganization) mgr.find(mto);
                assertNull("No ref object expected", mto);

                return null;
            }
        });

        // when assigning the supplier to the marketplace
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(mpOwner.getOrganizationId()),
                GLOBAL_MP_ID);

        // verify that the e-mail contains the admin url and that the other
        // e-mail params are correct.
        assertNotNull(adminUrl);
        assertEquals(EmailType.MARKETPLACE_SUPPLIER_ASSIGNED_OWNED, emailType1);
        assertTrue(publicAccessUrl.contains(
                org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
        assertEquals(1, mailCounter);
    }

    @Test
    public void addOrganizationsToMarketplace_supplierIsNotMPOwner()
            throws Exception {
        // given
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpGlobal, supp2);
                mto = (MarketplaceToOrganization) mgr.find(mto);
                return null;
            }
        });

        // when assigning the supplier to the marketplace
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp2.getOrganizationId()),
                GLOBAL_MP_ID);

        // verify that the e-mail does not contain the admin url and all other
        // e-mail params are correct.
        assertEquals(1, mailCounter);
        assertNull(adminUrl);
        assertEquals(EmailType.MARKETPLACE_SUPPLIER_ASSIGNED, emailType1);
        assertTrue(publicAccessUrl.contains(
                org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
    }

    @Test
    public void addOrganizationsToMarketplace_AddSuppliers() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        List<String> supplierIds = new ArrayList<String>();
        supplierIds.add(supp1.getOrganizationId());
        supplierIds.add(supp2.getOrganizationId());
        marketplaceService.addOrganizationsToMarketplace(supplierIds,
                GLOBAL_MP_ID);
        MarketplaceToOrganization mto1 = runTX(
                new Callable<MarketplaceToOrganization>() {

                    @Override
                    public MarketplaceToOrganization call() throws Exception {
                        MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                mpGlobal, supp1);
                        return (MarketplaceToOrganization) mgr.find(mto);
                    }
                });
        assertNotNull("Relation object expected", mto1);
        assertEquals("Not the same marketplace - ", mpGlobal.getKey(),
                mto1.getMarketplace_tkey());
        assertEquals("Not the same supplier - ", supp1.getKey(),
                mto1.getOrganization_tkey());
        MarketplaceToOrganization mto2 = runTX(
                new Callable<MarketplaceToOrganization>() {

                    @Override
                    public MarketplaceToOrganization call() throws Exception {
                        MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                mpGlobal, supp2);
                        return (MarketplaceToOrganization) mgr.find(mto);
                    }
                });
        assertNotNull("Relation object expected", mto2);
        assertEquals("Not the same marketplace - ", mpGlobal.getKey(),
                mto2.getMarketplace_tkey());
        assertEquals("Not the same supplier - ", supp2.getKey(),
                mto2.getOrganization_tkey());
    }

    @Test(expected = OrganizationAlreadyExistsException.class)
    public void addOrganizationsToMarketplace_DuplicateSupplier()
            throws Throwable {
        try {
            container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);
            marketplaceService.addOrganizationsToMarketplace(
                    Collections.singletonList(supp1.getOrganizationId()),
                    CLOSED_MP_ID);
            MarketplaceToOrganization mto = runTX(
                    new Callable<MarketplaceToOrganization>() {
                        @Override
                        public MarketplaceToOrganization call()
                                throws Exception {
                            MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                    mpClosed, supp1);
                            return (MarketplaceToOrganization) mgr.find(mto);
                        }
                    });
            assertNotNull("Relation object expected", mto);
            assertEquals("Whitelist ref exists",
                    PublishingAccess.PUBLISHING_ACCESS_GRANTED,
                    mto.getPublishingAccess());
            marketplaceService.addOrganizationsToMarketplace(
                    Collections.singletonList(supp1.getOrganizationId()),
                    CLOSED_MP_ID);
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /*
     * Use case: supp1 was banned on previously open MP, blacklist ref remains
     * after switching MP to closed => now adding supp1 to accepted list of MP
     * must work (no exc.)
     */
    @Test
    public void addOrganizationsToMarketplace_SupplierHadBeenBannedOnOpenMpBefore()
            throws Throwable {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        MarketplaceToOrganization ref = runTX(
                new Callable<MarketplaceToOrganization>() {

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

        marketplaceService.addOrganizationsToMarketplace(
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
        assertNotNull("Relation object expected", ref);
        assertEquals(
                "Previously blacklist ref has been updated to whitelist ref",
                PublishingAccess.PUBLISHING_ACCESS_GRANTED,
                ref.getPublishingAccess());
        assertEquals("Not the same marketplace - ", mpClosed.getKey(),
                ref.getMarketplace_tkey());
        assertEquals("Not the same supplier - ", supp1.getKey(),
                ref.getOrganization_tkey());
    }

    @Test
    public void createMarketplace_MarketplaceID()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        Organization newOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return org;
            }
        });

        VOMarketplace marketplace = buildMarketplace("FIRST_MP", "FIRST_MP",
                newOrg);
        VOMarketplace firstMarketPlace4newOrg = marketplaceService
                .createMarketplace(marketplace);

        assertNotNull(firstMarketPlace4newOrg);
        assertEquals("FIRST_MP", firstMarketPlace4newOrg.getMarketplaceId());

    }

    @Test
    public void createMarketplace_MarketplaceIDNull()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        Organization newOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return org;
            }
        });

        VOMarketplace marketplace = buildMarketplace("FIRST_MP", null,
                newOrg);
        VOMarketplace firstMarketPlace4newOrg = marketplaceService
                .createMarketplace(marketplace);

        assertNotNull(firstMarketPlace4newOrg);
        assertEquals(newOrg.getOrganizationId(), firstMarketPlace4newOrg.getMarketplaceId());

    }
    /**
     * if the deassigned owning organization has no more marketplaces assigned
     * the deassigned organization will be revoke the organization role
     * MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_DeassignedOwningOrgHasOrgRoleRemoved()
            throws Exception {
        supp1 = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization newOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, newOrg, true, "admin");
                mpOwnerUserKey = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                load(newOrg);
                return newOrg;
            }
        });

        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        // setup: create a marketplace for supp1
        VOMarketplace marketPlaceSupplier1 = marketplaceService
                .createMarketplace(
                        this.buildMarketplace("LOCAL", "LOCAL_SUPP1", supp1));

        // verify that organization supp1 has MARKETPLACE_OWNER role
        assertTrue("organization must not been given role MARKETPLACE_OWNER",
                this.hasSupp1MarketplaceOwnerRole());

        // re-assign marketplace to supp2
        marketPlaceSupplier1.setOwningOrganizationId(supp2.getOrganizationId());
        // Update organization 1009 -> 1002
        marketplaceService.updateMarketplace(marketPlaceSupplier1);
        // verify that organization supp1 has been REVOKED MARKETPLACE_OWNER
        // role
        assertFalse(
                "organization must have been revoked role MARKETPLACE_OWNER",
                this.hasSupp1MarketplaceOwnerRole());
    }

    /**
     * if the deassigned owning organization has still marketplaces assigned the
     * deassigned organization will NOT be revoked the organization role
     * MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_DeassignedOwningOrgKeepsOrgRole_whenOwningMp()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        // setup: supp1 gets 2 marketplaces
        VOMarketplace marketPlace1Supplier1 = marketplaceService
                .createMarketplace(
                        this.buildMarketplace("LOCAL1", "LOCAL1_SUPP1", supp1));
        marketplaceService.createMarketplace(
                this.buildMarketplace("LOCAL2", "LOCAL2_SUPP1", supp1));
        // verify that organization supp1 has MARKETPLACE_OWNER role
        Organization updatedOrg = getSupp1WithinTransaction();
        assertTrue("organization must not been given role MARKETPLACE_OWNER",
                updatedOrg.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));

        // re-assign marketplace to supp2
        marketPlace1Supplier1
                .setOwningOrganizationId(supp2.getOrganizationId());
        marketplaceService.updateMarketplace(marketPlace1Supplier1);
        // verify that organization supp1 has been REVOKED MARKETPLACE_OWNER
        // role
        updatedOrg = getSupp1WithinTransaction();
        assertTrue("organization must still have role MARKETPLACE_OWNER",
                updatedOrg.hasRole(OrganizationRoleType.MARKETPLACE_OWNER));
    }

    /**
     * verify that userRole MARKETPLACE_OWNER is removed from an organization
     * that looses it's organization role MARKETPLACE_OWNER
     * 
     * @throws Exception
     */
    @Test
    public void updateMarketplace_RemoveUserRoleMarketplaceOwner()
            throws Exception {
        // create organization
        supp1 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization newOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return newOrg;
            }
        });
        // add marketplace
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        VOMarketplace marketPlaceSupplier1 = marketplaceService
                .createMarketplace(
                        this.buildMarketplace("LOCAL", "GLOBAL_SUPP1", supp1));
        // add user with marketplace owner user role
        userToVerify = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser userForSupp1 = Organizations.createUserForOrg(mgr,
                        supp1, true, "mpoxxx");
                PlatformUsers.grantRoles(mgr, userForSupp1,
                        UserRoleType.MARKETPLACE_OWNER);
                return userForSupp1;
            }
        });

        // verify that user has been assigned MARKETPLACE_OWNER role
        PlatformUser userWithRole = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser u = (PlatformUser) mgr
                        .getReferenceByBusinessKey(userToVerify);
                load(u);
                return u;
            }
        });
        assertTrue("userWithRole has been granted role MARKETPLACE_OWNER",
                this.hasUserRole(userWithRole));

        // re-assign marketplace to supp2 => supp1 looses ALL marketplaces,
        // therefore userRole MARKETPLACE_OWNER should be removed
        marketPlaceSupplier1.setOwningOrganizationId(supp2.getOrganizationId());
        marketplaceService.updateMarketplace(marketPlaceSupplier1);

        // verify that user marketplaceOwner has been REVOKED user role
        // MARKETPLACE_OWNER
        PlatformUser updatedUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser template = new PlatformUser();
                template.setUserId(userToVerify.getUserId());
                PlatformUser u = (PlatformUser) mgr
                        .getReferenceByBusinessKey(template);
                load(u);
                return u;
            }
        });
        assertFalse("marketplaceOwner has been revoked role MARKETPLACE_OWNER",
                this.hasUserRole(updatedUser));
    }

    @Ignore
    public void test_getAllOrganizations() throws Exception {
        container.login(platformOperatorUserKey, ROLE_MARKETPLACE_OWNER);

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketRestricted = Marketplaces.createMarketplace(
                        platformOperatorOrg, "TEST_MARKETPLACE", false, mgr);
                return marketRestricted;
            }
        });
        List<VOOrganization> results = marketplaceService
                .getAllOrganizations(marketplaceFromDB.getMarketplaceId());
        assertFalse(results.isEmpty());
    }

    @Test
    public void testCloseMarketplace() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR,
                ROLE_MARKETPLACE_OWNER);
        final Organization marketplaceOwner = runTX(
                new Callable<Organization>() {
                    @Override
                    public Organization call() throws Exception {
                        Organization newOrg = Organizations.createOrganization(
                                mgr, OrganizationRoleType.PLATFORM_OPERATOR,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                                OrganizationRoleType.MARKETPLACE_OWNER);
                        return newOrg;
                    }
                });

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketRestricted = Marketplaces.createMarketplace(
                        marketplaceOwner, "RESTRICTED_MARKETPLACE", false, mgr);
                VOOrganization voOrganization = OrganizationAssembler
                        .toVOOrganization(marketplaceOwner);
                Set<Long> authorizedOrganizations = new HashSet<>();
                authorizedOrganizations.add(voOrganization.getKey());

                marketplaceService.closeMarketplace(
                        marketRestricted.getMarketplaceId(),
                        authorizedOrganizations, new HashSet<Long>());

                Marketplace mp = mgr.getReference(Marketplace.class,
                        marketRestricted.getKey());

                MarketplaceAccess ma = new MarketplaceAccess();
                ma.setOrganization_tkey(marketplaceOwner.getKey());
                ma.setMarketplace_tkey(marketRestricted.getKey());
                ma = (MarketplaceAccess) mgr.getReferenceByBusinessKey(ma);

                assertFalse(ma == null);
                return mp;
            }
        });

        assertTrue(marketplaceFromDB.isRestricted());
    }

    @Test
    public void testOpenMarketplace() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR,
                ROLE_MARKETPLACE_OWNER);
        final Organization marketplaceOwner = runTX(
                new Callable<Organization>() {
                    @Override
                    public Organization call() throws Exception {
                        Organization newOrg = Organizations.createOrganization(
                                mgr, OrganizationRoleType.PLATFORM_OPERATOR,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                                OrganizationRoleType.MARKETPLACE_OWNER);
                        return newOrg;
                    }
                });

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketRestricted = Marketplaces.createMarketplace(
                        marketplaceOwner, "RESTRICTED_MARKETPLACE_TO_OPEN",
                        false, mgr);
                VOOrganization voOrganization = OrganizationAssembler
                        .toVOOrganization(marketplaceOwner);
                Set<Long> authorizedOrganizations = new HashSet<>();
                authorizedOrganizations.add(voOrganization.getKey());

                marketplaceService.closeMarketplace(
                        marketRestricted.getMarketplaceId(),
                        authorizedOrganizations, new HashSet<Long>());

                Marketplace mp = mgr.getReference(Marketplace.class,
                        marketRestricted.getKey());

                assertTrue(mp.isRestricted());

                marketplaceService
                        .openMarketplace(marketRestricted.getMarketplaceId());

                Marketplace mpOpened = mgr.getReference(Marketplace.class,
                        marketRestricted.getKey());

                assertFalse(mpOpened.isRestricted());

                return mp;
            }
        });

    }

    @Test
    public void testGetAllAccessibleMarketplacesForOrg() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR,
                ROLE_MARKETPLACE_OWNER);

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketClosed = Marketplaces.createMarketplace(
                        platformOperatorOrg, "CLOSED", false, mgr);

                Marketplace marketOpen = Marketplaces.createMarketplace(
                        platformOperatorOrg, "OPEN", false, mgr);
                VOOrganization voOrganization = OrganizationAssembler
                        .toVOOrganization(platformOperatorOrg);
                Set<Long> authorizedOrganizations = new HashSet<>();
                authorizedOrganizations.add(voOrganization.getKey());

                marketplaceService.closeMarketplace(
                        marketClosed.getMarketplaceId(),
                        authorizedOrganizations, new HashSet<Long>());

                Marketplace mpClosed = mgr.getReference(Marketplace.class,
                        marketClosed.getKey());

                Marketplace mpOpen = mgr.getReference(Marketplace.class,
                        marketOpen.getKey());

                assertTrue(mpClosed.isRestricted());
                assertFalse(mpOpen.isRestricted());

                List<VOMarketplace> accessibleMarketplaces = marketplaceService
                        .getAccessibleMarketplaces();

                boolean closedFound = false;
                boolean openFound = false;
                for (VOMarketplace voMarketplace : accessibleMarketplaces) {
                    if (voMarketplace.getMarketplaceId()
                            .equals(marketClosed.getMarketplaceId())) {
                        closedFound = true;
                        continue;
                    }
                    if (voMarketplace.getMarketplaceId()
                            .equals(marketOpen.getMarketplaceId())) {
                        openFound = true;
                    }
                }

                assertTrue(closedFound);
                assertTrue(openFound);

                boolean resultClosed = marketplaceService
                        .doesOrganizationHaveAccessMarketplace(
                                marketClosed.getMarketplaceId(),
                                platformOperatorOrg.getOrganizationId());
                boolean resultOpen = marketplaceService
                        .doesOrganizationHaveAccessMarketplace(
                                marketOpen.getMarketplaceId(),
                                platformOperatorOrg.getOrganizationId());
                assertTrue(resultClosed);
                assertTrue(resultOpen);
                return mpClosed;
            }
        });
    }

    @Test
    public void testGetAllAccessibleMarketplacesForOrg_onlyOpen()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR,
                ROLE_MARKETPLACE_OWNER);

        Marketplace marketplaceFromDB = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketClosed = Marketplaces.createMarketplace(
                        platformOperatorOrg, "CLOSED1", false, mgr);

                Marketplace marketOpen = Marketplaces.createMarketplace(
                        platformOperatorOrg, "OPEN1", false, mgr);

                marketplaceService.closeMarketplace(
                        marketClosed.getMarketplaceId(), new HashSet<Long>(),
                        new HashSet<Long>());

                Marketplace mpClosed = mgr.getReference(Marketplace.class,
                        marketClosed.getKey());

                Marketplace mpOpen = mgr.getReference(Marketplace.class,
                        marketOpen.getKey());

                assertTrue(mpClosed.isRestricted());
                assertFalse(mpOpen.isRestricted());

                List<VOMarketplace> accessibleMarketplaces = marketplaceService
                        .getAccessibleMarketplaces();

                boolean closedFound = false;
                boolean openFound = false;
                for (VOMarketplace voMarketplace : accessibleMarketplaces) {
                    if (voMarketplace.getMarketplaceId()
                            .equals(marketClosed.getMarketplaceId())) {
                        closedFound = true;
                        continue;
                    }
                    if (voMarketplace.getMarketplaceId()
                            .equals(marketOpen.getMarketplaceId())) {
                        openFound = true;
                    }
                }

                assertFalse(closedFound);
                assertTrue(openFound);
                return mpClosed;
            }
        });
    }
}
