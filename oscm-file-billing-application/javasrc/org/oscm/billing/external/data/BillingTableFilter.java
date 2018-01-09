/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * A filter interface for a billing table
 *
 */
public interface BillingTableFilter<T extends BillingTableEntry> {

    /**
     * @param entry
     *            a table line
     * @return a table entry, if the line was accepted by the filter; otherwise
     *         <code>null</code>
     */
    public T accept(String tableLine);
}
