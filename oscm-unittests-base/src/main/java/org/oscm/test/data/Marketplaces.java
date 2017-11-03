/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Mar 8, 2011                                                      
 *                                                                              
 *  Completion Time: Mar 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.junit.Assert;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.EnterpriseLandingpage;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceAccess;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Test setup class regarding <code>Marketplace</code> objects.
 * 
 * @author Dirk Bernsau
 */
public class Marketplaces {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Marketplace findMarketplace(DataService mgr,
            String marketplaceId) {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId);
        return (Marketplace) mgr.find(mp);
    }

    /**
     * Retrieves one arbitrary global marketplace for testing purposes.
     * 
     * @param ds
     *            the data service
     * @return a <code>Marketplace</code>
     */
    public static Marketplace findOneGlobalMarketplace(DataService ds) {

        Query query = ds.createNamedQuery("Marketplace.getAll");
        List<Marketplace> result = ParameterizedTypes.list(
                query.getResultList(), Marketplace.class);
        Assert.assertNotNull("No global marketplace defined", result);
        Assert.assertTrue("No global marketplace defined", result.size() > 0);
        return result.get(0);
    }

    /**
     * Creates a global marketplace for the given owner.
     * 
     * @param owner
     *            the owner of the marketplace
     * @param marketplaceId
     *            optional id (default is '<i>&lt;oId&gt;_GLOBAL</i>')
     * @param ds
     *            a data service
     * @return the created marketplace
     * @throws NonUniqueBusinessKeyException
     */
    public static Marketplace createGlobalMarketplace(Organization owner,
            String marketplaceId, DataService ds)
            throws NonUniqueBusinessKeyException {
        return createGlobalMarketplace(owner, marketplaceId, ds,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static Marketplace createGlobalMarketplace(Organization owner,
            String marketplaceId, DataService ds, BigDecimal mpShare,
            BigDecimal brokerShare, BigDecimal resellerShare)
            throws NonUniqueBusinessKeyException {
        Assert.assertNotNull("Marketplace owner not defined", owner);
        Marketplace mp = new Marketplace();
        if (marketplaceId == null || marketplaceId.trim().length() == 0) {
            marketplaceId = owner.getOrganizationId() + "_GLOBAL";
        }
        mp.setMarketplaceId(marketplaceId.trim());
        mp.setOrganization(owner);
        setDefaultLandingpage(mp);

        createRevenueModels(mp, ds, mpShare, brokerShare, resellerShare);
        ds.persist(mp);
        return mp;
    }

    private static void createRevenueModels(Marketplace mp, DataService ds)
            throws NonUniqueBusinessKeyException {
        createRevenueModels(mp, ds, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    private static void createRevenueModels(Marketplace mp, DataService ds,
            BigDecimal mpShare, BigDecimal brokerShare, BigDecimal resellerShare)
            throws NonUniqueBusinessKeyException {
        RevenueShareModel brokerModel = createRevenueModel(
                RevenueShareModelType.BROKER_REVENUE_SHARE, mpShare);
        RevenueShareModel resellerModel = createRevenueModel(
                RevenueShareModelType.RESELLER_REVENUE_SHARE, brokerShare);
        RevenueShareModel priceModel = createRevenueModel(
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE, resellerShare);
        mp.setBrokerPriceModel(brokerModel);
        mp.setResellerPriceModel(resellerModel);
        mp.setPriceModel(priceModel);
        ds.persist(brokerModel);
        ds.persist(resellerModel);
        ds.persist(priceModel);
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type, BigDecimal revenueSharePercent) {
        RevenueShareModel m = new RevenueShareModel();
        m.setRevenueShare(revenueSharePercent);
        m.setRevenueShareModelType(type);
        return m;
    }

    /**
     * Creates a global marketplace for the given owner.
     * 
     * @param owner
     *            the owner of the marketplace
     * @param marketplaceId
     *            optional id (default is '<i>&lt;oId&gt;_GLOBAL</i>')
     * @param ds
     *            a data service
     * @return the created marketplace
     * @throws NonUniqueBusinessKeyException
     */

    public static Marketplace createMarketplace(Organization owner,
            String marketplaceId, boolean isOpen, DataService ds)
            throws NonUniqueBusinessKeyException {

        return createMarketplace(owner, marketplaceId, isOpen, ds, false);
    }

    public static Marketplace createMarketplace(Organization owner,
            String marketplaceId, boolean isOpen, DataService ds,
            boolean isEnterpriseLandingpage)
            throws NonUniqueBusinessKeyException {
        Assert.assertNotNull("Marketplace owner not defined", owner);
        Assert.assertNotNull("Marketplace id not defined", marketplaceId);
        Assert.assertTrue(marketplaceId.trim().length() > 0);
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId.trim());
        mp.setOrganization(owner);
        mp.setOpen(isOpen);
        if (isEnterpriseLandingpage) {
            setEnterpriseLandingpage(mp);
        } else {
            setDefaultLandingpage(mp);
        }
        createRevenueModels(mp, ds);
        ds.persist(mp);
        return mp;
    }

    /**
     * Creates a restricted marketplace with accessible organization.
     * 
     * @param owner
     *            the owner of the marketplace
     * @param marketplaceId
     *            optional id (default is '<i>&lt;oId&gt;_GLOBAL</i>')
     * @param ds
     *            a data service
     * @return the created marketplace
     * @throws NonUniqueBusinessKeyException
     */
    public static Marketplace createMarketplaceWithRestrictedAccessAndAccessibleOrganizations(
            Organization owner, String marketplaceId, DataService ds,
            List<Organization> accessibleOrganizations)
            throws NonUniqueBusinessKeyException {

        Assert.assertNotNull("Marketplace owner not defined", owner);
        Assert.assertNotNull("Marketplace id not defined", marketplaceId);
        Assert.assertTrue(marketplaceId.trim().length() > 0);

        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId.trim());
        mp.setOrganization(owner);
        mp.setOpen(true);
        setDefaultLandingpage(mp);
        createRevenueModels(mp, ds);
        mp.setRestricted(true);

        ds.persist(mp);

        for (Organization org : accessibleOrganizations) {
            MarketplaceAccess access = new MarketplaceAccess();
            access.setMarketplace(mp);
            access.setOrganization(org);
            ds.persist(access);
        }

        return mp;
    }

    /**
     * Creates a local marketplace for the given owner in case there is none.
     * 
     * @param owner
     *            the owner of the marketplace
     * @param marketplaceId
     *            optional id (default is '<i>&lt;oId&gt;_LOCAL</i>')
     * @param ds
     *            a data service
     * @return the created marketplace
     * @throws NonUniqueBusinessKeyException
     */
    public static Marketplace ensureMarketplace(Organization owner,
            String marketplaceId, DataService ds)
            throws NonUniqueBusinessKeyException {
        Assert.assertNotNull("Marketplace owner not defined", owner);
        Marketplace mp = null;
        try {
            mp = findOneGlobalMarketplace(ds);
        } catch (AssertionError ex) {
            // create a new one
        }
        if (mp == null) {
            if (marketplaceId == null || marketplaceId.trim().length() == 0) {
                marketplaceId = owner.getOrganizationId() + "_LOCAL";
            }
            mp = new Marketplace();
            mp.setCreationDate(System.currentTimeMillis());
            mp.setMarketplaceId(marketplaceId);
            mp.setOrganization(owner);
            mp.setCatalogEntries(new ArrayList<CatalogEntry>());
            setDefaultLandingpage(mp);
            createRevenueModels(mp, ds);
            ds.persist(mp);
            grantPublishing(owner, mp, ds, false);
        }
        return mp;
    }

    /**
     * Grants the provided {@link Organization} publishing rights for the
     * provided {@link Marketplace}. If the passed {@link Organization} doesn't
     * have the {@link OrganizationRoleType#SUPPLIER}, a
     * {@link SaaSSystemException} will be thrown.
     * 
     * @param supplier
     *            the supplier
     * @param marketplace
     *            the marketplace
     * @param ds
     *            the data service
     * @param addToLists
     *            <code>true</code> if the created relation should be added to
     *            the relation lists on {@link Organization} and
     *            {@link Marketplace} (e.g. in complex setups in one
     *            transactions)
     */
    public static void grantPublishing(Organization supplier,
            Marketplace marketplace, DataService ds, boolean addToLists) {
        if (!supplier.hasRole(OrganizationRoleType.SUPPLIER)) {
            throw new SaaSSystemException(
                    "Supplier doesn't have the SUPPLIER role");
        }
        MarketplaceToOrganization relnew = new MarketplaceToOrganization(
                marketplace, supplier);
        try {
            MarketplaceToOrganization rel = (MarketplaceToOrganization) ds
                    .find(relnew);
            if (rel == null) {
                rel = relnew;
                ds.persist(rel);
            } else {
                rel.setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                ds.flush();
            }
            if (addToLists) {
                marketplace.getMarketplaceToOrganizations().add(rel);
                supplier.getMarketplaceToOrganizations().add(rel);
            }
        } catch (NonUniqueBusinessKeyException e) {
            // ignore - publishing rights already granted
        }
    }

    /**
     * Creates a landingpage with landingpageservices
     * 
     * @param marketplace
     *            the marketplace of the landingpage
     * @param numberServices
     *            number of services in the landingpage
     * @param fillinCriterion
     *            fillin criterion of the landingpage
     * @param landingpageProducts
     *            a list of landingpage products
     * @param ds
     *            a data service
     * @return new created landingpage
     */
    private static PublicLandingpage setDefaultLandingpage(
            Marketplace marketplace) {
        PublicLandingpage landingpage = new PublicLandingpage();
        landingpage.setNumberServices(6);
        landingpage.setFillinCriterion(FillinCriterion.ACTIVATION_DESCENDING);
        landingpage.setMarketplace(marketplace);
        marketplace.setPublicLandingpage(landingpage);
        return landingpage;
    }

    private static EnterpriseLandingpage setEnterpriseLandingpage(
            Marketplace marketplace) {
        EnterpriseLandingpage landingpage = new EnterpriseLandingpage();
        landingpage.setMarketplace(marketplace);
        marketplace.setEnterpiseLandingpage(landingpage);
        return landingpage;
    }

    public static void createLandingpageProducts(PublicLandingpage landingpage,
            List<Product> products, DataService ds)
            throws NonUniqueBusinessKeyException {
        if (products != null) {
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);

                LandingpageProduct landingpageProduct = new LandingpageProduct();
                landingpageProduct.setPosition(i + 1);
                landingpageProduct.setProduct(product);
                landingpageProduct.setLandingpage(landingpage);
                landingpage.getLandingpageProducts().add(landingpageProduct);
            }
            ds.persist(landingpage);
        }
    }

    public static void createMarketplaceHistory(final DataService ds,
            final long objKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            long organizationObjKey, long priceModelObjKey,
            long brokerPriceModelObjKey, long resellerPriceModelObjKey)
            throws Exception {
        MarketplaceHistory mpHist = new MarketplaceHistory();

        mpHist.setInvocationDate(new Date());
        mpHist.setObjKey(objKey);
        mpHist.setObjVersion(version);
        mpHist.setModdate(new SimpleDateFormat(DATE_PATTERN)
                .parse(modificationDate));
        mpHist.setModtype(modificationType);
        mpHist.setModuser("moduser");

        mpHist.getDataContainer().setMarketplaceId("marketplaceId");
        mpHist.setOrganizationObjKey(organizationObjKey);
        mpHist.getDataContainer().setOpen(true);
        mpHist.getDataContainer().setTaggingEnabled(true);
        mpHist.getDataContainer().setReviewEnabled(true);
        mpHist.getDataContainer().setSocialBookmarkEnabled(true);
        mpHist.getDataContainer().setCategoriesEnabled(true);
        mpHist.setPriceModelObjKey(priceModelObjKey);
        mpHist.setBrokerPriceModelObjKey(brokerPriceModelObjKey);
        mpHist.setResellerPriceModelObjKey(resellerPriceModelObjKey);

        ds.persist(mpHist);
    }
}
