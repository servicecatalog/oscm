/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 2, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

/**
 * @author qiu
 * 
 */
public class FieldInfo {

    private VariableInfo newField;
    private VariableInfo oldField;

    public VariableInfo getNewField() {
        return newField;
    }

    public void setNewField(VariableInfo newField) {
        this.newField = newField;
    }

    public VariableInfo getOldField() {
        return oldField;
    }

    public void setOldField(VariableInfo oldField) {
        this.oldField = oldField;
    }

}
