/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.io.Serializable;

import org.oscm.validation.Invariants;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author zhaohang
 * 
 */
public class OperationRow implements Serializable {

    private static final long serialVersionUID = -3915406824053919504L;

    private VOTechnicalServiceOperation operation;

    public OperationRow(VOTechnicalServiceOperation voOperation) {
        Invariants.assertNotNull(voOperation);
        operation = voOperation;
    }

    public VOTechnicalServiceOperation getVOOperation() {
        return operation;
    }

    public String getOperationId() {
        return operation.getOperationId();
    }

    public String getParameterId() {
        return null;
    }

    public String getDisplayName() {
        return operation.getOperationName();
    }

    public String getOperationDescription() {
        return operation.getOperationDescription();
    }

    public void setOperationDescription(String description) {
        operation.setOperationDescription(description);
    }

    public boolean isOperation() {
        return true;
    }

    public boolean isParameter() {
        return false;
    }

    public String getName() {
        return operation.getOperationName();
    }

    public void setName(String name) {
        operation.setOperationName(name);
    }
}
