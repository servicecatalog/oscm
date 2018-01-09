/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 07.06.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.mocksts;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

/**
 * 
 * @author gao
 */
@HandlerChain(file = "handlers.xml")
@WebServiceProvider(serviceName = "MockSTS", portName = "IMockSTS_Port", targetNamespace = "http://sts.mocksts.oscm.org/", wsdlLocation = "WEB-INF/wsdl/MockSTS/MockSTS.wsdl")
@ServiceMode(value = Service.Mode.PAYLOAD)
public class MockSTS //extends com.sun.xml.ws.security.trust.sts.BaseSTSImpl
        implements Provider<Source> {
    @Resource
    WebServiceContext context;

    @Override
    public Source invoke(Source rstElement) {
//        return super.invoke(rstElement);
        return null;
    }

//    @Override
    protected MessageContext getMessageContext() {
        MessageContext msgCtx = context.getMessageContext();
        return msgCtx;
    }

}
