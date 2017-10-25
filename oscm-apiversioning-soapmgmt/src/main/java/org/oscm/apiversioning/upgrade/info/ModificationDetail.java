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
public class ModificationDetail {

    private ModificationType type;
    private ModificationPart part;
    private VariableInfo variable;

    public ModificationDetail(ModificationType type, ModificationPart part,
            VariableInfo variable) {
        this.type = type;
        this.part = part;
        this.variable = variable;
    }

    public ModificationType getType() {
        return type;
    }

    public void setType(ModificationType type) {
        this.type = type;
    }

    public VariableInfo getVariable() {
        return variable;
    }

    public void setVariable(VariableInfo variable) {
        this.variable = variable;
    }

    public ModificationPart getPart() {
        return part;
    }

    public void setPart(ModificationPart part) {
        this.part = part;
    }

}
