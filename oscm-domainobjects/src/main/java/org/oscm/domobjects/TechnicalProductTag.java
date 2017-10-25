/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Relation entity for saving the tags of a technical service.
 * 
 * @author soehnges
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "TECHNICALPRODUCT_TKEY", "TAG_TKEY" }))
@NamedQueries({
        @NamedQuery(name = "TechnicalProductTag.getAllTagsGreaterMinScore", query = ""
                + "SELECT obj.tag.dataContainer.value, "
                + "       COUNT(*)"
                + "FROM TechnicalProductTag obj "
                + "WHERE obj.tag.dataContainer.locale = :locale "
                + "GROUP BY obj.tag.dataContainer.value "
                + "HAVING COUNT(*) >= :tagMinScore "
                + "ORDER BY COUNT(*) DESC "),
        @NamedQuery(name = "TechnicalProductTag.getAllVisibleTagsGreaterMinScore", query = ""
                + "SELECT obj.tag.dataContainer.value, COUNT(*) "
                + "FROM TechnicalProductTag obj "
                + "INNER JOIN obj.technicalProduct.products p, CatalogEntry ce, Marketplace mp "
                + "WHERE ce.product = p"
                + "  AND ce.marketplace = mp"
                + "  AND mp.dataContainer.marketplaceId= :marketplaceId "
                + "  AND ce.dataContainer.visibleInCatalog = TRUE "
                + "  AND p.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE "
                + "  AND obj.tag.dataContainer.locale = :locale "
                + "GROUP BY obj.tag.dataContainer.value "
                + "HAVING COUNT(*) >= :tagMinScore "
                + "ORDER BY COUNT(*) DESC "), })
public class TechnicalProductTag extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = 1638439818204043251L;

    /**
     * The technical product the tag has been defined for
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TechnicalProduct technicalProduct;

    /**
     * The defined tag itself
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Tag tag;

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
