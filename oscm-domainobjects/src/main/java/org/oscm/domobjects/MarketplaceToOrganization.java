/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 12.09.2011                                                      
 *                                                                              
 *  Completion Time: 12.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.PublishingAccess;

/**
 * The relation between a {@link Marketplace} and an {@link Organization}. If
 * the relation exists with publishing access set to granted, the organization
 * can publish services on the marketplace. If the relation exists with
 * publishing access set to banned, the organization must not publish services
 * on the marketplace. In addition: For open marketplaces, an organization is
 * allowed to publish also if no such relation exists at all
 * 
 * @author weiser
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "marketplace_tkey", "organization_tkey" }))
@NamedQueries({
        @NamedQuery(name = "MarketplaceToOrganization.findByBusinessKey", query = "SELECT c FROM MarketplaceToOrganization c WHERE c.marketplace_tkey=:marketplace_tkey AND c.organization_tkey=:organization_tkey"),
        @NamedQuery(name = "MarketplaceToOrganization.findMarketplacesForOrgByPublishingAccess", query = "SELECT c FROM MarketplaceToOrganization c, Marketplace m WHERE c.organization_tkey=:organization_tkey AND c.marketplace_tkey = m.key AND m.dataContainer.open=:isOpen AND c.dataContainer.publishingAccess=:publishingAccess"),
        @NamedQuery(name = "MarketplaceToOrganization.findSuppliersForMpByPublishingAccess", query = "SELECT c FROM MarketplaceToOrganization c WHERE c.marketplace_tkey=:marketplace_tkey AND c.dataContainer.publishingAccess=:publishingAccess") })
@BusinessKey(attributes = { "marketplace_tkey", "organization_tkey" })
public class MarketplaceToOrganization extends
        DomainObjectWithVersioning<MarketplaceToOrganizationData> {

    private static final long serialVersionUID = 6476060292996834053L;

    @Column(name = "marketplace_tkey", insertable = false, updatable = false, nullable = false)
    private long marketplace_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_tkey")
    private Marketplace marketplace;

    @Column(name = "organization_tkey", insertable = false, updatable = false, nullable = false)
    private long organization_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_tkey")
    private Organization organization;

    protected MarketplaceToOrganization() {
        dataContainer = new MarketplaceToOrganizationData();
    }

    public MarketplaceToOrganization(Marketplace marketplace,
            Organization organization) {
        this();
        setMarketplace(marketplace);
        setOrganization(organization);
        setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_GRANTED);
    }

    public MarketplaceToOrganization(Marketplace marketplace,
            Organization organization, PublishingAccess publishingAccess) {
        this();
        setMarketplace(marketplace);
        setOrganization(organization);
        setPublishingAccess(publishingAccess);
    }

    public void setPublishingAccess(PublishingAccess publishingAccess) {
        dataContainer.setPublishingAccess(publishingAccess);
    }

    public PublishingAccess getPublishingAccess() {
        return dataContainer.getPublishingAccess();
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            setOrganization_tkey(organization.getKey());
        }
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
        if (marketplace != null) {
            setMarketplace_tkey(marketplace.getKey());
        }
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace_tkey(long marketplace_tkey) {
        this.marketplace_tkey = marketplace_tkey;
    }

    public long getMarketplace_tkey() {
        return marketplace_tkey;
    }

    public void setOrganization_tkey(long organization_tkey) {
        this.organization_tkey = organization_tkey;
    }

    public long getOrganization_tkey() {
        return organization_tkey;
    }

}
