/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年3月2日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.oscm.servlet.WSDLDeliverServlet;

/**
 * @author yuyin
 * 
 */
public class WSDLFilter implements Filter {

    private final String urlPattern = "/(v.*/)?(.*service)/(basic|sts|clientcert)";
    private final String servletUrl = "/WSDLDeliverServlet";
    private final String tenantIdParam = "tenantID";
    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        String url = httpRequest.getServletPath();
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            String version = matcher.group(1);
            String serviceName = matcher.group(2);
            String portType = matcher.group(3);
            httpRequest.setAttribute(WSDLDeliverServlet.VERSION, version);
            httpRequest.setAttribute(WSDLDeliverServlet.SERVICE_NAME,
                    serviceName);
            httpRequest.setAttribute(WSDLDeliverServlet.PORT_TYPE, portType);
            String fileType = request.getParameterNames().nextElement();
            httpRequest.setAttribute(WSDLDeliverServlet.FILE_TYPE, fileType);
            
            String tenantId = httpRequest.getParameter(tenantIdParam);
            httpRequest.setAttribute(WSDLDeliverServlet.TENANT_ID, tenantId);
            
            httpRequest.getRequestDispatcher(servletUrl).forward(httpRequest,
                    response);
        }
        return;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

}
