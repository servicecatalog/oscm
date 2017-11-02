/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterEncoder;
import org.oscm.resolver.IPResolver;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.common.SessionListener;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * This bean is responsible for managing the confirmation process of an new user
 * account.
 */
public class ConfirmationBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 51857665841358988L;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ConfirmationBean.class);

    // JSF Managed properties
    private SessionBean sessionBean;
    private String encodedParam;

    private String organizationId;

    // Control the visibility of page elements
    private boolean showError = false;
    private boolean showButton = true;
    private boolean showContent = true;

    /**
     * This getter is used as an "onPageLoad" handler. It'll be invoked as soon
     * as the corresponding web page is loaded. The method encapsulates the
     * business logic behind the confirm process.
     * 
     * @return Since the return value is not relevant for further processing,
     *         the string "ok" will be returned in any case.
     */
    @PostConstruct
    public void initialize() {
        if (encodedParam == null || encodedParam.length() == 0) {
            addErrorMessage(BaseBean.ERROR_CONFIRMATION_INVALID_LINK, false,
                    false);
            return;
        }

        // Bug 7865: Remove the dummy postfix if exist
        if (encodedParam.endsWith("&et")) {
            encodedParam = encodedParam.substring(0,
                    encodedParam.indexOf("&et"));
        }

        String[] decodedParam = ParameterEncoder.decodeParameters(encodedParam);
        if (decodedParam == null) {
            addErrorMessage(BaseBean.ERROR_CONFIRMATION_INVALID_LINK, false,
                    false);
            return;
        }

        String userId = null;
        String serviceKeyString = null;
        String marketplaceId = null;
        switch (decodedParam.length) {
        case 4:
            serviceKeyString = decodedParam[3];
            try {
                // Store the service id in order to redirect to the service
                // after login.
                Long serviceKey = Long.valueOf(serviceKeyString);
                sessionBean.setSubscribeToServiceKey(serviceKey);
                sessionBean.setServiceKeyForPayment(serviceKey);
            } catch (NumberFormatException ex) {
                // The service key is invalid
                addErrorMessage(BaseBean.ERROR_CONFIRMATION_INVALID_LINK, false,
                        false);
                return;
            }
        case 3:
            marketplaceId = decodedParam[2];
            setMarketplaceId(marketplaceId);
        case 2:
            organizationId = decodedParam[0];
            userId = decodedParam[1];
            break;
        default:
            addErrorMessage(BaseBean.ERROR_CONFIRMATION_INVALID_LINK, false,
                    false);
            return;
        }

        // Acquire user details and check for the specific status of the user.
        IdentityService service = getIdService();

        VOUser voUser = new VOUser();
        voUser.setOrganizationId(organizationId);
        voUser.setUserId(userId);

        try {
            voUser = service.getUser(voUser);

            if (voUser.getStatus() != UserAccountStatus.LOCKED_NOT_CONFIRMED) {
                // The user already confirmed the registration => shown an error
                // message and the continue button
                addErrorMessage(BaseBean.ERROR_USER_ALREADY_CONFIRMED, true);
            } else {
                // The state of the user is ok, proceed with the confirmation.
                try {
                    service.confirmAccount(voUser, marketplaceId);

                    FacesContext fc = FacesContext.getCurrentInstance();
                    VOUserDetails currentUser = getUserFromSessionWithoutException(
                            fc);
                    HttpServletRequest request = getRequest();
                    HttpSession session = request.getSession(false);
                    if (currentUser != null && currentUser.getUserId() != null
                            && !currentUser.getUserId().equals(userId)) {
                        // b7300
                        // Another user is currently logged in => reset the
                        // session and prepare for login of the currently
                        // registered user
                        session.removeAttribute(Constants.SESS_ATTR_USER);
                        getSessionService()
                                .deletePlatformSession(session.getId());
                        SessionListener.cleanup(session);

                        session = request.getSession(true);

                        if (sessionBean != null) {
                            session.setAttribute(
                                    Constants.SESS_ATTR_SESSION_BEAN,
                                    sessionBean);
                        }
                        setMarketplaceId(marketplaceId);
                    }

                    if (isServiceProvider()) {
                        String password = (String) session
                                .getAttribute(Constants.REQ_ATTR_PASSWORD);
                        loginUser(voUser, password, request, session);
                    }
                } catch (MailOperationException e) {
                    addErrorMessage(
                            BaseBean.ERROR_REGISTRATION_ACKNOWLEDGE_MAIL,
                            false);
                } catch (OperationNotPermittedException e) {
                    addErrorMessage(BaseBean.ERROR_USER_LOCKED, false);
                } catch (LoginException | CommunicationException e) {
                    addErrorMessage(BaseBean.ERROR_USER_CONFIRMED_LOGIN_FAIL,
                            true);
                }
            }
        } catch (ObjectNotFoundException e) {
            addErrorMessage(e.getMessageKey(), false);
        } catch (OperationNotPermittedException e) {
            addErrorMessage(e.getMessageKey(), false);
        } catch (OrganizationRemovedException e) {
            addErrorMessage(e.getMessageKey(), false);
        }
    }

    /**
     * Adds a error message to the page and sets showError=true
     * 
     * @param errorKey
     *            The error key.
     * @param showContinueButton
     *            true: the continue button is visible on the page.
     */
    private void addErrorMessage(String errorKey, boolean showContinueButton) {
        addMessage(null, FacesMessage.SEVERITY_ERROR, errorKey);
        showError = true;
        showButton = showContinueButton;
    }

    /**
     * Adds a error message to the page and sets showError=true
     * 
     * @param errorKey
     *            The error key.
     * @param showContinueButton
     *            true: the continue button is visible on the page.
     */
    private void addErrorMessage(String errorKey, boolean showContinueButton,
            boolean showContent) {
        addErrorMessage(errorKey, showContinueButton);
        this.showContent = showContent;
    }

    public boolean getShowError() {
        return showError;
    }

    public boolean getShowButton() {
        return showButton;
    }

    public boolean getShowContent() {
        return showContent;
    }

    public void setInit(@SuppressWarnings("unused") String init) {
        // Only the getter is relevant for its use as "onPageLoad" handler.
    }

    public String getEncodedParam() {
        return encodedParam;
    }

    public void setEncodedParam(String encodedParam) {
        this.encodedParam = encodedParam;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    boolean isServiceProvider() {
        ConfigurationService service = getConfigurationService();
        return service.isServiceProvider();
    }

    void loginUser(VOUser voUser, String password,
            HttpServletRequest httpRequest, HttpSession session)
            throws LoginException, CommunicationException {
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(session);
        IdentityService service = getIdService();

        // authenticate the user
        httpRequest.getSession();
        try {
            httpRequest.login(String.valueOf(voUser.getKey()), password);
        } catch (ServletException e) {
            throw new LoginException(e.getMessage());
        }
        serviceAccess.login(voUser, password, httpRequest, getResponse());

        // log info on the successful login
        logger.logInfo(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS,
                voUser.getUserId(), IPResolver.resolveIpAddress(httpRequest),
                voUser.getTenantId());

        // read the user details value object and store it in the session
        session.setAttribute(Constants.SESS_ATTR_USER,
                service.getCurrentUserDetails());
    }
}
