/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.math.BigDecimal;

/**
 * Represents the information on a service role defined for a technical service
 * that is relevant for price models.
 * 
 */
public class VOPricedRole extends BaseVO {

    private static final long serialVersionUID = -3868435676681763178L;

    /**
     * The role-specific price for users.
     */
    private BigDecimal pricePerUser = BigDecimal.ZERO;

    /**
     * The service role the price is defined for.
     */
    private VORoleDefinition role;

    /**
     * Retrieves the price for users with the service role.
     * 
     * @return the price for users
     */
    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    /**
     * Retrieves the service role for which a price is defined.
     * 
     * @return the role definition
     */
    public VORoleDefinition getRole() {
        return role;
    }

    /**
     * Sets the price for users with the service role.
     * 
     * @param pricePerUser
     *            the price for users
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    /**
     * Sets the service role for which a price is defined.
     * 
     * @param role
     *            the role definition
     */
    public void setRole(VORoleDefinition role) {
        this.role = role;
    }

}
