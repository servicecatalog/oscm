/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 25, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;

/**
 * Class to make list of user assignments and user role assignments from usage
 * license histories.
 * 
 * @author tokoda
 */
public class UserAssignmentExtractor {

    private final List<UsageLicenseHistory> ulHistList;

    private final long periodStart;

    private final long periodEnd;

    private final Map<Long, List<UserAssignment>> userAssignments = new HashMap<Long, List<UserAssignment>>();

    public UserAssignmentExtractor(List<UsageLicenseHistory> ulHistList,
            long periodStart, long periodEnd) {
        this.ulHistList = ulHistList;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public Set<Long> getUserKeys() {
        return userAssignments.keySet();
    }

    /**
     * Get the list of user assignments for specified user key with roles which
     * has been created by extract() method.
     */
    public List<UserAssignment> getUserAssignments(Long userKey) {
        return userAssignments.get(userKey);
    }

    /**
     * Creates a user assignment list with user role assignments based on the
     * provided usage license histories.
     */
    public void extract() {
        long userKeyOfPreviousHistory = 0;
        List<UsageLicenseHistory> ulHistListOfOneUser = new ArrayList<UsageLicenseHistory>();
        for (UsageLicenseHistory ulHist : ulHistList) {
            if (!ulHistListOfOneUser.isEmpty()
                    && userKeyOfPreviousHistory != ulHist.getUserObjKey()) {
                List<UserAssignment> userAssignmentsForOneUser = extractUserAssignmentsForOneUser(ulHistListOfOneUser);
                if (!userAssignmentsForOneUser.isEmpty()) {
                    userAssignments.put(Long.valueOf(userKeyOfPreviousHistory),
                            userAssignmentsForOneUser);
                }
                ulHistListOfOneUser.clear();
            }
            ulHistListOfOneUser.add(ulHist);
            userKeyOfPreviousHistory = ulHist.getUserObjKey();
        }

        if (!ulHistListOfOneUser.isEmpty()) {
            List<UserAssignment> userAssignmentsForOneUser = extractUserAssignmentsForOneUser(ulHistListOfOneUser);
            if (!userAssignmentsForOneUser.isEmpty()) {
                userAssignments.put(Long.valueOf(userKeyOfPreviousHistory),
                        userAssignmentsForOneUser);
            }
        }
    }

    private List<UserAssignment> extractUserAssignmentsForOneUser(
            List<UsageLicenseHistory> ulHistListOfOneUser) {

        List<UserAssignment> result = new ArrayList<UserAssignment>();

        UserAssignment userAssignment = null;
        for (UsageLicenseHistory ulHist : ulHistListOfOneUser) {
            if (isHistoryAfterTheTime(ulHist, periodEnd)) {
                userAssignment = null;
                continue;
            } else if (isDeleteHistoryBeforeTheTime(ulHist, periodStart)) {
                userAssignment = null;
                break;
            }

            if (userAssignment == null) {
                userAssignment = createNewUserAssignment(ulHist);
            }

            updateUserAssignmentAccordingToHistory(userAssignment, ulHist);

            if (ulHist.getModtype() == ModificationType.ADD) {
                result.add(userAssignment);
                userAssignment = null;
            }
        }

        if (userAssignment != null) {
            result.add(userAssignment);
        }

        extractUserRoleAssignmentsForOneUser(result, ulHistListOfOneUser);

        return result;
    }

    private boolean isHistoryAfterTheTime(UsageLicenseHistory ulHist, long time) {
        long historyModTime = ulHist.getModdate().getTime();
        return historyModTime > time;
    }

    private boolean isDeleteHistoryBeforeTheTime(UsageLicenseHistory ulHist,
            long time) {
        long historyModTime = ulHist.getModdate().getTime();
        return historyModTime < time
                && ulHist.getModtype() == ModificationType.DELETE;
    }

    private UserAssignment createNewUserAssignment(UsageLicenseHistory ulHist) {
        UserAssignment userAssignment = new UserAssignment();
        userAssignment.setUserKey(ulHist.getUserObjKey());
        userAssignment.setUserId(ulHist.getDataContainer()
                .getApplicationUserId());
        userAssignment.setUsageStartTime(periodStart);
        userAssignment.setUsageEndTime(periodEnd);
        return userAssignment;
    }

    private void updateUserAssignmentAccordingToHistory(
            UserAssignment userAssignment, UsageLicenseHistory ulHist) {
        long historyModdate = ulHist.getModdate().getTime();
        switch (ulHist.getModtype()) {
        case ADD:
            if (userAssignment.getUsageStartTime() <= historyModdate
                    && historyModdate <= userAssignment.getUsageEndTime()) {
                userAssignment.setUsageStartTime(historyModdate);
            }
            break;
        case DELETE:
            if (userAssignment.getUsageStartTime() <= historyModdate
                    && historyModdate <= userAssignment.getUsageEndTime()) {
                userAssignment.setUsageEndTime(historyModdate);
            }
            break;
        default:
            break;
        }
    }

    private void extractUserRoleAssignmentsForOneUser(
            List<UserAssignment> userAssignments,
            List<UsageLicenseHistory> ulHistListOfOneUser) {
        for (UserAssignment userAssignment : userAssignments) {
            List<UserRoleAssignment> userRoleAssignments = extractUserRoleAssignmentsForOneUserAssignment(
                    ulHistListOfOneUser, userAssignment.getUsageStartTime(),
                    userAssignment.getUsageEndTime());
            if (!userRoleAssignments.isEmpty()) {
                userAssignment.setRoleAssignments(userRoleAssignments);
            }
        }
    }

    private List<UserRoleAssignment> extractUserRoleAssignmentsForOneUserAssignment(
            List<UsageLicenseHistory> ulHistListOfOneUser,
            long userAssignmentStartTime, long userAssignmentEndTime) {

        List<UserRoleAssignment> userRoleAssignments = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment userRoleAssignment = null;
        UsageLicenseHistory previousHistory = null;
        for (UsageLicenseHistory currentHistory : ulHistListOfOneUser) {
            Long roleKey = currentHistory.getRoleDefinitionObjKey();
            if (roleKey != null) {
                if (isHistoryAfterTheTime(currentHistory, userAssignmentEndTime)) {
                    userRoleAssignment = null;
                    continue;
                }

                if (userRoleAssignment == null) {
                    userRoleAssignment = new UserRoleAssignment(roleKey,
                            userAssignmentStartTime, userAssignmentEndTime);
                } else if (previousHistory != null
                        && !roleKey.equals(previousHistory
                                .getRoleDefinitionObjKey())) {
                    long moddateOfPreviousHistory = previousHistory
                            .getModdate().getTime();
                    userRoleAssignment.setStartTime(moddateOfPreviousHistory);
                    userRoleAssignments.add(userRoleAssignment);
                    userRoleAssignment = new UserRoleAssignment(roleKey,
                            userAssignmentStartTime, moddateOfPreviousHistory);
                }
                if (currentHistory.getModdate().getTime() < userAssignmentStartTime
                        || currentHistory.getModtype() == ModificationType.ADD) {
                    break;
                }
            }
            previousHistory = currentHistory;
        }

        if (userRoleAssignment != null) {
            userRoleAssignments.add(userRoleAssignment);
        }

        setFlagForFirstAndLastRole(userRoleAssignments);
        return userRoleAssignments;
    }

    private void setFlagForFirstAndLastRole(
            List<UserRoleAssignment> userRoleAssignments) {
        if (!userRoleAssignments.isEmpty()) {
            userRoleAssignments.get(0).setLastRoleAssignment(true);
            userRoleAssignments.get(userRoleAssignments.size() - 1)
                    .setFirstRoleAssignment(true);
        }
    }
}
