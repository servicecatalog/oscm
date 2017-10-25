/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *******************************************************************************/

package org.oscm.internal.usermanagement;

public class POUserAndOrganization extends POUserDetails {

    private static final long serialVersionUID = 279072057940068106L;

    private String organizationName;
    private String organizationId;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

}
