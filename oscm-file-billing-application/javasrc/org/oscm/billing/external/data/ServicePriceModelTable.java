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
public class ServicePriceModelTable extends BillingTable<ServicePriceModelTableEntry> {

    public static final String SERVICE_PM_TABLE_FILE_NAME = "pm/ServicePriceModel.csv";

    /**
     * Get the price model table file
     */
    public ServicePriceModelTable() {
        super(SERVICE_PM_TABLE_FILE_NAME);
    }

}
