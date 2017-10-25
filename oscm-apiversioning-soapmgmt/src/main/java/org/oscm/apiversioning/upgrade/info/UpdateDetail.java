/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;

/**
 * @author qiu
 * 
 */
public class UpdateDetail extends ModificationDetail {

    public UpdateDetail(ModificationType type, ModificationPart part,
            VariableInfo newVariable, VariableInfo oldVariable,
            boolean isMethodRenamed, String newMethodName,
            String oldMethodName, boolean isRequest) {
        super(type, part, newVariable);
        this.oldVariable = oldVariable;
        this.isMethodRenamed = isMethodRenamed;
        this.newMethodName = newMethodName;
        this.oldMethodName = oldMethodName;
        this.isRequest = isRequest;
    }

    private VariableInfo oldVariable;

    private boolean isMethodRenamed;
    private String newMethodName;
    private String oldMethodName;
    private boolean isRequest;

    public boolean isMethodRenamed() {
        return isMethodRenamed;
    }

    public void setMethodRenamed(boolean isMethodRenamed) {
        this.isMethodRenamed = isMethodRenamed;
    }

    public String getNewMethodName() {
        return newMethodName;
    }

    public void setNewMethodName(String newMethodName) {
        this.newMethodName = newMethodName;
    }

    public String getOldMethodName() {
        return oldMethodName;
    }

    public void setOldMethodName(String oldMethodName) {
        this.oldMethodName = oldMethodName;
    }

    public VariableInfo getOldVariable() {
        return oldVariable;
    }

    public void setOldVariable(VariableInfo oldVariable) {
        this.oldVariable = oldVariable;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

}
