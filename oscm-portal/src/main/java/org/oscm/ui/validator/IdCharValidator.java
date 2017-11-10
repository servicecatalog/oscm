/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 08.10.2009                                                      
 *                                                                              
 *  Completion Time: 08.10.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import org.oscm.validator.ADMValidator;

/**
 * JSF id validation which Checks that an id contains only valid characters.
 */
public class IdCharValidator extends IdValidator {

    /**
     * Returns the maximum allowed length for a id field. <br>
     * 
     * @see ADMValidator.LENGTH_ID.
     */
    @Override
    protected int getMaxLength() {
        return ADMValidator.LENGTH_ID;
    }
}
