/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Validator interface
 */
public interface Validator {

    /**
     * Checks if object is supported by validator implementation. If so then
     * validate action is executed.
     * 
     * @param obj
     * @return
     */
    boolean supports(Object obj);

    /**
     * Method used to validate given object. If validation fails then concrete
     * exception with message should be thrown.
     * 
     * @param obj
     * @throws SaaSApplicationException
     */
    void validate(Object obj) throws ValidationException;
}
