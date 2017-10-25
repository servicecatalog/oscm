/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 17.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;

/**
 * Class to represent the default, country and customer related vat rates.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VatRateDetails {

    /**
     * The default VAT rate for the supplier.
     */
    private BigDecimal defaultVatRate;

    /**
     * The VAT rate as defined by the supplier for the customer's country.
     */
    private BigDecimal countryVatRate;

    /**
     * The VAT rate as defined by the supplier for the customer.
     */
    private BigDecimal customerVatRate;

    /**
     * The net costs without any VAT rate effect.
     */
    private BigDecimal netCosts;

    /**
     * The costs considering the VAT rate.
     */
    private BigDecimal totalCosts;

    public BigDecimal getDefaultVatRate() {
        return defaultVatRate;
    }

    public BigDecimal getCountryVatRate() {
        return countryVatRate;
    }

    public BigDecimal getCustomerVatRate() {
        return customerVatRate;
    }

    public void setDefaultVatRate(BigDecimal defaultVatRate) {
        this.defaultVatRate = defaultVatRate;
    }

    public void setCountryVatRate(BigDecimal countryVatRate) {
        this.countryVatRate = countryVatRate;
    }

    public void setCustomerVatRate(BigDecimal customerVatRate) {
        this.customerVatRate = customerVatRate;
    }

    /**
     * Determines the VAT rate to be used for the customer according to the
     * given schema:
     * 
     * <ul>
     * <li>If there is a customer-specific VAT rate, return it.</li>
     * <li>If there is no customer-specific VAT rate, return the
     * country-specific VAT rate for the customer country.</li>
     * <li>If there is no country-specific VAT rate, return the supplier's
     * default VAT rate.</li>
     * <li>If this one is not set either, <code>null</code> is returned.</li>
     * </ul>
     * 
     * @return The VAT rate effective for the customer.
     */
    public BigDecimal getEffectiveVatRateForCustomer() {
        if (getCustomerVatRate() != null) {
            return getCustomerVatRate();
        }
        if (getCountryVatRate() != null) {
            return getCountryVatRate();
        }
        if (getDefaultVatRate() != null) {
            return getDefaultVatRate();
        }
        return null;
    }

    public BigDecimal getNetCosts() {
        if (netCosts == null) {
            return BigDecimal.ZERO;
        }
        return netCosts;
    }

    public BigDecimal getTotalCosts() {
        if (totalCosts == null) {
            return BigDecimal.ZERO;
        }
        return totalCosts;
    }

    public void setNetCosts(BigDecimal netCosts) {
        this.netCosts = netCosts;
    }

    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getVatAmount() {
        if (totalCosts == null) {
            return BigDecimal.ZERO;
        } else if (netCosts == null) {
            return totalCosts;
        } else {
            return totalCosts.subtract(netCosts);
        }
    }

}
