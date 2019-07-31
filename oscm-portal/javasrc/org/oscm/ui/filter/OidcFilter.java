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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

public class OidcFilter extends BaseBesFilter implements Filter {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(OidcFilter.class);
  protected String excludeUrlPattern;
 

  @Inject
  private TenantResolver tenantResolver;

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

    // if request contains id_token, validate it and refresh session with it
    Optional<String> idTokenParam = Optional.ofNullable(httpRequest.getParameter("id_token"));
    //FIXME: Determine from where should the tenant be retrieved
    Optional<String> tenantIdParam = Optional.ofNullable(httpRequest.getParameter("tenantId"));

    if(idTokenParam.isPresent()) {
            try {
                    makeTokenValidationRequest(httpRequest, idTokenParam.get(), tenantIdParam);
                    httpRequest.getSession().setAttribute(Constants.SESS_ATTR_ID_TOKEN, idTokenParam.get());
            } catch (ValidationException | URISyntaxException e) {
                    LOGGER.logError(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
                    request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_TOKEN_VALIDATION_FAILED);

                    forward(errorPage,request, response);
            }
    }

    if (!(httpRequest.getServletPath().matches(excludeUrlPattern))
        || isLoginOnMarketplaceRequested(httpRequest)) {

      // if session does not contain id token,
      // redirect to oscm-identity with current state (current url)
      String existingIdToken = (String) httpRequest.getSession()
          .getAttribute(Constants.SESS_ATTR_ID_TOKEN);

      if (StringUtils.isBlank(existingIdToken)) {
        try {
          String loginUrl = new Login(rdo, httpRequest, tenantResolver).buildUrl();

          JSFUtils.sendRedirect(httpResponse, loginUrl);
        } catch (URISyntaxException | MarketplaceRemovedException excp) {

          LOGGER.logError(Log4jLogger.SYSTEM_LOG, excp,
              LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
          request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_GENERATE_AUTHNREQUEST);

          forward(errorPage,request, response);

        }
        return;

      } else {
        try {
          makeTokenValidationRequest(httpRequest, existingIdToken, tenantIdParam);
        } catch (ValidationException | URISyntaxException e) {
          LOGGER.logError(
                  Log4jLogger.SYSTEM_LOG,
                  e,
                  LogMessageIdentifier.ERROR_TOKEN_VALIDATION_FAILED);
          request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_TOKEN_VALIDATION_FAILED);

          forward(errorPage,request, response);
          return;
        }
      }
    }

    chain.doFilter(request, response);
  }

  private void makeTokenValidationRequest(HttpServletRequest httpRequest, String idToken, Optional<String> tenantIdParam)
          throws ValidationException, IOException, URISyntaxException {
    String requestedUrl = httpRequest.getRequestURL().toString();

    HttpResponse validationResponse = null;
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
  }

    private boolean isLoginOnMarketplaceRequested(HttpServletRequest request) {

    boolean isMarketplacePage = request.getServletPath().contains(Marketplace.MARKETPLACE_ROOT);
    Optional<String> loginRequested = Optional.ofNullable(request.getParameter("login"));

    return isMarketplacePage && loginRequested.isPresent();
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
      bf.append(String.format("http://%s:9090/oscm-identity/login?", hostname));

      String tenantId = res.getTenantID(rdo, request);

      if (tenantId != null) {
        bf.append("tenantID=");
        bf.append(tenantId);
        bf.append("&");
      }
      bf.append("state=");
      bf.append(getRequestedURL());
      return bf.toString();
    }

    String getRequestedURL() {
      return request.getRequestURL().toString();
    }

  }

  @Override
  public void destroy() {
  }
}
