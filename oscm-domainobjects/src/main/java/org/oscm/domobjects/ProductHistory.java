/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of Product, used for auditing. Will be automatically created
 * during persist, save or remove operations (if performed via DataManager)
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ProductHistory.findByObject", query = "select c from ProductHistory c where c.objKey=:objKey order by objversion, modDate"),
        @NamedQuery(name = "ProductHistory.findByObjectDateAndModTypeDesc", query = "select c from ProductHistory c where c.objKey=:objKey and c.modDate<:endDate AND NOT c.modType IN (:modTypes) order by objversion DESC, modDate DESC"),
        @NamedQuery(name = "ProductHistory.findProductOfVendor", query = "SELECT templ FROM ProductHistory templ, ProductHistory p, SubscriptionHistory s WHERE s.objKey=:subscriptionObjKey AND p.objKey=s.productObjKey AND templ.objKey=p.templateObjKey AND p.priceModelObjKey=:pmKey AND templ.modDate < :modDate AND templ.objVersion = (SELECT MAX(tmp.objVersion) FROM ProductHistory tmp WHERE tmp.objKey=templ.objKey and tmp.modDate < :modDate)") })
public class ProductHistory extends DomainHistoryObject<ProductData> {

    private static final long serialVersionUID = 1L;

    public ProductHistory() {
        dataContainer = new ProductData();
    }

    /**
     * Constructs ProductHistory from a Product domain object
     * 
     * @param c
     *            - the product
     */
    public ProductHistory(Product c) {
        super(c);
        if (c.getVendor() != null) {
            setVendorObjKey(c.getVendor().getKey());
        }
        if (c.getPriceModel() != null) {
            setPriceModelObjKey(Long.valueOf(c.getPriceModel().getKey()));
        }
        if (c.getTechnicalProduct() != null) {
            setTechnicalProductObjKey(c.getTechnicalProduct().getKey());
        }
        if (c.getParameterSet() != null) {
            setParameterSetObjKey(Long.valueOf(c.getParameterSet().getKey()));
        }
        if (c.getTemplate() != null) {
            setTemplateObjKey(Long.valueOf(c.getTemplate().getKey()));
        }
        if (c.getTargetCustomer() != null) {
            setTargetCustomerObjKey(Long
                    .valueOf(c.getTargetCustomer().getKey()));
        }
    }

    /**
     * Reference to the Organization (only id)
     */
    private long vendorObjKey;

    /**
     * Reference to the original PriceModel
     */
    private Long priceModelObjKey;

    /**
     * Reference to the original ParameterSet
     */
    private Long parameterSetObjKey;

    /**
     * Reference to corresponding technical product.
     */
    @Column(nullable = false)
    private long technicalProductObjKey;

    /**
     * Reference to corresponding template product (id only).
     */
    private Long templateObjKey;

    /**
     * Reference to the corresponding target customer (id only).
     */
    private Long targetCustomerObjKey;

    public void setVendorObjKey(long vendor_objid) {
        this.vendorObjKey = vendor_objid;
    }

    public long getVendorObjKey() {
        return vendorObjKey;
    }

    public void setPriceModelObjKey(Long priceModelObjid) {
        this.priceModelObjKey = priceModelObjid;
    }

    public Long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    public long getTechnicalProductObjKey() {
        return technicalProductObjKey;
    }

    public void setTechnicalProductObjKey(long technicalProductObjKey) {
        this.technicalProductObjKey = technicalProductObjKey;
    }

    public void setParameterSetObjKey(Long parameterSetObjKey) {
        this.parameterSetObjKey = parameterSetObjKey;
    }

    public Long getTemplateObjKey() {
        return templateObjKey;
    }

    public void setTemplateObjKey(Long templateObjKey) {
        this.templateObjKey = templateObjKey;
    }

    public Long getParameterSetObjKey() {
        return parameterSetObjKey;
    }

    public Long getTargetCustomerObjKey() {
        return targetCustomerObjKey;
    }

    public void setTargetCustomerObjKey(Long targetCustomerObjKey) {
        this.targetCustomerObjKey = targetCustomerObjKey;
    }

    public String getCleanProductId() {
        return dataContainer.getCleanProductId();
    }
}
