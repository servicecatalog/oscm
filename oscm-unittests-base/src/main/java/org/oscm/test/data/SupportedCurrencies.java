/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Utility class to work with <code>SupportedCurrency</code>
 * 
 * @author cheld
 * 
 */
public class SupportedCurrencies {

    /**
     * Returns the SupportedCurrency domain object for the given currency code.
     * Creates the object if needed.
     * 
     */
    public static SupportedCurrency findOrCreate(DataService mgr,
            String currencyCode) throws NonUniqueBusinessKeyException {
        SupportedCurrency result = (SupportedCurrency) mgr
                .find(new SupportedCurrency(currencyCode));
        if (result == null) {
            result = persistCurrency(mgr, currencyCode);
        }
        return result;
    }

    private static SupportedCurrency persistCurrency(DataService mgr,
            String currencyCode) throws NonUniqueBusinessKeyException {
        SupportedCurrency currency = new SupportedCurrency(currencyCode);
        mgr.persist(currency);
        return currency;
    }

    /**
     * Creates one currency.
     */
    public static SupportedCurrency createOneSupportedCurrency(DataService mgr)
            throws NonUniqueBusinessKeyException {
        return findOrCreate(mgr, "EUR");
    }

}
