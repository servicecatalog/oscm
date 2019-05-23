/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 23 May 2019                                                      
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

import org.oscm.ui.common.IgnoreCharacterEncodingHttpRequestWrapper;

/**
 * @author farmaki
 * 
 * Filter which sets the character encoding of marketplace pages to UTF-8, in case it is not set.
 *
 */

public class CharacterEncodingFilter implements Filter {
    
    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service.
     * 
     * @param filterConfig
     *            the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {       
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     */
    @Override
    public void destroy() {    
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        final HttpServletRequest httpRequest = new IgnoreCharacterEncodingHttpRequestWrapper(
                (HttpServletRequest) request);
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        chain.doFilter(httpRequest, httpResponse);
    }

}
