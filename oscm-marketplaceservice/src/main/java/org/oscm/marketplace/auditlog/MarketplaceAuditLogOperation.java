/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.05.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.marketplace.auditlog;

import org.oscm.auditlog.AuditLogParameter;

/**
 * @author Min CHEN
 * 
 */
public enum MarketplaceAuditLogOperation {
    SET_SERVICE_AS_PUBLIC("30091", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SERVICE_PUBLIC),

    ASSIGN_SERVICE_TO_MARKETPLACE("30092", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.MARKETPLACE_ID,
            AuditLogParameter.MARKETPLACE_NAME),

    ASSIGN_CATAGORIES("30095", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.CATEGORIES_ID);

    private String operationId;
    private AuditLogParameter[] parameters;

    private MarketplaceAuditLogOperation(String operationId,
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
