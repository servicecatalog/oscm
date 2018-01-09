/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 07.05.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.app.ui.SessionConstants;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.service.APPAuthenticationServiceBean;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.vo.VOUserDetails;

/**
 * Servlet which starts the timers after restarting the domain and acts as
 * notification handler for intermediate manual provisioning steps.
 * 
 * @author soehnges
 * 
 */
public class NotificationServlet extends HttpServlet {
    private static final long serialVersionUID = -2067317865984575658L;
    private static final Logger logger = LoggerFactory
            .getLogger(NotificationServlet.class);

    @EJB
    APPAuthenticationServiceBean authService;

    @EJB
    APPTimerServiceBean timerService;

    @EJB
    APPConfigurationServiceBean configService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {

            if (configService.isAPPSuspend()) {
                timerService.restart(true);
            }
            // Startup timers
            timerService.initTimers();
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String locale = "en";
        try {
            // Get arguments
            String ctrlId = req.getParameter(SessionConstants.SESSION_CTRL_ID);
            String serviceId = req.getParameter("sid");
            if (serviceId == null) {
                // SID is mandatory
                String errText = Messages.get(locale,
                        "servlet_notification_error_nosid");
                printError("en", resp, errText);
                return;
            }
            if (ctrlId == null) {
                // CID is mandatory
                String errText = Messages.get(locale,
                        "servlet_notification_error_nocid");
                printError("en", resp, errText);
                return;
            }
            if (!isBasicAuthValid(ctrlId, serviceId, req, resp)) {
                return;
            }

            if (req.getAttribute("loginUserLocale") != null) {
                locale = req.getAttribute("loginUserLocale").toString();
            }

            // Transform given parameters into a property bag
            // If multiple value are given for the same key, we will only use
            // the first one.
            Properties p = new Properties();
            Enumeration<String> enm = req.getParameterNames();
            while (enm.hasMoreElements()) {
                String key = enm.nextElement();
                p.put(key, req.getParameter(key));
            }
            timerService.raiseEvent(ctrlId, serviceId, p);

            // Send output
            String okText = Messages
                    .get(locale, "servlet_notification_success");
            printOutput(locale, resp, okText);

        } catch (APPlatformException pe) {
            printError(locale, resp, pe.getMessage());
            return;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Displays some error output in the HTML response.
     */
    private void printError(final String locale, HttpServletResponse resp,
            String errorText) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String title = Messages.get(locale, "servlet_notification_title");
        String error = Messages.get(locale, "servlet_notification_error",
                new Object[] { errorText });

        String html = Messages.get(locale, "servlet_notification_html",
                new Object[] { title, title, error });
        PrintWriter out = resp.getWriter();

        out.println(html);
        out.close();
    }

    /**
     * Displays some output in the HTML response.
     */
    private void printOutput(final String locale, HttpServletResponse resp,
            String message) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String title = Messages.get(locale, "servlet_notification_title");
        String html = Messages.get(locale, "servlet_notification_html",
                new Object[] { title, title, message });
        PrintWriter out = resp.getWriter();

        out.println(html);
        out.close();
    }

    private boolean isBasicAuthValid(String controllerId, String serviceId,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        boolean valid = false;

        String authHeader = request.getHeader("Authorization");
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
                            VOUserDetails user = authService
                                    .authenticateTMForInstance(controllerId,
                                            serviceId, auth);
                            request.setAttribute("loginUserLocale",
                                    user.getLocale());
                            valid = true;
                        } catch (Exception e) {
                            // something wrong
                            logger.error(
                                    "Authentication of technology manager failed with Exception ",
                                    e);
                        }
                    }
                }
            }
        }

        if (!valid) {
            response.setHeader("WWW-Authenticate",
                    "Basic realm=\"Please log in as technology manager\"");
            response.setStatus(401);
        }
        return valid;
    }
}
