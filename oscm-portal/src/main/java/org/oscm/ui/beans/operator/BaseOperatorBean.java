/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                    
 *                                                                              
 *  Creation Date: 27.01.2011                                                      
 *                                                                              
 *  Completion Time: <date> 
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import java.io.Serializable;

import javax.faces.application.FacesMessage;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.intf.OperatorService;

/**
 * Base bean for operator service functionality.
 * 
 * @author weiser
 * 
 */
public class BaseOperatorBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 3046211428448842791L;

    protected static final String INFO_TASK_SUCCESSFUL = "operator.task.successful";

    protected static final String ERROR_TASK_EXECUTION = "operator.task.error";
    protected static final String ERROR_EXPORT_BILLING_DATA = "operator.exportBillingData.error";
    protected static final String ERROR_SHOW_BILLING_DATA = "operator.showBillingData.error";
    protected static final String ERROR_ORGANIZATION_ROLE_NOTSET = "error.organization.role.notset";
    protected static final String ERROR_EXPORT_AUDIT_LOG_DATA = "operator.exportAuditLogData.error";
    protected static final String ERROR_SHOW_AUDIT_LOG_DATA = "operator.showAuditLogData.error";
    protected static final String ERROR_EXPORT_AUDIT_LOG_TOO_MANY_ENTRIES = "ex.AuditLogTooManyRowsException";

    private OperatorService operatorService;

    /**
     * Initializes the operator service access if necessary and returns it.
     * 
     * @return the operator service
     */
    protected OperatorService getOperatorService() {
        operatorService = getService(OperatorService.class, operatorService);
        return operatorService;
    }

    /**
     * Checks the result of operator service functions that return a boolean
     * value and depending on the result either print a generic success or error
     * message and return the outcome string.
     * 
     * @param result
     *            the returned boolean result (true means successful)
     * @return the logical outcome.
     */
    protected String getOutcome(boolean result) {
        if (result) {
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TASK_SUCCESSFUL);
            return OUTCOME_SUCCESS;
        }
        addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_TASK_EXECUTION);
        return OUTCOME_ERROR;
    }

}
