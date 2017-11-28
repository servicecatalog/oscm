/*******************************************************************************
 *                                                                              
7*  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                              
 *                                                                              
 *  Creation Date: 15.06.2012                                                     
 *                                                                              
 *  Completion Time: 22.06.2012                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.landingpageService.local.LandingpageType;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.marketplace.cache.MarketplaceCacheServiceBean;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.FillinCriterion;

/**
 * Unit tests for the landing page management.
 */
public class LandingpageServiceBeanIT extends EJBTestBase {
    private DataService mgr;
    private LandingpageServiceLocal landingpageServiceLocal;
    private LocalizerServiceLocal localizer;
    private MarketplaceCacheServiceBean mpCache = spy(
            new MarketplaceCacheServiceBean());

    private Marketplace marketplace;
    private PublicLandingpage defaultLandingpage;
    private Organization mpOwner;
    private long mpOwnerUserKey;
    private long dummyOrgUserKey;
    private TechnicalProduct technicalProduct;
    private List<Product> products;
    private List<LandingpageProduct> landingPageroducts;
    private List<VOService> voServices;

    private static final String MARKETPLACEID = "1234567";
    private static final String MARKETPLACEID_ENTERPRISE = "1234567_ENTERPRISE";
    private static final String UNKNOWN_MARKETPLACEID = "unknownMarketplaceId";
    private static final int NUMBER_PRODUCTS = 5;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        initBeans(container);

