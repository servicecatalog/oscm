/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: KowalczykA                                                     
 *                                                                              
 *  Creation Date: 17.05.2016                                                                                                                                                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import org.oscm.domobjects.annotations.BusinessKey;

import javax.persistence.*;

/**
 * Defines marketplaces with restricted access and organizations which has
 * access to them.
 * 
 * @author KowalczykA
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "marketplace_tkey",
        "organization_tkey" }) )

@NamedQueries({
        @NamedQuery(name = "MarketplaceAccess.findByBusinessKey", query = "SELECT obj FROM MarketplaceAccess obj WHERE obj.marketplace_tkey"
                + " = :marketplace_tkey AND obj.organization_tkey = :organization_tkey"),
        @NamedQuery(name = "MarketplaceAccess.findByMarketplace", query = "SELECT obj FROM MarketplaceAccess obj WHERE "
                + "obj.marketplace_tkey = :marketplace_tkey"),
        @NamedQuery(name = "MarketplaceAccess.removeAllForMarketplace", query = "DELETE FROM MarketplaceAccess obj WHERE obj.marketplace_tkey = :marketplace_tkey") })
@BusinessKey(attributes = { "marketplace_tkey", "organization_tkey" })
public class MarketplaceAccess
        extends DomainObjectWithoutVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = -3270780943325084256L;

    @Column(name = "marketplace_tkey", nullable = false, insertable = false, updatable = false)
    private long marketplace_tkey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_tkey")
    private Marketplace marketplace;

    @Column(name = "organization_tkey", nullable = false, insertable = false, updatable = false)
    private long organization_tkey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_tkey")
    private Organization organization;

    public long getMarketplace_tkey() {
        return marketplace_tkey;
    }

    public void setMarketplace_tkey(long marketplace_tkey) {
        this.marketplace_tkey = marketplace_tkey;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
        if (marketplace != null) {
            setMarketplace_tkey(marketplace.getKey());
        }
    }

    public long getOrganization_tkey() {
        return organization_tkey;
    }

    public void setOrganization_tkey(long organization_tkey) {
        this.organization_tkey = organization_tkey;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            setOrganization_tkey(organization.getKey());
        }
    }

    @Override
    public boolean hasHistory() {
        return false;
    }

}
