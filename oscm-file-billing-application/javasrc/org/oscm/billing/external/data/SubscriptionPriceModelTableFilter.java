/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * A filter for the subscription price model table, which filters the table
 * entries according to the given price model context
 *
 */
public class SubscriptionPriceModelTableFilter implements
        BillingTableFilter<SubscriptionPriceModelTableEntry> {

    private PriceModelContext pmContext;

    public SubscriptionPriceModelTableFilter(PriceModelContext pmContext) {
        this.pmContext = pmContext;
    }

    @Override
    public SubscriptionPriceModelTableEntry accept(String tableLine) {

        SubscriptionPriceModelTableEntry entry = new SubscriptionPriceModelTableEntry(
                tableLine);

        if (pmContext.getSubscriptionId().equalsIgnoreCase(
                entry.getSubscriptionID())) {
            return entry;
        }

        return null;
    }

}
