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
public class OrganizationVatQuery extends UserOperationLogQuery {

    private static final String ORGAN_VAT = "";

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "organization", "id", "referencetype", "customer", "id", "vat" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_VAT;
    }

    @Override
    public String getQuery() {
        return ORGAN_VAT;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "ORGAN_VAT";
    }
}
