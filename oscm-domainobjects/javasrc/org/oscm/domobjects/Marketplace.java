/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                 
 *                                                                              
 *  Creation Date: 21.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * A marketplace represents an 'area' where services can be offered be suppliers
 * and subscribed by customers.
 */
@BusinessKey(attributes = { "marketplaceId" })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "marketplaceId" }))
@NamedQueries({
        @NamedQuery(name = "Marketplace.findByBusinessKey", query = "SELECT mp FROM Marketplace mp WHERE mp.dataContainer.marketplaceId = :marketplaceId"),
        @NamedQuery(name = "Marketplace.getAll", query = "SELECT mp FROM Marketplace mp"),
        @NamedQuery(name = "Marketplace.getAllAccessible", query = "SELECT mp FROM Marketplace mp WHERE mp"
                + ".dataContainer.restricted = FALSE OR EXISTS (SELECT ma FROM MarketplaceAccess ma WHERE ma"
                + ".marketplace_tkey = mp.key AND ma.organization_tkey = :organization_tkey)"),
        @NamedQuery(name = "Marketplace.getByOwner", query = "SELECT mp FROM Marketplace mp, Organization o WHERE mp.organization = o AND o.dataContainer.organizationId=:organizationId"),
        @NamedQuery(name = "Marketplace.findByService", query = "SELECT mp FROM Marketplace mp, CatalogEntry ce WHERE ce.marketplace = mp AND ce.product=:service"),
        @NamedQuery(name = "Marketplace.findMarketplacesForPublishingForOrg", query = "SELECT mp FROM Marketplace mp "
                + "WHERE ( (mp.dataContainer.open = FALSE AND EXISTS (SELECT mto FROM MarketplaceToOrganization mto WHERE mp.key = mto.marketplace_tkey "
                + "AND mto.organization_tkey=:organization_tkey AND mto.dataContainer.publishingAccess=:publishingAccessGranted)) "
                + "OR (mp.dataContainer.open = TRUE AND NOT EXISTS (SELECT mto FROM MarketplaceToOrganization mto "
                + "WHERE mp.key = mto.marketplace_tkey AND mto.organization_tkey=:organization_tkey "
                + "AND mto.dataContainer.publishingAccess=:publishingAccessDenied)))"),
        @NamedQuery(name = "Marketplace.getAllForTenant", query = "SELECT mp FROM Marketplace mp WHERE mp"
                + ".tenant = :tenant"),
        @NamedQuery(name = "Marketplace.getAllForDefaultTenant", query = "SELECT mp FROM Marketplace mp WHERE mp"
                + ".tenant is null") })
@Entity
public class Marketplace extends DomainObjectWithHistory<MarketplaceData> {

    private static final long serialVersionUID = -3734874169454511010L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(
                    Arrays.asList(LocalizedObjectTypes.MARKETPLACE_NAME,
                            LocalizedObjectTypes.MARKETPLACE_STAGE,
                            LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES));

    public Marketplace() {
        super();
        dataContainer = new MarketplaceData();
    }

    /**
     * Creates a new Marketplace object with the given marketplace id.
     * 
     * @param marketplaceId
     *            the id of the marketplace
     */
    public Marketplace(String marketplaceId) {
        this();
        setMarketplaceId(marketplaceId);
    }

