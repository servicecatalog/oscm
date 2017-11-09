/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 03.05.2011                                                      
 *                                                                              
 *  Completion Time: 03.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for denying access with http delete, put and trace methods.
 * 
 * @author weiser
 * 
 */
public class HttpMethodFilter implements Filter {

    private Set<String> forbiddenHttpMethods = new HashSet<String>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        forbiddenHttpMethods.add("PUT");
        forbiddenHttpMethods.add("DELETE");
        forbiddenHttpMethods.add("TRACE");
    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)
                || !(response instanceof HttpServletResponse)) {
            response.getWriter().print(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        if (forbiddenHttpMethods.contains(method)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, method
                    + " not allowed.");
            return;
        }

        chain.doFilter(request, response);
    }

}
