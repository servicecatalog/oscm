/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 31.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Currency;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Represents a currency that is supported by the BES.
 * 
 * <p>
 * All currently supported currencies are defined with the initial data for the
 * BES product. No API must allow adding new or even changing existing entries.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "currencyISOCode" }))
@BusinessKey(attributes = { "currencyISOCode" })
@NamedQueries({
        @NamedQuery(name = "SupportedCurrency.findByBusinessKey", query = "SELECT sc FROM SupportedCurrency sc WHERE sc.dataContainer.currencyISOCode = :currencyISOCode"),
        @NamedQuery(name = "SupportedCurrency.getAll", query = "SELECT sc FROM SupportedCurrency sc"),
        @NamedQuery(name = "SupportedCurrency.findAllCodes", query = "SELECT sc.dataContainer.currencyISOCode FROM SupportedCurrency sc ORDER BY sc.dataContainer.currencyISOCode") })
@Entity
public class SupportedCurrency extends
        DomainObjectWithVersioning<SupportedCurrencyData> {

    private static final long serialVersionUID = 8497917557706805641L;

    public SupportedCurrency() {
        this.dataContainer = new SupportedCurrencyData();
    }

    /**
     * Constructs a new supported currency for the given currency code defined
     * in ISO 4217.
     */
    public SupportedCurrency(String currencyCode) {
        this();
        setCurrency(Currency.getInstance(currencyCode));
    }

    /**
     * Refer to {@link SupportedCurrencyData#currencyISOCode}.
     */
    public String getCurrencyISOCode() {
        return dataContainer.getCurrencyISOCode();
    }

    /**
     * Refer to {@link SupportedCurrencyData#currencyISOCode}.
     */
    public void setCurrency(Currency currency) {
        dataContainer.setCurrency(currency);
    }

    public Currency getCurrency() {
        return dataContainer.getCurrency();
    }

    String toStringAttributes() {
        return String.format(", currencyISOCode='%s'", getCurrencyISOCode());
    }

}
