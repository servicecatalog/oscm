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

import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Represents the information on service parameters that is relevant for price
 * models.
 * 
 */
public class VOPricedParameter extends BaseVO  {

    private static final long serialVersionUID = 7663675959767010704L;

    private VOParameterDefinition voParameterDef;

    private BigDecimal pricePerUser = BigDecimal.ZERO;

    private BigDecimal pricePerSubscription = BigDecimal.ZERO;

    private long parameterKey;

    private List<VOPricedOption> pricedOptions = new ArrayList<VOPricedOption>();

    /**
     * The additional prices that have to be paid for users with a certain role.
     */
    private List<VOPricedRole> roleSpecificUserPrices = new ArrayList<VOPricedRole>();

    /**
     * The stepped prices that have to be paid for different parameter values.
     * Only for numeric parameters ({@link ParameterValueType#INTEGER
     * ParameterValueType#LONG ParameterValueType#DURATION}).
     */
    private List<VOSteppedPrice> steppedPrices = new ArrayList<VOSteppedPrice>();

    /**
     * Default constructor.
     */
    public VOPricedParameter() {

    }

    /**
     * Constructs a priced parameter object based on the specified parameter
     * definition.
     * 
     * @param voParameterDef
     *            the parameter definition
     */
    public VOPricedParameter(VOParameterDefinition voParameterDef) {
        this.voParameterDef = voParameterDef;
    }

    /**
     * Retrieves the price per user defined for the parameter.
     * 
     * @return the price per user
     */
    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    /**
     * Sets the price per user for the parameter.
     * 
     * @param pricePerUser
     *            the price per user
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    /**
     * Retrieves the price per subscription defined for the parameter.
     * 
     * @return the price per subscription
     */
    public BigDecimal getPricePerSubscription() {
        return pricePerSubscription;
    }

    /**
     * Sets the price per subscription for the parameter.
     * 
     * @param pricePerSubscription
     *            the price per subscription
     */
    public void setPricePerSubscription(BigDecimal pricePerSubscription) {
        this.pricePerSubscription = pricePerSubscription;
    }

    /**
     * Retrieves the numeric key of the parameter.
     * 
     * @return the key
     */
    public long getParameterKey() {
        return parameterKey;
    }

    /**
     * Sets the numeric key of the parameter.
     * 
     * @param parameterKey
     *            the key
     */
    public void setParameterKey(long parameterKey) {
        this.parameterKey = parameterKey;
    }

    /**
     * Retrieves the definition of the parameter.
     * 
     * @return the parameter definition
     */
    public VOParameterDefinition getVoParameterDef() {
        return voParameterDef;
    }

    /**
     * Sets the definition of the parameter.
     * 
     * @param voParameterDef
     *            the parameter definition
     */
    public void setVoParameterDef(VOParameterDefinition voParameterDef) {
        this.voParameterDef = voParameterDef;
    }

    /**
     * Retrieves the priced options defined for the parameter.
     * 
     * @return the list of priced options
     */
    public List<VOPricedOption> getPricedOptions() {
        return pricedOptions;
    }

    /**
     * Sets the priced options for the parameter.
     * 
     * @param pricedOptions
     *            the list of priced options
     */
    public void setPricedOptions(List<VOPricedOption> pricedOptions) {
        this.pricedOptions = pricedOptions;
    }

    /**
     * Retrieves the role-specific prices that have to be paid in addition to
     * the price per user for the parameter.
     * 
     * @return the role-specific prices
     */
    public List<VOPricedRole> getRoleSpecificUserPrices() {
        return roleSpecificUserPrices;
    }

    /**
     * Sets the role-specific prices that have to be paid in addition to the
     * price per user for the parameter.
     * 
     * @param roleSpecificUserPrices
     *            the role-specific prices
     */
    public void setRoleSpecificUserPrices(
            List<VOPricedRole> roleSpecificUserPrices) {
        this.roleSpecificUserPrices = roleSpecificUserPrices;
    }

    /**
     * Sets the price steps for the parameter if different prices are to be
     * applied depending on the parameter value. Stepped pricing is available
     * for numeric parameters only {@link ParameterValueType#INTEGER
     * ParameterValueType#LONG ParameterValueType#DURATION}.
     * 
     * @param steppedPrices
     *            the price steps
     */
    public void setSteppedPrices(List<VOSteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    /**
     * Retrieves the price steps for the parameter if different prices are
     * applied depending on the parameter value. Stepped pricing is available
     * for numeric parameters only {@link ParameterValueType#INTEGER
     * ParameterValueType#LONG ParameterValueType#DURATION}.
     * 
     * @return the price steps
     */
    public List<VOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }
}
