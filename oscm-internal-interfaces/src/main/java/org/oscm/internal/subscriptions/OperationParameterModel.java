/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 07.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import static org.oscm.internal.types.enumtypes.OperationParameterType.INPUT_STRING;
import static org.oscm.internal.types.enumtypes.OperationParameterType.REQUEST_SELECT;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.oscm.internal.vo.VOServiceOperationParameter;

/**
 * Model for handling operation parameters from view.
 * 
 * @author weiser
 * 
 */
public class OperationParameterModel implements Serializable {

    private static final long serialVersionUID = -1947404568059017120L;

    VOServiceOperationParameter parameter;
    List<SelectItem> values = new LinkedList<>();

    public VOServiceOperationParameter getParameter() {
        return parameter;
    }

    public void setParameter(VOServiceOperationParameter parameter) {
        this.parameter = parameter;
    }

    public List<SelectItem> getValues() {
        return values;
    }

    public void setValues(List<SelectItem> values) {
        this.values = values;
        if (this.parameter == null) {
        	return;
        }
        if (getValue() != null && !getValue().equals("")) {
        	return;
        }
        if (values != null && !values.isEmpty()) {
        	setValue(values.get(0).getValue().toString());
        }
    }

    public boolean isRenderInput() {
        return parameter.getType().equals(INPUT_STRING);
    }

    public boolean isRenderSelect() {
        return parameter.getType().equals(REQUEST_SELECT);
    }

    public boolean isMandatory() {
        return parameter.isMandatory();
    }

    public String getValue() {
        return parameter.getParameterValue();
    }

    public void setValue(String value) {
        parameter.setParameterValue(value);
    }

    public String getId() {
        return parameter.getParameterId();
    }

    public String getName() {
        return parameter.getParameterName();
    }
}
