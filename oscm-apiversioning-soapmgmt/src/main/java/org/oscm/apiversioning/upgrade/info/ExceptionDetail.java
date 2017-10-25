/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 7, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;

/**
 * @author zhaoh.fnst
 * 
 */
public class ExceptionDetail extends ModificationDetail {

    String newExceptionName;

    public ExceptionDetail(ModificationType type, ModificationPart part,
            VariableInfo variable, String newExceptionName) {
        super(type, part, variable);
        this.newExceptionName = newExceptionName;
    }

    public String getNewExceptionName() {
        return newExceptionName;
    }

    public void setNewExceptionName(String newExceptionName) {
        this.newExceptionName = newExceptionName;
    }

}
