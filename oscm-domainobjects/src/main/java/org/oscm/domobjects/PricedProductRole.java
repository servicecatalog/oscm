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

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class PricedProductRole extends
        DomainObjectWithHistory<PricedProductRoleData> {

    private static final long serialVersionUID = -82261850877087370L;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PriceModel priceModel;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PricedParameter pricedParameter;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PricedOption pricedOption;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private RoleDefinition roleDefinition;

    /**
     * Default constructor.
     */
    public PricedProductRole() {
        super();
        dataContainer = new PricedProductRoleData();
    }

    /**
     * Setter for price model.
     * 
     * @param priceModel
     */
    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    /**
     * Getter for price model.
     * 
     * @return
     */
    public PriceModel getPriceModel() {
        return priceModel;
    }

    /**
     * Setter for role definition.
     * 
     * @param roleDefinition
     */
    public void setRoleDefinition(RoleDefinition roleDefinition) {
        this.roleDefinition = roleDefinition;
    }

    /**
     * Getter for role definition.
     * 
     * @return
     */
    public RoleDefinition getRoleDefinition() {
        return roleDefinition;
    }

    /**
     * Setter for price.
     * 
     * @param pricePerUser
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        dataContainer.setPricePerUser(pricePerUser);
    }

    /**
     * Getter for price.
     * 
     * @return
     */
    public BigDecimal getPricePerUser() {
        return dataContainer.getPricePerUser();
    }

    /**
     * Returns the priced parameter this role related price refers to,
     * <code>null</code> if none is set.
     * 
     * @return The priced parameter.
     */
    public PricedParameter getPricedParameter() {
        return pricedParameter;
    }

    /**
     * Returns the priced option this role related price refers to,
     * <code>null</code> if none is set.
     * 
     * @return The priced option.
     */
    public PricedOption getPricedOption() {
        return pricedOption;
    }

    /**
     * Sets the priced parameter reference to the parameter this price refers
     * to.
     * 
     * @param pricedParameter
     *            The referenced priced parameter.
     */
    public void setPricedParameter(PricedParameter pricedParameter) {
        this.pricedParameter = pricedParameter;
    }

    /**
     * Sets the priced option reference to the option this price refers to.
     * 
     * @param pricedParameter
     *            The referenced priced option.
     */
    public void setPricedOption(PricedOption pricedOption) {
        this.pricedOption = pricedOption;
    }

    /**
     * Creates a copy of the current object.
     * 
     * @param pm
     *            The price model the copy should be assigned to.
     * 
     * @return A copy.
     */
    public PricedProductRole copy(PriceModel pm) {
        return copy(pm, null, null);
    }

    /**
     * Creates a copy of the current object.
     * 
     * @param pp
     *            The priced parameter the copy should be assigned to.
     * 
     * @return A copy.
     */
    public PricedProductRole copy(PricedParameter pp) {
        return copy(null, pp, null);
    }

    /**
     * Creates a copy of the current object.
     * 
     * @param pm
     *            The priced option the copy should be assigned to.
     * 
     * @return A copy.
     */
    public PricedProductRole copy(PricedOption po) {
        return copy(null, null, po);
    }

    /**
     * Copies the current priced product role object.
     * 
     * @param pm
     *            The price model to be set.
     * @param pp
     *            The priced parameter to be set.
     * @param po
     *            The priced option to be set.
     * @return The copy.
     */
    private PricedProductRole copy(PriceModel pm, PricedParameter pp,
            PricedOption po) {
        PricedProductRole copy = new PricedProductRole();
        copy.setPricePerUser(getPricePerUser());
        copy.setRoleDefinition(getRoleDefinition());
        copy.setPriceModel(pm);
        copy.setPricedParameter(pp);
        copy.setPricedOption(po);
        return copy;
    }
}
