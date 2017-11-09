/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.validator.ADMValidator;

/**
 * Validate that two password fields contain the same value.
 * 
 */
public class PasswordValidator implements Validator {

    /**
     * Validates that the given value is equal to the value of the field which
     * has the id that starts with the same characters as the id of the current
     * field but doesn't end with a 2.
     * 
     * @param context
     *            FacesContext for the request we are processing
     * @param component
     *            UIComponent we are checking for correctness
     * @param value
     *            the value to validate
     * @throws ValidatorException
     *             if validation fails
     */
    @Override
    public void validate(final FacesContext facesContext,
            final UIComponent component, final Object value)
            throws ValidatorException {
        if (value == null) {
            return;
        }

        String password = "";
        String password2 = "";

        // determine the ids of the password fields
        String clientId = component.getClientId(facesContext);
        String clientId2 = clientId;
        boolean check2 = false; // check for first or second occurrence?
        if (clientId != null) {
            if (clientId.endsWith("2")) {
                clientId = clientId.substring(0, clientId.length() - 1);
                password2 = value.toString();
                check2 = true;
                // Find the JSF component for this id.
                UIInput passwordInput = (UIInput) facesContext.getViewRoot()
                        .findComponent(clientId);
                if (passwordInput != null) {
                    if (passwordInput.getSubmittedValue() != null) {
                        // field not yet validated
                        password = (String) passwordInput.getSubmittedValue();
                    } else {
                        // field has already been validated
                        password = (String) passwordInput.getValue();
                    }
                }
            } else {
                clientId2 = clientId + "2";
                password = value.toString();
                // Find the JSF component for this id.
                UIInput passwordInput = (UIInput) facesContext.getViewRoot()
                        .findComponent(clientId2);
                if (passwordInput != null) {
                    if (passwordInput.getSubmittedValue() != null) {
                        // field not yet validated
                        password2 = (String) passwordInput.getSubmittedValue();
                    } else {
                        // field has already been validated
                        password2 = (String) passwordInput.getValue();
                    }
                }
            }

            // check first length prior to matching
            if (!check2 && !validPasswordLength(password)) {
                ((UIInput) component).setValid(false);
                String text = JSFUtils.getText(BaseBean.ERROR_USER_PWD_LENGTH,
                        null, facesContext);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, text, null));
            }
            if (!passwordsAreEqual(password, password2)) {
                ((UIInput) component).setValid(false);
                String text = JSFUtils.getText(BaseBean.ERROR_USER_PWD_MATCH,
                        null, facesContext);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, text, null));
            }
            // check second length after matching
            if (check2 && !validPasswordLength(password2)) {
                ((UIInput) component).setValid(false);
                String text = JSFUtils.getText(BaseBean.ERROR_USER_PWD_LENGTH,
                        null, facesContext);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, text, null));
            }
        }
    }

    /**
     * @return true, if both passwords are not null and equal
     */
    public static boolean passwordsAreEqual(String password, String password2) {
        return (password != null && password.equals(password2));
    }

    public static boolean validPasswordLength(String password) {
        return (password != null && password.length() >= ADMValidator.MIN_LENGTH_PASSWORD);
    }
}
