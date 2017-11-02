/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 19.08.15 11:04
 *
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.validator.FacesValidator;

import org.oscm.validator.ADMValidator;

/**
 * JSF id validation which Checks that an id contains only valid characters.
 */
@FacesValidator("banvalidator")
public class BillingAdapterNameValidator extends IdValidator {

    /**
     * Returns the maximum allowed length for a id field. <br>
     * 
     * @see ADMValidator.LENGTH_NAME.
     */
    @Override
    protected int getMaxLength() {
        return ADMValidator.LENGTH_NAME;
    }
}
