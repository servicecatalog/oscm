/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *  Completion Time: 18.02.2009
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.model.User;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Backing bean for registration related actions
 * 
 */
@ViewScoped
@ManagedBean(name="registrationBean")
public class RegistrationBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -1626833133436182182L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(RegistrationBean.class);

    private String verificationCode;
    private boolean acceptTerms;
    private VOOrganization organization;
    private String password;
    private String supplierId;
    protected User user;
    transient UserBean userBean;

    public RegistrationBean() {
        setSessionAttribute(Constants.CAPTCHA_INPUT_STATUS, Boolean.FALSE);
        init();
    }

    void init() {
        HttpServletRequest httpRequest = getRequest();
        Boolean isSamlRequest = (Boolean) getRequest().getAttribute(
                Constants.REQ_ATTR_IS_SAML_FORWARD);
        if (Boolean.TRUE.equals(isSamlRequest)) {
            String userId = (String) httpRequest
                    .getAttribute(Constants.REQ_PARAM_USER_ID);
            getUser().setUserId(userId);
        } else if (!isInternalMode()) {
            getUserBean().showRegistration();
        }
    }

    /**
     * Get a new organization object.
     * 
     * @return the new organization object.
     */
    public VOOrganization getOrganization() {

        if (organization == null) {
            organization = new VOOrganization();
            FacesContext fc = FacesContext.getCurrentInstance();
            Locale locale = fc.getViewRoot().getLocale();
            organization.setLocale(locale.toString());
        }
        return organization;
    }

    /**
     * Get the password.
     * 
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    public String getSupplierId() {
        if (supplierId == null) {
            supplierId = getRequest().getParameter(
                    Constants.REQ_PARAM_SUPPLIER_ID);
        }

        return supplierId;
    }

    /**
     * Get a new user object.
     * 
     * @return the new user object.
     */
    public User getUser() {

        if (user == null) {
            user = new User(new VOUserDetails());
            FacesContext fc = FacesContext.getCurrentInstance();
            Locale locale = fc.getViewRoot().getLocale();
            user.setLocale(locale.toString());
        }
        return user;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public boolean isAcceptTerms() {
        return acceptTerms;
    }

    public boolean isInternalMode() {
        return getUserBean().isInternalAuthMode();
    }

    UserBean getUserBean() {
        if (userBean == null) {
            userBean = ui.findUserBean();
        }
        return userBean;
    }

    /**
     * A yet unknown Internet user registers as a new organization at the
     * platform.
     * 
     * @return the logical outcome.
     * @throws NonUniqueBusinessKeyException
     *             Thrown if the organizationId is not unique
     * @throws ValidationException
     *             Thrown if the validation in the service layer failed
     * @throws ObjectNotFoundException
     *             Thrown if the organization contains a supplierId which
     *             doesn't exist in the database
     * @throws MailOperationException
     * @throws RegistrationException
     *             Thrown if no supplier is specified or in case the specified
     *             organization is not a supplier.
     */
    public String register() throws NonUniqueBusinessKeyException,
            ValidationException, ObjectNotFoundException,
            MailOperationException, RegistrationException {
        if (logger.isDebugLoggingEnabled()) {

        }

        String mId = getMarketplaceId();
        String parameter = getRequest().getParameter(
                Constants.REQ_PARAM_SERVICE_KEY);
        String outcome = BaseBean.OUTCOME_SUCCESS;

        Long serviceKey = null;
        if (parameter != null && parameter.trim().length() > 0) {
            serviceKey = Long.valueOf(parameter);
        }
        // FIXME: Must be fixed in identity service.
        if (!isInternalMode()) {
            // A confirmation mail must be send, not a user created mail.
            // If no password is given it will be generated
            password = "";
        }
        try {
            organization = getAccountingService().registerCustomer(
                    getOrganization(), user.getVOUserDetails(), password,
                    serviceKey, mId, getSupplierId());
        } catch (NonUniqueBusinessKeyException ex) {
            if (isInternalMode()) {
                throw ex;
            }
            ex.setMessageKey(BaseBean.ERROR_USER_ALREADY_EXIST);
            ExceptionHandler.execute(ex, true);
            return BaseBean.OUTCOME_ERROR;
        }
        if (logger.isDebugLoggingEnabled()) {

        }
        return outcome;
    }

    public void setAcceptTerms(final boolean acceptTerms) {
        this.acceptTerms = acceptTerms;
    }

    /**
     * Set the password.
     * 
     * @param password
     *            the password to be set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public void setVerificationCode(final String verificationCode) {
        this.verificationCode = verificationCode;
    }

    /**
     * Add a validation error to the context if the acceptTerms is not set.
     * 
     * @param context
     *            the context
     * @param toValidate
     *            checkbox which is validated
     * @param value
     *            the value to validate
     */
    public void validateAcceptTerm(final FacesContext context,
            final UIComponent toValidate, final Object value) {
        Boolean accept = (Boolean) value;
        if (!accept.booleanValue()) {
            ((UISelectBoolean) toValidate).setValid(false);
            addMessage(toValidate.getClientId(context),
                    FacesMessage.SEVERITY_ERROR, ERROR_REGISTRATION_TERMS);
        }
    }

}
