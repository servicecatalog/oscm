/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * The price model table
 *
 */
public class SubscriptionPriceModelTable extends
        BillingTable<SubscriptionPriceModelTableEntry> {

    public static final String SUBSCRIPTION_PM_TABLE_FILE_NAME = "pm/SubscriptionPriceModel.csv";

    /**
     * Get the price model table file
     */
    public SubscriptionPriceModelTable() {
        super(SUBSCRIPTION_PM_TABLE_FILE_NAME);
    }

}
