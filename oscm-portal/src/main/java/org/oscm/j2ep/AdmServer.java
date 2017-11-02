/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.j2ep;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.j2ep.model.Server;
import net.sf.j2ep.servers.ServerContainerBase;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.internal.vo.VOSubscription;

/**
 * J2EP Server which creates a new instance for each request. This allow the
 * rule of the server to store some information during the processing.
 * 
 */
public class AdmServer extends ServerContainerBase implements Server {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AdmServer.class);

    /**
     * The host and port for this server
     */
    private String domainName;

    /**
     * The path for this server
     */
    private String path;

    /**
     * Create a new server for each request so that we can store some data in
     * the member variables of the rule while the request is processed.
     * 
     * @see net.sf.j2ep.model.ServerContainer#getServer(javax.servlet.http.HttpServletRequest)
     */
    public Server getServer(HttpServletRequest request) {
        Server server = (Server) request.getAttribute("proxyServer");
        if (server != null) {
            return server;
        }

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI().substring(contextPath.length());
        String domainName = null;
        String path = "";
        Matcher matcher = AdmRule.getMatchPattern().matcher(uri);
        if (matcher.matches()) {
            String key = matcher.group(1);
            if (key == null || key.length() <= 1) {
                return null;
            }
            // remove the leading slash
            key = key.substring(1);
            Map<?, ?> map = (Map<?, ?>) request.getSession().getAttribute(
                    Constants.SESS_ATTR_ACTIVE_SUB_MAP);
            String url = null;
            if (map != null) {
                VOSubscription sub = (VOSubscription) map.get(key);
                if (sub != null) {
                    url = sub.getServiceBaseURL();
                }
            }
            if (url == null) {
                return null;
            }
            // ignore the http:// or https://
            int begin = url.indexOf("://");
            if (begin < 0) {
                return null;
            }
            begin += 3; // the length of "://"
            int end = url.indexOf("/", begin);
            if (end < 0) {
                domainName = url.substring(begin);
                path = "";
            } else {
                domainName = url.substring(begin, end);
                path = url.substring(end);

            }
        }

        AdmServer admServer = new AdmServer();
        admServer.setDomainName(domainName);
        admServer.setPath("");
        AdmRule rule = new AdmRule();
        rule.setRewriteTo(path + "$2"); // $1 is the subscription id
        rule.setRevertPattern(Pattern.compile("^" + path + "(/.*)?"));
        admServer.setRule(rule);
        return admServer;
    }

    /**
     * Read the method parameter from the request and store the usertoken for
     * the login method in the session
     * 
     * @see net.sf.j2ep.model.Server#preExecute(javax.servlet.http.HttpServletRequest)
     */
    public HttpServletRequest preExecute(HttpServletRequest request) {

        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug("preExecute " + this + " URI: "
                    + request.getRequestURI());
        }

        return request;
    }

    /**
     * Will not do any handling
     * 
     * @see net.sf.j2ep.model.Server#postExecute(javax.servlet.http.HttpServletResponse)
     */
    public HttpServletResponse postExecute(HttpServletResponse response) {
        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug("postExecute " + this);
        }

        return response;
    }

    /**
     * @see net.sf.j2ep.model.ServerContainer#getServerMapped(java.lang.String)
     */
    public Server getServerMapped(String location) {
        if (location != null && domainName != null
                && location.startsWith(domainName)) {
            return this;
        }
        return null;
    }

    /**
     * @see net.sf.j2ep.model.Server#getDomainName()
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Sets the host and port we are mapping to.
     * 
     * @param domainName
     *            Value to set
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * @see net.sf.j2ep.model.Server#getPath()
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path we are mapping to.
     * 
     * @param path
     *            The path
     */
    public void setPath(String path) {
        if (path == null) {
            path = "";
        } else {
            this.path = path;
        }
    }

    /**
     * @see net.sf.j2ep.model.Server#setConnectionExceptionRecieved(java.lang.Exception)
     */
    public void setConnectionExceptionRecieved(Exception e) {
        logger.logError(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.ERROR_PROXY_FORWARDING_FAILED);
    }

}
