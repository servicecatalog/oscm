/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Considering a subscription, it is possible that a user wants to migrate from
 * the used product to another marketing product (e.g. allowing more users). The
 * basic information on which products are compatible one to another is a many
 * to many relation of the type Product to itself. The default JPA managed
 * approach creates a join table, but does not take care of historization of the
 * objects. But historization is essential for our purposes, so this entity is
 * explicitly modelling the mentioned many to many relation. There is another
 * advantage in doing so: we can enhance the relation by additional information
 * in the future.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ProductReference.deleteBySourceProduct", query = "DELETE FROM ProductReference obj WHERE obj.sourceProduct=:sourceProduct"),
        @NamedQuery(name = "ProductReference.getTargetKeysForProduct", query = "SELECT ref.targetProduct.key FROM ProductReference ref WHERE ref.sourceProduct=:product") })
public class ProductReference extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = -2559482256679921063L;

    /**
     * Refers to the source product of the reference, actually the product that
     * is referencing to another one.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product sourceProduct;

    /**
     * Refers to the target product of the reference, actually the product that
     * is referenced by the source product.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product targetProduct;

    protected ProductReference() {

    }

    /**
     * Creates a reference from the source product to the target product. As
     * products are only compatible in case they belong to the same technical
     * product, this constructor will throw an {@link IllegalArgumentException}
     * if the technical products for both of the parameters is not the same.
     * 
     * @param sourceProduct
     *            The source product referring to another product.
     * @param targetProduct
     *            The target product referred to by the source product.
     */
    public ProductReference(Product sourceProduct, Product targetProduct) {
        if (sourceProduct.getTechnicalProduct().getKey() != targetProduct
                .getTechnicalProduct().getKey()) {
            throw new IllegalArgumentException(
                    "The products do not refer to the same technical product, no reference can be created.");
        }
        this.sourceProduct = sourceProduct;
        this.targetProduct = targetProduct;
    }

    public Product getSourceProduct() {
        return sourceProduct;
    }

    public Product getTargetProduct() {
        return targetProduct;
    }

}