        createMarketplaceOwnerOrg();
        createDummyOrg();
        createMarketplace();
        createMarketplaceWithEnterpriseLaandingpage();
        createServices();
        createLandingpageProducts();
    }

    @After
    public void cleanUp() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // to avoid ChildExistsException
                mgr.createQuery(
                        "UPDATE CatalogEntry c SET c.marketplace = NULL")
                        .executeUpdate();
                mgr.remove(mgr.getReferenceByBusinessKey(marketplace));
                return null;
            }
        });
    }

    /**
     * The setup creates a default public landing page. Load type of this
     * landing page.
     */
    @Test
    public void loadLandingpageType() throws Exception {

        // given public landing page from setup
        // when loading
        LandingpageType loadedType = landingpageServiceLocal
                .loadLandingpageType(MARKETPLACEID);

        // then
        assertEquals(LandingpageType.PUBLIC, loadedType);
    }

    /**
     * Try to load landing page type from non existing marketplace
     */
    @Test(expected = ObjectNotFoundException.class)
    public void loadLandingpageType_nonExistingMarketplace() throws Exception {
        landingpageServiceLocal.loadLandingpageType(UNKNOWN_MARKETPLACEID);
    }

    /**
     * Setup enterprise landing page and load type
     */
    @Test
    public void loadLandingpageType_enterprise() throws Exception {

        // given enterprise landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // when
        LandingpageType loadedType = landingpageServiceLocal
                .loadLandingpageType(MARKETPLACEID);

        // then
        assertEquals(LandingpageType.ENTERPRISE, loadedType);
    }

    @Test
    public void savePublicLandingpageConfig() throws Throwable {
        // given
        int givenNumberServices = 88;
        FillinCriterion givenFillinCriterion = FillinCriterion.NAME_ASCENDING;
        int givenNumberVOLandingpageServices = 2;

        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        VOPublicLandingpage givenVOLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);
        givenVOLandingpage.setNumberServices(givenNumberServices);
        givenVOLandingpage.setFillinCriterion(givenFillinCriterion);
        // change from 5 to 2 landingpage services
        givenVOLandingpage.getLandingpageServices().remove(0);

        // when
        landingpageServiceLocal.savePublicLandingpageConfig(givenVOLandingpage);

        // then
        VOPublicLandingpage voLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);

        assertLandingpage(voLandingpage, defaultLandingpage.getKey(), 1,
                givenFillinCriterion, givenNumberServices);

        assertLandingpageProducts(voLandingpage.getLandingpageServices(),
                givenNumberVOLandingpageServices);
    }

    @Test
    public void savePublicLandingpageConfig_MpCache() throws Throwable {
        // given
        int givenNumberServices = 88;
        FillinCriterion givenFillinCriterion = FillinCriterion.NAME_ASCENDING;

        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        assertNull(landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID_ENTERPRISE));

        VOPublicLandingpage givenVOLandingpage = new VOPublicLandingpage();
        givenVOLandingpage.setMarketplaceId(MARKETPLACEID_ENTERPRISE);
        givenVOLandingpage.setNumberServices(givenNumberServices);
        givenVOLandingpage.setFillinCriterion(givenFillinCriterion);

        // when
        landingpageServiceLocal.savePublicLandingpageConfig(givenVOLandingpage);

        // then
        VOPublicLandingpage voLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID_ENTERPRISE);

        assertNotNull(voLandingpage);

        verify(mpCache, atLeastOnce())
                .resetConfiguration(MARKETPLACEID_ENTERPRISE);
    }

    @Test
    public void resetLandingpage_MpCache() throws Throwable {
        // given
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID_ENTERPRISE);

        // then
        verify(mpCache, atLeastOnce())
                .resetConfiguration(MARKETPLACEID_ENTERPRISE);
    }

    @Test
    public void savePublicLandingpageConfig_removeProduct() throws Throwable {
        // given
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        Product product = addProductToLandingpage();
        VOPublicLandingpage givenVOLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);
        removeProduct(product);

        // when
        landingpageServiceLocal.savePublicLandingpageConfig(givenVOLandingpage);

        // then
        VOPublicLandingpage voLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);
        assertEquals(countLandingpageProductsWithValidStatus(),
                voLandingpage.getLandingpageServices().size());
    }

    /**
     * Save the public landing page configuration with a given value. Before an
     * enterprise landing page was saved.
     */
    @Test
    @Ignore
    //Glassfish upgrade
    public void savePublicLandingpageConfig_switchFromEnterpriseLandingpage()
            throws Throwable {

        // given enterprise landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // when saving a public landing page config
        VOPublicLandingpage publicLandingpage = newVOPublicLandingpage(
                MARKETPLACEID);
        publicLandingpage.setNumberServices(10);
        landingpageServiceLocal.savePublicLandingpageConfig(publicLandingpage);

        // then
        marketplace = reload(marketplace);
        PublicLandingpage landingPage = reload(
                marketplace.getPublicLandingpage());
        assertNull(marketplace.getEnterpriseLandingpage());
        assertNotNull(marketplace.getPublicLandingpage());
        assertEquals(10, landingPage.getNumberServices());
    }

    private void removeProduct(final Product product) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                product.setStatus(ServiceStatus.DELETED);
                return null;
            }
        });
    }

    private Product addProductToLandingpage() throws Throwable {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProduct(mpOwner,
                        technicalProduct, false, "ProductId_9843124", null,
                        marketplace, mgr);

                List<Product> products = new LinkedList<>();
                products.add(product);

                Marketplace marketplace = Marketplaces.findMarketplace(mgr,
                        MARKETPLACEID);
                Marketplaces.createLandingpageProducts(
                        marketplace.getPublicLandingpage(), products, mgr);
                return product;
            }
        });
    }

    private void assertLandingpageProducts(
            List<VOLandingpageService> loadedVOLandingpageServices,
            int expectedNumberLandingpageProducts) throws Exception {
        assertEquals(expectedNumberLandingpageProducts,
                loadedVOLandingpageServices.size());

        for (VOLandingpageService voLandingpageService : loadedVOLandingpageServices) {
            assertLandingpageService(voLandingpageService,
                    getVOService(voLandingpageService.getService().getKey()));
        }
    }

    private VOService getVOService(long key) throws Exception {
        setVOServices();
        for (VOService voService : voServices) {
            if (voService.getKey() == key) {
                return voService;
            }
        }
        return null;
    }

    @Test(expected = ObjectNotFoundException.class)
    public void savePublicLandingpageConfig_marketplaceNotFound()
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, ConcurrentModificationException,
            NonUniqueBusinessKeyException, FillinOptionNotSupportedException {

        // given unknown marketplace id
        VOPublicLandingpage voLandingpage = new VOPublicLandingpage();
        voLandingpage.setMarketplaceId(UNKNOWN_MARKETPLACEID);

        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.savePublicLandingpageConfig(voLandingpage);

        // then
        fail();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void savePublicLandingpageConfig_LandingpageConcurrentlyChanged()
            throws Exception {

        // given concurrently changed landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        VOPublicLandingpage voLandingpage = newVOPublicLandingpage(
                MARKETPLACEID);
        voLandingpage.setKey(defaultLandingpage.getKey());
        voLandingpage.setNumberServices(66);
        landingpageServiceLocal.savePublicLandingpageConfig(voLandingpage);
        voLandingpage.setNumberServices(666);
        voLandingpage.setVersion(0);

        // when, then save must fail
        landingpageServiceLocal.savePublicLandingpageConfig(voLandingpage);
    }

    private VOPublicLandingpage newVOPublicLandingpage(String marketplaceId) {
        VOPublicLandingpage voLandingpage = new VOPublicLandingpage();
        voLandingpage.setFillinCriterion(FillinCriterion.NO_FILLIN);
        voLandingpage.setMarketplaceId(marketplaceId);
        voLandingpage.setNumberServices(0);
        return voLandingpage;
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePublicLandingpageConfig_invalidOrganization()
            throws Exception {

        // given
        VOPublicLandingpage voLandingpage = new VOPublicLandingpage();
        voLandingpage.setMarketplaceId(MARKETPLACEID);

        // when not a marketplace owner user tries to save the landing page
        long invalidOrgUserKey = dummyOrgUserKey;
        container.login(invalidOrgUserKey,
                UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.savePublicLandingpageConfig(voLandingpage);

        // then
        fail();
    }

    @Test(expected = EJBAccessException.class)
    public void savePublicLandingpageConfig_invalidRole() throws Throwable {
        // given
        String invalidRole = UserRoleType.ORGANIZATION_ADMIN.name();
        container.login(mpOwnerUserKey, invalidRole);

        // when
        try {
            landingpageServiceLocal
                    .savePublicLandingpageConfig(new VOPublicLandingpage());
        } catch (EJBException e) {
            throw e.getCause();
        }

        // then
        fail();
    }

    /**
     * Define that the enterprise landing page is to be used instead of the
     * default public landing page.
     */
    @Test
    public void saveEnterpriseLandingpageConfig() throws Throwable {

        // given public landing page from setup
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // then
        Marketplace mp = reload(marketplace);
        assertNull(mp.getPublicLandingpage());
        assertNotNull(mp.getEnterpriseLandingpage());
    }

    @Test
    public void saveEnterpriseLandingpageConfig_MpCache() throws Throwable {

        // given public landing page from setup
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // then
        verify(mpCache, atLeastOnce()).resetConfiguration(MARKETPLACEID);
    }

    /**
     * Save enterprise landing page. All objects of the old public landing page
     * must be cleanded up.
     * 
     */
    @Test
    public void saveEnterpriseLandingpageConfig_removeDefaultLandingPage()
            throws Throwable {

        // given public landing page from setup
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // then make sure all child objects are removed
        assertNull(reload(defaultLandingpage));
        assertThat(reload(landingPageroducts), hasNoItems());

    }

    /**
     * Given marketplace must exist
     */
    @Test(expected = ObjectNotFoundException.class)
    public void saveEnterpriseLandingpageConfig_maketplaceNotFound()
            throws Throwable {
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal
                .saveEnterpriseLandingpageConfig(UNKNOWN_MARKETPLACEID);
    }

    /**
     * User must have "MARKETPLACE_OWNER" role
     */
    @Test(expected = EJBAccessException.class)
    public void saveEnterpriseLandingpageConfig_invalidRole() throws Throwable {

        // given wrong role
        String invalidRole = UserRoleType.ORGANIZATION_ADMIN.name();
        container.login(mpOwnerUserKey, invalidRole);

        // when saving, then exception must be thrown
        try {
            landingpageServiceLocal
                    .saveEnterpriseLandingpageConfig(MARKETPLACEID);
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /**
     * User must be owner of the marketplace
     */
    @Test(expected = OperationNotPermittedException.class)
    public void saveEnterpriseLandingpageConfig_invalidOrganization()
            throws Throwable {

        // given marketplace owner (right role), but wrong organization (not
        // owner)
        long invalidOrgUserKey = dummyOrgUserKey;
        container.login(invalidOrgUserKey,
                UserRoleType.MARKETPLACE_OWNER.name());

        // when saving, then exception must be thrown
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

    }

    @Test
    public void loadPublicLandingpageConfig() throws Throwable {
        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        VOPublicLandingpage voLandingpage = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);

        // then
        assertLandingpage(voLandingpage, defaultLandingpage.getKey(), 0,
                PublicLandingpage.DEFAULT_FILLINCRITERION,
                PublicLandingpage.DEFAULT_NUMBERSERVICES);

        assertLandingpageProducts(voLandingpage.getLandingpageServices(),
                countLandingpageProductsWithValidStatus());
    }

    private int countLandingpageProductsWithValidStatus() throws Exception {
        return runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Marketplace marketplace = (Marketplace) mgr
                        .getReferenceByBusinessKey(
                                new Marketplace(MARKETPLACEID));

                int counter = 0;
                for (LandingpageProduct lp : marketplace.getPublicLandingpage()
                        .getLandingpageProducts()) {

                    if (lp.getProduct().getStatus()
                            .isStatusValidForLandingPage()) {
                        counter++;
                    }
                }
                return new Integer(counter);
            }
        }).intValue();
    }

    private int countProductsNotUsedInLandingpageProducts() throws Exception {
        return runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Marketplace marketplace = (Marketplace) mgr
                        .getReferenceByBusinessKey(
                                new Marketplace(MARKETPLACEID));
                Query query = mgr
                        .createNamedQuery(
                                "Product.getPublishedProductTemplates")
                        .setParameter("filterOutWithStatus",
                                LandingpageServiceBeanLocal.FILTER_OUT_WITH_STATUS)
                        .setParameter("marketplace", marketplace);
                @SuppressWarnings("unchecked")
                List<Product> products = query.getResultList();

                int counter = 0;
                for (Product product : products) {
                    if (product.getStatus().isStatusValidForLandingPage()) {
                        counter++;
                    }
                }
                return new Integer(counter);
            }
        }).intValue();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void loadPublicLandingpageConfig_marketplaceNotFound()
            throws Throwable {
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal
                .loadPublicLandingpageConfig(UNKNOWN_MARKETPLACEID);
    }

    @Test(expected = EJBAccessException.class)
    public void loadPublicLandingpageConfig_invalidRole() throws Throwable {
        // given
        String invalidRole = UserRoleType.ORGANIZATION_ADMIN.name();
        container.login(mpOwnerUserKey, invalidRole);

        // when loading, then throw exception
        try {
            landingpageServiceLocal.loadPublicLandingpageConfig(MARKETPLACEID);
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /**
     * Try to load public landing page configuration data, but enterprise
     * landing page is defined.
     */
    @Test
    public void loadPublicLandingpageConfig_butEnterpriseLandingpageDefined()
            throws Throwable {

        // given enterprise landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // when
        VOPublicLandingpage result = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);

        // then
        assertNull(result);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void loadPublicLandingpageConfig_invalidOrganization()
            throws Throwable {
        // given
        long invalidOrgUserKey = dummyOrgUserKey;
        container.login(invalidOrgUserKey,
                UserRoleType.MARKETPLACE_OWNER.name());

        // when not a marketplace owner user tries to load the landing page
        landingpageServiceLocal.loadPublicLandingpageConfig(MARKETPLACEID);

        // then
        fail();
    }

    @Test
    public void resetLandingpage() throws Exception {
        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID);

        // then
        VOPublicLandingpage voLandingpageAfterReset = landingpageServiceLocal
                .loadPublicLandingpageConfig(MARKETPLACEID);

        assertLandingpage(voLandingpageAfterReset, defaultLandingpage.getKey(),
                0, PublicLandingpage.DEFAULT_FILLINCRITERION,
                PublicLandingpage.DEFAULT_NUMBERSERVICES);

        assertEquals(0,
                voLandingpageAfterReset.getLandingpageServices().size());
    }

    /**
     * The user has set the enterprise landing page and resets to default (this
     * is the public landing page)
     */
    @Test
    public void resetLandingpage_switchFromEnterpriseLandingpage()
            throws Exception {

        // given marketplace with enterprise landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);

        // when reseting
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID);

        // then landing page is switched to public landing page
        Marketplace mp = reload(marketplace);
        assertNotNull(mp.getPublicLandingpage());
        assertNull(mp.getEnterpriseLandingpage());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void resetLandingpage_marketplaceNotFound() throws Throwable {
        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.resetLandingpage(UNKNOWN_MARKETPLACEID);

        // then
        fail();
    }

    @Test(expected = EJBAccessException.class)
    public void resetLandingpage_invalidRole() throws Throwable {
        // given
        String invalidRole = UserRoleType.ORGANIZATION_ADMIN.name();
        container.login(mpOwnerUserKey, invalidRole);

        // when
        try {
            landingpageServiceLocal.resetLandingpage(MARKETPLACEID);
        } catch (EJBException e) {
            throw e.getCause();
        }

        // then
        fail();
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resetLandingpage_invalidOrganization() throws Throwable {
        // given
        long invalidOrgUserKey = dummyOrgUserKey;
        container.login(invalidOrgUserKey,
                UserRoleType.MARKETPLACE_OWNER.name());

        // when not a marketplace owner user tries to reset the landing page
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID);

        // then
        fail();
    }

    /**
     * When a product is removed, then the landing page must be cleaned up as
     * well
     */
    @Test
    public void removeProductFromLandingpage() throws Exception {

        // given
        int numberOfProductsBeforeRemove = products.size();

        // when removing product
        landingpageServiceLocal.removeProductFromLandingpage(marketplace,
                products.get(0));

        // then landing page must be cleaned up
        List<LandingpageProduct> reloadedProducts = reload(landingPageroducts);
        assertThat(reloadedProducts,
                hasItems(numberOfProductsBeforeRemove - 1));
    }

    /**
     * Currently, no products can be configured on the enterprise landing page.
     * A removed product should be ignored (considered as internal detail).
     */
    @Test
    public void removeProductFromLandingpage_doNothingForEnterpriseLandingpage()
            throws Exception {

        // given marketplace with enterprise landing page
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.saveEnterpriseLandingpageConfig(MARKETPLACEID);
        marketplace = reload(marketplace);

        // when removing product, nothing happens
        landingpageServiceLocal.removeProductFromLandingpage(marketplace,
                products.get(0));
    }

    @Test
    public void availableServices() throws Throwable {
        // given
        // Only products are selected which are not used in a landingpage.
        // Therefore remove all landingpage products.
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID);

        // when
        List<VOService> availableServices = landingpageServiceLocal
                .availableServices(MARKETPLACEID);

        // then
        assertNotNull(availableServices);
        assertEquals(countProductsNotUsedInLandingpageProducts(),
                availableServices.size());
        for (VOService availableService : availableServices) {
            assertTrue(availableService.getKey() > 0);
            assertNotNull(availableService.getServiceId());
            assertNotNull(availableService.getStatus());
            assertNotNull(availableService.getSellerName());
            assertTrue(
                    availableService.getStatus().isStatusValidForLandingPage());
        }
    }

    @Test
    public void availableServices_productsReferencedByLandingpageProduct()
            throws Throwable {
        // given landingpage with products in setup routine
        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        List<VOService> availableServices = landingpageServiceLocal
                .availableServices(MARKETPLACEID);

        // then
        // should be empty because all products are used in landingpage products
        assertNotNull(voServices);
        assertEquals(0, availableServices.size());
    }

    @Test
    public void availableServices_notPublishedOnMarketplace() throws Throwable {
        // given
        unpublishAllProducts();

        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        List<VOService> availableServices = landingpageServiceLocal
                .availableServices(MARKETPLACEID);

        // then
        assertNotNull(voServices);
        assertEquals(0, availableServices.size());
    }

    private void unpublishAllProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                for (Product product : products) {
                    if (product.getCatalogEntries().size() > 0) {
                        product.getCatalogEntries().get(0).setMarketplace(null);
                    }
                }
                return null;
            }
        });
    }

    @Test(expected = ObjectNotFoundException.class)
    public void availableServices_marketplaceNotFound() throws Throwable {
        // when
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        landingpageServiceLocal.availableServices(UNKNOWN_MARKETPLACEID);

        // then
        fail();
    }

    @Test(expected = EJBAccessException.class)
    public void availableServices_invalidRole() throws Throwable {
        // given
        String invalidRole = UserRoleType.ORGANIZATION_ADMIN.name();
        container.login(mpOwnerUserKey, invalidRole);

        // when
        try {
            landingpageServiceLocal.availableServices(MARKETPLACEID);
        } catch (EJBException e) {
            throw e.getCause();
        }

        // then
        fail();
    }

    @Test(expected = OperationNotPermittedException.class)
    public void availableServices_invalidOrganization() throws Throwable {
        // given
        long invalidOrgUserKey = dummyOrgUserKey;
        container.login(invalidOrgUserKey,
                UserRoleType.MARKETPLACE_OWNER.name());

        // when not a marketplace owner user tries to reset the landing page
        landingpageServiceLocal.availableServices(MARKETPLACEID);

        // then
        fail();
    }

    @Test
    public void availableServices_withPartnerProducts() throws Throwable {
        // given
        int availablePartnerTemplates = createPartnerServices();
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // Only products are selected which are not used in a landingpage.
        // Therefore remove all landingpage products.
        landingpageServiceLocal.resetLandingpage(MARKETPLACEID);

        // when
        List<VOService> availableServices = landingpageServiceLocal
                .availableServices(MARKETPLACEID);

        // then
        assertNotNull(availableServices);
        assertEquals("Templates and partner templates must be found",
                getAvailableTemplates() + availablePartnerTemplates,
                availableServices.size());

        int foundPartnerServices = 0;
        for (VOService availableService : availableServices) {
            assertTrue(availableService.getKey() > 0);
            assertNotNull(availableService.getServiceId());
            assertNotNull(availableService.getStatus());
            assertNotNull(availableService.getSellerName());
            assertTrue(
                    availableService.getStatus().isStatusValidForLandingPage());
            if (availableService.getOfferingType() != OfferingType.DIRECT) {
                foundPartnerServices++;
            }
        }
        assertEquals("Wrong number of partner templates found",
                availablePartnerTemplates, foundPartnerServices);
    }

    private int getAvailableTemplates() {
        int availableTemplates = 0;
        for (Product p : products) {
            if (p.getStatus() != ServiceStatus.DELETED
                    && p.getStatus() != ServiceStatus.SUSPENDED
                    && p.getType().equals(ServiceType.TEMPLATE)) {
                availableTemplates++;
            }
        }
        return availableTemplates;
    }

    private void initBeans(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        mgr = container.get(DataService.class);
        container.addBean(new LocalizerServiceBean());
        localizer = container.get(LocalizerServiceLocal.class);
        container.addBean(mpCache);
        LandingpageServiceBeanLocal lpBean = new LandingpageServiceBeanLocal();
        lpBean.marketplaceCache = mpCache;
        container.addBean(lpBean);
        landingpageServiceLocal = container.get(LandingpageServiceLocal.class);
    }

    private void createMarketplaceOwnerOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
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
    }

    /**
     * Needed to verify if a user of an organization tries to call a business
     * method that is not owner of the marketplace
     */
    private void createDummyOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, org, true, "admin");
                dummyOrgUserKey = createUserForOrg.getKey();
                return null;
            }
        });
    }

    private void createServices() throws Exception {
        // create products in database
        products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() throws Exception {
                technicalProduct = TechnicalProducts
                        .createTestData(mgr, mpOwner, 1).get(0);

                List<Product> result = new LinkedList<>();
                ServiceStatus[] serviceStatusArray = ServiceStatus.values();
                for (int i = 0; i < NUMBER_PRODUCTS; i++) {
                    Product product = Products.createProduct(mpOwner,
                            technicalProduct, false, "ProductId_" + i, null,
                            marketplace, mgr);
                    product.setStatus(
                            serviceStatusArray[i % serviceStatusArray.length]);
                    result.add(product);
                }
                return result;
            }
        });

        // create VOServices with given products
        setVOServices();
        assertEquals(NUMBER_PRODUCTS, voServices.size());
    }

    private void setVOServices() throws Exception {
        runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                mgr.createQuery("SELECT p FROM Product p").getResultList();

                voServices = new LinkedList<>();
                final LocalizerFacade facade = new LocalizerFacade(localizer,
                        "en");
                for (Product product : products) {
                    product = (Product) mgr.find(product);
                    voServices
                            .add(ProductAssembler.toVOProduct(product, facade));
                }
                return null;
            }
        });
    }

    private int createPartnerServices() throws Exception {
        return runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                final LocalizerFacade facade = new LocalizerFacade(localizer,
                        "en");

                Organization broker = Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
                Product brokerProduct = Products.createProductResaleCopy(
                        products.get(0), broker, marketplace, mgr);
                brokerProduct.setStatus(ServiceStatus.ACTIVE);
                products.add(brokerProduct);
                voServices.add(
                        ProductAssembler.toVOProduct(brokerProduct, facade));

                Organization reseller = Organizations.createOrganization(mgr,
                        OrganizationRoleType.RESELLER);
                Product resellerProduct = Products.createProductResaleCopy(
                        products.get(0), reseller, marketplace, mgr);
                resellerProduct.setStatus(ServiceStatus.INACTIVE);
                products.add(resellerProduct);
                voServices.add(
                        ProductAssembler.toVOProduct(resellerProduct, facade));

                return Integer.valueOf(2);
            }
        }).intValue();
    }

    private void createMarketplace() throws Exception {
        marketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                marketplace = Marketplaces.createMarketplace(mpOwner,
                        MARKETPLACEID, true, mgr);
                return marketplace;
            }
        });
    }

    private void createMarketplaceWithEnterpriseLaandingpage()
            throws Exception {
        runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplaces.createMarketplace(mpOwner,
                        MARKETPLACEID_ENTERPRISE, true, mgr, true);
                return marketplace;
            }
        });
    }

    private void createLandingpageProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                marketplace = Marketplaces.findMarketplace(mgr, MARKETPLACEID);
                defaultLandingpage = marketplace.getPublicLandingpage();
                Marketplaces.createLandingpageProducts(defaultLandingpage,
                        products, mgr);

                landingPageroducts = defaultLandingpage
                        .getLandingpageProducts();
                load(landingPageroducts);
                return null;
            }
        });
    }

    private void assertLandingpage(VOPublicLandingpage voLandingpage,
            long expectedKey, int expectedVersion,
            FillinCriterion expectedFillinCriterion,
            int expectedNumberServices) {
        assertNotNull(voLandingpage);
        assertEquals(expectedKey, voLandingpage.getKey());
        assertEquals(expectedVersion, voLandingpage.getVersion());
        assertEquals(MARKETPLACEID, voLandingpage.getMarketplaceId());
        assertEquals(expectedFillinCriterion,
                voLandingpage.getFillinCriterion());
        assertEquals(expectedNumberServices, voLandingpage.getNumberServices());
    }

    private void assertLandingpageService(
            VOLandingpageService voLandingpageService,
            VOService expectedVOService) {
        assertNotNull(voLandingpageService);
        assertNotNull(expectedVOService);
        assertEquals(expectedVOService.getKey(),
                voLandingpageService.getService().getKey());
    }
}
