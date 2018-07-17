/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.filter;

import java.io.IOException;
import java.util.StringTokenizer;

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
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dirk Bernsau
 *
 */
public class AuthorizationFilter implements Filter {

    private static final Logger logger = LoggerFactory
            .getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to init
    }

    public Logger getLogger() {
        return logger;
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

        if (session.getAttribute("loggedInUserId") != null) {
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
                        String username = userPass.substring(0, p);
                        String password = userPass.substring(p + 1);
                        PasswordAuthentication tpUser = new PasswordAuthentication(
                                username, password);

                        try {
                            // Check authority by loading controller settings
                            APPlatformService pSvc = getPlatformService();
                            pSvc.authenticate(Controller.ID, tpUser);

                            session.setAttribute("loggedInUserId", username);
                            session.setAttribute("loggedInUserPassword",
                                    password);

                            chain.doFilter(httpRequest, response);
                            return;
                        } catch (Exception e) {
                            getLogger().debug("doFilter: " + e.getMessage());
                        }
                    }
                }
            }
        }

        String clientLocale = httpRequest.getLocale().getLanguage();
        httpResponse.setHeader(
                "WWW-Authenticate",
                "Basic realm=\""
                        + Messages
                        .get(clientLocale, "ui.config.authentication")
                        + "\"");
        httpResponse.setStatus(401);
    }

    APPlatformService getPlatformService() {
        return APPlatformServiceFactory.getInstance();
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

}
