/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 13.06.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.ui.common.EJBServiceAccess;
import org.oscm.ui.common.ServiceAccess;

/**
 * @author ono
 *
 */
public class DisableUrlFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest)) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isHiddenPage(request)) {
            String url = request.getRequestURL()
                    .append(request.getQueryString() != null
                            ? "?" + request.getQueryString() : "")
                    .toString();
            response.setHeader("Location", url);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(req, res);
    }

    boolean isHiddenPage(HttpServletRequest request) {
        if (BesServletRequestReader.isManagePaymentTypesPage(request)
                || BesServletRequestReader.isAccountPaymentPage(request)) {
            ServiceAccess serviceAccess = new EJBServiceAccess();
            ConfigurationService cfgService = serviceAccess
                    .getService(ConfigurationService.class);
            return !cfgService.isPaymentInfoAvailable();
        } else
            return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
