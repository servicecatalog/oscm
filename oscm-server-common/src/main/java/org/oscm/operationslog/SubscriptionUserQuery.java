/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: barzu                                               
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 13, 2011                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Query for subscription usage license operations.
 * 
 * @author barzu
 */
public class SubscriptionUserQuery extends UserOperationLogQuery {

    private static final String SUBSCR_USER = "SELECT ul.modDate, ul.modType, (CASE WHEN mu.userId IS NOT NULL THEN mu.userId ELSE ul.modUser END) AS userId2, ul.objVersion"
            + SubscriptionQuery.SUBSCR_COMMON_SELECT
            + ", pu.userId, pu.firstName, pu.lastName, pu.email, ul.applicationUserId, rd.roleId"
            + SubscriptionQuery.SUBSCR_COMMON_FROM
            + " JOIN UsageLicenseHistory ul ON ul.subscriptionObjKey = s.objKey"
            + " JOIN PlatformUserHistory pu ON ul.userObjKey = pu.objKey"
            + " LEFT JOIN RoleDefinitionHistory rd ON ul.roleDefinitionObjKey = rd.objKey"
            + " LEFT JOIN PlatformUserHistory mu ON ul.modUser = CAST(mu.objKey AS VARCHAR(255))"
            + " WHERE ul.modDate >= :startDate AND ul.modDate <= :endDate"
            + SubscriptionQuery.SUBSCR_COMMON_AND
            + " ul.modDate)"
            + " AND s.objVersion = (SELECT MAX(s2.objVersion) FROM SubscriptionHistory s2 WHERE ul.subscriptionObjKey = s2.objKey AND s2.modDate <= ul.modDate)"
            + " AND pu.objVersion = (SELECT MAX(pu2.objVersion) FROM PlatformUserHistory pu2 WHERE ul.userObjKey = pu2.objKey AND pu2.modDate <= ul.modDate)"
            + " AND (rd.objVersion IS NULL OR rd.objVersion = (SELECT MAX(rd2.objVersion) FROM RoleDefinitionHistory rd2 WHERE ul.roleDefinitionObjKey = rd2.objKey AND rd2.modDate <= ul.modDate))"
            + " AND (mu.objVersion IS NULL OR mu.objVersion = (SELECT MAX(mu2.objVersion) FROM PlatformUserHistory mu2 WHERE ul.modUser = CAST(mu2.objKey AS VARCHAR(255)) AND mu2.modDate <= ul.modDate))"
            + SubscriptionQuery.SUBSCR_COMMON_ORDERBY;

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "subscription", "customer", "customer id", "user id", "first name",
            "last name", "email", "app user id", "app user role" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_USER;
    }

    @Override
    public String getQuery() {
        return SUBSCR_USER;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SUBSCR_USER";
    }
}
