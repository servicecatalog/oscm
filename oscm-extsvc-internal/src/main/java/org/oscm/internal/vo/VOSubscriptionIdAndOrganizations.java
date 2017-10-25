/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-01                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.vo.VOOrganization;

/**
 * Container for the mappings of subscription identifiers to organizations.
 * 
 */
public class VOSubscriptionIdAndOrganizations implements Serializable {

    private static final long serialVersionUID = 855549447171695438L;

    private String subscriptionId;

    private List<VOOrganization> organizations = new ArrayList<VOOrganization>();

    /**
     * Retrieves the subscription identifier.
     * 
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription identifier.
     * 
     * @param subscriptionId
     *            the subscription ID
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Retrieves the organizations which have a subscription with the given ID.
     * 
     * @return the list of organizations
     */
    public List<VOOrganization> getOrganizations() {
        return organizations;
    }

    /**
     * Sets the organizations which have a subscription with the given ID.
     * 
     * @param organizations
     *            the list of organizations
     */
    public void setOrganizations(List<VOOrganization> organizations) {
        this.organizations = organizations;
    }
}
