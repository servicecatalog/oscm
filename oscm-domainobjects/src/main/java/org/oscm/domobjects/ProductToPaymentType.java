/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                     
 *                                                                              
 *  Creation Date: 07.10.2011                                                      
 *                                                                              
 *  Completion Time: 07.10.2011                                              
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

/**
 * The relation between a {@link Product} and a {@link PaymentType} defines the
 * allowed payment types per marketable service.
 * 
 * @author brandstetter
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "product_tkey",
        "paymenttype_tkey" }))
@NamedQueries({ @NamedQuery(name = "ProductToPaymentType.findByBusinessKey", query = "SELECT c FROM ProductToPaymentType c WHERE c.product_tkey=:product_tkey AND c.paymenttype_tkey=:paymenttype_tkey") })
@BusinessKey(attributes = { "product_tkey", "paymenttype_tkey" })
public class ProductToPaymentType extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 3625206280823720826L;

    @Column(name = "product_tkey", insertable = false, updatable = false, nullable = false)
    private long product_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_tkey")
    private Product product;

    @Column(name = "paymenttype_tkey", insertable = false, updatable = false, nullable = false)
    private long paymenttype_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paymenttype_tkey")
    private PaymentType paymentType;

    public ProductToPaymentType() {
        super();
    }

    public ProductToPaymentType(Product product, PaymentType paymentType) {
        super();
        setProduct(product);
        setPaymentType(paymentType);
    }

    public long getProduct_tkey() {
        return product_tkey;
    }

    public void setProduct_tkey(long product_tkey) {
        this.product_tkey = product_tkey;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;

        if (product != null) {
            setProduct_tkey(product.getKey());
        }
    }

    public long getPaymenttype_tkey() {
        return paymenttype_tkey;
    }

    public void setPaymenttype_tkey(long paymenttype_tkey) {
        this.paymenttype_tkey = paymenttype_tkey;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;

        if (paymentType != null) {
            setPaymenttype_tkey(paymentType.getKey());
        }
    }
}
