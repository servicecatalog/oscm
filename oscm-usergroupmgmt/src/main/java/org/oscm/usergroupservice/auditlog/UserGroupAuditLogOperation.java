/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 7, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.auditlog;

import org.oscm.auditlog.AuditLogParameter;

/**
 * @author zhaoh.fnst
 * 
 */
public enum UserGroupAuditLogOperation {

    ENABLE_ACCESS_TO_SERVICES("30105", AuditLogParameter.GROUP,
            AuditLogParameter.MARKETPLACE_ID,
            AuditLogParameter.MARKETPLACE_NAME, AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SELLER_ID),

    DISABLE_ACCESS_TO_SERVICES("30108", AuditLogParameter.GROUP,
            AuditLogParameter.MARKETPLACE_ID,
            AuditLogParameter.MARKETPLACE_NAME, AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SELLER_ID),

    ASSIGN_USER_TO_GROUP("30106", AuditLogParameter.USER,
            AuditLogParameter.GROUP),

    REMOVE_USER_FROM_GROUP("30107", AuditLogParameter.USER,
            AuditLogParameter.GROUP);

    private String operationId;
    private AuditLogParameter[] parameters;

    private UserGroupAuditLogOperation(String operationId,
            AuditLogParameter... parameters) {
        this.operationId = operationId;
        this.parameters = parameters;
    }

    public String getOperationId() {
        return operationId;
    }

    public AuditLogParameter[] getParameters() {
        return parameters;
    }

}
