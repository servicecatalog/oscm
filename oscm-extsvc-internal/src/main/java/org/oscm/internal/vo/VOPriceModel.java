/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

import javax.ws.rs.core.MediaType;

/**
 * Represents a price model.
 */
public class VOPriceModel extends BaseVO {

    private static final long serialVersionUID = 5326854330135373796L;

    private byte[] presentation;

    private String presentationDataType;

    private String description;

    /**
     * The priced events to be considered.
     */
    private List<VOPricedEvent> consideredEvents = new ArrayList<VOPricedEvent>();

    /**
     * The priced parameters to be considered.
     */
    private List<VOPricedParameter> selectedParameters = new ArrayList<VOPricedParameter>();

    /**
     * The pricing period for recurring charges and cost calculation based on
     * time units (YEAR, MONTH, WEEK, DAY)
     */
    private PricingPeriod period;

    /**
     * The price per period (in the default currency of the platform)
     */
    private BigDecimal pricePerPeriod = BigDecimal.ZERO;

    /**
     * The recurring price for users assigned to a subscription.
     */
    private BigDecimal pricePerUserAssignment = BigDecimal.ZERO;

    /**
     * The ISO code of the currency to use according to the ISO 4217 standard.
     */
    private String currencyISOCode;

    /**
     * The one-time fee for a subscription.
     */
    private BigDecimal oneTimeFee = BigDecimal.ZERO;

    /**
     * The additional prices that have to be paid for users with specific
     * service roles.
     */
    private List<VOPricedRole> roleSpecificUserPrices = new ArrayList<VOPricedRole>();

    /**
     * The price steps if different prices are to be applied depending on the
     * number of users assigned to a subscription.
     */
    private List<VOSteppedPrice> steppedPrices = new ArrayList<VOSteppedPrice>();

    /**
     * The cost calculation type of the price model (free of charge, pro rata or
     * per time unit).
     */
    private PriceModelType type = PriceModelType.FREE_OF_CHARGE;
    
    private boolean relatedSubscription;

    /**
     * Retrieves the priced events to be considered in the price model.
     * 
     * @return the list of priced events
     */
    public List<VOPricedEvent> getConsideredEvents() {
        return consideredEvents;
    }

    /**
     * The localized license agreement of the service.
     */
    private String license;

    /**
     * Boolean flag for the type of the price model (external or native).
     */
    private boolean external;

    /**
     * UUID, the external price model id (from billing adapter).
     */
    private UUID uuid;

    /**
     * Returns a boolean flag for the type of the price model (external or
     * native).
     * 
     * @return true if the price model is external, false otherwise
     */
    public boolean isExternal() {
        return this.external;
    }

    /**
     * Sets the boolean flag for the price model type (external or native)
     * 
     * @param external
     *            boolean which is true for external price model
     */
    public void setExternal(boolean external) {
        this.external = external;
    }

    /**
     * Retrieves the price model presentation.
     * 
     * @return the price model presentation data
     */
    public byte[] getPresentation() {
        return presentation;
    }

    /**
     * Sets the price model presentation
     * 
     * @param presentation
     *            the price model presentation data
     */
    public void setPresentation(byte[] presentation) {
        this.presentation = presentation;
    }

    /**
     * Retrieves the price model presentation data type.
     * 
     * @return the price model presentation data type
     */
    public String getPresentationDataType() {
        return presentationDataType;
    }

    /**
     * Returns the JSON representation of the price model as a string. The
     * representation is only valid for external price models.
     * 
     * @return
     */
    public String getAsJSON() {
        if (presentation != null
                && MediaType.APPLICATION_JSON.equals(presentationDataType)) {
            return new String(presentation);
        } else {
            return null;
        }
    }

    /**
     * Sets the price model presentation data type
     * 
     * @param presentationDataType
     *            the price model presentation data type
     */
    public void setPresentationDataType(String presentationDataType) {
        this.presentationDataType = presentationDataType;
    }

    /**
     * Sets the priced events to be considered in the price model.
     * 
     * @param consideredEvents
     *            the list of priced events
     */
    public void setConsideredEvents(List<VOPricedEvent> consideredEvents) {
        this.consideredEvents = consideredEvents;
    }

    /**
     * Sets the text describing the price model.
     * 
     * @param description
     *            the price model description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the text describing the price model.
     * 
     * @return the price model description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks whether customers are charged for the service for which the price
     * model is defined.
     * 
     * @return <code>true</code> if customers are charged, <code>false</code> if
     *         the service is free of charge
     */
    public boolean isChargeable() {
        return type != PriceModelType.FREE_OF_CHARGE
                && type != PriceModelType.UNKNOWN;
    }

    /**
     * Checks whether customers are not charged for the service.
     * 
     * @return <code>true</code> if customers are not charged,
     *         <code>false</code> otherwise
     */
    public boolean isFree() {
        return type == PriceModelType.FREE_OF_CHARGE;
    }

    /**
     * Retrieves the pricing period for recurring charges and for price models
     * that charge for a service per time unit. For example, the price model
     * could define an amount of money that a customer has to pay per DAY for
     * each user or subscription.
     * 
     * @return the pricing period
     */
    public PricingPeriod getPeriod() {
        return period;
    }

    /**
     * Retrieves the recurring charge for subscriptions set for the price model.
     * 
     * @return the price per pricing period for each subscription
     */
    public BigDecimal getPricePerPeriod() {
        return pricePerPeriod;
    }

