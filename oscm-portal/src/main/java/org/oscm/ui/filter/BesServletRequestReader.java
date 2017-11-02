/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.07.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;

/**
 * @author kulle
 * 
 */
public class BesServletRequestReader {

    public final static String REQ_PARAM_PASSWORD = "password";
    public final static String REQ_PARAM_PASSWORD_NEW = "newPassword";
    public final static String REQ_PARAM_PASSWORD_NEW2 = "newPassword2";
    public final static String REQ_PARAM_PASSWORD_CHANGE_TOKEN = "passwordForm:passwordChangeToken";

    protected static boolean isRequestedToChangePassword(
            HttpServletRequest httpRequest) {
        String currentPwd = httpRequest.getParameter(REQ_PARAM_PASSWORD);
        String newPwd = httpRequest.getParameter(REQ_PARAM_PASSWORD_NEW);
        String pwdConfirmation = httpRequest
                .getParameter(REQ_PARAM_PASSWORD_NEW2);
        return !ADMStringUtils.isBlankNoTrim(currentPwd)
                && !ADMStringUtils.isBlankNoTrim(newPwd)
                && !ADMStringUtils.isBlankNoTrim(pwdConfirmation);
    }

    protected static String getPasswordChangeToken(
            HttpServletRequest httpRequest) {
        return httpRequest.getParameter(REQ_PARAM_PASSWORD_CHANGE_TOKEN);
    }

    protected static boolean hasPasswordChangeToken(
            HttpServletRequest httpRequest) {
        return !ADMStringUtils.isBlank(getPasswordChangeToken(httpRequest));
    }

    protected static String getRelativePath(HttpServletRequest httpRequest) {
        String relativePath = httpRequest.getServletPath();
        if (httpRequest.getPathInfo() != null) {
            relativePath += httpRequest.getPathInfo();
        }
        return relativePath;
    }

    protected static boolean isMarketplaceRequest(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(Marketplace.MARKETPLACE_ROOT);
    }

    protected static boolean isMarketplaceLoginPageRequest(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.MARKETPLACE_LOGIN_PAGE);
    }

    protected static boolean isMarketplaceErrorPageRequest(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.MARKETPLACE_ERROR_PAGE);
    }

    protected static boolean isMarketplaceRedirect(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.MARKETPLACE_REDIRECT);
    }

    /**
     * Returns true if the request targets the landing page of the market place
     */
    protected static boolean isLandingPage(HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.MARKETPLACE_START_SITE);
    }

    /**
     * Returns true if the request targets the manage payment types page of the
     * admin portal
     */
    protected static boolean isManagePaymentTypesPage(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.MANAGE_PAYMENT_TYPES_PAGE)
                || httpRequest.getServletPath()
                        .startsWith(BaseBean.MANAGE_PAYMENT_TYPES_PAGE_XHTML);
    }

    /**
     * Returns true if the request targets the payment page in account menu of
     * the market place
     */
    protected static boolean isAccountPaymentPage(
            HttpServletRequest httpRequest) {
        return httpRequest.getServletPath()
                .startsWith(BaseBean.ACCOUNT_PAYMENT_PAGE)
                || httpRequest.getServletPath()
                        .startsWith(BaseBean.ACCOUNT_PAYMENT_PAGE_XHTML);
    }

    /**
     * Copy the value of the given attribute from the given request as parameter
     * to the given URL.
     */
    protected static String copyRequestAttributeToURLParam(
            HttpServletRequest httpRequest, String attribute, String url) {
        String value = (String) httpRequest.getAttribute(attribute);
        if (value != null) {
            if (url.indexOf(attribute) == -1) {
                char appendChar = (url.indexOf('?') == -1) ? '?' : '&';
                url += (appendChar + attribute + "=" + value);
            }
        }
        return url;
    }

    /**
     * Return true if and only if the session attribute
     * SESS_ATTR_ONLY_SERVICE_LOGIN is true.
     * 
     * @param session
     *            the HttpSession.
     * @return true if and only if the session attribute
     *         SESS_ATTR_ONLY_SERVICE_LOGIN is true.
     */
    protected static boolean onlyServiceLogin(HttpSession session) {
        Boolean onlyServiceLogin = (Boolean) session
                .getAttribute(Constants.SESS_ATTR_ONLY_SERVICE_LOGIN);
        return onlyServiceLogin != null && onlyServiceLogin.booleanValue();
    }

    /**
     * Read a request parameter and store the value as a request attribute if it
     * is not blank.
     * 
     * @param request
     *            the HttpServletRequest.
     * @param name
     *            the name of the request parameter/attribute
     */
    protected static void param2Attr(HttpServletRequest request, String name) {
        String val = request.getParameter(name);
        if (val != null) {
            request.setAttribute(name, val.trim());
        }
    }

    /**
     * Reads a request parameter and stores its value, if it is not blank, to
     * the session.
     */
    protected static void requestParamToSession(HttpServletRequest httpRequest,
            String parameterName) {

        String parameterValue = httpRequest.getParameter(parameterName);
        if (!ADMStringUtils.isBlank(parameterValue)) {
            httpRequest.getSession().setAttribute(parameterName,
                    parameterValue);
        }
    }

    /**
     * Check if given request is related to the market place or a subscription
     * of the marketplace.
     */
    protected static boolean isMarketplaceLogin(
            HttpServletRequest httpRequest) {
        if (isMarketplaceRequest(httpRequest)) {
            return true;
        }
        // login for market place requested?
        String loginType = (String) httpRequest
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        return Constants.REQ_ATTR_LOGIN_TYPE_MPL.equals(loginType);
    }

    protected static boolean isServiceLogin(HttpServletRequest httpRequest) {
        // is service login for given type requested?
        String loginType = (String) httpRequest
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        return loginType != null;
    }

    /**
     * Copy the value of the given parameter from the given URL in a concerning
     * request attribute.
     */
    protected static String getParamFromURL(String param, String url) {
        int paramIdx = url.indexOf(param);
        int startValueIdx = paramIdx + param.length() + 1;
        if (paramIdx != -1 && url.length() > startValueIdx) {
            String value = url.substring(startValueIdx);
            int nextParmIdx = value.indexOf('&');
            if (nextParmIdx != -1) {
                value = value.substring(0, nextParmIdx);
            }
            return value;
        }
        return null;
    }

    /**
     * Copy the value of the given parameter from the given URL in a concerning
     * request attribute.
     */
    protected static void copyURLParamToRequestAttribute(
            HttpServletRequest httpRequest, String param, String url) {
        String value = getParamFromURL(param, url);
        if (value != null) {
            httpRequest.setAttribute(param, value);
        }
    }

    /**
     * Set the error attribute in the current request
     * 
     * @param request
     *            the HttpServletRequest.
     * @param e
     *            the application exception causing the error
     */
    protected static void setErrorAttributes(HttpServletRequest request,
            SaaSApplicationException e) {
        request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, e.getMessageKey());
        Object[] params = e.getMessageParams();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                request.setAttribute(Constants.REQ_ATTR_ERROR_PARAM + i,
                        params[i]);
            }
        }
    }

}
