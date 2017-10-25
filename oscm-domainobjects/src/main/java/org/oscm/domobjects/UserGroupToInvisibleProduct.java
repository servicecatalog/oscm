/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-24                                                      
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
 * This object defines the relationship between user group and invisible
 * product.
 * 
 * @author Fang
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "usergroup_tkey",
        "product_tkey" }))
@NamedQueries({ @NamedQuery(name = "UserGroupToInvisibleProduct.findByBusinessKey", query = "SELECT obj FROM UserGroupToInvisibleProduct obj WHERE obj.usergroup_tkey = :usergroup_tkey AND obj.product_tkey = :product_tkey ORDER BY obj.usergroup_tkey ASC") })
@BusinessKey(attributes = { "usergroup_tkey", "product_tkey" })
public class UserGroupToInvisibleProduct extends
        DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = -6790851399980883130L;

    @Column(name = "usergroup_tkey", nullable = false, insertable = false, updatable = false)
    private long usergroup_tkey;

    @Column(name = "product_tkey", nullable = false, insertable = false, updatable = false)
    private long product_tkey;

    public UserGroupToInvisibleProduct() {
        super();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergroup_tkey")
    private UserGroup userGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_tkey")
    private Product product;

    private boolean forallusers;

    public long getUsergroup_tkey() {
        return usergroup_tkey;
    }

    public void setUsergroup_tkey(long usergroup_tkey) {
        this.usergroup_tkey = usergroup_tkey;
    }

    public long getProduct_tkey() {
        return product_tkey;
    }

    public void setProduct_tkey(long product_tkey) {
        this.product_tkey = product_tkey;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
        if (userGroup != null) {
            setUsergroup_tkey(userGroup.getKey());
        }
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

    public boolean isForallusers() {
        return forallusers;
    }

    public void setForallusers(boolean forallusers) {
        this.forallusers = forallusers;
    }

}
