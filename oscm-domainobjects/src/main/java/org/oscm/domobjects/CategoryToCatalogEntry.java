/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The class <code>CategoryToCatalogEntry</code> stores a category entry and the
 * catalogEntry entry this category is assigned to.
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "category_tkey",
        "catalogentry_tkey" }))
public class CategoryToCatalogEntry extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 5336379997821816252L;

    /**
     * The catalogEntry entry this category is assigned to
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CatalogEntry catalogEntry;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Category category;

    /**
     * Default constructor
     */
    public CategoryToCatalogEntry() {
        super();
    }

    /**
     * Setter for catalogEntry entry.
     * 
     * @param catalogEntry
     */
    public void setCatalogEntry(CatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry;
    }

    /**
     * Getter for catalogEntry entry.
     * 
     * @return CatalogEntry.
     */
    public CatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

}
