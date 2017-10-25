/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 13, 2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * 
 * @author tokoda
 * 
 */
public class OrganizationUserQuery extends UserOperationLogQuery {

    private static final String ORGAN_USER = "";

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "organization", "id", "user id", "first name", "last name",
            "email", "phone", "realm user id", "locale", "status" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_USER;
    }

    @Override
    public String getQuery() {
        return ORGAN_USER;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "ORGAN_USER";
    }
}
