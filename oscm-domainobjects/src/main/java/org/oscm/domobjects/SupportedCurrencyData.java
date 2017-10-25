/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 31.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The data container for the SupportedCurrency object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class SupportedCurrencyData extends DomainDataContainer {

    private static final long serialVersionUID = -6126726356783312817L;

    /**
     * The ISO code of the currency according to the ISO 4217 standard.
     */
    @Column(nullable = false)
    private String currencyISOCode;

    public String getCurrencyISOCode() {
        return currencyISOCode;
    }

    public void setCurrencyISOCode(String currencyISOCode) {
        this.currencyISOCode = currencyISOCode;
    }

    public void setCurrency(Currency currency) {
        this.currencyISOCode = currency.getCurrencyCode();
    }

    /**
     * Returns the java class representing the currency.
     */
    public Currency getCurrency() {
        return Currency.getInstance(currencyISOCode);
    }

}
