/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 24, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

/**
 * @author tokoda
 * 
 */
public class UserRoleAssignment {

    private Long roleKey;

    private long startTime;

    private long endTime;

    private boolean firstRoleAssignment;

    private boolean lastRoleAssignment;

    public UserRoleAssignment(Long roleKey, long startTime, long endTime) {
        this.roleKey = roleKey;
        this.startTime = startTime;
        this.endTime = endTime;
        this.firstRoleAssignment = false;
        this.lastRoleAssignment = false;
    }

    public Long getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(Long roleKey) {
        this.roleKey = roleKey;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * This flag will be true if the object is first role for the user
     * assignment in the specified period, even if the user assignment has
     * different role before the specified period.
     */
    public boolean isFirstRoleAssignment() {
        return firstRoleAssignment;
    }

    public void setFirstRoleAssignment(boolean firstRoleAssignment) {
        this.firstRoleAssignment = firstRoleAssignment;
    }

    /**
     * This flag will be true if the object is last role for the user assignment
     * in the specified period, even if the user assignment has different role
     * after the specified period.
     */
    public boolean isLastRoleAssignment() {
        return lastRoleAssignment;
    }

    public void setLastRoleAssignment(boolean lastRoleAssignment) {
        this.lastRoleAssignment = lastRoleAssignment;
    }

}
