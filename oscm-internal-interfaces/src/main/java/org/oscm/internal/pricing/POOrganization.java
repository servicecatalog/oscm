/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.io.Serializable;

/**
 * @author tokoda
 * 
 */
public class POOrganization implements Serializable {

    private static final long serialVersionUID = 7564240128775339293L;

    private String organizationId;
    private String organizationName;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

}
