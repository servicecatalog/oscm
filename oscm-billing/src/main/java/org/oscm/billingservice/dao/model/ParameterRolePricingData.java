/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                   
 *                                                                              
 *  Creation Date: 08.07.2010                                                      
 *                                                                              
 *  Completion Time: 08.07.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

/**
 * Data object to represent the billing relevant information for a parameter and
 * a parameter option specific for a role.
 * 
 * @author Aleh Khomich.
 * 
 */
public class ParameterRolePricingData extends ParameterPricingData {

    /** Role id */
    private String roleId;

    /**
     * Setter for role id.
     * 
     * @param roleId
     *            role id.
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Getter for role id.
     * 
     * @return role id.
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Method for printing information about object in debug.
     */
    public String toString() {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("Identifier: " + getIdentifier());
        strBuf.append("\n");
        strBuf.append("BasePricePeriod: " + getBasePricePeriod());
        strBuf.append("\n");
        strBuf.append("BasePriceUser: " + getBasePriceUser());
        strBuf.append("\n");
        strBuf.append("FactorForPeriod: " + getFactorForPeriod());
        strBuf.append("\n");
        strBuf.append("FactorForUsers: " + getFactorForUsers());
        strBuf.append("\n");
        strBuf.append("Value: " + getValue());
        strBuf.append("\n");
        strBuf.append("Type: " + getType());
        strBuf.append("\n");
        strBuf.append("Period: " + getPeriod());
        strBuf.append("\n");
        strBuf.append("OptionId: " + getOptionId());
        strBuf.append("\n");
        strBuf.append("roleId: " + getRoleId());

        return strBuf.toString();
    }
}
