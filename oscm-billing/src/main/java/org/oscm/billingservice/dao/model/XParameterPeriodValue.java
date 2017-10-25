/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 07.09.2011                                                      
 *                                                                              
 *  Completion Time: 07.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.util.Date;

import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Represents the different value settings a parameter has during a certain
 * period.
 * 
 * @author Mike J&auml;ger
 * 
 */
public abstract class XParameterPeriodValue {

    private Long key;
    private long startTime;
    private long endTime;
    private double periodFactor;
    private double userAssignmentFactor;
    private double valueFactor;
    private BigDecimal pricePerSubscription = BigDecimal.ZERO;
    private BigDecimal pricePerUser = BigDecimal.ZERO;
    private BigDecimal periodCosts = BigDecimal.ZERO;
    private BigDecimal userAssignmentCosts = BigDecimal.ZERO;
    private BigDecimal totalCosts = BigDecimal.ZERO;
    private BigDecimal totalCostsForUser = BigDecimal.ZERO;
    private BigDecimal totalCostsForSubscription = BigDecimal.ZERO;
    private BigDecimal totalCostsForRoles = BigDecimal.ZERO;
    private BigDecimal totalCostsForSteppedPrices = BigDecimal.ZERO;
    private SteppedPriceDetail steppedPrices;

    /**
     * Provides the parent parameter id data structure.
     * 
     * @return The parameter id data.
     */
    public abstract XParameterIdData getParent();

    /**
     * Provides the option data for this parameter.
     * 
     * @return <code>null</code> in case the parameter is not of value type
     *         enumeration, the option data otherwise.
     */
    public abstract XParameterOption getParameterOption();

    /**
     * Provides the parameter value.
     * 
     * @return The parameter value or the option id in case the parameter is of
     *         value type enumeration.
     */
    public abstract String getValue();

    /**
     * Determines if the current parameter is based on options or not.
     * 
     * @return <code>true</code> if the parameter is based on option,
     *         <code>false</code> otherwise.
     */
    public abstract boolean isParameterOption();

    /**
     * Determines the role pricing data.
     * 
     * @return The role pricing data.
     */
    public abstract RolePricingData getRolePrices();

    public double getPeriodFactor() {
        return periodFactor;
    }

    public double getUserAssignmentFactor() {
        return userAssignmentFactor;
    }

    public BigDecimal getPricePerSubscription() {
        return pricePerSubscription;
    }

    public BigDecimal getPeriodCosts() {
        return periodCosts;
    }

    public BigDecimal getUserAssignmentCosts() {
        return userAssignmentCosts;
    }

    public void setPeriodFactor(double factor) {
        this.periodFactor = factor;
    }

    public void setUserAssignmentFactor(double usageFactor) {
        this.userAssignmentFactor = usageFactor;
    }

    public void setPricePerSubscription(BigDecimal basePrice) {
        this.pricePerSubscription = basePrice;
    }

    public void setPeriodCosts(BigDecimal periodCosts) {
        this.periodCosts = periodCosts;
    }

    public void setUserAssignmentCosts(BigDecimal usageCosts) {
        this.userAssignmentCosts = usageCosts;
    }

    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public ParameterValueType getValueType() {
        return getParent().getValueType();
    }

    public SteppedPriceDetail getSteppedPricesForParameter() {
        if (steppedPrices == null) {
            return new SteppedPriceDetail(BigDecimal.ZERO);
        }
        return steppedPrices;
    }

    protected void setSteppedPrices(SteppedPriceDetail steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    private void addTotalCosts(BigDecimal totalCosts) {
        getParent().addTotalCosts(totalCosts);
        this.totalCosts = this.totalCosts.add(totalCosts);
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void addTotalCostsForUser(BigDecimal totalCostsForUser) {
        addTotalCosts(totalCostsForUser);
        getParent().addTotalCostsForUser(totalCostsForUser);
        this.totalCostsForUser = this.totalCostsForUser.add(totalCostsForUser);
    }

    public BigDecimal getTotalCostsForUser() {
        return totalCostsForUser;
    }

    public void addTotalCostsForSubscription(
            BigDecimal totalCostsForSubscription) {
        addTotalCosts(totalCostsForSubscription);
        getParent().addTotalCostsForSubscription(totalCostsForSubscription);
        this.totalCostsForSubscription = this.totalCostsForSubscription
                .add(totalCostsForSubscription);
    }

    public BigDecimal getTotalCostsForSubscription() {
        return totalCostsForSubscription;
    }

    public void addTotalCostsForRoles(BigDecimal totalCostsForRoles) {
        addTotalCosts(totalCostsForRoles);
        this.totalCostsForRoles = this.totalCostsForRoles
                .add(totalCostsForRoles);
    }

    public void addTotalCostsForSteppedPrices(
            BigDecimal totalCostsForSteppedPrices) {
        this.totalCostsForSteppedPrices = this.totalCostsForSteppedPrices
                .add(totalCostsForSteppedPrices);
    }

    public BigDecimal getTotalCostsForSteppedPrices() {
        return totalCostsForSteppedPrices;
    }

    public BigDecimal getTotalCostsForRoles() {
        return totalCostsForRoles;
    }

    public BigDecimal getTotalUserAssignmentCosts() {
        return totalCostsForRoles.add(getTotalCostsForUser());
    }

    public void setValueFactor(double valueFactor) {
        this.valueFactor = valueFactor;
    }

    public double getValueFactor() {
        return valueFactor;
    }

    public String getId() {
        return getParent().getId();
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Long getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "start: " + (new Date(startTime)) + ", end: "
                + (new Date(endTime));
    }

}
