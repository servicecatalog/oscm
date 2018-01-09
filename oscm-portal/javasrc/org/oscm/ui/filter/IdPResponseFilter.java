/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 4, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.enumtypes.SigningAlgorithmType;
import org.oscm.internal.types.exception.InvalidSigningAlgorithmException;
import org.oscm.internal.types.exception.IssuerNotMatchException;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SessionIndexNotFoundException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.saml2.api.LogoutRequestGenerator;
import org.oscm.saml2.api.SAMLResponseExtractor;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;

import static org.oscm.internal.types.enumtypes.ConfigurationKey.SSO_DEFAULT_TENANT_ID;
import static org.oscm.types.constants.Configuration.GLOBAL_CONTEXT;
import static org.oscm.ui.common.Constants.REQ_PARAM_TENANT_ID;
import static org.oscm.ui.common.Constants.SESSION_PARAM_SAML_LOGOUT_REQUEST;

/**
 * @author farmaki
 * 
 */
public class IdPResponseFilter extends BaseBesFilter implements Filter {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(IdPResponseFilter.class);

    private RequestRedirector redirector;
    private String excludeUrlPattern;
    private AuthenticationSettings authSettings;
    private SAMLResponseExtractor samlResponseExtractor;
    private SessionBean sessionBean;

    private LogoutRequestGenerator logoutRequestGenerator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        redirector = new RequestRedirector(filterConfig);
        excludeUrlPattern = filterConfig
                .getInitParameter("exclude-url-pattern");

