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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Categories;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.types.enumtypes.EmailType;

/**
 * Unit tests for categorization management.
 * 
 * @author Mani Afschar
 * 
 */
public class CategorizationServiceBeanIT extends EJBTestBase {

    private static final String GLOBAL_MP_ID = "GLOBAL_MP";

    private static final String CATEGORY_ID_1 = "Pharma";
    private static final String CATEGORY_ID_2 = "Cars";
    private static final String CATEGORY_ID_3 = "Food";
    private static final String CATEGORY_NAME_EN_1 = "english Pharma";
    private static final String CATEGORY_NAME_EN_2 = "english Cars";
    private static final String CATEGORY_NAME_DE_2 = "deutsche Autos";
    private static final String CATEGORY_NAME_EN_3 = "english Food";

    private Marketplace mpGlobal, mpGlobal2, mpGlobal3;

    private DataService mgr;

    private Organization mpOwner;
    private long mpOwnerUserKey;

    private CategorizationService categorizationService;
    private List<TaskMessage> sentMessages;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new CategorizationServiceBean());
        container.addBean(new TaskQueueServiceStub() {
            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
                sentMessages = messages;
            }
        });

        categorizationService = container.get(CategorizationService.class);
        mgr = container.get(DataService.class);

        // create marketplace + corresponding owner
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, mpOwner, true, "admin");
                mpOwnerUserKey = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);
                return null;
            }
        });
        mpGlobal = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, GLOBAL_MP_ID,
                        true, mgr);
            }
        });

        mpGlobal2 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, GLOBAL_MP_ID + 2,
                        false, mgr);
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product prod = Products.createProduct(
                        mpOwner.getOrganizationId(), "prodId", "techProductId",
                        mgr);

                Category c1 = Categories.create(mgr, CATEGORY_ID_1, mpGlobal);
                Category c2 = Categories.create(mgr, CATEGORY_ID_2, mpGlobal);
                Category c3 = Categories.create(mgr, CATEGORY_ID_3, mpGlobal2);

                Categories.assignToProduct(mgr, c1, prod, mpGlobal);
                Categories.assignToProduct(mgr, c2, prod, mpGlobal);

                LocalizedResource l1 = new LocalizedResource();
                l1.setLocale("en");
                l1.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                l1.setValue(CATEGORY_NAME_EN_1);
                l1.setObjectKey(c1.getKey());
                mgr.persist(l1);
                LocalizedResource l2 = new LocalizedResource();
                l2.setLocale("de");
                l2.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                l2.setValue(CATEGORY_NAME_DE_2);
                l2.setObjectKey(c2.getKey());
                mgr.persist(l2);
                LocalizedResource l3 = new LocalizedResource();
                l3.setLocale("en");
                l3.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                l3.setValue(CATEGORY_NAME_EN_3);
                l3.setObjectKey(c3.getKey());
                mgr.persist(l3);
                return null;
            }
        });

        createCategoriesForMarketplace3();
    }

    private void createCategoriesForMarketplace3() throws Exception {

        mpGlobal3 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, GLOBAL_MP_ID + 3,
                        false, mgr);
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Category cat1 = Categories.create(mgr, CATEGORY_ID_1,
                        mpGlobal3);
                LocalizedResource loc1 = new LocalizedResource();
                loc1.setLocale("en");
                loc1.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                loc1.setValue(CATEGORY_NAME_EN_1);
                loc1.setObjectKey(cat1.getKey());
                mgr.persist(loc1);

                Category cat2 = Categories.create(mgr, CATEGORY_ID_2,
                        mpGlobal3);
                LocalizedResource loc2 = new LocalizedResource();
                loc2.setLocale("en");
                loc2.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                loc2.setValue(CATEGORY_NAME_EN_2);
                loc2.setObjectKey(cat2.getKey());
                mgr.persist(loc2);

                Category cat3 = Categories.create(mgr, CATEGORY_ID_3,
                        mpGlobal3);
                LocalizedResource loc3 = new LocalizedResource();
                loc3.setLocale("en");
                loc3.setObjectType(LocalizedObjectTypes.CATEGORY_NAME);
                loc3.setValue(CATEGORY_NAME_EN_3);
                loc3.setObjectKey(cat3.getKey());
                mgr.persist(loc3);
                return null;
            }
        });
    }

    @Test
    public void getCategories() {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(CATEGORY_ID_1, list.get(0).getCategoryId());
        Assert.assertEquals(CATEGORY_NAME_EN_1, list.get(0).getName());
        Assert.assertEquals(mpGlobal.getMarketplaceId(),
                list.get(0).getMarketplaceId());
        Assert.assertEquals(0, list.get(0).getVersion());
        Assert.assertEquals(CATEGORY_ID_2, list.get(1).getCategoryId());
        Assert.assertEquals("", list.get(1).getName());
        Assert.assertEquals(mpGlobal.getMarketplaceId(),
                list.get(1).getMarketplaceId());
        Assert.assertEquals(0, list.get(1).getVersion());

        list = categorizationService.getCategories(mpGlobal.getMarketplaceId(),
                "de");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(CATEGORY_NAME_EN_1, list.get(0).getName());
        Assert.assertEquals(CATEGORY_NAME_DE_2, list.get(1).getName());

        list = categorizationService.getCategories(mpGlobal2.getMarketplaceId(),
                "de");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void getCategories_nonExistingMP() {
        List<VOCategory> list = categorizationService.getCategories("xyz",
                "de");
        Assert.assertEquals(0, list.size());
    }

    @Test(expected = EJBException.class)
    public void saveCategories_IllegalAccess() throws Exception {
        List<VOCategory> toBeSaved = new ArrayList<>();
        VOCategory c = new VOCategory();
        c.setCategoryId("1234");
        c.setMarketplaceId(mpGlobal.getMarketplaceId());
        c.setName("english 1234");
        toBeSaved.add(c);
        categorizationService.saveCategories(toBeSaved, null, "en");
    }

    @Test
    public void createCategories() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        List<VOCategory> toBeSaved = new ArrayList<>();
        VOCategory c = new VOCategory();
        c.setCategoryId("1234");
        c.setMarketplaceId(mpGlobal.getMarketplaceId());
        c.setName("english 1234");
        toBeSaved.add(c);
        categorizationService.saveCategories(toBeSaved, null, "en");
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        // we created 2 in setup plus one now, that makes.... ah, 3
        Assert.assertEquals(list.size(), 3);
        VOCategory catToCompare = null;
        for (VOCategory category : list) {
            if (category.getCategoryId().equals("1234")) {
                catToCompare = category;
                Assert.assertEquals("1234", catToCompare.getCategoryId());
                Assert.assertEquals("english 1234", catToCompare.getName());
                Assert.assertEquals(mpGlobal.getMarketplaceId(),
                        catToCompare.getMarketplaceId());
                break;
            }
        }
        Assert.assertNotNull(catToCompare);
    }

    @Test
    public void updateCategories() throws Exception {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal2.getMarketplaceId(), "en");
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        list.get(0).setName("newname");
        list.get(0).setCategoryId("newID");
        categorizationService.saveCategories(list, null, "en");
        list = categorizationService.getCategories(mpGlobal2.getMarketplaceId(),
                "en");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("newname", list.get(0).getName());
        Assert.assertEquals("newID", list.get(0).getCategoryId());
    }

    @Test
    public void updateCategories_noChanges() throws Exception {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal2.getMarketplaceId(), "en");
        String name = list.get(0).getName();
        String id = list.get(0).getCategoryId();
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
        list = categorizationService.getCategories(mpGlobal2.getMarketplaceId(),
                "en");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(name, list.get(0).getName());
        Assert.assertEquals(id, list.get(0).getCategoryId());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void updateCategories_NonUniqueBusinessKey() throws Exception {
        List<VOCategory> list = new ArrayList<>();
        VOCategory vo = new VOCategory();
        vo.setCategoryId(CATEGORY_ID_3);
        vo.setName(CATEGORY_ID_3);
        vo.setMarketplaceId(mpGlobal2.getMarketplaceId());
        list.add(vo);
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void updateSeveralCategories_NonUniqueBusinessKey()
            throws Exception {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        list.get(1).setName("testtesttesttest");
        VOCategory vo = new VOCategory();
        vo.setCategoryId(CATEGORY_ID_3);
        vo.setName(CATEGORY_ID_3);
        vo.setMarketplaceId(mpGlobal2.getMarketplaceId());
        list.add(vo);
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void updateCategories_NonUniqueBusinessKeyOnSameID()
            throws Exception {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        list.get(0).setCategoryId(list.get(1).getCategoryId());
        list.get(1).setName("testtesttesttest");
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void updateCategories_NonUniqueName() throws Exception {
        List<VOCategory> list = new ArrayList<>();
        VOCategory vo = new VOCategory();
        vo.setCategoryId("Food 2");
        vo.setName(CATEGORY_NAME_EN_3);
        vo.setMarketplaceId(mpGlobal2.getMarketplaceId());
        list.add(vo);
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test
    public void updateCategories_NonUniqueName_InDifferentLocale()
            throws Exception {
        List<VOCategory> list = new ArrayList<>();
        VOCategory vo = new VOCategory();
        vo.setCategoryId("Food 2");
        vo.setName(CATEGORY_NAME_EN_3);
        vo.setMarketplaceId(mpGlobal2.getMarketplaceId());
        list.add(vo);
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "de");
        list = categorizationService.getCategories(mpGlobal2.getMarketplaceId(),
                "de");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(CATEGORY_NAME_EN_3, list.get(1).getName());
        Assert.assertEquals("Food 2", list.get(1).getCategoryId());
    }

    @Test
    public void updateCategories_NonUniqueName_InDifferentMP()
            throws Exception {
        List<VOCategory> list = new ArrayList<>();
        VOCategory vo = new VOCategory();
        vo.setCategoryId("Food 2");
        vo.setName(CATEGORY_NAME_EN_3);
        vo.setMarketplaceId(mpGlobal.getMarketplaceId());
        list.add(vo);
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
        list = categorizationService.getCategories(mpGlobal.getMarketplaceId(),
                "en");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(CATEGORY_NAME_EN_3, list.get(2).getName());
        Assert.assertEquals("Food 2", list.get(2).getCategoryId());
    }

    @Test
    public void updateCategories_SwitchTwoNames_Bug9064() throws Exception {

        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal3.getMarketplaceId(), "en");
        for (VOCategory category : list) {
            if (category.getCategoryId().equals(CATEGORY_ID_1)) {
                category.setName(CATEGORY_NAME_EN_2);
            }
            if (category.getCategoryId().equals(CATEGORY_ID_2)) {
                category.setName(CATEGORY_NAME_EN_1);
            }
        }
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test
    public void updateCategories_SwitchThreeNames_Bug9064() throws Exception {

        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal3.getMarketplaceId(), "en");
        for (VOCategory category : list) {
            if (category.getCategoryId().equals(CATEGORY_ID_1)) {
                category.setName(CATEGORY_NAME_EN_2);
            }
            if (category.getCategoryId().equals(CATEGORY_ID_2)) {
                category.setName(CATEGORY_NAME_EN_3);
            }
            if (category.getCategoryId().equals(CATEGORY_ID_3)) {
                category.setName(CATEGORY_NAME_EN_1);
            }
        }

        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        categorizationService.saveCategories(list, null, "en");
    }

    @Test
    public void deleteCategoriesAndCatalogEntries() throws Exception {
        final List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        final List<Long> catalogEntries = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = Products.createProduct("supId", "prodId",
                        "techProd", mgr);
                Marketplace mp = Marketplaces.ensureMarketplace(p.getVendor(),
                        null, mgr);
                assertNotNull("Local marketplace expected", mp);
                CatalogEntry ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setMarketplace(mp);
                ce.setAnonymousVisible(true);
                mgr.persist(ce);
                CategoryToCatalogEntry cc = new CategoryToCatalogEntry();
                cc.setCatalogEntry(ce);
                cc.setCategory(
                        mgr.getReference(Category.class, list.get(0).getKey()));
                mgr.persist(cc);
                mgr.getReference(CategoryToCatalogEntry.class, cc.getKey());
                catalogEntries.add(Long.valueOf(cc.getKey()));
                cc = new CategoryToCatalogEntry();
                cc.setCatalogEntry(ce);
                cc.setCategory(
                        mgr.getReference(Category.class, list.get(1).getKey()));
                mgr.persist(cc);
                catalogEntries.add(Long.valueOf(cc.getKey()));
                mgr.getReference(CategoryToCatalogEntry.class, cc.getKey());
                return null;
            }
        });
        categorizationService.saveCategories(null, list, null);
        Assert.assertEquals(0, categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en").size());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    mgr.getReference(CategoryToCatalogEntry.class,
                            catalogEntries.get(0).longValue());
                    org.junit.Assert.assertTrue(
                            "Object CategoryToCatalogEntry still exists in DB",
                            false);
                } catch (ObjectNotFoundException ex) {
                    // expected
                }
                try {
                    mgr.getReference(CategoryToCatalogEntry.class,
                            catalogEntries.get(1).longValue());
                    org.junit.Assert.assertTrue(
                            "Object CategoryToCatalogEntry still exists in DB",
                            false);
                } catch (ObjectNotFoundException ex) {
                    // expected
                }
                return null;
            }
        });
    }

    @Test
    public void deleteCategories() throws Exception {
        List<VOCategory> list = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        final VOCategory catToCompare = list.remove(0);
        final VOCategory deletedCat = list.get(0);
        categorizationService.saveCategories(null, list, null);
        list = categorizationService.getCategories(mpGlobal.getMarketplaceId(),
                "en");
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getKey(), catToCompare.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query deletedResource = mgr.createNamedQuery(
                        "LocalizedResource.findByBusinessKey");
                deletedResource.setParameter("objectKey",
                        Long.valueOf(deletedCat.getKey()));
                deletedResource.setParameter("locale", "en");
                deletedResource.setParameter("objectType",
                        LocalizedObjectTypes.CATEGORY_NAME);
                Assert.assertEquals(deletedResource.getResultList().size(), 0);
                deletedResource.setParameter("locale", "de");
                Assert.assertEquals(deletedResource.getResultList().size(), 0);
                return null;
            }
        });
    }

    @Test
    public void deleteCategories_sendEnglishEmail() throws Exception {

        // when category food is removed
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        VOCategory categoryPharmaToBeRemoved = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en").get(0);
        categorizationService.saveCategories(null,
                Collections.singletonList(categoryPharmaToBeRemoved), null);

        // then mail is sent
        SendMailPayload message = (SendMailPayload) sentMessages.get(0)
                .getPayload();
        assertEquals(EmailType.CATEGORY_REMOVED,
                message.getMailObjects().get(0).getType());
        assertEquals(mpGlobal.getKey(), message.getMailObjects().get(0)
                .getMarketplaceKey().longValue());
        assertEquals(mpOwnerUserKey,
                message.getMailObjects().get(0).getKey().longValue());
        assertEquals(CATEGORY_NAME_EN_1,
                message.getMailObjects().get(0).getParams()[0]);
    }

    @Test
    public void deleteCategories_sendEmailWithoutLocalization()
            throws Exception {

        // when category food is removed
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        VOCategory categoryCarToBeRemoved = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en").get(1);
        categorizationService.saveCategories(null,
                Collections.singletonList(categoryCarToBeRemoved), null);

        // then mail is sent
        SendMailPayload message = (SendMailPayload) sentMessages.get(0)
                .getPayload();
        assertEquals(EmailType.CATEGORY_REMOVED,
                message.getMailObjects().get(0).getType());
        assertEquals(mpGlobal.getKey(), message.getMailObjects().get(0)
                .getMarketplaceKey().longValue());
        assertEquals(mpOwnerUserKey,
                message.getMailObjects().get(0).getKey().longValue());
        assertEquals(CATEGORY_ID_2,
                message.getMailObjects().get(0).getParams()[0]);
    }

    @Test
    public void deleteCategories_sendGermanEmail() throws Exception {

        // given admin in german locale
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser admin = PlatformUsers.findUser(mgr, "admin",
                        mpOwner);
                admin.setLocale("de");
                mgr.persist(admin);
                return null;
            }
        });

        // when category car is removed
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        VOCategory categoryCarToBeRemoved = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en").get(1);
        categorizationService.saveCategories(null,
                Collections.singletonList(categoryCarToBeRemoved), null);

        // then mail is sent in german locale
        SendMailPayload message = (SendMailPayload) sentMessages.get(0)
                .getPayload();
        assertEquals(EmailType.CATEGORY_REMOVED,
                message.getMailObjects().get(0).getType());
        assertEquals(mpGlobal.getKey(), message.getMailObjects().get(0)
                .getMarketplaceKey().longValue());
        assertEquals(mpOwnerUserKey,
                message.getMailObjects().get(0).getKey().longValue());
        assertEquals(CATEGORY_NAME_DE_2,
                message.getMailObjects().get(0).getParams()[0]);
    }

    @Test
    public void getServicesForCategory() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());
        List<VOCategory> categories = categorizationService
                .getCategories(mpGlobal2.getMarketplaceId(), "en");
        VOCategory category = categories.get(0);
        List<VOService> services = categorizationService
                .getServicesForCategory(category.getKey());
        assertEquals(0, services.size());
        setupService(category, mpGlobal, 1);
        setupService(category, mpGlobal, 2);
        services = categorizationService
                .getServicesForCategory(category.getKey());
        assertEquals(2, services.size());
        assertEquals(services.get(0).getServiceId(), "prodId1");
        assertEquals(services.get(1).getServiceId(), "prodId2");
    }

    private void setupService(final VOCategory category, final Marketplace mp,
            final int counter) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = Products.createProduct("supId", "prodId" + counter,
                        "techProd" + counter, mgr);
                assertNotNull("Local marketplace expected", mp);
                CatalogEntry ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setMarketplace(mp);
                ce.setAnonymousVisible(true);
                mgr.persist(ce);
                CategoryToCatalogEntry cc = new CategoryToCatalogEntry();
                cc.setCatalogEntry(ce);
                cc.setCategory(
                        mgr.getReference(Category.class, category.getKey()));
                mgr.persist(cc);
                return null;
            }
        });
    }

    @Test
    public void saveCategories_Bug9017() throws Exception {
        final String NEW_ID = "newID";
        final String NEW_NAME = "newName";

        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());

        List<VOCategory> categories = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        Assert.assertEquals(2, categories.size());

        // update category
        for (VOCategory cat : categories) {
            if (cat.getCategoryId().equals(CATEGORY_ID_1)) {
                cat.setCategoryId(NEW_ID);
                cat.setName(NEW_NAME);
            } else if (cat.getCategoryId().equals(CATEGORY_ID_2)) {
                cat.setName(CATEGORY_NAME_EN_2);
            }
        }

        // create category with category id already existing in db
        VOCategory category = new VOCategory();
        category.setCategoryId(CATEGORY_ID_1);
        category.setName(CATEGORY_NAME_EN_1);
        category.setMarketplaceId(mpGlobal.getMarketplaceId());
        categories.add(0, category);// add at first position

        // the categories must first be updated and then be inserted!
        categorizationService.saveCategories(categories, null, "en");
        categories = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        Assert.assertEquals(3, categories.size());

        for (VOCategory cat : categories) {
            assertTrue(cat.getCategoryId().equals(CATEGORY_ID_1)
                    || cat.getCategoryId().equals(CATEGORY_ID_2)
                    || cat.getCategoryId().equals(NEW_ID));
        }
    }

    @Test
    public void saveCategories_Bug9117() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name());

        List<VOCategory> createCategories = new ArrayList<>();
        List<VOCategory> deleteCategories = new ArrayList<>();
        List<VOCategory> existingCategories = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        Assert.assertEquals(2, existingCategories.size());

        // select CATEGORY_ID_1 to delete
        for (VOCategory cat : existingCategories) {
            if (cat.getCategoryId().equals(CATEGORY_ID_1)) {
                deleteCategories.add(cat);
            }
        }

        // (re-)create category CATEGORY_ID_1 (already existing in db)
        VOCategory category = new VOCategory();
        category.setCategoryId(CATEGORY_ID_1);
        category.setName(CATEGORY_NAME_EN_1);
        category.setMarketplaceId(mpGlobal.getMarketplaceId());
        createCategories.add(category);

        // first delete category CATEGORY_ID_1 and then create it again
        categorizationService.saveCategories(createCategories, deleteCategories,
                "en");
        existingCategories = categorizationService
                .getCategories(mpGlobal.getMarketplaceId(), "en");
        Assert.assertEquals(2, existingCategories.size());

        for (VOCategory cat : existingCategories) {
            assertTrue(cat.getCategoryId().equals(CATEGORY_ID_1)
                    || cat.getCategoryId().equals(CATEGORY_ID_2));
        }
    }
}
