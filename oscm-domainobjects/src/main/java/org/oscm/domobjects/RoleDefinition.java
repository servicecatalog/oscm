/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 25.06.2010                                                     
 *                                                                              
 *  Completion Time: 25.05.2010                                            
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalProduct_tkey", "roleId" }))
@NamedQueries({ @NamedQuery(name = "RoleDefinition.findByBusinessKey", query = "select c from RoleDefinition c where c.dataContainer.roleId=:roleId AND c.technicalProduct_tkey=:technicalProduct_tkey") })
@BusinessKey(attributes = { "technicalProduct_tkey", "roleId" })
public class RoleDefinition extends DomainObjectWithHistory<RoleDefinitionData> {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 7518987529559309325L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(LocalizedObjectTypes.ROLE_DEF_DESC,
                    LocalizedObjectTypes.ROLE_DEF_NAME));

    @Column(name = "technicalProduct_tkey", insertable = false, updatable = false, nullable = false)
    private long technicalProduct_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalProduct_tkey")
    private TechnicalProduct technicalProduct;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "roleDefinition", fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedProductRole> pricedRoles = new ArrayList<PricedProductRole>();

    /**
     * Default constructor.
     */
    public RoleDefinition() {
        super();
        dataContainer = new RoleDefinitionData();
    }

    public long getTechnicalProduct_tkey() {
        return technicalProduct_tkey;
    }

    public void setTechnicalProduct_tkey(long technicalProduct_tkey) {
        this.technicalProduct_tkey = technicalProduct_tkey;
    }

    /**
     * Setter for technical product.
     * 
     * @param technicalProduct
     */
    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
        if (null != technicalProduct) {
            setTechnicalProduct_tkey(technicalProduct.getKey());
        }
    }

    /**
     * Getter for technical product.
     * 
     * @return
     */
    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    /**
     * Setter for priced roles list.
     * 
     * @param pricedRoles
     *            Priced roles for the role definition.
     * 
     */
    public void setPricedRoles(List<PricedProductRole> pricedRoles) {
        this.pricedRoles = pricedRoles;
    }

    /**
     * Getter for priced roles.
     * 
     * @return Priced roles for the role definition.
     */
    public List<PricedProductRole> getPricedRoles() {
        return pricedRoles;
    }

    /**
     * Setter for role ID.
     * 
     * @param roleId
     *            Role ID.
     */
    public void setRoleId(String roleId) {
        dataContainer.setRoleId(roleId);
    }

    /**
     * Getter for role ID.
     * 
     * @return Role ID.
     */
    public String getRoleId() {
        return dataContainer.getRoleId();
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }
}