        authSettings = getAuthenticationSettings();
        samlResponseExtractor = getSamlResponseExtractor();
        logoutRequestGenerator = new LogoutRequestGenerator();
    }

    protected AuthenticationSettings getAuthenticationSettings() {
        if (authSettings == null) {
            authSettings = new AuthenticationSettings(
                    new ServiceLocator().findService(TenantService.class),
                    new ServiceLocator()
                            .findService(ConfigurationService.class));
        }
        return authSettings;
    }

    public SessionBean getSessionBean() {
        if (sessionBean == null) {
            sessionBean = new UiDelegate().findSessionBean();
        }
        return sessionBean;
    }

    /**
     * If the request contains a SAML 2.0 response, forward to the originally
     * requested resource is done. In case of service login, the request is
     * forwarded to an auto-submit page, to do the login in UserBean. <br/>
     * If the response does not contain a SAML 2.0 response, the next filter is
     * called. See web.xml for excluded url pattern.
     * 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!httpRequest.getServletPath().matches(excludeUrlPattern)) {

            if (containsSamlResponse(httpRequest)) {
                httpRequest.setAttribute(Constants.REQ_ATTR_IS_SAML_FORWARD,
                        Boolean.TRUE);
                String samlResponse = httpRequest.getParameter("SAMLResponse");
                try {
                    if (samlResponseExtractor.isFromLogin(samlResponse)) {
                        buildSAMLLogoutRequestAndStoreInSession(
                                (HttpServletRequest) request, samlResponse);
                        String relayState = httpRequest
                                .getParameter("RelayState");
                        if (relayState != null) {
                            String forwardUrl = getForwardUrl(httpRequest,
                                    relayState);
                            redirector.forward(httpRequest, httpResponse,
                                    forwardUrl);
                            return;
                        }
                    }
                } catch (SessionIndexNotFoundException e) {
                    LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_SESSION_INDEX_NOT_FOUND);
                    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_INVALID_SAML_RESPONSE);
                } catch (IssuerNotMatchException e) {
                    LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_ISSUER_DOES_NOT_MATCH);
                    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_INVALID_SAML_RESPONSE);
                } catch (SaaSApplicationException e) {
                    LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR);
                    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            BaseBean.ERROR_INVALID_SAML_RESPONSE);
                }

                if (httpRequest
                        .getAttribute(Constants.REQ_ATTR_ERROR_KEY) != null) {
                    redirector.forward(httpRequest, httpResponse,
                            BaseBean.ERROR_PAGE);
                    return;
                }
                httpRequest.setAttribute(Constants.REQ_ATTR_IS_SAML_FORWARD,
                        Boolean.FALSE);
            }

        }

        chain.doFilter(request, response);
    }

    protected void buildSAMLLogoutRequestAndStoreInSession(
            HttpServletRequest request, String samlResponse)
            throws SaaSApplicationException {
        String samlSessionId = getSamlResponseExtractor()
                .getSessionIndex(samlResponse);
        String nameID = getSamlResponseExtractor().getUserId(samlResponse);
        String tenantID = getSamlResponseExtractor().getTenantID(samlResponse);
        String issuer = getSamlResponseExtractor().getIssuer(samlResponse);
        authSettings.init(tenantID);
        if(!StringUtils.equalsIgnoreCase(issuer, authSettings.getIdpIssuer())) {
            //TODO: move issuer verification to AssertionContentVerifier if possible.
            throw new IssuerNotMatchException();
        }
        final SigningAlgorithmType signingAlgorithm = getSigningAlgorithmType();
        String logoutRequest = logoutRequestGenerator.generateLogoutRequest(
                    samlSessionId, nameID, getLogoutURL(),
                    getKeystorePath(), getIssuer(),
                    getKeyAlias(), getKeystorePass(), signingAlgorithm);
        request.getSession().setAttribute(SESSION_PARAM_SAML_LOGOUT_REQUEST, logoutRequest);
    }

    String getForwardUrl(HttpServletRequest httpRequest, String relayState) throws MarketplaceRemovedException {

        String forwardUrl;
        setLoginTypeAttribute(httpRequest, relayState);

        if (relayState.startsWith(Marketplace.MARKETPLACE_REGISTRATION)) {
            setRequestAttributesForSelfRegistration(httpRequest, relayState);
            forwardUrl = relayState;
        } else if (relayState.startsWith(Marketplace.MARKETPLACE_ADD)) {
            forwardUrl = relayState;
        } else if (Constants.REQ_ATTR_LOGIN_TYPE_MPL.equals(
                httpRequest.getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE))
                || relayState.startsWith(Marketplace.MARKETPLACE_ROOT)) {
            forwardUrl = setRequestAttributesForAutosubmit(httpRequest,
                    relayState);
        } else {
            forwardUrl = relayState;
        }

        return forwardUrl;
    }

    void setLoginTypeAttribute(HttpServletRequest httpRequest,
            String relayState) {
        BesServletRequestReader.copyURLParamToRequestAttribute(httpRequest,
                Constants.REQ_ATTR_SERVICE_LOGIN_TYPE, relayState);
    }

    String setRequestAttributesForAutosubmit(HttpServletRequest httpRequest,
            String relayState) throws MarketplaceRemovedException {
        String result = BaseBean.SAML_SP_LOGIN_AUTOSUBMIT_PAGE;
        SAMLCredentials samlCredentials = new SAMLCredentials(httpRequest);
        // TODO: possible security issue. This should be taken from session/cookie not from request.
        String tenantID = authSettings.getTenantID();
        httpRequest.setAttribute(Constants.REQ_PARAM_USER_ID,
                samlCredentials.getUserId());
        String generatedPassword = samlCredentials.generatePassword(tenantID);
        if (generatedPassword == null) {
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SAML_TIMEOUT);
            result = BaseBean.MARKETPLACE_START_SITE;
        }
        httpRequest.setAttribute(Constants.REQ_ATTR_PASSWORD,
                generatedPassword);
        httpRequest.setAttribute(Constants.REQ_ATTR_REQUESTED_REDIRECT,
                relayState);
        return result;
    }
    //TODO: refactor and move it to BaseBesFilter and remove duplication in AuthFilter.
    private String getTenantIDFromMarketplace(HttpServletRequest httpRequest) throws MarketplaceRemovedException {
        String marketplaceId = JSFUtils.getCookieValue(httpRequest, Constants.REQ_PARAM_MARKETPLACE_ID);
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
    //TODO: refactor and move it to BaseBesFilter and remove duplication in AuthFilter.
    private String getTenantIDFromRequest(HttpServletRequest request) {
        return request.getParameter(REQ_PARAM_TENANT_ID);
    }
    //TODO: refactor and move it to BaseBesFilter and remove duplication in AuthFilter.
    public String getTenantID(HttpServletRequest httpRequest) throws MarketplaceRemovedException {
        String tenantID;
        if (BesServletRequestReader
                .isMarketplaceRequest(httpRequest)) {
            tenantID = getTenantIDFromMarketplace(httpRequest);
        } else {
            tenantID = getTenantIDFromRequest(httpRequest);
        }
        if(StringUtils.isNotBlank(tenantID)) {
            httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID, tenantID);
        } else {
            tenantID = (String) httpRequest.getSession().getAttribute(REQ_PARAM_TENANT_ID);
        }
        if(StringUtils.isBlank(tenantID)) {
            tenantID = getConfigurationService(httpRequest).getVOConfigurationSetting(SSO_DEFAULT_TENANT_ID, GLOBAL_CONTEXT).getValue();
            httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID, tenantID);
        }
        return tenantID;
    }

    void setRequestAttributesForSelfRegistration(HttpServletRequest httpRequest,
            String relayState) {
        SAMLCredentials samlCredentials = new SAMLCredentials(httpRequest);
        httpRequest.setAttribute(Constants.REQ_PARAM_USER_ID,
                samlCredentials.getUserId());
        httpRequest.setAttribute(Constants.REQ_ATTR_REQUESTED_REDIRECT,
                relayState);
        httpRequest.getSession().setAttribute(Constants.REQ_ATTR_PASSWORD,
                samlCredentials.generatePassword());
    }

    boolean containsSamlResponse(HttpServletRequest httpRequest) {

        if (!authSettings.isServiceProvider()) {
            return false;
        }

        String samlResponse = httpRequest.getParameter("SAMLResponse");
        if (samlResponse != null) {
            return true;
        }

        return false;
    }

    @Override
    public void destroy() {
    }

    public SAMLResponseExtractor getSamlResponseExtractor() {
        if (samlResponseExtractor == null) {
            samlResponseExtractor = new SAMLResponseExtractor();
        }
        return samlResponseExtractor;
    }

    public void setRedirector(RequestRedirector redirector) {
        this.redirector = redirector;
    }

    public void setExcludeUrlPattern(String excludeUrlPattern) {
        this.excludeUrlPattern = excludeUrlPattern;
    }

    public void setAuthSettings(AuthenticationSettings authSettings) {
        this.authSettings = authSettings;
    }

    public String getKeystorePass() {
        return authSettings.getSigningKeystorePass();
    }

    public String getKeyAlias() {
        return authSettings.getSigningKeyAlias();
    }

    public String getIssuer() {
        return authSettings.getIssuer();
    }

    public String getKeystorePath() {
        return authSettings.getSigningKeystore();
    }

    public String getLogoutURL() {
        return authSettings.getLogoutURL();
    }

    public SigningAlgorithmType getSigningAlgorithmType() throws InvalidSigningAlgorithmException {
        final String alg = authSettings.getSigningAlgorithm();
        if (alg.equals(SigningAlgorithmType.SHA1.name())) {
            return SigningAlgorithmType.SHA1;
        } else if (alg.equals(SigningAlgorithmType.SHA256.name())) {
            return SigningAlgorithmType.SHA256;
        } else {
            throw new InvalidSigningAlgorithmException("Invalid signature hash algorithm: " + alg);
        } 
    }
}
