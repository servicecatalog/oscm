/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.usergroupmgmt.POUserGroup;

/**
 * Maps a user to the subscriptions that are available (reading) or shall be
 * assigned (writing).
 * 
 * @author weiser
 * 
 */
public class POUserAndSubscriptions extends POUserDetails {

    private static final long serialVersionUID = 279072057940068106L;

    private List<POSubscription> subscriptions = new ArrayList<POSubscription>();

    private List<POUserGroup> groupsToBeAssigned = new ArrayList<POUserGroup>();

    private List<POUserGroup> allGroups = new ArrayList<POUserGroup>();

    public List<POSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<POSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<POUserGroup> getGroupsToBeAssigned() {
        return groupsToBeAssigned;
    }

    public void setGroupsToBeAssigned(List<POUserGroup> groupsToBeAssigned) {
        this.groupsToBeAssigned = groupsToBeAssigned;
    }

    /**
     * @return the allGroups
     */
    public List<POUserGroup> getAllGroups() {
        return allGroups;
    }

    /**
     * @param allGroups
     *            the allGroups to set
     */
    public void setAllGroups(List<POUserGroup> allGroups) {
        this.allGroups = allGroups;
    }
}
