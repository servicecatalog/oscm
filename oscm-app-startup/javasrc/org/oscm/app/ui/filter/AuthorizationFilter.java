/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 27.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.ui.i18n.Messages;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.service.APPAuthenticationServiceBean;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOUserDetails;
import org.slf4j.LoggerFactory;

/**
 * @author Dirk Bernsau
 * 
 */
public class AuthorizationFilter implements Filter {

    public static final String LOCALE_DEFAULT = "en";
    public static final String LOCALE_JA = "ja";

    private transient Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @EJB
    protected APPAuthenticationServiceBean authService;

    @EJB
    protected APPConfigurationServiceBean configService;

    private static String excludeUrlPattern;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        excludeUrlPattern = filterConfig
                .getInitParameter("exclude-url-pattern");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)
                || !(response instanceof HttpServletResponse)) {
            response.getWriter().print(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();

        if (!httpRequest.getServletPath().matches(excludeUrlPattern)) {
            String requireTMforOrg = null;
            String controllerId = null;

            String path = httpRequest.getServletPath();
            if (path != null && path.startsWith("/controller/")) {
                controllerId = httpRequest
                        .getParameter(SessionConstants.SESSION_CTRL_ID);
                if (controllerId == null || controllerId.trim().isEmpty()) {
                    controllerId = (String) session
                            .getAttribute(SessionConstants.SESSION_CTRL_ID);
                }
                HashMap<String, String> organizations = configService
                        .getControllerOrganizations();
                if (!organizations.containsKey(controllerId)
                        || organizations.get(controllerId) == null) {
                    httpResponse.setStatus(404);
                    return;
                }
                requireTMforOrg = organizations.get(controllerId);
            }

            Object loggedInUserId = session
                    .getAttribute(SessionConstants.SESSION_USER_ID);
            Object loggedInUserOrgId = session
                    .getAttribute(SessionConstants.SESSION_USER_ORG_ID);
            Object loggedInUserRoles = session
                    .getAttribute(SessionConstants.SESSION_USER_ROLES);

            if (loggedInUserId != null) {
                if (requireTMforOrg != null && !hasRole(UserRoleType.PLATFORM_OPERATOR, (Set<?>) loggedInUserRoles)) {
                    if (!requireTMforOrg.equals(loggedInUserOrgId)
                            || loggedInUserRoles == null
                            || !(loggedInUserRoles instanceof Set)
                            || !hasRole(UserRoleType.TECHNOLOGY_MANAGER, (Set<?>) loggedInUserRoles)) {

                        send401(httpRequest, httpResponse, session, true);
                        return;
                    }
                } else {
                    if (!Boolean
                            .parseBoolean(""
                                    + session
                                    .getAttribute(SessionConstants.SESSION_USER_IS_ADMIN))) {
                        send401(httpRequest, httpResponse, session, false);
                        return;
                    }
                }

                // logged in with valid org and role => continue normally
                session.setAttribute(SessionConstants.SESSION_CTRL_ID,
                        controllerId);
                chain.doFilter(httpRequest, response);
                return;
            }

            // Check HTTP Basic authentication
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();

                    // only handle HTTP Basic authentication
                    if (basic.equalsIgnoreCase("basic")) {
                        String credentials = st.nextToken();
                        String userPass = new String(
                                Base64.decodeBase64(credentials));

                        // The decoded string is in the form "userID:password".
                        int p = userPass.indexOf(":");
                        if (p != -1) {
                            String userName = userPass.substring(0, p);
                            String password = userPass.substring(p + 1);
                            PasswordAuthentication auth = new PasswordAuthentication(
                                    userName, password);

                            try {

                                // Check authority
                                VOUserDetails voUser = null;
                                if (requireTMforOrg != null) {
                                    voUser = authService
                                            .getAuthenticatedTMForController(
                                                    controllerId, auth);
                                    session.removeAttribute(SessionConstants.SESSION_USER_IS_ADMIN);
                                    session.setAttribute(
                                            SessionConstants.SESSION_CTRL_ID,
                                            controllerId);
                                    session.setAttribute("loggedInUserOrgId",
                                            voUser.getOrganizationId());
                                    session.setAttribute("loggedInUserRoles",
                                            voUser.getUserRoles());
                                } else {
                                    voUser = authService
                                            .authenticateAdministrator(auth);
                                    session.setAttribute(
                                            SessionConstants.SESSION_USER_IS_ADMIN,
                                            "true");
                                    session.setAttribute("loggedInUserOrgId",
                                            voUser.getOrganizationId());
                                    session.setAttribute("loggedInUserRoles",
                                            voUser.getUserRoles());
                                }
                                session.setAttribute(
                                        SessionConstants.SESSION_USER_LOCALE,
                                        voUser.getLocale());

                                // Valid => store data in session
                                session.setAttribute(
                                        SessionConstants.SESSION_USER_ID,
                                        userName);
                                session.setAttribute(
                                        SessionConstants.SESSION_USER_PASSWORD,
                                        password);

                                // And continue
                                chain.doFilter(httpRequest, response);
                                return;

                            } catch (Exception e) {
                                if (null != logger) {
                                    logger.error("doFilter: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            send401(httpRequest, httpResponse, session, requireTMforOrg != null);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean hasRole(UserRoleType userRoleType, Set<?> loggedInUserRoles) {
        return loggedInUserRoles
                .contains(userRoleType);
    }

    private void send401(ServletRequest httpRequest,
            HttpServletResponse httpResponse, HttpSession session, boolean asTM) {
        // Return 401 error
        String clientLocale = httpRequest.getLocale().getLanguage();
        if (clientLocale.equals(LOCALE_JA)) {
            clientLocale = LOCALE_DEFAULT;
        }
        httpResponse.setHeader(
                "WWW-Authenticate",
                "Basic realm=\""
                        + Messages.get(clientLocale,
                                (asTM ? "ui.config.authentication.tm"
                                        : "ui.config.authentication.appadmin"))
                        + "\"");
        httpResponse.setStatus(401);
        session.invalidate();

    }

    @Override
    public void destroy() {
    }

}
