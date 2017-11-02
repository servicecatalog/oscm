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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.*;

/**
 * Filter which establishes the marketplace context.
 * 
 */
public class MarketplaceContextFilter extends BaseBesFilter {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceContextFilter.class);

    /**
     * Establishes the correct marketplace context, and stores/updates the
     * corresponding mId in the session. Upon leaving the doFilter() method, the
     * session mId is either set to a valid marketplace id or deleted.
     * 
     * The doFilter method of the Filter is called by the container each time a
     * request/response pair is passed through the chain due to a client request
     * for a resource at the end of the chain.
     * 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = new IgnoreCharacterEncodingHttpRequestWrapper(
                (HttpServletRequest) request);
        final AuthorizationRequestData rdo = initializeRequestDataObject(httpRequest);
        final HttpSession httpSession = httpRequest.getSession();
        final String mId = retrieveMarketplaceId(httpRequest, rdo);

        if (rdo.isMarketplaceLoginPage()) {
            String forwardUrl;

            if (authSettings.isServiceProvider()) {
                forwardUrl = httpRequest.getParameter("RelayState");
            } else {
                forwardUrl = (String) httpRequest.getSession().getAttribute(
                        Constants.SESS_ATTR_FORWARD_URL);
            }

            if (!ADMStringUtils.isBlank(forwardUrl)) {
                BesServletRequestReader.copyURLParamToRequestAttribute(
                        httpRequest, Constants.REQ_ATTR_SERVICE_LOGIN_TYPE,
                        forwardUrl);
                httpRequest.setAttribute(
                        Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET, forwardUrl);
            }
        }

        // execute appropriately
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (rdo.isRequiredToChangePwd()
                && !BesServletRequestReader.hasPasswordChangeToken(httpRequest)) {
            forwardToLoginPage(rdo.getRelativePath(), true, httpRequest,
                    httpResponse, chain);
            return;
        }

        if (BesServletRequestReader.isMarketplaceRedirect(httpRequest)) {
            redirectToMarketplace(httpRequest, httpResponse);
            return;
        }

        if (!rdo.isAccessToServiceUrl()) {
            updateSessionAndCookie(httpSession, httpRequest, httpResponse, mId);
        }
        if (ADMStringUtils.isBlank(mId) && rdo.isMarketplace()
                && !rdo.isMarketplaceErrorPage()) {
            handleWrongMarketplaceIdCase(httpRequest, httpResponse, rdo);
            return;
        }

        if (!ADMStringUtils.isBlank(httpRequest.getQueryString())) {
            if (hasInvalidChangePasswordToken(httpRequest)) {
                handleInvalidURL(httpRequest, httpResponse, rdo);
                return;
            }
        }
        chain.doFilter(httpRequest, httpResponse);
    }

    /**
     * Bug 10154: In case if an URL with incomplete, not decode-able token query
     * part is given, the first getParmeter call fails parsing parameter names.
     */
    private boolean containsInvalidToken(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        int tokenIdx = queryString.indexOf("token=");
        if (tokenIdx >= 0) {
            String encodedParam = queryString.substring(tokenIdx + 5);
            try {
                URLDecoder.decode(encodedParam, "UTF-8");
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    boolean hasInvalidChangePasswordToken(final HttpServletRequest httpRequest) {
        // TODO: cause UI test failed due to redirect to error page in
        // handleWrongMarketplaceIdCase() line 112
        return (isChangePasswordRequested(httpRequest) && containsInvalidToken(httpRequest));
    }

    boolean isChangePasswordRequested(final HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().matches(".*changePassword\\.jsf");

    }

    void handleInvalidURL(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, AuthorizationRequestData rdo)
            throws IOException {
        final ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(httpRequest.getSession());
        httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_INVALID_MARKETPLACE_URL);

        String page = getDefaultUrl(serviceAccess, rdo, httpRequest);
        String uri = httpRequest.getRequestURI();
        if (uri != null && uri.equals(httpRequest.getContextPath() + page)) {
            page = BaseBean.MARKETPLACE_ERROR_PAGE;
            httpRequest.getSession().invalidate();
        }
        sendRedirect(httpRequest, httpResponse, page);
    }

    private String retrieveMarketplaceId(final HttpServletRequest httpRequest,
            final AuthorizationRequestData rdo) {

        if (rdo.isAccessToServiceUrl()) {
            /*
             * Here we must NOT read the request parameters to get marketplace
             * id for service URLs because this would cause a state switch of
             * the request. Afterwards the rewriting of a POST request may fail
             * because the parameters can't be accessed via the request input
             * stream.
             */
            return null;
        }

        final HttpSession httpSession = httpRequest.getSession();
        String mId = httpRequest
                .getParameter(Constants.REQ_PARAM_MARKETPLACE_ID);
        if (ADMStringUtils.isBlank(mId)) {
            mId = (String) httpSession
                    .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
        }
        if (ADMStringUtils.isBlank(mId)) {
            mId = getCookieValue(httpRequest,
                    Constants.REQ_PARAM_MARKETPLACE_ID);
        }
        boolean validMarketplace = validateGivenMarketplaceId(httpSession, mId);

        // check if old local marketplace exists
        if (!validMarketplace && !rdo.isMarketplace()) {
            mId = httpRequest.getParameter(Constants.REQ_PARAM_SUPPLIER_ID);
            if (ADMStringUtils.isBlank(mId) && rdo.getUserDetails() != null) {
                mId = rdo.getUserDetails().getOrganizationId();
            }
            validMarketplace = validateGivenMarketplaceIdWithoutLogging(httpSession, mId);
        }
        return validMarketplace ? mId : null;
    }

    protected String getCookieValue(HttpServletRequest httpRequest,
            String attrKey) {
        return JSFUtils.getCookieValue(httpRequest, attrKey);
    }

    private boolean validateGivenMarketplaceId(final HttpSession session,
            String mId) {
        try {
            return checkMarketplaceExists(session, mId);
        } catch (ObjectNotFoundException e) {
            final String msg = String
                    .format("Marketplace with given mId %s does not exist, remove it from session and cookie",
                            mId);
            logger.logDebug(msg);
            return false;
        }
    }
    
    private boolean validateGivenMarketplaceIdWithoutLogging(final HttpSession session,
            String mId) {
        try {
            return checkMarketplaceExists(session, mId);
        } catch (ObjectNotFoundException e) {
            return false;
        }
    }

    private boolean checkMarketplaceExists(final HttpSession session,
            String mId) throws ObjectNotFoundException {
        if (session == null || ADMStringUtils.isBlank(mId)) {
            return false;
        }
        if (mId.equals(session.getAttribute(Constants.SESS_ATTR_MARKETPLACE_ID))) {
            return true;
        }

        getMarketplaceService(session).getMarketplaceById(mId);
        session.setAttribute(Constants.SESS_ATTR_MARKETPLACE_ID, mId);
        return true;

    }

    void handleWrongMarketplaceIdCase(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, AuthorizationRequestData rdo)
            throws ServletException, IOException {
        final ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(httpRequest.getSession());
        httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_INVALID_MARKETPLACE_URL);

        String page = getDefaultUrl(serviceAccess, rdo, httpRequest);
        String uri = httpRequest.getRequestURI();
        if (uri != null
                && redirectToMpUrl(httpRequest, httpResponse)) {
            return;
        }

        if (uri != null && uri.equals(httpRequest.getContextPath() + page)) {
            page = BaseBean.MARKETPLACE_ERROR_PAGE;
            httpRequest.getSession().invalidate();
            sendRedirect(httpRequest, httpResponse, page);
            return;
        }

        forward(page, httpRequest, httpResponse);
    }

    protected MarketplaceService getMarketplaceService(HttpSession session) {
        final ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(session);
        return serviceAccess.getService(MarketplaceService.class);
    }

    private void redirectToMarketplace(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        String forwardUrl = (String) httpRequest.getSession().getAttribute(
                Constants.SESS_ATTR_FORWARD_URL);
        if (ADMStringUtils.isBlank(forwardUrl)) {
            forwardUrl = BaseBean.MARKETPLACE_START_SITE;
        }
        httpRequest.getSession().setAttribute(Constants.SESS_ATTR_FORWARD_URL,
                null);
        JSFUtils.sendRedirect(httpResponse, httpRequest.getContextPath()
                + forwardUrl);
    }

    /**
     * store marketplaceId in a cookie so we can use it after a session-timeout
     * 
     * @throws UnsupportedEncodingException
     */
    private void updateSessionAndCookie(HttpSession session,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            String marketplaceId) throws UnsupportedEncodingException {
        session.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, marketplaceId);
        // store mId in cookie to not loose it in case of session timeout
        String cookieMIdValue = ADMStringUtils.isBlank(marketplaceId) ? ""
                : URLEncoder.encode(marketplaceId,
                        Constants.CHARACTER_ENCODING_UTF8);
        int maxAge = -1;
        if (ADMStringUtils.isBlank(marketplaceId)) {
            // this causes the cookie to delete
            maxAge = 0;
        }
        JSFUtils.setCookieValue(httpRequest, httpResponse,
                Constants.REQ_PARAM_MARKETPLACE_ID, cookieMIdValue, maxAge);
    }

}
