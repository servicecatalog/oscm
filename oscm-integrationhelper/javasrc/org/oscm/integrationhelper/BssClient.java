/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationhelper;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oscm.apiversioning.handler.VersionHandler;
import org.oscm.wsproxy.ServicePort;
import org.oscm.wsproxy.WsProxy;
import org.oscm.intf.SessionService;

/**
 * Implementation of a web service client for the Session Service form the OSCM
 * platform API. The necessary information for building a web service client is
 * read from property file.
 * 
 */
public class BssClient {
    private static Log logger = LogFactory.getLog(BssClient.class);

    private static final String SERVICE_NAME = "SessionService";
    private static final String PROPERTY_FILE_NAME = "tokenhandler.properties";
    private static final String SERVICE_CLIENT_FILE_NAME = "webserviceclient.properties";
    private static final WsProxyInfo wsProxyInfo = new WsProxyInfo(
            SERVICE_CLIENT_FILE_NAME, SERVICE_NAME, PROPERTY_FILE_NAME);

    /**
     * Helper method for calling the a session service method for deleting a
     * service session.
     * 
     * @param subKey
     *            - subscription key
     * @param sessionId
     *            - session identifier
     * @return
     */
    private static String deleteServiceSession(long subKey, String sessionId) {
        return createServiceProxy().deleteServiceSession(subKey, sessionId);
    }

    /**
     * Helper method for calling the resolve user token method of the session
     * web service.
     * 
     * @param subKey
     *            - subscription key
     * @param sessionId
     *            - session identifier
     * @param userToken
     *            - user token
     * @return
     */
    private static String resolveUserToken(long subKey, String sessionId,
            String userToken) {
        return createServiceProxy().resolveUserToken(subKey, sessionId,
                userToken);
    }

    /**
     * Helper method to create a web service proxy for accessing the Session
     * Service.
     * 
     * @return session service proxy
     */
    private static SessionService createServiceProxy() {
        if (wsProxyInfo.getServicePort() == ServicePort.STS) {
            return WsProxy.getProxySTS(wsProxyInfo.getWsInfo(),
                    wsProxyInfo.getUserCredentials(), SessionService.class);
        } else {
            return WsProxy.getProxyInternal(wsProxyInfo.getWsInfo(),
                    wsProxyInfo.getUserCredentials(), SessionService.class);
        }
    }

    /**
     * This method requests the platform to provide a user ID for the given user
     * token.
     * 
     * @param session
     *            the HTTP session for the current request. After successful
     *            execution of this method, the user ID, instance ID, platform
     *            session ID, subscription key, and user token are stored as
     *            attributes of this session.
     * @param usertoken
     *            the user token to resolve
     * @param instanceId
     *            the identifier of the application instance for which the user
     *            ID is requested. The instance ID is not evaluated but only
     *            added to the HTTP session attributes.
     * @param subKey
     *            the key of the service subscription related to the application
     *            instance for which the user ID is requested
     * @param bssId
     *            the identifier of the user's platform session
     * @return the user ID for the user token
     * @throws IOException
     * @throws NumberFormatException
     *             if the subscription key is not a valid LONG value
     */
    public static String resolveUsertoken(HttpSession session,
            String usertoken, String instanceId, String subKey, String bssId)
            throws NumberFormatException, IOException {
        String sessionId = session.getId();
        logger.debug("resolveUsertoken - " + sessionId);
        if (usertoken == null || subKey == null || bssId == null) {
            logger.error("resolveUsertoken: "
                    + "usertoken, subKey or bssId missing!");
            return null;
        }
        String userId = resolveUserToken(Long.parseLong(subKey), bssId,
                usertoken);
        session.setAttribute(Constants.USER_ID, userId);
        session.setAttribute(Constants.INSTANCE_ID, instanceId);
        session.setAttribute(Constants.CM_ID, bssId);
        session.setAttribute(Constants.SUB_KEY, subKey);
        session.setAttribute(Constants.USERTOKEN, usertoken);
        return userId;
    }

