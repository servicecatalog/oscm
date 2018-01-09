/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Jun 01, 2016
 *
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.oscm.ui.beans.BaseBean.ERROR_PAGE;
import static org.oscm.ui.common.Constants.PORTAL_HAS_BEEN_REQUESTED;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.EJBServiceAccess;
import org.oscm.ui.common.ServiceAccess;

/**
 * @author Paulina Badziak
 *
 */
public class ClosedMarketplaceFilter extends BaseBesFilter implements Filter {

    RequestRedirector redirector;
    String excludeUrlPattern;
    ServiceAccess serviceAccess;

    @EJB
    private MarketplaceService marketplaceService;

    private boolean isSaml;

    public MarketplaceConfiguration getConfig(String marketplaceId) {
        return marketplaceService
                .getCachedMarketplaceConfiguration(marketplaceId);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        redirector = new RequestRedirector(filterConfig);
        excludeUrlPattern = filterConfig
                .getInitParameter("exclude-url-pattern");
    }

    /**
     * If the request does not match exclude pattern, it is checked in the
     * context of restricted marketplace. If requested marketplace is
     * restricted, current user is checked if he has access to it. If not the
     * request is forwarded to the page informing about insufficient rights. See
     * web.xml for excluded url pattern.
     *
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!httpRequest.getServletPath().matches(excludeUrlPattern)) {
            String mId = httpRequest
                    .getParameter(Constants.REQ_PARAM_MARKETPLACE_ID);
            if (mId == null || "".equals(mId)) {
                mId = (String) httpRequest.getSession()
                        .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
            }

            VOUserDetails voUserDetails = (VOUserDetails) httpRequest
                    .getSession().getAttribute(Constants.SESS_ATTR_USER);

            if (mId == null || mId.equals("")) {
                chain.doFilter(request, response);
                return;
            }

            MarketplaceConfiguration config = getConfig(mId);

            if (config != null && voUserDetails != null
                    && !isSameTenant(config, voUserDetails)) {
                forwardToErrorPage(httpRequest, httpResponse);
                return;
            }

            if (isMkpRestricted(config)) {
                if (voUserDetails != null
                        && voUserDetails.getOrganizationId() != null) {
                    if (!config.getAllowedOrganizations()
                            .contains(voUserDetails.getOrganizationId())) {
                        if (portalHasBeenRequested(httpRequest)) {
                            httpResponse.sendRedirect(
                                    getRedirectToMkpAddress(httpRequest));
                        } else {
                            forwardToErrorPage(httpRequest, httpResponse);
                        }
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isSameTenant(MarketplaceConfiguration config,
            VOUserDetails voUserDetails) throws ServletException, IOException {
        final String tenantIdFromMarketplace = config.getTenantId();

        String userOrgTenant = voUserDetails.getTenantId();

        if (userOrgTenant == null && tenantIdFromMarketplace == null) {
            return true;
        } else if (userOrgTenant != null
                && userOrgTenant.equals(tenantIdFromMarketplace)) {
            return true;
        }
        return false;
    }

    private String getRedirectToMkpAddress(HttpServletRequest httpRequest) {
        String result;
        if (httpRequest.isSecure()) {
            result = getRedirectMpUrlHttps(
                    getConfigurationService(httpRequest));
        } else {
            result = getRedirectMpUrlHttp(getConfigurationService(httpRequest));
        }
        return result;
    }

    private boolean portalHasBeenRequested(HttpServletRequest httpRequest) {
        Object portalRequest = httpRequest.getSession()
                .getAttribute(PORTAL_HAS_BEEN_REQUESTED);
        return portalRequest != null ? ((Boolean) portalRequest).booleanValue()
                : false;
    }

    private boolean isMkpRestricted(MarketplaceConfiguration config) {
        return config != null && config.isRestricted();
    }

    boolean isSAMLAuthentication() {
        if (!isSaml) {
            ConfigurationService cfgService = getServiceAccess()
                    .getService(ConfigurationService.class);
            authSettings = new AuthenticationSettings(null, cfgService);
            isSaml = authSettings.isServiceProvider();
        }
        return isSaml;
    }

    private ServiceAccess getServiceAccess() {
        if (serviceAccess == null) {
            serviceAccess = new EJBServiceAccess();
        }
        return serviceAccess;
    }

    private void forwardToErrorPage(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse)
            throws ServletException, IOException {
        if (isSAMLAuthentication()) {
            RequestDispatcher requestDispatcher = httpRequest
                    .getServletContext().getRequestDispatcher(ERROR_PAGE);
            httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_ACCESS_TO_CLOSED_MARKETPLACE);
            requestDispatcher.forward(httpRequest, httpResponse);
        } else {
            redirector.forward(httpRequest, httpResponse,
                    Marketplace.MARKETPLACE_ROOT
                            + Constants.INSUFFICIENT_AUTHORITIES_URI);
        }
    }

    @Override
    public void destroy() {
    }

}
