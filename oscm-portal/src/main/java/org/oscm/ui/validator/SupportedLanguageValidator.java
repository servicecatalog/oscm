/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.12.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;

/**
 * 
 * @author Mao
 * 
 */

public class SupportedLanguageValidator implements Validator {

    public static final String APPLICATION_BEAN = "appBean";
    private ApplicationBean appBean;
    UiDelegate ui = new UiDelegate();

    protected ApplicationBean getApplicationBean() {
        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }
        return appBean;
    }

    @Override
    public void validate(FacesContext context, UIComponent uiComponent,
            Object input) throws ValidatorException {
        String value = null;
        if (input != null) {
            value = input.toString();
        }
        if (value == null || value.length() == 0) {
            return;
        }
        if (!getApplicationBean().getActiveLocales().contains(value)) {
            ValidatorException e = getException(JSFUtils.getText(
                    BaseBean.ERROR_LOCALE_INVALID, new String[] { new Locale(
                            value).getDisplayLanguage(ui.getViewLocale()) },
                    context));
            throw e;
        }
    }

    /**
     * Wraps the given message in a <code>FacesMessage</code> and throws it as
     * <code>ValidatorException</code>.
     * 
     * @param message
     *            the message string
     * @throws ValidatorException
     *             the <code>ValidatorException</code> containing the provided
     *             message
     */
    private ValidatorException getException(String message)
            throws ValidatorException {
        FacesMessage facesMessage = new FacesMessage(
                FacesMessage.SEVERITY_ERROR, message, null);
        return new ValidatorException(facesMessage);
    }
}