    /**
     * Retrieves the recurring charge for users set for the price model.
     * 
     * @return the price per pricing period for each user assigned to a
     *         subscription
     */
    public BigDecimal getPricePerUserAssignment() {
        return pricePerUserAssignment;
    }

    /**
     * Retrieves the currency used in the price model.
     * 
     * @return the ISO 4217 code of the currency
     */
    public String getCurrencyISOCode() {
        return currencyISOCode;
    }

    /**
     * Sets the pricing period for recurring charges and for price models that
     * charge for a service per time unit. For example, the price model could
     * define an amount of money that a customer has to pay per DAY for each
     * user or subscription.
     * 
     * @param period
     *            the pricing period
     */
    public void setPeriod(PricingPeriod period) {
        this.period = period;
    }

    /**
     * The free trial period defined in number of days.
     */
    private int freePeriod;

    /**
     * Retrieves the free trial period of the price model.
     * 
     * @return the number of days the service is free of charge
     */
    public int getFreePeriod() {
        return freePeriod;
    }

    /**
     * Sets the free trial period for the price model.
     * 
     * @param freePeriod
     *            the number of days the service is free of charge
     */
    public void setFreePeriod(int freePeriod) {
        this.freePeriod = freePeriod;
    }

    /**
     * Sets the recurring charge for subscriptions for the price model.
     * 
     * @param pricePerPeriod
     *            the price per pricing period for each subscription
     */
    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        this.pricePerPeriod = pricePerPeriod;
    }

    /**
     * Sets the recurring charge for users for the price model.
     * 
     * @param pricePerUserAssignment
     *            the price per pricing period for each user assigned to a
     *            subscription
     */
    public void setPricePerUserAssignment(BigDecimal pricePerUserAssignment) {
        this.pricePerUserAssignment = pricePerUserAssignment;
    }

    /**
     * Sets the currency to be used in the price model.
     * 
     * @param currencyISOCode
     *            the ISO 4217 code of the currency
     */
    public void setCurrencyISOCode(String currencyISOCode) {
        this.currencyISOCode = currencyISOCode;
    }

    /**
     * Retrieves the one-time fee defined for the price model.
     * 
     * @return the one-time fee
     */
    public BigDecimal getOneTimeFee() {
        return oneTimeFee;
    }

    /**
     * Sets the one-time fee for the price model.
     * 
     * @param oneTimeFee
     *            the one-time fee
     */
    public void setOneTimeFee(BigDecimal oneTimeFee) {
        this.oneTimeFee = oneTimeFee;
    }

    /**
     * Retrieves the priced parameters to be considered in the price model.
     * 
     * @return the list of priced parameters
     */
    public List<VOPricedParameter> getSelectedParameters() {
        return selectedParameters;
    }

    /**
     * Sets the priced parameters to be considered in the price model.
     * 
     * @param selectedParameters
     *            the list of priced parameters
     */
    public void setSelectedParameters(
            List<VOPricedParameter> selectedParameters) {
        this.selectedParameters = selectedParameters;
    }

    /**
     * Returns the currency used in the price model as a string.
     * 
     * @return the currency
     */
    public String getCurrency() {
        return null != getCurrencyISOCode() ? "[" + getCurrencyISOCode() + "]"
                : "";
    }

    /**
     * Retrieves the role-specific prices that have to be paid in addition to
     * the price for users.
     * 
     * @return the list of priced roles
     */
    public List<VOPricedRole> getRoleSpecificUserPrices() {
        return roleSpecificUserPrices;
    }

    /**
     * Sets the role-specific prices that have to be paid in addition to the
     * price for users.
     * 
     * @param roleSpecificUserPrices
     *            the list of priced roles
     */
    public void setRoleSpecificUserPrices(
            List<VOPricedRole> roleSpecificUserPrices) {
        this.roleSpecificUserPrices = roleSpecificUserPrices;
    }

    /**
     * Sets the price steps for the price model if different prices are to be
     * applied depending on the number of users assigned to a subscription.
     * 
     * @param steppedPrices
     *            the price steps
     */
    public void setSteppedPrices(List<VOSteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    /**
     * Retrieves the price steps for the price model if different prices are
     * applied depending on the number of users assigned to a subscription.
     * 
     * @return the price steps
     */
    public List<VOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    /**
     * Sets the license agreement for the price model.
     * 
     * @param license
     *            the license agreement
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Retrieves the license agreement of the price model.
     * 
     * @return the license agreement
     */
    public String getLicense() {
        return license;
    }

    /**
     * Retrieves the cost calculation type of the price model (free of charge,
     * pro rata, or per time unit).
     * 
     * @return the cost calculation type
     */
    public PriceModelType getType() {
        return type;
    }

    /**
     * Sets the cost calculation type of the price model (free of charge, pro
     * rata, or per time unit).
     * 
     * @param type
     *            the cost calculation type
     */
    public void setType(PriceModelType type) {
        this.type = type;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isPricePerPeriodSet() {
        return this.pricePerPeriod.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPricePerUserAssignmentSet() {
        return this.pricePerUserAssignment.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isOneTimeFeeSet() {
        return this.oneTimeFee.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * @return the relatedSubscription
     */
    public boolean isRelatedSubscription() {
        return relatedSubscription;
    }

    /**
     * @param relatedSubscription the relatedSubscription to set
     */
    public void setRelatedSubscription(boolean relatedSubscription) {
        this.relatedSubscription = relatedSubscription;
    }
}
