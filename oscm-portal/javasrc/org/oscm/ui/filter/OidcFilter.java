/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                              
 *  Creation Date: Jul 9, 2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    //FIXME: Determine from where should the tenant be retrieved
    Optional<String> tenantIdParam = Optional.ofNullable(httpRequest.getParameter("tenantId"));

    idTokenParam.ifPresent(
        idToken -> {
          try {
            makeTokenValidationRequest(httpRequest, idToken, tenantIdParam);
          } catch (ServletException | IOException e) {
            LOGGER.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
          }
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
        makeTokenValidationRequest(httpRequest, existingIdToken, tenantIdParam);
      }
    }

    chain.doFilter(request, response);
  }

  private void makeTokenValidationRequest(HttpServletRequest httpRequest, String idToken, Optional<String> tenantIdParam)
          throws ServletException, IOException {
    String requestedUrl = httpRequest.getRequestURL().toString();

    HttpResponse validationResponse = null;
    try {
      String hostname = new URI(requestedUrl).getHost();
      String resourceUrl = "http://" + hostname + ":9090/oscm-identity/verify_token";

      CloseableHttpClient client = HttpClients.createDefault();
      HttpPost post = new HttpPost(resourceUrl);

      List<NameValuePair> params = new ArrayList<NameValuePair>(3);
      params.add(new BasicNameValuePair("token", idToken));
      post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

      validationResponse = client.execute(post);
      if (validationResponse.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
        throw new ValidationException("Token validation failed!");
      }

    } catch (URISyntaxException | IOException | ValidationException e) {
        LOGGER.logError(
          Log4jLogger.SYSTEM_LOG,
          e,
          LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);

      filterConfig
              .getServletContext()
              .getRequestDispatcher(errorPage)
              .forward(httpRequest, (ServletResponse) validationResponse);
    }
  }

    private boolean isLoginOnMarketplaceRequested(HttpServletRequest request) {

    boolean isMarketplacePage = request.getServletPath().contains(Marketplace.MARKETPLACE_ROOT);
    Optional<String> loginRequested = Optional.ofNullable(request.getParameter("login"));

    return isMarketplacePage && loginRequested.isPresent();
    }

  @Override
  public void destroy() {}
}
