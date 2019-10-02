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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class OidcFilter extends BaseBesFilter implements Filter {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(OidcFilter.class);
  protected String excludeUrlPattern;

  @Inject private TenantResolver tenantResolver;

  public TenantResolver getTenantResolver() {
    return tenantResolver;
  }

  public void setTenantResolver(TenantResolver tr) {
    this.tenantResolver = tr;
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);
    this.excludeUrlPattern = config.getInitParameter("exclude-url-pattern");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    AuthorizationRequestData rdo = initializeRequestDataObject(httpRequest);

    boolean isIdProvider = authSettings.isServiceProvider();
    boolean isUrlExcluded = httpRequest.getServletPath().matches(excludeUrlPattern);

    if (isIdProvider && (!isUrlExcluded || isLoginOnMarketplaceRequested(httpRequest))) {

      storeTokens(httpRequest);
      Optional<String> idTokenParam = Optional.ofNullable(httpRequest.getParameter("id_token"));
      Optional<String> accessTokenParam =
          Optional.ofNullable(httpRequest.getParameter("access_token"));

      if (idTokenParam.isPresent() || accessTokenParam.isPresent()) {
        try {
          String tenantid = tenantResolver.getTenantID(rdo, httpRequest);
          makeTokenValidationRequest(
              httpRequest, idTokenParam.get(), accessTokenParam.get(), tenantid);
          httpRequest.getSession().setAttribute(Constants.SESS_ATTR_ID_TOKEN, idTokenParam.get());
        } catch (ValidationException | URISyntaxException | MarketplaceRemovedException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          request.setAttribute(
              Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_TOKEN_VALIDATION_FAILED);
          forward(errorPage, request, response);
          return;
        }
      }

      String existingIdToken =
          (String) httpRequest.getSession().getAttribute(Constants.SESS_ATTR_ID_TOKEN);
      String existingAccessToken =
          (String) httpRequest.getSession().getAttribute(Constants.SESS_ATTR_ACCESS_TOKEN);

      if (StringUtils.isBlank(existingIdToken)) {
        try {
          String loginUrl = new Login(rdo, httpRequest, tenantResolver).buildUrl();
          JSFUtils.sendRedirect(httpResponse, loginUrl);
        } catch (URISyntaxException | MarketplaceRemovedException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
          request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_GENERATE_AUTHNREQUEST);
          forward(errorPage, request, response);
        }
        return;

      } else {
        try {
          String tenantId = tenantResolver.getTenantID(rdo, httpRequest);
          makeTokenValidationRequest(httpRequest, existingIdToken, existingAccessToken, tenantId);
        } catch (ValidationException | URISyntaxException | MarketplaceRemovedException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          request.setAttribute(
              Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_TOKEN_VALIDATION_FAILED);
          forward(errorPage, request, response);
          return;
        }
      }
    }

    chain.doFilter(request, response);
  }

  protected void makeTokenValidationRequest(
      HttpServletRequest httpRequest, String idToken, String accessToken, String tenantId)
      throws ValidationException, IOException, URISyntaxException {
    String requestedUrl = httpRequest.getRequestURL().toString();

    String hostname = new URI(requestedUrl).getHost();
    String resourceUrl =
        "http://oscm-identity:9090/oscm-identity/tenants/" + tenantId + "/token/verify";

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost post = new HttpPost(resourceUrl);
    post.setHeader("Content-type", "application/json");

    if (idToken != null) validateIdToken(idToken, post, client);
    if (accessToken != null) validateAccessToken(accessToken, post, client);
  }

  private void validateIdToken(String idToken, HttpPost post, CloseableHttpClient client)
      throws IOException, ValidationException {
    HttpResponse validationResponse = null;
    StringBuilder jsonStringBuilder = new StringBuilder();
    jsonStringBuilder.append("{\n");
    jsonStringBuilder.append("\t\"token\": \"");
    jsonStringBuilder.append(idToken);
    jsonStringBuilder.append("\",\n");
    jsonStringBuilder.append("\t\"tokenType\": \"");
    jsonStringBuilder.append("ID_TOKEN");
    jsonStringBuilder.append("\"\n");
    jsonStringBuilder.append("}");

    post.setEntity(new StringEntity(jsonStringBuilder.toString()));

    validationResponse = client.execute(post);
    if (validationResponse.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
      throw new ValidationException("ID Token validation failed!");
    }
  }

  private void validateAccessToken(String accessToken, HttpPost post, CloseableHttpClient client)
          throws IOException, ValidationException {
    HttpResponse validationResponse = null;
    StringBuilder jsonStringBuilder = new StringBuilder();
    jsonStringBuilder.append("{\n");
    jsonStringBuilder.append("\t\"token\": \"");
    jsonStringBuilder.append(accessToken);
    jsonStringBuilder.append("\",\n");
    jsonStringBuilder.append("\t\"tokenType\": \"");
    jsonStringBuilder.append("ACCESS_TOKEN");
    jsonStringBuilder.append("\"\n");
    jsonStringBuilder.append("}");

    post.setEntity(new StringEntity(jsonStringBuilder.toString()));

    validationResponse = client.execute(post);
    if (validationResponse.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
      throw new ValidationException("Access Token validation failed!");
    }
  }

  private boolean isLoginOnMarketplaceRequested(HttpServletRequest request) {

    boolean isMarketplacePage = request.getServletPath().contains(Marketplace.MARKETPLACE_ROOT);
    Optional<String> loginRequested = Optional.ofNullable(request.getParameter("login"));

    return isMarketplacePage && loginRequested.isPresent();
  }

  private void storeTokens(HttpServletRequest httpRequest) {

    Optional<String> accessTokenParam =
        Optional.ofNullable(httpRequest.getParameter("access_token"));
    Optional<String> refreshTokenParam =
        Optional.ofNullable(httpRequest.getParameter("refresh_token"));

    if (accessTokenParam.isPresent()) {
      httpRequest
          .getSession()
          .setAttribute(Constants.SESS_ATTR_ACCESS_TOKEN, accessTokenParam.get());
    }

    if (refreshTokenParam.isPresent()) {
      httpRequest
          .getSession()
          .setAttribute(Constants.SESS_ATTR_REFRESH_TOKEN, refreshTokenParam.get());
    }
  }

  class Login {
    private HttpServletRequest request;
    TenantResolver res;

    AuthorizationRequestData rdo;

    Login(AuthorizationRequestData rdo, HttpServletRequest request, TenantResolver res) {
      this.request = request;
      this.res = res;
      this.rdo = rdo;
    }

    String buildUrl() throws URISyntaxException, MarketplaceRemovedException {
      String hostname = new URI(getRequestedURL()).getHost();
      StringBuffer bf = new StringBuffer();

      // TODO adapt for HTTPS protocol and port
      bf.append(String.format("https://%s:9091/oscm-identity/login?", hostname));
      bf.append("state=");
      bf.append(getRequestedURL());

      String tenantId = res.getTenantID(rdo, request);

      if (tenantId != null) {
        bf.append("&");
        bf.append("tenantId=");
        bf.append(tenantId);
      }

      return bf.toString();
    }

    String getRequestedURL() {
      return request.getRequestURL().toString();
    }
  }

  @Override
  public void destroy() {}
}
