/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 24, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tokoda
 * 
 */
public class UserAssignment {

    private long userKey;

    private String userId;

    private long usageStartTime;

    private long usageEndTime;

    private List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();

    public long getUserKey() {
        return userKey;
    }

    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getUsageStartTime() {
        return usageStartTime;
    }

    public void setUsageStartTime(long usageStartTime) {
        this.usageStartTime = usageStartTime;
    }

    public long getUsageEndTime() {
        return usageEndTime;
    }

    public void setUsageEndTime(long usageEndTime) {
        this.usageEndTime = usageEndTime;
    }

    public boolean hasUserRole() {
        return roleAssignments != null && !roleAssignments.isEmpty();
    }

    public List<UserRoleAssignment> getRoleAssignments() {
        return roleAssignments;
    }

    public void setRoleAssignments(List<UserRoleAssignment> roleAssignments) {
        this.roleAssignments = roleAssignments;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("UserId: ");
        b.append(userId);
        b.append("; Usage Time: ");
        b.append(new Date(usageStartTime));
        b.append(" - ");
        b.append(new Date(usageEndTime));
        return b.toString();
    }

}
