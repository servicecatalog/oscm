/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 07.07.2014                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import org.oscm.validator.ADMValidator;

/**
 * Validator for group id fields. The general validation is similar to other id
 * field, but the length differs.
 * 
 */
public class GroupIdValidator extends IdValidator {

    /**
     * Returns the maximum allowed length for a group id field. <br>
     * 
     * @see ADMValidator.LENGTH_USER_GROUP_NAME.
     */
    @Override
    protected int getMaxLength() {
        return ADMValidator.LENGTH_USER_GROUP_NAME;
    }

}
