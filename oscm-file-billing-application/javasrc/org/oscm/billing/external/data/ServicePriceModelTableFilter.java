/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * A filter for the service price model table, which filters the table entries
 * according to the given price model context
 *
 */
public class ServicePriceModelTableFilter implements
        BillingTableFilter<ServicePriceModelTableEntry> {

    private PriceModelContext pmContext;

    public ServicePriceModelTableFilter(PriceModelContext pmContext) {
        this.pmContext = pmContext;
    }

    @Override
    public ServicePriceModelTableEntry accept(String tableLine) {

        ServicePriceModelTableEntry entry = new ServicePriceModelTableEntry(
                tableLine);

        if (pmContext.getInstanceType().equalsIgnoreCase(
                entry.getInstanceType())
                && pmContext.getRegion().equalsIgnoreCase(entry.getRegion())
                && pmContext.getOs().equalsIgnoreCase(entry.getOs())) {

            if (pmContext.getCustomerId() == null) {
                if (entry.getExternalCustomerID().isEmpty()) {
                    return entry;
                }
            } else if (pmContext.getCustomerId().equalsIgnoreCase(
                    entry.getExternalCustomerID())) {
                return entry;
            }
        }

        return null;
    }

}
