/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.oscm.converter.PriceConverter;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * Data object to represent the billing relevant information for a parameter and
 * a parameter option.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterPricingData {

    /** PricedParameter key TKEY */
    private long key;

    /** PricedOption key TKEY */
    private long pricedOptionKey;

    /** Additional parameter costs for roles */
    private BigDecimal totalParameterRolesCosts;
    /**
     * Base price for period of the parameter or option, when parameter is
     * ENUMERATION type.
     */
    private BigDecimal basePricePeriod;
    /**
     * Total price for period of the parameter or option, when parameter is
     * ENUMERATION type.
     */
    private BigDecimal totalPricePeriod;
    /**
     * Base price for user of the parameter or option, when parameter is
     * ENUMERATION type.
     */
    private BigDecimal basePriceUser;
    /**
     * Total price for period of the parameter or option, when parameter is
     * ENUMERATION type.
     */
    private BigDecimal totalPriceUser;
    /**
     * Total cost of the parameter.
     */
    private BigDecimal totalCosts;
    /**
     * Multiply factor for period price.
     */
    private double factorForPeriod;
    /**
     * Multiply factor for user price.
     */
    private double factorForUsers;
    /**
     * Parameter identifier.
     */
    private String identifier;
    /**
     * Parameter value, or option id for ENUMERATION parameter.
     */
    private String value;
    /**
     * Multiply factor for value. Used only for number types.
     */
    private double factorValue;
    /**
     * Parameter type.
     */
    private ParameterValueType type;
    /**
     * Pricing period.
     */
    private PricingPeriod period;
    /**
     * Option ID.
     */
    private String optionId;

    /** Stepped price list for INTEGER and LONG parameters */
    private List<SteppedPriceData> steppedPriceList;

    public BigDecimal getBasePricePeriod() {
        if (basePricePeriod == null) {
            return BigDecimal.ZERO;
        }
        return basePricePeriod;
    }

    public BigDecimal getBasePriceUser() {
        if (basePriceUser == null) {
            return BigDecimal.ZERO;
        }
        return basePriceUser;
    }

    public double getFactorForPeriod() {
        return factorForPeriod;
    }

    public double getFactorForUsers() {
        return factorForUsers;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getValue() {
        return value;
    }

    public ParameterValueType getType() {
        return type;
    }

    public PricingPeriod getPeriod() {
        return period;
    }

    public String getOptionId() {
        return optionId;
    }

    /**
     * Sets the period base price. Depending on the value type, this is either
     * the price for the current option or the price for the parameter itself.
     * 
     * @param basePricePeriod
     *            The period base price.
     */
    public void setBasePricePeriod(BigDecimal basePricePeriod) {
        this.basePricePeriod = basePricePeriod;
    }

    /**
     * Sets the user base price. Depending on the value type, this is either the
     * price for the current option or the price for the parameter itself.
     * 
     * @param basePriceUser
     *            The user base price.
     */
    public void setBasePriceUser(BigDecimal basePriceUser) {
        this.basePriceUser = basePriceUser;
    }

    public void setFactorForPeriod(double factorForPeriod) {
        this.factorForPeriod = factorForPeriod;
    }

    public void setFactorForUsers(double factorForUsers) {
        this.factorForUsers = factorForUsers;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Only for representation in XML, not cost calculation relevant. Ought to
     * be <code>null</code> in case the type is
     * {@link ParameterValueType#ENUMERATION}.
     * 
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    public void setType(ParameterValueType type) {
        this.type = type;
    }

    public void setPeriod(PricingPeriod period) {
        this.period = period;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public double getFactorValue() {
        return factorValue;
    }

    public void setFactorValue(double factorValue) {
        this.factorValue = factorValue;
    }

    public void setTotalPricePeriod(BigDecimal totalPricePeriod) {
        this.totalPricePeriod = totalPricePeriod;
    }

    public BigDecimal getTotalPricePeriod() {
        if (totalPricePeriod == null) {
            return BigDecimal.ZERO;
        }
        return totalPricePeriod;
    }

    public void setTotalPriceUser(BigDecimal totalPriceUser) {
        this.totalPriceUser = totalPriceUser;
    }

    public BigDecimal getTotalPriceUser() {
        if (totalPriceUser == null) {
            return BigDecimal.ZERO;
        }
        return totalPriceUser;
    }

    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getTotalCosts() {
        if (totalCosts == null) {
            return BigDecimal.ZERO;
        }
        return totalCosts;
    }

    public BigDecimal getNormalizedTotalCosts() {
        if (totalCosts == null) {
            return BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        }
        return totalCosts.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                RoundingMode.HALF_UP);
    }

    public void setSteppedPriceList(List<SteppedPriceData> steppedPriceList) {
        this.steppedPriceList = steppedPriceList;
    }

    public List<SteppedPriceData> getSteppedPriceList() {
        return steppedPriceList;
    }

    /**
     * Setter for key.
     * 
     * @param key
     *            the key to set
     */
    public void setKey(long key) {
        this.key = key;
    }

    /**
     * Getter for key.
     * 
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * @param pricedOptionKey
     *            the pricedOptionKey to set
     */
    public void setPricedOptionKey(long pricedOptionKey) {
        this.pricedOptionKey = pricedOptionKey;
    }

    /**
     * @return the pricedOptionKey
     */
    public long getPricedOptionKey() {
        return pricedOptionKey;
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
        strBuf.append("totalParameterRolesCosts: "
                + getTotalParameterRolesCosts());
        strBuf.append("\n");
        strBuf.append("key: " + getKey());
        strBuf.append("\n");
        strBuf.append("pricedOptionKey: " + getPricedOptionKey());

        return strBuf.toString();
    }

    /**
     * @param totalParameterRolesCosts
     *            the totalParameterRolesCosts to set
     */
    public void setTotalParameterRolesCosts(BigDecimal totalParameterRolesCosts) {
        this.totalParameterRolesCosts = totalParameterRolesCosts;
    }

    /**
     * @return the totalParameterRolesCosts
     */
    public BigDecimal getTotalParameterRolesCosts() {
        if (totalParameterRolesCosts == null) {
            return BigDecimal.ZERO;
        }
        return totalParameterRolesCosts;
    }
}
