/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 07.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class OperationModel implements Serializable {

    private static final long serialVersionUID = 1468577372189232222L;

    VOTechnicalServiceOperation operation;
    List<OperationParameterModel> parameters = new LinkedList<>();

    public VOTechnicalServiceOperation getOperation() {
        return operation;
    }

    public void setOperation(VOTechnicalServiceOperation operation) {
        this.operation = operation;
    }

    public List<OperationParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<OperationParameterModel> parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return operation.getOperationDescription();
    }
}
