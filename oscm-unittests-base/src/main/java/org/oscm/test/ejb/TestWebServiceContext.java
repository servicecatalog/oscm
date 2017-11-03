/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                  
 *                                                                              
 *  Creation Date: 28.06.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import java.security.Principal;

import javax.ejb.SessionContext;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

/**
 * @author weiser
 * 
 */
public class TestWebServiceContext implements WebServiceContext {

    private SessionContext msgCtx;

    public TestWebServiceContext(SessionContext ctx) {
        msgCtx = ctx;
    }

    public Principal getUserPrincipal() {
        return msgCtx.getCallerPrincipal();
    }

    public MessageContext getMessageContext() {
        throw new UnsupportedOperationException();
    }

    public EndpointReference getEndpointReference(Element... arg0) {
        throw new UnsupportedOperationException();
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> arg0,
            Element... arg1) {
        throw new UnsupportedOperationException();
    }

    public boolean isUserInRole(String arg0) {
        throw new UnsupportedOperationException();
    }

}
