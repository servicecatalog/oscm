/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

/**
 * Represents one line of the service price model table
 *
 */
public class ServicePriceModelTableEntry extends BillingTableEntry {

    public ServicePriceModelTableEntry(String entry) {
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
     * @return the external customer ID from the table line
     */
    public String getExternalCustomerID() {

        if (fields.length > 1) {
            return fields[1].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the instance type from the table line
     */
    public String getInstanceType() {

        if (fields.length > 2) {
            return fields[2].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the region from the table line
     */
    public String getRegion() {

        if (fields.length > 3) {
            return fields[3].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the operating system from the table line
     */
    public String getOs() {

        if (fields.length > 4) {
            return fields[4].trim();
        } else {
            return "";
        }
    }

    @Override
    public String getLocale() {

        if (fields.length > 5) {
            return fields[5].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the file type from the table line
     */
    public String getFileType() {

        if (fields.length > 6) {
            return fields[6].trim();
        } else {
            return "";
        }
    }

    /**
     * @return the file name from the table line
     */
    public String getFileName() {

        if (fields.length > 7) {
            return fields[7].trim();
        } else {
            return "";
        }

    }

    /**
     * @return the file tag from the table line
     */
    public String getTag() {

        if (fields.length > 8) {
            return fields[8].trim();
        } else {
            return "";
        }
    }
}
