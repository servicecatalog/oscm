/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-4-1                                                      
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

/**
 * Disable session id from url
 */
public class DisableUrlSessionFilter implements Filter {

    private String excludeUrlPattern;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest)) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (hasToBeFiltered(request) && request.isRequestedSessionIdFromURL()) {
            String url = request
                    .getRequestURL()
                    .append(request.getQueryString() != null ? "?"
                            + request.getQueryString() : "").toString();
            response.setHeader("Location", url);
            response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return;
        }
        chain.doFilter(req, res);
    }

    boolean hasToBeFiltered(HttpServletRequest request) {
        return !request.getServletPath().matches(excludeUrlPattern);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        excludeUrlPattern = config.getInitParameter("exclude-url-pattern");
    }

    @Override
    public void destroy() {
    }

}
