/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.validation.Invariants;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class OperationParameterRow extends OperationRow {

    private static final long serialVersionUID = -4134317796788352160L;

    private VOServiceOperationParameter parameter;

    public OperationParameterRow(VOTechnicalServiceOperation operation,
            VOServiceOperationParameter parameter) {
        super(operation);
        Invariants.assertNotNull(parameter);
        this.parameter = parameter;
    }

    @Override
    public String getOperationId() {
        return null;
    }

    @Override
    public String getParameterId() {
        return parameter.getParameterId();
    }

    @Override
    public String getDisplayName() {
        return parameter.getParameterName();
    }

    @Override
    public String getOperationDescription() {
        return null;
    }

    @Override
    public boolean isOperation() {
        return false;
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public void setOperationDescription(String description) {
        // do nothing
    }

    @Override
    public String getName() {
        return parameter.getParameterName();
    }

    @Override
    public void setName(String name) {
        parameter.setParameterName(name);
    }

}
