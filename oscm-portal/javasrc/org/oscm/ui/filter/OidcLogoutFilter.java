/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                              
 *  Creation Date: Jul 9, 2019                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscm.internal.intf.SessionService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.delegates.ServiceLocator;

public class OidcLogoutFilter extends BaseBesFilter {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(OidcLogoutFilter.class);
  private FilterConfig filterConfig;
  private SessionService sessionService;
  private String errorPage = "/public/error.jsf";

  public SessionService getSessionService() {
    if (sessionService == null) {
      sessionService = new ServiceLocator().findService(SessionService.class);
    }
    return sessionService;
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);
  }


  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (isLogoutRequested(httpRequest)) {
      HttpSession currentSession = httpRequest.getSession();
      getSessionService().deletePlatformSession(currentSession.getId());
      currentSession.invalidate();

      String requestedUrl = httpRequest.getRequestURL().toString();

      try {
        String logoutUrl = buildLogoutUrl(requestedUrl);
        
        JSFUtils.sendRedirect(httpResponse, logoutUrl);
      } catch (URISyntaxException excp) {

        LOGGER.logError(
            Log4jLogger.SYSTEM_LOG,
            excp,
            LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);

        request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_GENERATE_AUTHNREQUEST);

        forward(errorPage, request, response);
      }
      return;
    }

    chain.doFilter(request, response);
  }

 
  protected String buildLogoutUrl(String requestedUrl) throws URISyntaxException {
    String logoutUrl;
    URI uri = new URI(requestedUrl);
    logoutUrl = buildLogoutUrlFromRequestUri(uri);
    return logoutUrl;
  }

  protected String buildLogoutUrlFromRequestUri(URI uri) {
    String logoutUrl;
    String redirectionUrl = buildRedirectionUrl(uri);

    String hostname = uri.getHost();
    logoutUrl =
        "https://" + hostname + ":9091/oscm-identity/logout?state=" + redirectionUrl;
    return logoutUrl;
  }

  private boolean isLogoutRequested(HttpServletRequest request) {

    Optional<String> logoutRequested = Optional.ofNullable(request.getParameter("logout"));
    return logoutRequested.isPresent();
  }

  private String buildRedirectionUrl(URI requestedUri) {

    StringBuffer mainUrl =
        new StringBuffer()
            .append(requestedUri.getScheme())
            .append("://")
            .append(requestedUri.getAuthority());

    if (requestedUri.getPath().contains(Marketplace.MARKETPLACE_ROOT)) {
      mainUrl.append("/oscm-portal");
      mainUrl.append(Marketplace.MARKETPLACE_ROOT + "/index.jsf");
    } else {
      mainUrl.append("/oscm-portal");
    }

    return mainUrl.toString();
  }

  @Override
  public void destroy() {}
}
