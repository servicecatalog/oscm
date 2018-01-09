/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: 26.01.2011                                                      
 *                                                                              
 *  Completion Time: 27.01.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * The class <code>CatalogEntry</code> defines the appearance of a product in
 * the catalog.
 * 
 * @author Dirk Bernsau
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "CatalogEntry.findByService", query = "SELECT ce FROM CatalogEntry ce WHERE ce.product=:service"),
        @NamedQuery(name = "CatalogEntry.findByTechnicalService", query = "SELECT ce FROM CatalogEntry ce WHERE ce.product.technicalProduct.key=:technicalProductKey"),
        @NamedQuery(name = "CatalogEntry.countActiveServices", query = "SELECT COUNT(*) FROM CatalogEntry ce WHERE ce.marketplace=:marketplace AND (ce.product.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE OR EXISTS(SELECT p FROM Product p WHERE p.template = ce.product AND p.targetCustomer IS NOT NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p)))"),
        @NamedQuery(name = "CatalogEntry.countActiveServicesByVendor", query = "SELECT COUNT(*) FROM CatalogEntry ce WHERE ce.marketplace=:marketplace AND ce.product.vendor=:vendor AND (ce.product.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE OR EXISTS(SELECT p FROM Product p WHERE p.template = ce.product AND p.targetCustomer IS NOT NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p)))") })
public class CatalogEntry extends DomainObjectWithHistory<CatalogEntryData> {

    private static final long serialVersionUID = -2892684970632621444L;

    public CatalogEntry() {
        super();
        dataContainer = new CatalogEntryData();
    }

    /**
     * Reference to the product for which this catalog entry defines the
     * appearance in the catalog.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    /**
     * The context of this catalog entry (so where is the product listed).
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Marketplace marketplace;

    /**
     * References to the category to catalogEntry entries.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "catalogEntry", fetch = FetchType.LAZY)
    private List<CategoryToCatalogEntry> categoryToCatalogEntry = new ArrayList<CategoryToCatalogEntry>();

    @OneToOne(optional = true, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel brokerPriceModel;

    @OneToOne(optional = true, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel resellerPriceModel;

    @OneToOne(optional = true, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel operatorPriceModel;

    /**
     * Sets the associated product this catalog entry is presenting.
     * 
     * @param product
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns the associated product this catalog entry is presenting.
     * 
     * @return Product
     */
    public Product getProduct() {
        return product;
    }

    public void setAnonymousVisible(boolean anonymousVisible) {
        dataContainer.setAnonymousVisible(anonymousVisible);
    }

    public boolean isAnonymousVisible() {
        return dataContainer.isAnonymousVisible();
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    /**
     * Checks if the entry is visible in the catalog.
     * 
     * @return <code>true</code> if the entry is visible in the catalog,
     *         otherwise <code>false</code>.
     */
    public boolean isVisibleInCatalog() {
        return dataContainer.isVisibleInCatalog();
    }

    /**
     * Sets the visibility of the entry in the catalog.
     * 
     * @param catalogVisibility
     *            <code>true</code> if the entry should be visible in the
     *            catalog, otherwise <code>false</code>.
     */
    public void setVisibleInCatalog(boolean visibleInCatalog) {
        dataContainer.setVisibleInCatalog(visibleInCatalog);
    }

    public void setCategoryToCatalogEntry(
            List<CategoryToCatalogEntry> categoryToCatalogEntry) {
        this.categoryToCatalogEntry = categoryToCatalogEntry;
    }

    public List<CategoryToCatalogEntry> getCategoryToCatalogEntry() {
        return categoryToCatalogEntry;
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

    public RevenueShareModel getOperatorPriceModel() {
        return operatorPriceModel;
    }

    public void setOperatorPriceModel(RevenueShareModel operatorPriceModel) {
        this.operatorPriceModel = operatorPriceModel;
    }

    /**
     * Adds the given {@link Category} to the this {@link CatalogEntry} by
     * creating the {@link CategoryToCatalogEntry} that is returned. It will not
     * be checked if the {@link Category} has already been added.
     * 
     * @param c
     *            the {@link Category} to add
     * @return the created {@link CategoryToCatalogEntry}
     */
    public CategoryToCatalogEntry addCategory(Category c) {
        CategoryToCatalogEntry entry = new CategoryToCatalogEntry();
        entry.setCatalogEntry(this);
        entry.setCategory(c);
        getCategoryToCatalogEntry().add(entry);
        return entry;
    }
}
