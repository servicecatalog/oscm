/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents the usage factors for user assignments, containing the factor for
 * the basic usage and for the role depending factors.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UserAssignmentFactors {

    private double basicFactor;

    private Map<Long, UserAssignmentDetails> userFactors = new HashMap<Long, UserAssignmentDetails>();

    private Map<Long, Double> roleFactors = new HashMap<Long, Double>();

    public double getBasicFactor() {
        return basicFactor;
    }

    public int getNumberOfUsers() {
        return getUserKeys().size();
    }

    public Set<Long> getUserKeys() {
        return userFactors.keySet();
    }

    public UserAssignmentDetails getUserAssignmentDetails(Long userKey) {
        return userFactors.get(userKey);
    }

    /**
     * Sets the period duration and factor data for the given user and role. For
     * the role relation, only the duration will be considered. To set the
     * factor, invoke {@link UsageDetails#setFactor(double)}.
     * 
     * @param userKey
     *            The key of the user.
     * @param roleKey
     *            The key of the role the user is assigned to.
     * @param details
     *            The usage details to be stored.
     */
    public void addUsageDataForUserAndRole(Long userKey, String userId,
            Long roleKey, UsageDetails details) {
        if (!userFactors.containsKey(userKey)) {
            final UserAssignmentDetails u = new UserAssignmentDetails(this);
            u.setUserId(userId);
            userFactors.put(userKey, u);
        }
        UserAssignmentDetails userAssignmentDetails = userFactors.get(userKey);
        userAssignmentDetails.addUsageDetails(roleKey, details);
        userAssignmentDetails.addUsageDetails(details);
        basicFactor += details.getFactor();
    }

    public void addUsageDataForUser(Long userKey, String userId,
            UsageDetails details) {
        if (!userFactors.containsKey(userKey)) {
            final UserAssignmentDetails u = new UserAssignmentDetails(this);
            u.setUserId(userId);
            userFactors.put(userKey, u);
        }
        UserAssignmentDetails userAssignmentDetails = userFactors.get(userKey);
        userAssignmentDetails.addUsageDetails(details);
        basicFactor += details.getFactor();
    }

    /**
     * Return map of roles-factors pair.
     * 
     * @return Map of roles-factors pair.
     */
    public Map<Long, Double> getRoleFactors() {
        return Collections.unmodifiableMap(roleFactors);
    }

    /**
     * Adds the specified factor for the given role to the role factor details.
     * 
     * @param roleKey
     *            The key of the role.
     * @param factorToAdd
     *            The role related factor that has to be stored.
     */
    public void addRoleFactor(Long roleKey, double factorToAdd) {
        if (roleFactors.containsKey(roleKey)) {
            Double currentFactor = roleFactors.get(roleKey);
            Double newFactor = Double.valueOf(currentFactor.doubleValue()
                    + factorToAdd);
            roleFactors.put(roleKey, newFactor);
        } else {
            roleFactors.put(roleKey, Double.valueOf(factorToAdd));
        }
    }
}
