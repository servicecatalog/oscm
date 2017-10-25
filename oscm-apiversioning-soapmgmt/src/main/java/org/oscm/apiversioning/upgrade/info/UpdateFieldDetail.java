/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import java.util.List;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;

/**
 * @author qiu
 * 
 */
public class UpdateFieldDetail extends ModificationDetail {

    public UpdateFieldDetail(ModificationType type, VariableInfo variable,
            ModificationPart part, List<FieldInfo> fields) {
        super(type, part, variable);
        this.fields = fields;
    }

    private List<FieldInfo> fields;

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

}
