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
import org.oscm.identity.IdentityConfiguration;
import org.oscm.identity.WebIdentityClient;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.TokenType;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class OidcFilter extends BaseBesFilter implements Filter {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(OidcFilter.class);
  protected String excludeUrlPattern;
  protected String publicMplUrlPattern;
  private WebIdentityClient identityClient;

  @Inject private TenantResolver tenantResolver;

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);
    this.excludeUrlPattern = config.getInitParameter("exclude-url-pattern");
    this.publicMplUrlPattern = config.getInitParameter("public-mpl-url-pattern");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    AuthorizationRequestData rdo = initializeRequestDataObject(httpRequest);

    try {
      identityClient = setUpIdentityClient(rdo, httpRequest);
    } catch (MarketplaceRemovedException e) {
      LOGGER.logError(
          Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
      redirectToErrorPage(BaseBean.ERROR_TOKEN_VALIDATION_FAILED, errorPage, request, response);
      return;
    }

    boolean isIdProvider = authSettings.isServiceProvider();
    boolean isUrlExcluded = httpRequest.getServletPath().matches(excludeUrlPattern);
    boolean isUrlPublicMpl = httpRequest.getServletPath().matches(publicMplUrlPattern);
    
    if (isIdProvider && (!isUrlExcluded || isLoginOnMarketplaceRequested(httpRequest))) {

      Optional<String> requestedIdToken = Optional.ofNullable(httpRequest.getParameter("id_token"));
      Optional<String> requestedAccessToken =
          Optional.ofNullable(httpRequest.getParameter("access_token"));

      // Validating tokens that came from the request
      if (requestedIdToken.isPresent() || requestedAccessToken.isPresent()) {
        try {
          identityClient.validateToken(requestedIdToken.get(), TokenType.ID_TOKEN);
          identityClient.validateToken(requestedAccessToken.get(), TokenType.ACCESS_TOKEN);
          saveTokensToSession(httpRequest);
        } catch (IdentityClientException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          redirectToErrorPage(BaseBean.ERROR_TOKEN_VALIDATION_FAILED, errorPage, request, response);
          return;
        }
      }

      // Validating ID token stored in the session
      String sessionIdToken =
          (String) httpRequest.getSession().getAttribute(Constants.SESS_ATTR_ID_TOKEN);

      if (StringUtils.isBlank(sessionIdToken)) {
        try {
          if (!isUrlPublicMpl) {
            redirectToLoginPage(rdo, httpRequest, httpResponse, tenantResolver);
          }
        } catch (URISyntaxException | MarketplaceRemovedException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
          redirectToErrorPage(BaseBean.ERROR_GENERATE_AUTHNREQUEST, errorPage, request, response);
        }
        return;
      } else {
        try {
          identityClient.validateToken(sessionIdToken, TokenType.ID_TOKEN);
        } catch (IdentityClientException e) {
          LOGGER.logError(
              Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          try {
            redirectToLoginPage(rdo, httpRequest, httpResponse, tenantResolver);
          } catch (MarketplaceRemovedException | URISyntaxException ex) {
            LOGGER.logError(
                Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          }
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }

  protected WebIdentityClient setUpIdentityClient(
      AuthorizationRequestData rdo, HttpServletRequest httpRequest)
      throws MarketplaceRemovedException {
    String tenantID = tenantResolver.getTenantID(rdo, httpRequest);
    IdentityConfiguration configuration =
        IdentityConfiguration.of()
            .tenantId(tenantID)
            .sessionContext(httpRequest.getSession())
            .build();
    return new WebIdentityClient(configuration);
  }

  private void redirectToErrorPage(
      String errorMessage, String errorPageURI, ServletRequest request, ServletResponse response)
      throws ServletException, IOException {
    request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, errorMessage);
    request.setAttribute("hideRedirectLinkStyle", "display: none;");
    forward(errorPageURI, request, response);
  }

  private void redirectToLoginPage(
      AuthorizationRequestData rdo,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse,
      TenantResolver tenantResolver)
      throws IOException, MarketplaceRemovedException, URISyntaxException {
    String loginUrl = new Login(rdo, httpRequest, tenantResolver).buildUrl();
    JSFUtils.sendRedirect(httpResponse, loginUrl);
  }

  private boolean isLoginOnMarketplaceRequested(HttpServletRequest request) {

    boolean isMarketplacePage = request.getServletPath().contains(Marketplace.MARKETPLACE_ROOT);
    Optional<String> loginRequested = Optional.ofNullable(request.getParameter("login"));

    return isMarketplacePage && loginRequested.isPresent();
  }

  private void saveTokensToSession(HttpServletRequest httpRequest) {
    Optional<String> idTokenParam = Optional.ofNullable(httpRequest.getParameter("id_token"));
    Optional<String> accessTokenParam =
        Optional.ofNullable(httpRequest.getParameter("access_token"));
    Optional<String> refreshTokenParam =
        Optional.ofNullable(httpRequest.getParameter("refresh_token"));

    idTokenParam.ifPresent(
        s -> httpRequest.getSession().setAttribute(Constants.SESS_ATTR_ID_TOKEN, s));
    accessTokenParam.ifPresent(
        s -> httpRequest.getSession().setAttribute(Constants.SESS_ATTR_ACCESS_TOKEN, s));

    refreshTokenParam.ifPresent(
        s -> httpRequest.getSession().setAttribute(Constants.SESS_ATTR_REFRESH_TOKEN, s));
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

  public TenantResolver getTenantResolver() {
    return tenantResolver;
  }

  public void setTenantResolver(TenantResolver tr) {
    this.tenantResolver = tr;
  }
}
