/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * A category used for categorization of marketable services.
 * 
 * @author Mani Afschar
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "marketplaceKey",
        "categoryId" }))
@NamedQueries({
        @NamedQuery(name = "Category.findByBusinessKey", query = "SELECT c FROM Category c WHERE c.dataContainer.categoryId = :categoryId and c.marketplaceKey = :marketplaceKey"),
        @NamedQuery(name = "Category.findByMarketplaceId", query = "SELECT c FROM Category c, Marketplace mp WHERE mp.dataContainer.marketplaceId = :marketplaceId AND c.marketplace = mp ORDER BY c.key"),
        @NamedQuery(name = "Category.findByLocalizedName", query = "SELECT c FROM LocalizedResource l, Category c WHERE l.objectKey = c.key AND l.locale = :locale AND l.objectType = :objectType AND l.value = :value AND c.marketplaceKey = :marketplaceKey AND c.key <> :key"),
        @NamedQuery(name = "Category.findServices", query = "SELECT p FROM Product p, CategoryToCatalogEntry cc, CatalogEntry ce WHERE cc.category.key = :categoryKey and cc.catalogEntry = ce and ce.product = p"),
        @NamedQuery(name = "Category.findAdminsOfServices", query = "SELECT pu FROM PlatformUser pu, RoleAssignment ra, CategoryToCatalogEntry assignedCat WHERE pu.organization=assignedCat.catalogEntry.product.vendor AND assignedCat.category.key=:categoryKey AND ra.user=pu AND ra.userRole.dataContainer.roleName=org.oscm.internal.types.enumtypes.UserRoleType.ORGANIZATION_ADMIN") })
@BusinessKey(attributes = { "marketplaceKey", "categoryId" })
public class Category extends DomainObjectWithVersioning<CategoryData> {

    private static final long serialVersionUID = -2321303407897558514L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(LocalizedObjectTypes.CATEGORY_NAME));

    /**
     * The marketplace of the category.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplaceKey")
    private Marketplace marketplace;

    @Column(nullable = false, insertable = false, updatable = false)
    private long marketplaceKey;

    /**
     * References to the category to catalogEntry entries.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "category", fetch = FetchType.LAZY)
    private List<CategoryToCatalogEntry> categoryToCatalogEntry = new ArrayList<CategoryToCatalogEntry>();

    /**
     * @return the marketplaceKey
     */
    public long getMarketplaceKey() {
        return marketplaceKey;
    }

    /**
     * @param marketplaceKey
     *            the marketplaceKey to set
     */
    public void setMarketplaceKey(long marketplaceKey) {
        this.marketplaceKey = marketplaceKey;
    }

    public Category() {
        setDataContainer(new CategoryData());
    }

    /**
     * Refer to {@link CategoryData#id}
     */
    public String getCategoryId() {
        return dataContainer.getCategoryId();
    }

    /**
     * Refer to {@link CategoryData#id}
     */
    public void setCategoryId(String categoryId) {
        dataContainer.setCategoryId(categoryId);
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        if (marketplace != null) {
            marketplaceKey = marketplace.getKey();
        }
        this.marketplace = marketplace;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public void setCategoryToCatalogEntry(
            List<CategoryToCatalogEntry> categoryToCatalogEntry) {
        this.categoryToCatalogEntry = categoryToCatalogEntry;
    }

    public List<CategoryToCatalogEntry> getCategoryToCatalogEntry() {
        return categoryToCatalogEntry;
    }

}