    /**
     * This method informs the platform about a logout (session destruction).
     * 
     * @param session
     *            the HTTP session to be destroyed
     * @return the URL of the logout page of the underlying service. This can be
     *         used for redirections.
     * @throws IOException
     * @throws NumberFormatException
     */
    static public String logoutUser(HttpSession session)
            throws NumberFormatException, IOException {
        logger.debug("logoutUser - " + session.getId());

        if (session.getAttribute(Constants.SUB_KEY) == null
                || session.getAttribute(Constants.CM_ID) == null) {
            logger.error("logoutUser - subKey or bssId missing!");
            return null;
        }

        return deleteServiceSession(Long.parseLong(session.getAttribute(
                Constants.SUB_KEY).toString()),
                session.getAttribute(Constants.CM_ID).toString());
    }

    /**
     * This method handles the HTTP request for the user token resolution. It
     * calls the <code>resolveUsertoken</code> method. After the token has been
     * resolved, it forwards the request to the target path configured in the
     * <code>TOKENHANDLER_FORWARD</code> property.
     * 
     * @param request
     *            the HTTP servlet request with the user token to be resolved
     * @param response
     *            the HTTP servlet response for the request
     * @param servletContext
     *            the servlet context of the request
     * @throws ServletException
     *             if the forwarding of the request or the call to the platform
     *             fails
     * @throws IOException
     *             if the forwarding of the request fails
     */
    static public void processHTTPRequest(HttpServletRequest request,
            HttpServletResponse response, ServletContext servletContext)
            throws ServletException, IOException {

        String usertoken = request.getParameter(Constants.USERTOKEN);
        String instanceId = request.getParameter(Constants.INSTANCE_ID);
        String bssId = request.getParameter(Constants.CM_ID);
        String subKey = request.getParameter(Constants.SUB_KEY);

        String userId = BssClient.resolveUsertoken(request.getSession(),
                usertoken, instanceId, subKey, bssId);

        if (userId == null || userId.length() == 0) {
            logger.error("Error: missing userId!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String forward = wsProxyInfo.getForward();
        logger.debug("Forward to " + forward);
        RequestDispatcher rd = servletContext.getRequestDispatcher(forward);
        rd.forward(request, response);
    }

    /**
     * This method tests a web service call to CT_MG.
     * 
     * @param request
     *            the HTTP servlet request with the user token to be resolved
     * @param response
     *            the HTTP servlet response for the request
     * @param servletContext
     *            the servlet context of the request
     * @throws ServletException
     *             if the forwarding of the request or the call to the platform
     *             fails
     * @throws IOException
     *             if the forwarding of the request fails
     */
    static public void testWebServiceCall(HttpServletRequest request,
            HttpServletResponse response, ServletContext servletContext)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<b>OSCM web service call test</b></br></br>");
        getWebServiceClientProperties(out);
        try {
            createServiceProxy().getSubscriptionKeysForSessionId("xxxx");
            out.println("Request SOAP message: </br>"
                    + VersionHandler.getMessage() + "</br>");
            out.println("OSCM web service call was successful.");
        } catch (Exception e) {
            out.println("Error calling OSCM web service:</br></br>");
            out.println(e.getMessage() + "</br>");
            if (e.getCause() != null) {
                out.println(e.getCause().getMessage() + "</br>");
            }
        }
    }

    /**
     * This method prints the web service client properties
     * 
     * @param out
     *            response writer object
     */
    private static void getWebServiceClientProperties(PrintWriter out) {
        out.println("Web service client properties:</br></br>");
        out.println("Endpoint Address: "
                + wsProxyInfo.getWsInfo().getEndpointAddress() + "</br>");
        out.println("Client service version: "
                + wsProxyInfo.getServiceVersion() + "</br>");
        out.println("User: " + wsProxyInfo.getUserCredentials().getUser()
                + "</br>");
        out.println("Password: ****</br>");
        out.println("Tenant ID: ****</br></br></br>");
    }
}
