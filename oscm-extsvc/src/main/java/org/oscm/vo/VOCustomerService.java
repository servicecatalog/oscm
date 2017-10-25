/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-08-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents a customer-specific service.
 * <p>
 * A customer-specific service is a copy of a marketable service. It is created
 * when a supplier defines a customer-specific price model for a marketable
 * service.
 * <p>
 * The value object holds the following information on the target customer:
 * <ul>
 * <li>organization ID</li>
 * <li>numeric organization key</li>
 * <li>organization name</li>
 * </ul>
 * 
 */
public class VOCustomerService extends VOService {

    private static final long serialVersionUID = 3923689218075784173L;

    private String organizationId;
    private Long organizationKey;
    private String organizationName;

    /**
     * Sets the ID of the customer organization to which the service is related.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Retrieves the ID of the customer organization to which the service is
     * related.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the numeric key of the customer organization to which the service is
     * related.
     * 
     * @param organizationKey
     *            the organization key
     */
    public void setOrganizationKey(Long organizationKey) {
        this.organizationKey = organizationKey;
    }

    /**
     * Retrieves the numeric key of the customer organization to which the
     * service is related.
     * 
     * @return the organization key
     */
    public Long getOrganizationKey() {
        return organizationKey;
    }

    /**
     * Sets the name of the customer organization to which the service is
     * related.
     * 
     * @param organizationName
     *            the organization name
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * Retrieves the name of the customer organization to which the service is
     * related.
     * 
     * @return the organization name
     */
    public String getOrganizationName() {
        return organizationName;
    }
}
