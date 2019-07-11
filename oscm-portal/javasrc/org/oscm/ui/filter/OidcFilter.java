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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

public class OidcFilter implements Filter {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(OidcFilter.class);
  private FilterConfig filterConfig;
  private String excludeUrlPattern;
  private String errorPage = "/public/error.jsf";

  @Override
  public void init(FilterConfig config) throws ServletException {
    this.filterConfig = config;
    this.excludeUrlPattern = config.getInitParameter("exclude-url-pattern");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    // if request contains id_token, validate it and refresh session with it
    Optional<String> idTokenParam = Optional.ofNullable(httpRequest.getParameter("id_token"));

    idTokenParam.ifPresent(
        idToken -> {
          // TODO: validate id token
          httpRequest.getSession().setAttribute(Constants.SESS_ATTR_ID_TOKEN, idToken);
        });
    
    if (!(httpRequest.getServletPath().matches(excludeUrlPattern))
        || isLoginOnMarketplaceRequested(httpRequest)) {

      // if session does not contain id token,
      // redirect to oscm-identity with current state (current url)
      String existingIdToken =
          (String) httpRequest.getSession().getAttribute(Constants.SESS_ATTR_ID_TOKEN);

      if (StringUtils.isBlank(existingIdToken)) {

        String requestedUrl = httpRequest.getRequestURL().toString();

        try {
          String hostname = new URI(requestedUrl).getHost();
          String loginUrl =
              "http://" + hostname + ":9090/oscm-identity/login?state=" + requestedUrl;
          JSFUtils.sendRedirect(httpResponse, loginUrl);
        } catch (URISyntaxException excp) {

          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG,
              excp,
              LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
          request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_GENERATE_AUTHNREQUEST);

          filterConfig
              .getServletContext()
              .getRequestDispatcher(errorPage)
              .forward(request, response);
        }
        return;

      } else {
        // TODO: validate id token
      }
    }

    chain.doFilter(request, response);
  }

  private boolean isLoginOnMarketplaceRequested(HttpServletRequest request) {

    boolean isMarketplacePage = request.getServletPath().contains(Marketplace.MARKETPLACE_ROOT);
    Optional<String> loginRequested = Optional.ofNullable(request.getParameter("login"));

    return isMarketplacePage && loginRequested.isPresent();
  }

  @Override
  public void destroy() {}
}
