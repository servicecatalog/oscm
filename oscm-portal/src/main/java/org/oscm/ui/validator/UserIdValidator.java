/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.04.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import org.oscm.validator.ADMValidator;

/**
 * Validator for user id fields. The general validation is similar to other id
 * field, but the length differs.
 * 
 */
public class UserIdValidator extends IdValidator {

    /**
     * Returns the maximum allowed length for a user id field. <br>
     * 
     * @see ADMValidator.LENGTH_USERID.
     */
    @Override
    protected int getMaxLength() {
        return ADMValidator.LENGTH_USERID;
    }

}
