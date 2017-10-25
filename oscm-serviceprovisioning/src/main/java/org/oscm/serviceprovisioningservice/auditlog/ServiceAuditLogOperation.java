/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import org.oscm.auditlog.AuditLogParameter;

/**
 * @author Mao
 * 
 */
public enum ServiceAuditLogOperation {

    DEFINE_SERVICE("30090", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.TECHSERVICE_NAME,
            AuditLogParameter.SHORT_DESCRIPTION, AuditLogParameter.DESCRIPTION,
            AuditLogParameter.LOCALE, AuditLogParameter.AUTO_ASSIGN_USER),

    PUBLISH_SERVICE("30091", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SERVICE_PUBLIC),

    ASSIGN_SERVICE_TO_MARKETPLACE("30092",
            AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.MARKETPLACE_ID,
            AuditLogParameter.MARKETPLACE_NAME),

    ASSIGN_SERVICE_BROKERS("30093", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.BROKER_ID),

    ASSIGN_SERVICE_RESELLERS("30094", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.RESELLER_ID),

    DEASSIGN_SERVICE_RESELLER("30103", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.RESELLER_ID),

    DEASSIGN_SERVICE_BROKER("30104", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.BROKER_ID),

    ASSIGN_SERVICE_CATEGORIES("30095", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.CATEGORIES_ID),

    UPDATE_SERVICE("30096", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SHORT_DESCRIPTION, AuditLogParameter.DESCRIPTION,
            AuditLogParameter.LOCALE, AuditLogParameter.AUTO_ASSIGN_USER),

    UPDATE_SERVICE_PARAMETERS("30097", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.PARAMETER_NAME,
            AuditLogParameter.USEROPTION, AuditLogParameter.PARAMETER_VALUE),

    LOCALIZE_SERVICE("30098", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SHORT_DESCRIPTION, AuditLogParameter.DESCRIPTION,
            AuditLogParameter.LOCALE),

    COPY_SERVICE("30099", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.COPY_SERVICE_ID,
            AuditLogParameter.COPY_SERVICE_NAME),

    DELETE_SERVICE("30100", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME),

    DEFINE_UPGRADE_DOWNGRADE_SERVICE("30101", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.TARGET_SERVICE_ID,
            AuditLogParameter.TARGET_SERVICE_NAME,
            AuditLogParameter.UPDOWNGRADE),

    ACTIVATE_DEACTIVATE_SERVICE("30102", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.MARKETPLACE_ID,
            AuditLogParameter.MARKETPLACE_NAME, AuditLogParameter.ACTIVATION,
            AuditLogParameter.INCATALOG);

    private String operationId;
    private AuditLogParameter[] parameters;

    private ServiceAuditLogOperation(String operationId,
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
