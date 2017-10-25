/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.oscm.internal.types.enumtypes.ConfigurationKey.SSO_DEFAULT_TENANT_ID;
import static org.oscm.types.constants.Configuration.GLOBAL_CONTEXT;
import static org.oscm.ui.common.Constants.PORTAL_HAS_BEEN_REQUESTED;
import static org.oscm.ui.common.Constants.REQ_PARAM_TENANT_ID;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.AccessException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJBException;
import javax.faces.application.ViewExpiredException;
import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.SubscriptionStateException.Reason;
import org.oscm.internal.vo.*;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.resolver.IPResolver;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.UserNotAssignedException;
import org.oscm.ui.authorization.PageAuthorization;
import org.oscm.ui.authorization.PageAuthorizationBuilder;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.*;
import org.oscm.ui.model.User;
import org.oscm.ui.validator.PasswordValidator;

/**
 * Filter which checks that a request which tries to access a protected URL has
 * an active session containing a user value object. If this is not the case a
 * login is performed and on success the user value object is stored in the
 * session.
 * 
 */
public class AuthorizationFilter extends BaseBesFilter {

    static final String PARAM_LOGIN_USER_ID = "loginForm:loginUserId";
    private static final String PARAM_LOGIN_PASSWORD = "loginForm:loginPassword";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AuthorizationFilter.class);

    /**
     * Check if the current URL is protected and the current session doesn't
     * contain a user object. If this is the case perform a login.
     * 
     * The doFilter method of the Filter is called by the container each time a
     * request/response pair is passed through the chain due to a client request
     * for a resource at the end of the chain.
     * 
     * @throws IOException
     * @throws ServletException
     * 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = new IgnoreCharacterEncodingHttpRequestWrapper(
                (HttpServletRequest) request);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        AuthorizationRequestData rdo = initializeRequestDataObject(httpRequest);

        try {
            if (isPublicAccess(rdo, httpRequest)) {
                proceedWithFilterChain(chain, httpRequest, httpResponse);
            } else {
                handleProtectedUrlAndChangePwdCase(chain, httpRequest,
                        httpResponse, rdo);
            }
        } catch (ServletException e) {

            // relogin is not possible in this case,
            // no SAML response to extract userid and generate password.
            if (authSettings.isServiceProvider()) {
                throw e;
            }

            if (e.getCause() instanceof ViewExpiredException) {
                // if we were logged in but a logout occurs from a different
                // browser tab, we get this exception - so redirect to the
                // same page to stay on it (Bug 7552)
                final StringBuffer url = new StringBuffer(
                        rdo.getRelativePath() == null ? ""
                                : rdo.getRelativePath());
                reLogginUserIfRequired(httpRequest, httpResponse, rdo, url);
                sendRedirect(httpRequest, httpResponse, url.toString());
            } else {
                throw e;
            }
        }
    }

    private void rollbackDefaultTimeout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession();
        Integer attributeInt = (Integer) session
                .getAttribute(Constants.SESS_ATTR_DEFAULT_TIMEOUT);
        if (attributeInt != null) {
            session.setMaxInactiveInterval(attributeInt.intValue());
            session.removeAttribute(Constants.SESS_ATTR_DEFAULT_TIMEOUT);
        }
    }

    /**
     * Returns true if one of the following conditions is matching:<br>
     * 1) the landing page is requested and defined as public <br>
     * 2) the requested URL matches the public URL-pattern
     */
    boolean isPublicAccess(AuthorizationRequestData rdo,
            HttpServletRequest request) {

        // first, check if landing page is public
        if (rdo.isLandingPage()) {
            LandingpageConfigurationService service = lookupLandingpageConfigurationService(
                    request);
            try {
                LandingpageType type = service
                        .loadLandingpageType(rdo.getMarketplaceId());
                return type.isDefault();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // second, check url pattern of request
        return rdo.isPublicURL(publicUrlPattern);
    }

    private LandingpageConfigurationService lookupLandingpageConfigurationService(
            HttpServletRequest httpRequest) {
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(httpRequest.getSession());
        return serviceAccess.getService(LandingpageConfigurationService.class);
    }

    private void appendParam(StringBuffer url, String param, String value,
            String encoding) {
        if (url.indexOf("?") > -1)
            url.append('&');
        else
            url.append('?');
        url.append(param);
        url.append("=");
        try {
            url.append(URLEncoder.encode(value, encoding));
        } catch (UnsupportedEncodingException e) {
            throw new SaaSSystemException(e);
        }
    }

    /**
     * This method is not adapted used in SAML_SP case.
     * 
     */
    void reLogginUserIfRequired(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, AuthorizationRequestData rdo,
            StringBuffer url) {
        final String userId = httpRequest.getParameter(PARAM_LOGIN_USER_ID);
        if (!ADMStringUtils.isBlank(userId)) {
            // user login data was just provided by the login dialog
            try {
                ServiceAccess serviceAccess = ServiceAccess
                        .getServiceAcccessFor(httpRequest.getSession());
                IdentityService identityService = serviceAccess
                        .getService(IdentityService.class);
                rdo.setUserId(userId);
                rdo.setPassword(httpRequest.getParameter(PARAM_LOGIN_PASSWORD));
                VOUser voUser = readTechnicalUserFromDb(identityService, rdo);
                httpRequest.getSession();
                httpRequest.login(String.valueOf(voUser.getKey()),
                        rdo.getPassword());
                // serviceAccess.login(voUser, rdo.getPassword(), httpRequest,
                // httpResponse);
                httpRequest.getSession().setAttribute(Constants.SESS_ATTR_USER,
                        identityService.getCurrentUserDetails());
            } catch (Exception e2) {
                httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_LOGIN);
                // open marketplace login dialog again and fill in
                // userId
                appendParam(url, Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG,
                        Boolean.TRUE.toString(),
                        httpRequest.getCharacterEncoding());
                appendParam(url, Constants.REQ_PARAM_USER_ID, userId,
                        httpRequest.getCharacterEncoding());
            }
        }
    }

    private void proceedWithFilterChain(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {

        BesServletRequestReader.param2Attr(httpRequest,
                Constants.REQ_PARAM_SUB_KEY);
        BesServletRequestReader.param2Attr(httpRequest,
                Constants.REQ_PARAM_CONTEXT_PATH);
        BesServletRequestReader.param2Attr(httpRequest,
                Constants.REQ_PARAM_SUPPLIER_ID);
        BesServletRequestReader.param2Attr(httpRequest,
                Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        BesServletRequestReader.param2Attr(httpRequest,
                Constants.REQ_ATTR_ERROR_KEY);

        chain.doFilter(httpRequest, httpResponse);
    }

    protected void handleProtectedUrlAndChangePwdCase(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws IOException, ServletException {

        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug(
                    "Access to protected URL='" + rdo.getRelativePath() + "'");
        }

        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(httpRequest.getSession());

        try {
            if (rdo.isAccessToServiceUrl()) {
                /*
                 * We must NOT read the request parameters for service URLs
                 * because this would cause a state switch of the request.
                 * Afterwards the rewriting of a POST request may fail because
                 * the parameters can't be accessed via the request input
                 * stream.
                 */
                httpRequest = handleServiceUrl(chain, httpRequest, httpResponse,
                        rdo);
                if (httpRequest == null) {
                    return;
                }
            } else if (ADMStringUtils.isBlank(rdo.getUserId())) {
                if (authSettings.isServiceProvider()) {
                    if (isSamlForward(httpRequest)) {
                        SAMLCredentials samlCredentials = new SAMLCredentials(
                                httpRequest);
                        rdo.setUserId(samlCredentials.getUserId());
                        if (rdo.getUserId() == null) {
                            httpRequest.setAttribute(
                                    Constants.REQ_ATTR_ERROR_KEY,
                                    BaseBean.ERROR_INVALID_SAML_RESPONSE);
                            forward(errorPage, httpRequest, httpResponse);
                        }
                    }
                } else {
                    rdo.setUserId(httpRequest
                            .getParameter(Constants.REQ_PARAM_USER_ID));
                }
            }

            // continue if user is already logged-in
            if (handleLoggedInUser(chain, httpRequest, httpResponse,
                    serviceAccess, rdo)) {
                return;
            }

            // the httpRequest was already processed and we forwarded to the
            // corresponding page therefore we must not try to login again
            if (httpRequest
                    .getAttribute(Constants.REQ_ATTR_ERROR_KEY) != null) {
                chain.doFilter(httpRequest, httpResponse);
                return;
            }

            refreshData(authSettings, rdo, httpRequest, httpResponse);

            // user not logged in, check user-name and password before login
            // don't do a trim on password because it may have
            // leading/trailing/only blanks

            if (authSettings.isServiceProvider()) {
                rollbackDefaultTimeout(httpRequest);
                if (ADMStringUtils.isBlank(rdo.getUserId())) {
                    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_INVALID_SAML_RESPONSE);
                    if (isSamlForward(httpRequest)) {
                        forward(errorPage, httpRequest, httpResponse);
                    } else {
                        forwardToLoginPage(rdo.getRelativePath(), true,
                                httpRequest, httpResponse, chain);
                    }
                    return;
                }
            } else {
                if (ADMStringUtils.isBlank(rdo.getUserId())
                        || !rdo.isPasswordSet()) {
                    if (!rdo.isMarketplace()
                            && (!ADMStringUtils.isBlank(rdo.getUserId())
                                    || rdo.isPasswordSet())) {
                        // login data not complete, user or password empty
                        httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                                BaseBean.ERROR_LOGIN);
                    }
                    forwardToLoginPage(rdo.getRelativePath(), true, httpRequest,
                            httpResponse, chain);
                    return;
                }
            }

            IdentityService identityService = serviceAccess
                    .getService(IdentityService.class);
            VOUser voUser;
            try {
                voUser = readTechnicalUserFromDb(identityService, rdo);
            } catch (ObjectNotFoundException e) {
                handleUserNotRegistered(chain, httpRequest, httpResponse, rdo);
                return;
            } catch (SaaSApplicationException e) {
                setErrorAttributesAndForward(errorPage, httpRequest,
                        httpResponse, e);
                return;
            }

            if (!authSettings.isServiceProvider()) {
                if (isAccountLocked(httpRequest, httpResponse, voUser)) {
                    return;
                }
            }

            final boolean operationSucceeded;
            if (!authSettings.isServiceProvider()
                    && rdo.isRequestedToChangePwd()) {
                operationSucceeded = handleChangeUserPasswordRequest(chain,
                        httpRequest, httpResponse, rdo, identityService);
            } else {
                operationSucceeded = loginUser(chain, httpRequest, httpResponse,
                        voUser, rdo, identityService);
            }
            if (!operationSucceeded) {
                return;
            }
            rdo.setUserDetails(identityService.getCurrentUserDetails());

            // read user details value object and store it in the session, DON'T
            // use old session, because it might have been invalidated
            httpRequest.getSession().setAttribute(Constants.SESS_ATTR_USER,
                    rdo.getUserDetails());

            if (isPageForbiddenToAccess(httpRequest, rdo, serviceAccess)) {
                forward(insufficientAuthoritiesUrl, httpRequest, httpResponse);
            }
            // check if user must change his password
            if (!authSettings.isServiceProvider() && (rdo.getUserDetails()
                    .getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED)) {
                forwardToPwdPage(rdo.getUserDetails().getUserId(), httpRequest,
                        httpResponse);
            } else {
                redirectToPrimarilyRequestedUrl(chain, httpRequest,
                        httpResponse, serviceAccess, rdo);
            }

        } catch (NumberFormatException e) {
            handleNumberFormatException(chain, httpRequest, httpResponse, e,
                    rdo);
        } catch (ServletException e) {
            handleServletException(httpRequest, httpResponse, e);
        } catch (MarketplaceRemovedException e) {
            handleMarketplaceRemovedException(httpRequest, httpResponse);
        }
    }

    private boolean isPageForbiddenToAccess(HttpServletRequest httpRequest,
            AuthorizationRequestData rdo, ServiceAccess serviceAccess) {
        @SuppressWarnings("unchecked")
        List<PageAuthorization> pageAuthorizationList = ((List<PageAuthorization>) httpRequest
                .getSession()
                .getAttribute(Constants.SESS_ATTR_PAGE_AUTHORIZATION));
        if (pageAuthorizationList == null) {
            PageAuthorizationBuilder builder = new PageAuthorizationBuilder(
                    serviceAccess);
            pageAuthorizationList = builder
                    .buildPageAuthorizationList(new User(rdo.getUserDetails()));
            httpRequest.getSession().setAttribute(
                    Constants.SESS_ATTR_PAGE_AUTHORIZATION,
                    pageAuthorizationList);
        }

        for (PageAuthorization page : pageAuthorizationList) {
            if (page.getCurrentPageLink()
                    .equalsIgnoreCase(httpRequest.getServletPath())) {
                return !page.isAuthorized();
            }
        }
        return false;
    }

    private void handleNumberFormatException(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            NumberFormatException e, AuthorizationRequestData rdo)
            throws ServletException, IOException {
        logger.logError(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.ERROR_PARSE_SUBSCRIPTION_KEY_FAILED,
                rdo.getSubscriptionKey());
        if (authSettings.isServiceProvider()) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SUBSCRIPTION_KEY);
            forward(errorPage, httpRequest, httpResponse);
        } else {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_LOGIN);
            forwardToLoginPage(rdo.getRelativePath(), false, httpRequest,
                    httpResponse, chain);
        }
    }

    private void handleCommunicationException(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws ServletException, IOException {
        httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_LOGIN_IMPOSSIBLE);
        if (authSettings.isServiceProvider()) {
            forward(errorPage, httpRequest, httpResponse);
        } else {
            forwardToLoginPage(rdo.getRelativePath(), false, httpRequest,
                    httpResponse, chain);
        }
    }

    private void handleServletException(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, ServletException e)
            throws IOException, ServletException {
        EJBException ejbEx = ExceptionHandler.getEJBException(e);
        if (ejbEx != null && ejbEx.getCause() instanceof Exception
                && ejbEx.getCausedByException() instanceof AccessException) {
            String forwardErrorPage;
            if (BesServletRequestReader.isMarketplaceRequest(httpRequest)) {
                forwardErrorPage = Marketplace.MARKETPLACE_ROOT
                        + Constants.INSUFFICIENT_AUTHORITIES_URI;
            } else {
                forwardErrorPage = Constants.INSUFFICIENT_AUTHORITIES_URI;
            }
            JSFUtils.sendRedirect(httpResponse,
                    httpRequest.getContextPath() + forwardErrorPage);
        } else {
            // make sure we do not catch exceptions cause by
            // ViewExpiredException here, they'll be handled directly in the
            // doFilter()
            throw e;
        }
    }

    private void handleLoginException(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws ServletException, IOException {
        if (authSettings.isServiceProvider()) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_INVALID_SAML_RESPONSE);
            forward(errorPage, httpRequest, httpResponse);
        } else {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_LOGIN);
            forwardToLoginPage(rdo.getRelativePath(), true, httpRequest,
                    httpResponse, chain);
        }
    }

    private void handleMarketplaceRemovedException(
            HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException, IOException {
        httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_MARKETPLACE_REMOVED);
        forward(errorPage, httpRequest, httpResponse);
    }

    private void handleUserNotRegistered(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws IOException, ServletException {
        if (authSettings.isServiceProvider()) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_LOGIN_SAML_SP);
            forward(errorPage, httpRequest, httpResponse);
        } else {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_LOGIN);
            forwardToLoginPage(rdo.getRelativePath(), false, httpRequest,
                    httpResponse, chain);
        }
    }

    private void setErrorAttributesAndForward(String forwardUrl,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            SaaSApplicationException e) throws ServletException, IOException {
        BesServletRequestReader.setErrorAttributes(httpRequest, e);
        forward(forwardUrl, httpRequest, httpResponse);
    }

    private void refreshData(AuthenticationSettings authSettings,
            AuthorizationRequestData rdo, HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, MarketplaceRemovedException {

        if (authSettings.isServiceProvider()) {

            rdo.setTenantID(getTenantID(rdo, request));

            if (!isSamlForward(request)) {
                return;
            }

            rdo.refreshData(request);

            SAMLCredentials samlCredentials = new SAMLCredentials(request);

            if (rdo.getUserId() == null) {
                rdo.setUserId(samlCredentials.getUserId());
            }

            if (rdo.getPassword() == null) {
                String generatedPassword = samlCredentials.generatePassword();
                if (generatedPassword == null) {
                    request.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_SAML_TIMEOUT);
                    forward(errorPage, request, response);
                }
                rdo.setPassword(generatedPassword);

                // if generated password is null, then timeout!!!
            }
        } else {
            rdo.refreshData(request);
            // store some parameters if the login fails (needed for login.xhtml)
            request.setAttribute(Constants.REQ_PARAM_USER_ID, rdo.getUserId());
        }

    }

    public String getTenantID(AuthorizationRequestData ard,
            HttpServletRequest httpRequest) throws MarketplaceRemovedException {
        String tenantID;
        if (ard.isMarketplace()) {
            tenantID = getTenantIDFromMarketplace(httpRequest, ard);
        } else {
            tenantID = getTenantIDFromRequest(httpRequest);
        }
        if (StringUtils.isNotBlank(tenantID)) {
            httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID,
                    tenantID);
        } else {
            tenantID = (String) httpRequest.getSession()
                    .getAttribute(REQ_PARAM_TENANT_ID);
        }
        if (StringUtils.isBlank(tenantID)) {
            logger.logDebug("TenantID is missing. Using default.");
            tenantID = getConfigurationService(httpRequest)
                    .getVOConfigurationSetting(SSO_DEFAULT_TENANT_ID,
                            GLOBAL_CONTEXT)
                    .getValue();
            httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID,
                    tenantID);
        }
        return tenantID;
    }

    private String getTenantIDFromMarketplace(HttpServletRequest httpRequest,
            AuthorizationRequestData ard) throws MarketplaceRemovedException {
        String marketplaceId = ard.getMarketplaceId();
        String tenantID = null;
        if (StringUtils.isNotBlank(marketplaceId)) {
            tenantID = getMarketplaceServiceCache(httpRequest)
                    .getConfiguration(marketplaceId).getTenantId();
            if (StringUtils.isBlank(tenantID)) {
                try {
                    tenantID = getMarketplaceService(httpRequest)
                            .getMarketplaceById(marketplaceId).getTenantId();
                } catch (ObjectNotFoundException e) {
                    throw new MarketplaceRemovedException();
                }
            }
        }
        return tenantID;
    }

    private String getTenantIDFromRequest(HttpServletRequest request) {
        return request.getParameter(REQ_PARAM_TENANT_ID);
    }

    private HttpServletRequest handleServiceUrl(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws IOException, ServletException {

        VOMarketplace mpl;
        try {
            mpl = determineMarketplaceForSubscription(httpRequest, rdo);
        } catch (ObjectNotFoundException e) {

            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_SUBSCRIPTION_NOT_FOUND,
                    rdo.getSubscriptionKey());
            BesServletRequestReader.setErrorAttributes(httpRequest, e);

            handleSubscriptionNotFound(chain, httpRequest, httpResponse, rdo);
            return null;
        }

        // Bug 9588: Marketplace may have been deleted
        if (mpl != null) {
            httpRequest.setAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE,
                    Constants.REQ_ATTR_LOGIN_TYPE_MPL);

            httpRequest.getSession().setAttribute(
                    Constants.REQ_PARAM_MARKETPLACE_ID, mpl.getMarketplaceId());
        } else {
            httpRequest.setAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE,
                    Constants.REQ_ATTR_LOGIN_TYPE_NO_MPL);
        }

        String contextPath = rdo.getContextPath();
        if (!ADMStringUtils.isBlank(httpRequest.getQueryString())) {
            contextPath += "?" + httpRequest.getQueryString();
        }
        httpRequest.setAttribute(Constants.REQ_PARAM_SUB_KEY,
                rdo.getSubscriptionKey());
        httpRequest.setAttribute(Constants.REQ_PARAM_CONTEXT_PATH, contextPath);

        return processServiceUrl(httpRequest, httpResponse, chain,
                rdo.getSubscriptionKey(), contextPath, rdo);
    }

    private void handleSubscriptionNotFound(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo) throws IOException, ServletException {
        if (authSettings.isServiceProvider()) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SUBSCRIPTION_NOT_FOUND);
            forward(errorPage, httpRequest, httpResponse);
        } else {
            forwardToLoginPage(rdo.getRelativePath(), false, httpRequest,
                    httpResponse, chain);
        }
    }

    /**
     * Retrieve the marketplace for the given subscription in order to login
     * 
     */
    private VOMarketplace determineMarketplaceForSubscription(
            HttpServletRequest httpRequest, AuthorizationRequestData rdo)
            throws ObjectNotFoundException {
        Map<String, VOMarketplace> cachedMarketplaces = getMarketplaceMapFromSession(
                httpRequest.getSession());
        VOMarketplace mpl = cachedMarketplaces.get(rdo.getSubscriptionKey());
        if (mpl == null) {
            MarketplaceService marketplaceService = ServiceAccess
                    .getServiceAcccessFor(httpRequest.getSession())
                    .getService(MarketplaceService.class);
            mpl = marketplaceService.getMarketplaceForSubscription(
                    ADMStringUtils.parseUnsignedLong(rdo.getSubscriptionKey()),
                    "en");

            // Bug 9588: Marketplace may have been deleted
            if (mpl != null) {
                cachedMarketplaces.put(rdo.getSubscriptionKey(), mpl);
            }
        }
        return mpl;
    }

    private boolean isAccountLocked(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, VOUser voUser)
            throws ServletException, IOException {

        if (voUser.getStatus() == UserAccountStatus.LOCKED_NOT_CONFIRMED) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_USER_LOCKED_NOT_CONFIRMED);
            forward(errorPage, httpRequest, httpResponse);
            return true;
        }

        if (voUser.getStatus() != null && voUser.getStatus()
                .getLockLevel() > UserAccountStatus.LOCK_LEVEL_LOGIN) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_USER_LOCKED);
            sendRedirect(httpRequest, httpResponse, errorPage);
            return true;
        }

        return false;
    }

    boolean handleLoggedInUser(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            ServiceAccess serviceAccess, AuthorizationRequestData rdo)
            throws ServletException, IOException {

        VOUserDetails userDetails = rdo.getUserDetails();
        if (userDetails != null) {
            httpRequest.getSession().setAttribute(PORTAL_HAS_BEEN_REQUESTED,
                    !rdo.isMarketplace());

            // if the user wants to use another organization he must login
            // again (the service sessions are destroyed as well)

            // don't let a user with status PASSWORD_MUST_BE_CHANGED see any
            // site but the one to change the pwd
            if (!authSettings.isServiceProvider()) {
                if (userDetails
                        .getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED
                        && !rdo.isRequestedToChangePwd()) {
                    forwardToPwdPage(userDetails.getUserId(), httpRequest,
                            httpResponse);
                    return true;
                }
            }

            // TODO stavreva: check this again
            if (authSettings.isServiceProvider()
                    || !rdo.isRequestedToChangePwd()) {
                long t = System.currentTimeMillis();
                if (ADMStringUtils.isBlank(httpRequest.getServletPath())
                        || httpRequest.getServletPath()
                                .startsWith(MenuBean.LINK_DEFAULT)) {
                    String defaultUrl = getDefaultUrl(serviceAccess, rdo,
                            httpRequest);
                    forward(defaultUrl, httpRequest, httpResponse);
                }

                if (loginPage.equalsIgnoreCase(httpRequest.getServletPath())) {
                    sendRedirect(httpRequest, httpResponse,
                            MenuBean.LINK_DEFAULT);
                }

                if (isPageForbiddenToAccess(httpRequest, rdo, serviceAccess)) {
                    forward(insufficientAuthoritiesUrl, httpRequest,
                            httpResponse);
                }
                chain.doFilter(httpRequest, httpResponse);
                if (logger.isDebugLoggingEnabled()) {
                    logger.logDebug(
                            "URL='" + rdo.getRelativePath() + "' processed in "
                                    + (System.currentTimeMillis() - t) + "ms");
                }
                return true;
            }
        }

        return false;
    }

    VOUser readTechnicalUserFromDb(IdentityService service,
            AuthorizationRequestData ard) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException {
        VOUser voUser = new VOUser();
        voUser.setUserId(ard.getUserId());
        voUser.setTenantId(ard.getTenantID());
        voUser = service.getUser(voUser);
        return voUser;
    }

    protected boolean loginUser(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            VOUser voUser, AuthorizationRequestData rdo,
            IdentityService identityService)
            throws ServletException, IOException {

        HttpSession session = httpRequest.getSession();
        boolean onlyServiceLogin = BesServletRequestReader
                .onlyServiceLogin(session);
        String forwardUrl = (String) session
                .getAttribute(Constants.SESS_ATTR_FORWARD_URL);
        SessionBean sessionBean = (SessionBean) session
                .getAttribute(Constants.SESS_ATTR_SESSION_BEAN);

        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(session);

        if (onlyServiceLogin) {
            session.setAttribute(Constants.SESS_ATTR_ONLY_SERVICE_LOGIN,
                    Boolean.TRUE);
        }

        if (!ADMStringUtils.isBlank(forwardUrl)) {
            session.setAttribute(Constants.SESS_ATTR_FORWARD_URL, forwardUrl);
        }

        if (sessionBean != null) {
            session.setAttribute(Constants.SESS_ATTR_SESSION_BEAN, sessionBean);
        }

        if (!ADMStringUtils.isBlank(rdo.getMarketplaceId())) {
            session.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID,
                    rdo.getMarketplaceId());
        }

        // authenticate the user
        // IMPORTANT: Changes to this method must also be applied to
        // UserBean.login()
        try {
            serviceAccess.login(voUser, rdo.getPassword(), httpRequest,
                    httpResponse);
        } catch (CommunicationException e) {
            handleCommunicationException(chain, httpRequest, httpResponse, rdo);
            return false;
        } catch (LoginException e) {
            logger.logInfo(Log4jLogger.ACCESS_LOG,
                    LogMessageIdentifier.INFO_USER_LOGIN_INVALID,
                    httpRequest.getRemoteHost(),
                    Integer.toString(httpRequest.getRemotePort()),
                    StringUtils.isNotBlank(voUser.getUserId())
                            ? voUser.getUserId()
                            : "",
                    IPResolver.resolveIpAddress(httpRequest),
                    voUser.getTenantId());
            try {
                voUser = identityService.getUser(voUser);
            } catch (ObjectNotFoundException e1) {
                handleUserNotRegistered(chain, httpRequest, httpResponse, rdo);
                return false;
            } catch (SaaSApplicationException e1) {
                setErrorAttributesAndForward(errorPage, httpRequest,
                        httpResponse, e1);
                return false;
            }

            if (voUser.getStatus() != null && voUser.getStatus()
                    .getLockLevel() > UserAccountStatus.LOCK_LEVEL_LOGIN) {
                httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_USER_LOCKED);
                forward(errorPage, httpRequest, httpResponse);
                return false;
            }

            handleLoginException(chain, httpRequest, httpResponse, rdo);
            return false;
        }

        if (!rdo.isMarketplace() && !rdo.isAccessToServiceUrl() // BE09588 Login
                                                                // is OK
                                                                // if a
                                                                // service is
                                                                // accessed,
                                                                // whose
                                                                // subscription
                                                                // has no
                                                                // marketplace
                && identityService.getCurrentUserDetails()
                        .getOrganizationRoles().size() == 1
                && identityService.getCurrentUserDetails()
                        .getOrganizationRoles()
                        .contains(OrganizationRoleType.CUSTOMER))

        {
            if (ADMStringUtils.isBlank(rdo.getMarketplaceId())) {
                if (redirectToMpUrl(httpRequest, httpResponse)) {
                    setupUserDetail(httpRequest, rdo, identityService, session);
                    return false;
                } else {
                    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_INVALID_MARKETPLACE_URL);
                    forward(BaseBean.MARKETPLACE_ERROR_PAGE, httpRequest,
                            httpResponse);
                }
            } else {
                setupUserDetail(httpRequest, rdo, identityService, session);
                forward(BaseBean.MARKETPLACE_START_SITE, httpRequest,
                        httpResponse);
            }
            return false;
        }

        // get the service again because the credentials have been
        // changed (important for WS usage)
        identityService = serviceAccess.getService(IdentityService.class);
        try {
            identityService.refreshLdapUser();
        } catch (ValidationException e) {
            logger.logDebug(
                    "Refresh of LDAP user failed, most likely due to missing/wrong LDAP settings");
        }

        logger.logInfo(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS,
                StringUtils.isNotBlank(voUser.getUserId()) ? voUser.getUserId()
                        : "",
                IPResolver.resolveIpAddress(httpRequest), voUser.getTenantId());
        return true;
    }

    /**
     * @param httpRequest
     * @param rdo
     * @param identityService
     * @param session
     */
    private void setupUserDetail(HttpServletRequest httpRequest,
            AuthorizationRequestData rdo, IdentityService identityService,
            HttpSession session) {
        rdo.setUserDetails(identityService.getCurrentUserDetails());
        HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession != null) {
            session.setAttribute(Constants.SESS_ATTR_USER,
                    rdo.getUserDetails());
        }
    }

    void redirectToPrimarilyRequestedUrl(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            ServiceAccess serviceAccess, AuthorizationRequestData rdo)
            throws IOException, ServletException {

        String forwardUrl = (String) httpRequest.getSession()
                .getAttribute(Constants.SESS_ATTR_FORWARD_URL);

        if (BesServletRequestReader
                .onlyServiceLogin(httpRequest.getSession())) {
            if (forwardUrl == null) {
                forwardUrl = Constants.SERVICE_BASE_URI + "/"
                        + rdo.getSubscriptionKey() + "/";
            }
            JSFUtils.sendRedirect(httpResponse,
                    httpRequest.getContextPath() + forwardUrl);
            return;
        }

        if (ADMStringUtils.isBlank(forwardUrl)
                || forwardUrl.startsWith(MenuBean.LINK_DEFAULT)) {
            forwardUrl = getDefaultUrl(serviceAccess, rdo, httpRequest);
        }

        if ((ADMStringUtils.isBlank(forwardUrl)
                || rdo.getRelativePath().startsWith(forwardUrl))
                && !rdo.isMarketplaceLoginPage()) {
            chain.doFilter(httpRequest, httpResponse);
        } else {
            JSFUtils.sendRedirect(httpResponse,
                    httpRequest.getContextPath() + forwardUrl);
        }

    }

    /**
     * Invokes the validators and bean actions specified in the xhtml file to
     * change the user's password.
     * 
     * @throws ServletException
     * @throws IOException
     * @throws DatatypeConfigurationException
     * @throws SAML2AuthnRequestException
     */
    protected boolean handleChangeUserPasswordRequest(FilterChain chain,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            AuthorizationRequestData rdo, IdentityService identityService)
            throws IOException, ServletException {
        if (rdo.isRequestedToChangePwd()) {

            if (!PasswordValidator.validPasswordLength(rdo.getNewPassword())
                    || !PasswordValidator
                            .validPasswordLength(rdo.getNewPassword2())
                    || !PasswordValidator.passwordsAreEqual(
                            rdo.getNewPassword(), rdo.getNewPassword2())) {
                // Let JSF run the validators and return the response!
                chain.doFilter(httpRequest, httpResponse);
                return false;
            }

            // Run the validators and bean methods. Prevent JSF
            // from writing content to the response, otherwise the following
            // redirect's wouldn't work.
            HttpServletResponse resp = new HttpServletResponseWrapper(
                    httpResponse) {
                @Override
                public void flushBuffer() throws IOException {
                }

                @Override
                public PrintWriter getWriter() throws IOException {
                    return new PrintWriter(getOutputStream());
                }

                @Override
                public ServletOutputStream getOutputStream()
                        throws IOException {
                    return new ServletOutputStream() {
                        @Override
                        public boolean isReady() {
                            return false;
                        }

                        @Override
                        public void setWriteListener(
                                WriteListener writeListener) {

                        }

                        @Override
                        public void write(int b) throws IOException {

                        }
                    };
                }
            };
            chain.doFilter(httpRequest, resp);
            httpResponse.reset();
        }

        VOUser voUser = new VOUser();
        voUser.setUserId(rdo.getUserId());
        try {
            voUser = identityService.getUser(voUser);
        } catch (ObjectNotFoundException e) {
            handleUserNotRegistered(chain, httpRequest, httpResponse, rdo);
            return false;
        } catch (SaaSApplicationException e) {
            setErrorAttributesAndForward(errorPage, httpRequest, httpResponse,
                    e);
            return false;
        }

        if (httpRequest.getAttribute(Constants.REQ_ATTR_ERROR_KEY) != null) {
            // Error occurred - check if user is locked now
            if (voUser.getStatus() != null && voUser.getStatus()
                    .getLockLevel() > UserAccountStatus.LOCK_LEVEL_LOGIN) {
                httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_USER_LOCKED);
                sendRedirect(httpRequest, httpResponse, errorPage);
            } else {
                // Run it again to get error result on current response
                chain.doFilter(httpRequest, httpResponse);
            }

            return false;
        }

        if (voUser.getStatus() != UserAccountStatus.ACTIVE) {
            // the password change request failed
            // set the REQ_ATTR_ERROR_KEY to avoid an infinite loop
            httpRequest.getSession().setAttribute(Constants.SESS_ATTR_USER,
                    identityService.getCurrentUserDetails());
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, "");
            if (rdo.isMarketplace()) {
                forward(BaseBean.MARKETPLACE_LOGIN, httpRequest, httpResponse);
            } else {
                forward(pwdPage, httpRequest, httpResponse);
            }
            return false;
        }

        rdo.setPassword(httpRequest
                .getParameter(BesServletRequestReader.REQ_PARAM_PASSWORD_NEW));
        rdo.getUserDetails().setStatus(UserAccountStatus.ACTIVE);
        return true;
    }

    /**
     * Forward the request to the change password page. For the marketplace
     * portal the change password is handled by the actual login page.
     * 
     * @param userId
     *            the current user identifier
     * @param request
     *            the current HTTP servlet request
     * @param response
     *            the current HTTP servlet response
     * @throws IOException
     * @throws ServletException
     */
    private void forwardToPwdPage(String userId, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String actualPwdPage = getActualLoginPage(request, pwdPage, null);
        forward(actualPwdPage, request, response);
        return;
    }

    /**
     * If the user wants to access a service URL we must check if he is logged
     * into the service.
     * 
     * @return the given HTTP request, a request wrapper which must be used to
     *         perform the service login or null if the caller should continue
     *         with the filter chain
     * @throws IOException
     * @throws ServletException
     * @throws DatatypeConfigurationException
     * @throws SAML2AuthnRequestException
     * @throws Exception
     */
    private HttpServletRequest processServiceUrl(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, String subKey,
            String contextPath, AuthorizationRequestData rdo)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(session);

        if ("/".equals(contextPath)
                && !BesServletRequestReader.onlyServiceLogin(session)) {
            // if the user accesses a subscription from the my subscription list
            // we check the subscription key map in the session (this causes
            // some overhead - EJB call - and should NOT be done for every
            // service request)

            // preserve mId
            String mId = (String) session
                    .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);

            SessionListener.cleanup(session);

            session = request.getSession();
            session.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, mId);
        }

        Map<String, VOSubscription> map = getSubMapFromSession(session);
        VOSubscription sub = map.get(subKey);
        VOUserDetails userDetails = (VOUserDetails) session
                .getAttribute(Constants.SESS_ATTR_USER);
        if (BesServletRequestReader.onlyServiceLogin(session)) {
            session.removeAttribute(Constants.SESS_ATTR_ONLY_SERVICE_LOGIN);

            // at least remove the user details from the session
            session.removeAttribute(Constants.SESS_ATTR_USER);
            if (userDetails != null) {
                session.setAttribute(Constants.REQ_PARAM_LOCALE,
                        userDetails.getLocale());
            }
        }

        if (userDetails != null && userDetails
                .getStatus() != UserAccountStatus.PASSWORD_MUST_BE_CHANGED) {
            // the user is already logged in

            if (sub == null) {
                // the user is not logged in the service, we must call the
                // SSO bridge

                sub = getSubscription(serviceAccess, subKey);

                if (sub == null) {
                    UserNotAssignedException e = new UserNotAssignedException(
                            subKey, userDetails.getUserId());
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, e,
                            LogMessageIdentifier.ERROR_ACTIVE_SUBSCRIPTION_FOR_CURRENT_USER_FAILED,
                            subKey);
                    setErrorAttributesAndForward(
                            getDefaultUrl(serviceAccess, rdo, request), request,
                            response, e);
                    return null;

                } else if (sub.getStatus() != SubscriptionStatus.ACTIVE
                        && sub.getStatus() != SubscriptionStatus.PENDING_UPD) {
                    SubscriptionStateException e = new SubscriptionStateException(
                            "Subscription '" + subKey
                                    + "' not active or pending update.",
                            Reason.ONLY_ACTIVE);
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, e,
                            LogMessageIdentifier.ERROR_SUBSCRIPTION_NOT_ACTIVE,
                            subKey);
                    setErrorAttributesAndForward(
                            getDefaultUrl(serviceAccess, rdo, request), request,
                            response, e);
                    return null;

                } else if (!sub.getServiceBaseURL().toLowerCase().startsWith(
                        request.getScheme().toLowerCase() + "://")) {
                    setErrorAttributesAndForward(errorPage, request, response,
                            new ServiceSchemeException());
                    return null;
                }

                String userToken = ADMStringUtils.getRandomString(40);

                // create a service session database record (which is used by
                // the service to resolve the user token)
                try {
                    synchronized (map) {
                        createServiceSession(serviceAccess, subKey,
                                session.getId(), userToken);
                        // the map must be filled after the service session was
                        // created otherwise the session listener cleanup method
                        // might clear the list
                        map.put(subKey, sub);
                    }
                } catch (ObjectNotFoundException e) {
                    handleSubscriptionNotFound(chain, request, response, rdo);
                    return null;
                } catch (ServiceParameterException e) {
                    setErrorAttributesAndForward(
                            Constants.SERVICE_USAGE_ERROR_URI, request,
                            response, e);
                    return null;
                } catch (SaaSApplicationException e) {
                    setErrorAttributesAndForward(errorPage, request, response,
                            e);
                    return null;
                }

                if (sub.getServiceAccessType() == ServiceAccessType.LOGIN) {
                    // perform a redirect to the SSO bridge with the user token
                    String url = removeEndingSlash(sub.getServiceBaseURL());
                    if (sub.getServiceLoginPath() != null) {
                        url += sub.getServiceLoginPath();
                    }
                    if (url.contains("?")) {
                        url += "&";
                    } else {
                        url += "?";
                    }
                    SsoParameters ssoParameters = new SsoParameters();
                    ssoParameters.setContextPath(contextPath);
                    ssoParameters.setInstanceId(sub.getServiceInstanceId());
                    ssoParameters.setLanguage(userDetails.getLocale());
                    ssoParameters.setSubscriptionKey(subKey);
                    ssoParameters.setBssId(request.getSession().getId());
                    ssoParameters.setUsertoken(userToken);
                    url += ssoParameters.getQueryString();
                    JSFUtils.sendRedirect(response, url);
                    return null;
                }

            } else {
                if (sub.getServiceAccessType() == ServiceAccessType.LOGIN) {
                    // send a redirect to the service base URL, the service
                    // session should be still active
                    JSFUtils.sendRedirect(response, sub.getServiceBaseURL());
                    return null;
                }

                // nothing to do (the user is logged in the platform and the
                // service) the rewriting is done by the subsequent filters in
                // the filter chain which is activated by the caller
            }
        } else {
            // the user is not logged in

            if (sub == null) {
                // the user is neither logged in platform nor in the
                // service,
                //
                // The later processing will forward to the service login
                // page processing. After the login there will be a redirect to
                // the primarily requested URL which will perform the service
                // login
                session.setAttribute(Constants.SESS_ATTR_ONLY_SERVICE_LOGIN,
                        Boolean.TRUE);

            } else {
                // the user is logged in the service

                if (sub.getServiceAccessType() == ServiceAccessType.LOGIN) {
                    // send a redirect to the service base URL, the service
                    // session should be still active
                    JSFUtils.sendRedirect(response, sub.getServiceBaseURL());
                } else {
                    // don't perform any other checks continue with the
                    // filter chain which will perform the rewriting
                    chain.doFilter(request, response);
                }
                return null;
            }
        }
        return request;
    }

    /**
     * Get the map from the session which maps a subscription key to the
     * subscription value object.
     * 
     * @param session
     *            the current session.
     * @return the requested map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, VOSubscription> getSubMapFromSession(
            HttpSession session) {
        Map<String, VOSubscription> map = (Map<String, VOSubscription>) session
                .getAttribute(Constants.SESS_ATTR_ACTIVE_SUB_MAP);
        if (map != null) {
            return map;
        }
        map = Collections
                .synchronizedMap(new HashMap<String, VOSubscription>());
        session.setAttribute(Constants.SESS_ATTR_ACTIVE_SUB_MAP, map);
        return map;
    }

    /**
     * Cache the marketplaces. If BES acts as reverse proxy, it is important
     * that no SQL requests are performed. Otherwise for each HTTP request
     * (html, css, images, ajax) that is performed for the target system several
     * SQL requests would be performed on BES side. BES would be dead long time
     * before the target system gets busy.
     */
    @SuppressWarnings("unchecked")
    private Map<String, VOMarketplace> getMarketplaceMapFromSession(
            HttpSession session) {
        Map<String, VOMarketplace> map = (Map<String, VOMarketplace>) session
                .getAttribute(Constants.SESS_ATTR_MARKETPLACE_MAP);
        if (map != null) {
            return map;
        }
        map = new ConcurrentHashMap<>();
        session.setAttribute(Constants.SESS_ATTR_MARKETPLACE_MAP, map);
        return map;
    }

    /**
     * Helper method which performs an EJB call and gets the subscription value
     * object for the given subscription key.
     * 
     * @param key
     *            the (hex)-key of the subscription.
     * @return the subscription value object.
     */
    private VOSubscription getSubscription(ServiceAccess serviceAccess,
            String key) {
        SubscriptionService subscriptionService = serviceAccess
                .getService(SubscriptionService.class);
        List<VOUserSubscription> list = subscriptionService
                .getSubscriptionsForCurrentUser();
        if (list != null) {
            for (VOSubscription vo : list) {
                if (key.equals(Long.toHexString(vo.getKey()))) {
                    return vo;
                }
            }
        }
        return null;
    }

    private void createServiceSession(ServiceAccess serviceAccess,
            String subKey, String sessionId, String userToken)
            throws ObjectNotFoundException, ServiceParameterException,
            OperationNotPermittedException, ValidationException {
        SessionService service = serviceAccess.getService(SessionService.class);
        long subscriptionKey = ADMStringUtils.parseUnsignedLong(subKey);
        service.createServiceSession(subscriptionKey, sessionId, userToken);
    }

    private String removeEndingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private boolean isSamlForward(HttpServletRequest httpRequest) {
        Boolean isSamlForward = (Boolean) httpRequest
                .getAttribute(Constants.REQ_ATTR_IS_SAML_FORWARD);
        return isSamlForward != null && isSamlForward.booleanValue();

    }
}