    /**
     * n:1 relation to owning Organization
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organization organization;

    @OneToMany(mappedBy = "marketplace", fetch = FetchType.LAZY)
    private List<CatalogEntry> catalogEntries;

    @OneToMany(mappedBy = "marketplace", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<MarketplaceToOrganization> marketplaceToOrganizations = new ArrayList<>();

    @OneToMany(mappedBy = "marketplace", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<MarketplaceAccess> marketplaceAccesses = new ArrayList<>();

    @OneToOne(cascade = { CascadeType.PERSIST,
            CascadeType.REMOVE }, fetch = FetchType.LAZY, optional = true)
    private PublicLandingpage publicLandingpage;

    @OneToOne(cascade = { CascadeType.PERSIST,
            CascadeType.REMOVE }, fetch = FetchType.LAZY, optional = true)
    private EnterpriseLandingpage enterpriseLandingpage;

    @OneToOne(optional = false, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel priceModel;

    @OneToOne(optional = false, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel brokerPriceModel;

    @OneToOne(optional = false, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel resellerPriceModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tenant_tkey")
    private Tenant tenant;

    public RevenueShareModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(RevenueShareModel priceModel) {
        this.priceModel = priceModel;
    }

    public RevenueShareModel getBrokerPriceModel() {
        return brokerPriceModel;
    }

    public void setBrokerPriceModel(RevenueShareModel brokerPriceModel) {
        this.brokerPriceModel = brokerPriceModel;
    }

    public RevenueShareModel getResellerPriceModel() {
        return resellerPriceModel;
    }

    public void setResellerPriceModel(RevenueShareModel resellerPriceModel) {
        this.resellerPriceModel = resellerPriceModel;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Refer to {@link MarketplaceData#creationDate}
     */
    public long getCreationDate() {
        return dataContainer.getCreationDate();
    }

    /**
     * Refer to {@link MarketplaceData#creationDate}
     */
    public void setCreationDate(long creationDate) {
        dataContainer.setCreationDate(creationDate);
    }

    public void setMarketplaceId(String marketplaceId) {
        dataContainer.setMarketplaceId(marketplaceId);
    }

    public String getMarketplaceId() {
        return dataContainer.getMarketplaceId();
    }

    public void setOpen(boolean open) {
        dataContainer.setOpen(open);
    }

    public boolean isOpen() {
        return dataContainer.isOpen();
    }

    public void setTaggingEnabled(boolean taggingEnabled) {
        dataContainer.setTaggingEnabled(taggingEnabled);
    }

    public boolean isTaggingEnabled() {
        return dataContainer.isTaggingEnabled();
    }

    public void setReviewEnabled(boolean reviewEnabled) {
        dataContainer.setReviewEnabled(reviewEnabled);
    }

    public boolean isReviewEnabled() {
        return dataContainer.isReviewEnabled();
    }

    public void setCategoriesEnabled(boolean categoriesEnabled) {
        dataContainer.setCategoriesEnabled(categoriesEnabled);
    }

    public boolean isCategoriesEnabled() {
        return dataContainer.isCategoriesEnabled();
    }

    public String getBrandingUrl() {
        return dataContainer.getBrandingUrl();
    }

    public void setBrandingUrl(String brandingUrl) {
        dataContainer.setBrandingUrl(brandingUrl);
    }

    public void setCatalogEntries(List<CatalogEntry> catalogEntries) {
        this.catalogEntries = catalogEntries;
    }

    public List<CatalogEntry> getCatalogEntries() {
        return catalogEntries;
    }

    public void setMarketplaceToOrganizations(
            List<MarketplaceToOrganization> marketplaceToOrganizations) {
        this.marketplaceToOrganizations = marketplaceToOrganizations;
    }

    public List<MarketplaceToOrganization> getMarketplaceToOrganizations() {
        return marketplaceToOrganizations;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public PublicLandingpage getPublicLandingpage() {
        return publicLandingpage;
    }

    public void setPublicLandingpage(PublicLandingpage landingpage) {
        this.publicLandingpage = landingpage;
    }

    public EnterpriseLandingpage getEnterpriseLandingpage() {
        return enterpriseLandingpage;
    }

    public void setEnterpiseLandingpage(EnterpriseLandingpage landingpage) {
        this.enterpriseLandingpage = landingpage;
    }

    public void setTrackingCode(String trackingCode) {
        dataContainer.setTrackingCode(trackingCode);
    }

    public String getTrackingCode() {
        return dataContainer.getTrackingCode();
    }

    public void setRestricted(boolean restricted) {
        dataContainer.setRestricted(restricted);
    }

    public boolean isRestricted() {
        return dataContainer.isRestricted();
    }

    public List<MarketplaceAccess> getMarketplaceAccesses() {
        return marketplaceAccesses;
    }

    public void setMarketplaceAccesses(
            List<MarketplaceAccess> marketplaceAccesses) {
        this.marketplaceAccesses = marketplaceAccesses;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
