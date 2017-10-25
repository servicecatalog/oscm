/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-12-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the information on parameter options that is relevant for price
 * models.
 * 
 */
public class VOPricedOption extends BaseVO {

    private static final long serialVersionUID = -3492198807771584164L;

    private BigDecimal pricePerUser = BigDecimal.ZERO;

    private BigDecimal pricePerSubscription = BigDecimal.ZERO;

    private long parameterOptionKey;

    private String optionId;

    /**
     * The additional prices that have to be paid for users with a certain role.
     */
    private List<VOPricedRole> roleSpecificUserPrices = new ArrayList<VOPricedRole>();

    /**
     * Default constructor.
     */
    public VOPricedOption() {

    }

    /**
     * Retrieves the price per user defined for the parameter option.
     * 
     * @return the price per user
     */
    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    /**
     * Sets the price per user for the parameter option.
     * 
     * @param pricePerUser
     *            the price per user
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    /**
     * Retrieves the price per subscription defined for the parameter option.
     * 
     * @return the price per subscription
     */
    public BigDecimal getPricePerSubscription() {
        return pricePerSubscription;
    }

    /**
     * Sets the price per subscription for the parameter option.
     * 
     * @param pricePerSubscription
     *            the price per subscription
     */
    public void setPricePerSubscription(BigDecimal pricePerSubscription) {
        this.pricePerSubscription = pricePerSubscription;
    }

    /**
     * Retrieves the numeric key of the parameter option.
     * 
     * @return the key
     */
    public long getParameterOptionKey() {
        return parameterOptionKey;
    }

    /**
     * Sets the numeric key of the parameter option.
     * 
     * @param parameterOptionKey
     *            the key
     */
    public void setParameterOptionKey(long parameterOptionKey) {
        this.parameterOptionKey = parameterOptionKey;
    }

    /**
     * Retrieves the role-specific prices that have to be paid in addition to
     * the price per user for the parameter option.
     * 
     * @return the role-specific prices
     */
    public List<VOPricedRole> getRoleSpecificUserPrices() {
        return roleSpecificUserPrices;
    }

    /**
     * Sets the role-specific prices that have to be paid in addition to the
     * price per user for the parameter option.
     * 
     * @param roleSpecificUserPrices
     *            the role-specific prices
     */
    public void setRoleSpecificUserPrices(
            List<VOPricedRole> roleSpecificUserPrices) {
        this.roleSpecificUserPrices = roleSpecificUserPrices;
    }

    /**
     * Retrieves the identifier of the parameter option.
     * 
     * @return the identifier
     */
    public String getOptionId() {
        return optionId;
    }

    /**
     * Sets the identifier of the parameter option.
     * 
     * @param optionId
     *            the identifier
     */
    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }
}
