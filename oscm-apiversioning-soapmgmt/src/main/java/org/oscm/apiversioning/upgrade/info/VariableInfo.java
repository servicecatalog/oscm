/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

/**
 * @author qiu
 * 
 */
public class VariableInfo {

    private String variableClassName;
    private String variableName;

    public VariableInfo(String variableClassName, String variableName) {
        this.variableClassName = variableClassName;
        this.variableName = variableName;
    }

    public String getVariableClassName() {
        return variableClassName;
    }

    public void setVariableClassName(String variableClassName) {
        this.variableClassName = variableClassName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}
