/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;

/**
 * Validate that the from date is not after the to date.
 * 
 */
public class DateFromToValidator implements Validator {

    private static String FROM_DATE = "fromDate";
    private static String TO_DATE = "toDate";
    private Date fromDate;
    private Date toDate;

    /**
     * Validate that the from date is not after the to date.
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
    public void validate(final FacesContext facesContext,
            final UIComponent component, final Object value)
            throws ValidatorException {
        // determine the id of the component
        String clientId = component.getClientId(facesContext);

        if (value == null) {
            handleError(facesContext, clientId, "javax.faces.component.UIInput.REQUIRED");
        }

        Date fromDate = getFromDate();
        Date toDate = getToDate();

        String msgKey = null;
        if (clientId.endsWith(FROM_DATE)) {
            fromDate = (Date) value;
            msgKey = BaseBean.ERROR_FROM_DATE_AFTER_TO_DATE;
        } else if (clientId.endsWith(TO_DATE)) {
            msgKey = BaseBean.ERROR_TO_DATE_BEFORE_FROM_DATE;
            toDate = (Date) value;
        }

        if (fromDate != null && toDate != null) {
            if (fromDate.after(toDate)) {
                handleError(facesContext, clientId, msgKey);
            }
        }
    }

    protected void handleError(final FacesContext facesContext,
            @SuppressWarnings("unused") final String clientid,
            final String msgKey) throws ValidatorException {
        String text = JSFUtils.getText(msgKey, null, facesContext);
        throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getToDate() {
        return toDate;
    }
}
