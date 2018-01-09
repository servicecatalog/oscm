/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 21, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.UserGroup;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUser;

/**
 * @author farmaki
 * 
 */
public class CustomerData {

    private VOOrganization organization;
    private VOUser adminUser;
    private List<UserGroup> userGroups = new ArrayList<UserGroup>();

    public VOOrganization getOrganization() {
        return organization;
    }

    public String getOrganizationId() {
        return organization.getOrganizationId();
    }

    public void setOrganization(VOOrganization customer) {
        this.organization = customer;
    }

    public VOUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(VOUser customerAdmin) {
        this.adminUser = customerAdmin;
    }

    public long getKey() {
        return organization.getKey();
    }

    public long getAdminKey() {
        return adminUser.getKey();
    }

    public void addUserGroup(UserGroup userGroup) {
        userGroups.add(userGroup);
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

}
