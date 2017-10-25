/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                           
 *                                                                              
 *  Creation Date: Sep 8, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.resolver;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * @author tokoda
 * 
 */
public class IPResolver {

    public static String resolveIpAddress(HttpServletRequest request) {
        Enumeration<?> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                if (headerName.equalsIgnoreCase("x-forwarded-for")) {
                    String ipAddress = request.getHeader(headerName);
                    if (ipAddress != null && ipAddress.trim().length() > 0) {
                        return ipAddress;
                    }
                }
            }
        }
        return request.getRemoteAddr();
    }

    public static String resolveIpAddress(WebServiceContext wsContext) {
        HttpServletRequest request = (HttpServletRequest) wsContext
                .getMessageContext().get(MessageContext.SERVLET_REQUEST);
        return resolveIpAddress(request);
    }
}
