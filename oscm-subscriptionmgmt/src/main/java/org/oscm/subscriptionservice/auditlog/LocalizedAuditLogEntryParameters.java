/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

/**
 * Class for storing localized parameters of log entry.
 * 
 * @author Mao
 * 
 */
public class LocalizedAuditLogEntryParameters {

    private String description;
    private String license;

    public LocalizedAuditLogEntryParameters() {
        this.description = "";
        this.license = "";
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the license
     */
    public String getLicense() {
        return license;
    }

    /**
     * @param license
     *            the license to set
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Check if all the parameters equals accordingly
     * 
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        LocalizedAuditLogEntryParameters localizedAuditLogEntryParameters = (LocalizedAuditLogEntryParameters) other;
        if (!description.equals(localizedAuditLogEntryParameters.description))
            return false;
        if (!license.equals(localizedAuditLogEntryParameters.license))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (license != null ? license.hashCode() : 0);
        return result;
    }
}
