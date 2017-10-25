/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 27.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents the details on a user assignment to a certain subscription.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UserAssignmentDetails {
    /**
     * The technical key of the user.
     */
    private long userKey;

    private String userId;

    /**
     * A map containing the technical keys of the roles the user was assigned to
     * as keys, and the usage details as value.
     */
    private Map<Long, UsageDetails> roleAssignmentDetails = new HashMap<Long, UsageDetails>();

    /**
     * The usage details for the general user assignment - irrespective of any
     * role.
     */
    private UsageDetails usageDetails;

    /**
     * The reference to the containing user assignment factors object.
     */
    private UserAssignmentFactors userFactors;

    /**
     * Creates an object with the reference to the parent user assignment factor
     * object.
     * 
     * @param userAssignmentFactors
     */
    UserAssignmentDetails(UserAssignmentFactors userAssignmentFactors) {
        this.userFactors = userAssignmentFactors;
        this.usageDetails = new UsageDetails();
    }

    public long getUserKey() {
        return userKey;
    }

    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    public Set<Long> getRoleKeys() {
        return roleAssignmentDetails.keySet();
    }

    /**
     * Returns the usage details stored for a certain role.
     * 
     * @param roleKey
     *            The key of the role to obtain the details for.
     * @return The usage details for the role.
     */
    public UsageDetails getUsageDetails(Long roleKey) {
        return roleAssignmentDetails.get(roleKey);
    }

    /**
     * Adds the role related factor for the current user. Also adds the
     * information to the parent structure, so that it contains the summary for
     * all users.
     * 
     * @param roleKey
     *            The key of the role the current user is assigned to.
     * @param factor
     *            The factor to set.
     */
    public void addRoleFactor(Long roleKey, double factor) {
        // update member
        UsageDetails details = roleAssignmentDetails.get(roleKey);
        if (details == null) {
            details = new UsageDetails();
            putUsageDetails(roleKey, details);
        }
        details.setFactor(details.getFactor() + factor);

        // update value in containing element
        userFactors.addRoleFactor(roleKey, factor);
    }

    /**
     * Stores the provided usage details object for the given role key.
     * Overwrites existing ones.
     * 
     * @param roleKey
     *            The key of the role.
     * @param details
     *            The usage details to store.
     */
    protected void putUsageDetails(Long roleKey, UsageDetails details) {
        roleAssignmentDetails.put(roleKey, details);
    }

    /**
     * Stores the provided usage details for the given role key. If there
     * already is some data stored for that role, the new settings of the usage
     * details will be added to the already existing ones.
     * 
     * @param roleKey
     *            The key of the role.
     * @param details
     *            The usage details to add.
     */
    protected void addUsageDetails(Long roleKey, UsageDetails details) {
        if (roleAssignmentDetails.containsKey(roleKey)) {
            UsageDetails entry = roleAssignmentDetails.get(roleKey);
            entry.addUsagePeriods(details.getUsagePeriods());
            entry.setFactor(entry.getFactor() + details.getFactor());
        } else {
            putUsageDetails(roleKey, details);
        }
    }

    /**
     * Stores the provided usage details for the given role key. If there
     * already is some data stored for that role, the new settings of the usage
     * details will be added to the already existing ones.
     * 
     * @param details
     *            The usage details to add.
     */
    protected void addUsageDetails(UsageDetails details) {
        usageDetails.addUsagePeriods(details.getUsagePeriods());
        usageDetails.setFactor(usageDetails.getFactor() + details.getFactor());
    }

    /**
     * Returns the usage details for the user.
     * 
     * @return The user related usage details.
     */
    public UsageDetails getUsageDetails() {
        return usageDetails;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
