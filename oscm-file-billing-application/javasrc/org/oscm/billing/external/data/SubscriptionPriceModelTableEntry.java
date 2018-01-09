/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * Represents one line of the subscription price model table
 *
 */
public class SubscriptionPriceModelTableEntry extends BillingTableEntry {

    public SubscriptionPriceModelTableEntry(String entry) {
        super(entry);
    }

    /**
     * @return the price model UUID from the table line
     */
    public String getPriceModelUUID() {

        if (fields.length > 0) {
            return fields[0].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the subscription ID from the table line
     */
    public String getSubscriptionID() {

        if (fields.length > 1) {
            return fields[1].trim();
        } else {
            return "";
        }
    }

    @Override
    public String getLocale() {

        if (fields.length > 2) {
            return fields[2].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the file type from the table line
     */
    public String getFileType() {

        if (fields.length > 3) {
            return fields[3].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the file name from the table line
     */
    public String getFileName() {

        if (fields.length > 4) {
            return fields[4].trim();
        } else {
            return "";
        }

    }

}
