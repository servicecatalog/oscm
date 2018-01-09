/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * Represents one line of a billing table, which is stored in CSV format
 *
 */
public abstract class BillingTableEntry {

    String[] fields; // the fields in one line of the price model table

    public BillingTableEntry(String entry) {
        fields = entry.split(";");
    }

    /**
     * @return the locale from the table line
     */
    public abstract String getLocale();

    /**
     * @return <code>true</code> if the given locale matches the locale from the
     *         table line
     */
    public boolean localeEquals(String locale) {

        return getLocale().equals(locale);
    }

}
