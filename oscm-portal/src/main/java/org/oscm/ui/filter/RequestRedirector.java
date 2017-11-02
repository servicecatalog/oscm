/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kulle
 * 
 */
public class RequestRedirector {

    private FilterConfig filterConfig;

    public RequestRedirector(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Forwards the request to the given page
     * 
     * @param request
     *            the current HTTP servlet request
     * @param response
     *            the current HTTP servlet response
     * @param target
     *            the target page to which the request is forwarded
     */
    public void forward(HttpServletRequest request,
            HttpServletResponse response, String target)
            throws ServletException, IOException {

        filterConfig.getServletContext().getRequestDispatcher(target)
                .forward(request, response);
    }

}
