/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: Jul 9, 2019
 *
 * <p>*****************************************************************************
 */
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

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
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

      String path = getRedirectPath(httpRequest);
      HttpSession currentSession = httpRequest.getSession();

      getSessionService().deletePlatformSession(currentSession.getId());
      currentSession.invalidate();

      try {
        String logoutUrl = buildLogoutUrl(httpRequest, path);

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

  protected String buildLogoutUrl(HttpServletRequest request, String path)
      throws URISyntaxException {

    String requestedUrl = request.getRequestURL().toString();

    String isu =
        getConfigurationService(request)
            .getVOConfigurationSetting(
                ConfigurationKey.OSCM_IDENTITY_SERVICE_URL, Configuration.GLOBAL_CONTEXT)
            .getValue();

    URI uri = new URI(requestedUrl);
    String redirectionUrl = buildRedirectionUrl(uri, path);
    String logoutUrl = isu + "/logout?state=" + redirectionUrl;
    return logoutUrl;
  }

  private boolean isLogoutRequested(HttpServletRequest request) {

    Optional<String> logoutRequested = Optional.ofNullable(request.getParameter("logout"));
    return logoutRequested.isPresent();
  }

  private String buildRedirectionUrl(URI requestedUri, String path) {
    StringBuffer mainUrl =
        new StringBuffer()
            .append(requestedUri.getScheme())
            .append("://")
            .append(requestedUri.getAuthority());

    if (requestedUri.getPath().contains(Marketplace.MARKETPLACE_ROOT)) {
      mainUrl.append(path);
      mainUrl.append(Marketplace.MARKETPLACE_ROOT + "/index.jsf");
    } else {
      mainUrl.append(path);
    }
    return mainUrl.toString();
  }

  protected String getRedirectPath(HttpServletRequest httpRequest) {
    String baseUrl = getBaseUrl(httpRequest);
    return getPath(baseUrl);
  }

  private String getPath(String baseUrl) {
    String path = "";
    try {
      URI uri = new URI(baseUrl);
      path = uri.getPath();
    } catch (URISyntaxException e) {
      LOGGER.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_INVALID_BASE_URL);
    }
    return path;
  }

  private String getBaseUrl(HttpServletRequest httpRequest) {
    ConfigurationService cs = getConfigurationService(httpRequest);
    String baseUrl =
        cs.getVOConfigurationSetting(ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT)
            .getValue();
    if (baseUrl == null || baseUrl.length() == 0) {
      baseUrl =
          cs.getVOConfigurationSetting(
                  ConfigurationKey.BASE_URL_HTTPS, Configuration.GLOBAL_CONTEXT)
              .getValue();
    }
    return baseUrl;
  }

  @Override
  public void destroy() {}
}
