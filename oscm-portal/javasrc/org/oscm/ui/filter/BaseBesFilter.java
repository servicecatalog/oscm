/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Oct 6, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;

import org.oscm.internal.intf.*;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SAML2AuthnRequestException;
import org.oscm.internal.types.exception.WrongTenantConfigurationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.*;
import org.oscm.ui.dialog.common.saml2.AuthenticationHandler;
import org.oscm.validator.ADMValidator;

/**
 * @author groch
 * 
 */
public abstract class BaseBesFilter implements Filter {

    protected final static String PARAM_LOGIN_PAGE = "login-page";
    protected final static String PARAM_PUBLIC_URL_PATTERN = "public-url-pattern";
    protected final static String PARAM_PWD_PAGE = "pwd-page";
    protected final static String PARAM_ERROR_PAGE = "error-page";

    protected String loginPage = "/login.jsf";
    protected String publicUrlPattern;
    protected String pwdPage = "/public/pwd.jsf";
    protected String errorPage = "/public/error.jsf";
    protected String defaultPage = "/default.jsf";
    protected String insufficientAuthoritiesUrl = "/insufficientAuthorities.jsf";

    protected AuthenticationSettings authSettings;

    private FilterConfig filterConfig;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BaseBesFilter.class);
    private MarketplaceService mkpService;
    private MarketplaceCacheService mkpServiceCache;

    /**
     * Read the parameter from filter configuration. Called by the web container
     * to indicate to a filter that it is being placed into service.
     * 
     * @param filterConfig
     *            the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        String value = filterConfig.getInitParameter(PARAM_LOGIN_PAGE);

        loginPage = checkPage(value, loginPage);
        logger.logDebug("loginPage=" + loginPage);

        value = filterConfig.getInitParameter(PARAM_PWD_PAGE);
        pwdPage = checkPage(value, pwdPage);
        logger.logDebug("pwdPage=" + pwdPage);

        value = filterConfig.getInitParameter(PARAM_ERROR_PAGE);
        errorPage = checkPage(value, errorPage);
        logger.logDebug("errorPage=" + errorPage);

        publicUrlPattern = filterConfig
                .getInitParameter(PARAM_PUBLIC_URL_PATTERN);
        logger.logDebug("publicUrlPattern=" + publicUrlPattern);

        ServiceAccess serviceAccess = new EJBServiceAccess();
        ConfigurationService cfgService = serviceAccess
                .getService(ConfigurationService.class);
        TenantService tenantService = serviceAccess
                .getService(TenantService.class);
        authSettings = new AuthenticationSettings(tenantService, cfgService);
        try {
            authSettings.init(null);
        } catch (NotExistentTenantException e) {
            //ain't gonna happen. Configsettins will be used.
        } catch (WrongTenantConfigurationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_TENANT_MISCONFIGURED);
        }
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     */
    @Override
    public void destroy() {
    }

    protected AuthorizationRequestData initializeRequestDataObject(
            HttpServletRequest httpRequest) {
        final AuthorizationRequestData ard = new AuthorizationRequestData();

        ard.setRelativePath(BesServletRequestReader
                .getRelativePath(httpRequest));
        if (ard.getRelativePath().startsWith(pwdPage)) {
            ard.setPassword(httpRequest
                    .getParameter(BesServletRequestReader.REQ_PARAM_PASSWORD));
            ard.setRequestedToChangePwd(BesServletRequestReader
                    .isRequestedToChangePassword(httpRequest));

            if (ard.isRequestedToChangePwd()) {
                ard.setNewPassword(httpRequest
                        .getParameter(BesServletRequestReader.REQ_PARAM_PASSWORD_NEW));
                ard.setNewPassword2(httpRequest
                        .getParameter(BesServletRequestReader.REQ_PARAM_PASSWORD_NEW2));
            }
        }
        ard.setMarketplace(BesServletRequestReader
                .isMarketplaceRequest(httpRequest));

        if (authSettings.isServiceProvider()) {
            ard.setMarketplaceLoginPage(httpRequest.getServletPath()
                    .startsWith(BaseBean.SAML_SP_LOGIN_AUTOSUBMIT_PAGE));
        } else {
            ard.setMarketplaceLoginPage(BesServletRequestReader
                    .isMarketplaceLoginPageRequest(httpRequest));
        }

        ard.setMarketplaceErrorPage(BesServletRequestReader
                .isMarketplaceErrorPageRequest(httpRequest));

        String mId = (String) httpRequest.getSession().getAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID);
        ard.setMarketplaceId(mId);

        ard.setUserDetails((VOUserDetails) httpRequest.getSession()
                .getAttribute(Constants.SESS_ATTR_USER));

        ard.setLandingPage(BesServletRequestReader.isLandingPage(httpRequest));
        return ard;
    }

    /**
     * If the value is set check if it starts with a /. Otherwise use the
     * default value
     * 
     * @param value
     *            the value to check
     * @param defaultValue
     *            the default value to consider
     * @return the checked value or the default value
     */
    private String checkPage(String value, String defaultValue) {
        if (ADMStringUtils.isBlank(value)) {
            value = defaultValue;
        }
        if (!value.startsWith("/") && !value.startsWith("\\")) {
            value = "/" + value;
        }
        return value;
    }

    /**
     * Forward the request to the given page
     * 
     * @param page
     *            the page to which the request is forwarded
     * @param request
     *            the current http servlet request
     * @param response
     *            the current http servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void forward(String page, ServletRequest request,
            ServletResponse response) throws ServletException, IOException {
        filterConfig.getServletContext().getRequestDispatcher(page)
                .forward(request, response);
    }

    /**
     * Redirect to the given page overtaking errors if exist.
     * 
     * @param relativePath
     *            the relative path of the page to which to be redirected
     * 
     * @param request
     *            the current http servlet request
     * @param response
     *            the current http servlet response
     * @throws IOException
     */
    protected void sendRedirect(HttpServletRequest httpRequest,
            HttpServletResponse response, String relativePath)
            throws IOException {
        sendRedirectStatic(httpRequest, response, relativePath);
    }

    public static void sendRedirectStatic(HttpServletRequest httpRequest,
            HttpServletResponse response, String relativePath)
            throws IOException {
        // Bug 7607: Avoid error message is lost
        final String errMsg = (String) httpRequest
                .getAttribute(Constants.REQ_ATTR_ERROR_KEY);
        if (errMsg != null) {
            StringBuffer sb = new StringBuffer(relativePath);
            if (relativePath.indexOf('?') > 0)
                sb.append('&');
            else
                sb.append('?');
            sb.append(Constants.REQ_ATTR_ERROR_KEY);
            sb.append("=");
            sb.append(errMsg);
            relativePath = sb.toString();
        }
        // And redirect...
        JSFUtils.sendRedirect(response, httpRequest.getContextPath()
                + relativePath);
    }

    /**
     * Get the default URL for the given user
     * 
     * @param serviceAccess
     *            the service access implementation for the current session.
     * @param userDetails
     *            the user for which we want to get the default URL.
     * @param httpRequest
     * @return the default URL.
     */
    protected String getDefaultUrl(ServiceAccess serviceAccess,
            AuthorizationRequestData rdo, HttpServletRequest httpRequest) {

        String defaultUrl = MenuBean.LINK_PROFILE_EDIT;
        VOUserDetails userDetails = rdo.getUserDetails();
        boolean hasMpId = !ADMStringUtils.isBlank(rdo.getMarketplaceId());

        if (userDetails == null) {
            if (rdo.isMarketplace()) {
                defaultUrl = BaseBean.MARKETPLACE_ERROR_PAGE;
            } else {
                defaultUrl = errorPage;
            }
        } else if (BesServletRequestReader.isMarketplaceLogin(httpRequest)
                && hasMpId) {
            defaultUrl = BaseBean.MARKETPLACE_START_SITE;
        } else {
            Set<UserRoleType> roles = userDetails.getUserRoles();
            if (roles.contains(UserRoleType.PLATFORM_OPERATOR)) {
                defaultUrl = MenuBean.LINK_MANAGE_CONFIGURATION;
            } else if (roles.contains(UserRoleType.SERVICE_MANAGER)) {
                defaultUrl = MenuBean.LINK_SERVICE_ADD;
                if (roles.contains(UserRoleType.TECHNOLOGY_MANAGER)) {
                    ServiceProvisioningServiceInternal sps = serviceAccess
                            .getService(ServiceProvisioningServiceInternal.class);
                    try {
                        if (sps.getTechnicalServices(
                                OrganizationRoleType.SUPPLIER,
                                PerformanceHint.ONLY_IDENTIFYING_FIELDS)
                                .isEmpty()) {
                            defaultUrl = MenuBean.LINK_TECHSERVICE_IMPORT;
                        }
                    } catch (OrganizationAuthoritiesException e) {
                        logger.logError(
                                Log4jLogger.SYSTEM_LOG,
                                e,
                                LogMessageIdentifier.ERROR_USER_OPERATE_NOT_PERMITTED_THE_USER_ALREADY_DELETED);
                    }
                }
            } else if (roles.contains(UserRoleType.TECHNOLOGY_MANAGER)) {
                defaultUrl = MenuBean.LINK_TECHSERVICE_IMPORT;
            } else if (!roles.contains(UserRoleType.MARKETPLACE_OWNER)
                    && !roles.contains(UserRoleType.BROKER_MANAGER)
                    && !roles.contains(UserRoleType.ORGANIZATION_ADMIN)
                    && !roles.contains(UserRoleType.RESELLER_MANAGER)
                    || (userDetails.getOrganizationRoles().size() == 1 && userDetails
                            .getOrganizationRoles().contains(
                                    OrganizationRoleType.CUSTOMER))) {
                // Every user is a customer. If he isn't also marketplace owner,
                // redirect him to the marketplace
                defaultUrl = MenuBean.LINK_MARKETPLACE;
            }
        }

        // Add user details to session for service login
        if (BesServletRequestReader.isServiceLogin(httpRequest)
                && (userDetails != null)) {
            // Ensure user is not removed from session after service login, for
            // example if user is not assigned to subscription!
            httpRequest.getSession().setAttribute(Constants.SESS_ATTR_USER,
                    userDetails);
        }
        return defaultUrl;
    }

    /**
     * Forward to the login page if the given relative path is not the login
     * page.
     * 
     * @param relativePath
     *            the current request's URL
     * @param save
     *            if true the value of the relativePath is store in the session
     * @param request
     *            the current HTTP servlet request
     * @param response
     *            the current HTTP servlet response
     * @param chain
     * @throws ServletException
     * @throws DatatypeConfigurationException
     * @throws IOException
     * @throws SAML2AuthnRequestException
     * @throws Exception
     */
    protected void forwardToLoginPage(String relativePath, boolean save,
                                      HttpServletRequest request, HttpServletResponse response,
                                      FilterChain chain) throws IOException, ServletException {

        String actualLoginPage = getActualLoginPage(request, loginPage,
                authSettings);
        String redirectUrl = request.getContextPath() + actualLoginPage;

        if (authSettings.isServiceProvider()) {

            if (save) {
                saveForwardUrl(relativePath, request);
            }

            storeRelayStateInSession(relativePath, request);
            try {
                AuthenticationHandler ah = new AuthenticationHandler(request,
                        response, authSettings);
                ah.handleAuthentication(false, request.getSession());
                return;
            } catch (SAML2AuthnRequestException e) {
                // forward to chain.doFilter
            }
        }

        if (!isLoginPage(relativePath, actualLoginPage)) {
            logger.logDebug("Forward to login page");
            if (save) {
                saveForwardUrl(relativePath, request);
            }
            try {
                if (BesServletRequestReader.isMarketplaceLogin(request)) {
                    JSFUtils.sendRedirect(response, redirectUrl);
                } else {
                    forward(actualLoginPage, request, response);
                }
            } catch (ServletException e) {
                JSFUtils.sendRedirect(response, redirectUrl);
            }

            return;
        }

        chain.doFilter(request, response);
    }

    ConfigurationService getConfigurationService(HttpServletRequest request) {
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(request.getSession());
        ConfigurationService cfgService = serviceAccess
                .getService(ConfigurationService.class);
        return cfgService;
    }

    MarketplaceService getMarketplaceService(HttpServletRequest request) {
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(request.getSession());
        if (mkpService == null) {
            mkpService = serviceAccess
                    .getService(MarketplaceService.class);
        }
        return mkpService;
    }

    MarketplaceCacheService getMarketplaceServiceCache(HttpServletRequest request) {
        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(request.getSession());
        if (mkpServiceCache == null) {
            mkpServiceCache = serviceAccess
                    .getService(MarketplaceCacheService.class);
        }
        return mkpServiceCache;
    }

    private boolean isLoginPage(String relativePath, String actualLoginPage) {
        return relativePath.startsWith(actualLoginPage)
                || relativePath.startsWith(pwdPage)
                || relativePath.startsWith("/a4j/");
    }

    private void saveForwardUrl(String relativePath, HttpServletRequest request) {
        String forwardUrl = relativePath;
        forwardUrl = copyParameters(request, forwardUrl);

        if (BesServletRequestReader.isMarketplaceLogin(request)) {
            // For marketplace login copy login type parameter
            forwardUrl = BesServletRequestReader
                    .copyRequestAttributeToURLParam(request,
                            Constants.REQ_ATTR_SERVICE_LOGIN_TYPE, forwardUrl);
            request.setAttribute(Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET,
                    forwardUrl);
        }

        if (authSettings.isServiceProvider()) {
            if (loginPage.equals(forwardUrl)) {
                forwardUrl = defaultPage;
            }
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(Constants.SESS_ATTR_FORWARD_URL, forwardUrl);
        session.setAttribute(Constants.SESS_ATTR_DEFAULT_TIMEOUT, session.getMaxInactiveInterval());
    }

    private String copyParameters(HttpServletRequest request, String forwardUrl) {
        if (!ADMStringUtils.isBlank(request.getQueryString())) {
            Map<String, String[]> paramMap = request.getParameterMap();
            Set<String> paramsKeys = paramMap.keySet();
            String paramString = "?";
            for (String paramsKey : paramsKeys) {
                if (paramsKey.equalsIgnoreCase("SAMLResponse")) {
                    continue;
                }//TODO
                paramString += paramsKey + "=" + paramMap.get(paramsKey)[0] + "&";
            }
            if (paramString.startsWith("?")) {
                return forwardUrl;
            }
            if (paramString.endsWith("&")) {
                paramString = paramString.substring(0, paramString.length()-1);
            }
            forwardUrl += paramString;
        }
        return forwardUrl;
    }

    private void storeRelayStateInSession(String relativePath,
            HttpServletRequest request) {
        String forwardUrl = relativePath;
        forwardUrl = copyParameters(request, forwardUrl);

        if (BesServletRequestReader.isMarketplaceLogin(request)) {
            forwardUrl = BesServletRequestReader
                    .copyRequestAttributeToURLParam(request,
                            Constants.REQ_ATTR_SERVICE_LOGIN_TYPE, forwardUrl);
        }

        request.getSession().setAttribute(Constants.SESS_ATTR_RELAY_STATE,
                forwardUrl);
    }

    /**
     * Get the relative URL for the login page. In case of a marketplace login
     * the path of the marketplace login page is returned. In case of a BES
     * login the passed default page is returned.
     * 
     * @param httpRequest
     *            - the HTTP request
     * @param defaultLoginPage
     *            - the path of page to be shown if it is no marketplace login.
     * 
     * @return see above.
     */
    protected String getActualLoginPage(HttpServletRequest httpRequest,
            String defaultLoginPage, AuthenticationSettings authSettings) {

        if (BesServletRequestReader.isMarketplaceRequest(httpRequest)) {
            return BaseBean.MARKETPLACE_LOGIN_PAGE;
        }

        // Check if service login for a marketplace service is
        // requested. In this case add it as parameter to be treated by the
        // login panel.
        String loginType = (String) httpRequest
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        if (Constants.REQ_ATTR_LOGIN_TYPE_MPL.equals(loginType)) {
            String serviceLoginType = "?serviceLoginType=" + loginType;
            if (authSettings != null && authSettings.isServiceProvider()) {
                return BaseBean.SAML_SP_LOGIN_AUTOSUBMIT_PAGE
                        + serviceLoginType;
            } else {
                return BaseBean.MARKETPLACE_LOGIN_PAGE + serviceLoginType;
            }
        }

        return defaultLoginPage;
    }

    /**
     * redirectToMp Url
     */
    protected boolean redirectToMpUrl(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException {
        String uri = httpRequest.getRequestURI();
        ConfigurationService cs = getConfigurationService(httpRequest);
        if (httpRequest.getRequestURL() == null)
            return false;
        String requestUrl = httpRequest.getRequestURL().toString();
        String queryString = httpRequest.getQueryString() == null ? ""
                : httpRequest.getQueryString();

        String mpRedirect;
        if (ADMValidator.isHttpsScheme(requestUrl)) {
            mpRedirect = getRedirectMpUrlHttps(cs);
        } else {
            mpRedirect = getRedirectMpUrlHttp(cs);
        }
        if (uri != null && !ADMStringUtils.isBlank(mpRedirect)) {
            String suffix = mpRedirect.contains("?") ? mpRedirect
                    .substring(mpRedirect.lastIndexOf('?') + 1) : "";
            if (!mpRedirect.contains(uri.replaceFirst("/index.jsf", ""))
                    || !suffix.equals(queryString)) {
                JSFUtils.sendRedirect(httpResponse, mpRedirect);
                return true;
            }
        }
        return false;
    }

    /**
     * @param cs
     *            the ConfigurationService for getting VOConfigurationSetting.
     * @return mpRedirect the value of ConfigurationSetting by key
     *         "HTTP_MP_ERROR_REDIRECT"
     */
    String getRedirectMpUrlHttp(ConfigurationService cs) {
        String mpRedirect = cs.getVOConfigurationSetting(
                ConfigurationKey.MP_ERROR_REDIRECT_HTTP,
                Configuration.GLOBAL_CONTEXT).getValue();
        return mpRedirect;
    }

    /**
     * @param cs
     *            the ConfigurationService for getting VOConfigurationSetting.
     * @return mpRedirect the value of ConfigurationSetting by key
     *         "HTTPS_MP_ERROR_REDIRECT"
     */
    String getRedirectMpUrlHttps(ConfigurationService cs) {
        String mpRedirect = cs.getVOConfigurationSetting(
                ConfigurationKey.MP_ERROR_REDIRECT_HTTPS,
                Configuration.GLOBAL_CONTEXT).getValue();
        return mpRedirect;
    }

}
