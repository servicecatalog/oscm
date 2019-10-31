/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.11.2015                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.test.ws;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.oscm.security.SOAPSecurityHandler;

/** @author stavreva */
public class WebServiceProxy {

  private static final String CONTEXT_ROOT = "/oscm-webservices/";

  public static <T> T get(
      String baseUrl,
      String authMode,
      String namespace,
      Class<T> remoteInterface,
      String userName,
      String password)
      throws Exception {

    String wsdlUrl =
        baseUrl + CONTEXT_ROOT + remoteInterface.getSimpleName() + "/BASIC?wsdl";

    URL url = new URL(wsdlUrl);
    QName qName = new QName(namespace, remoteInterface.getSimpleName());
    Service service = Service.create(url, qName);

    T port = service.getPort(remoteInterface);
    BindingProvider bindingProvider = (BindingProvider) port;

    if ("OIDC".equals(authMode)) {

    } else {
      Binding binding = bindingProvider.getBinding();
      List<Handler> handlerChain = binding.getHandlerChain();
      if (handlerChain == null) {
        handlerChain = new ArrayList<>();
      }

      handlerChain.add(new SOAPSecurityHandler(userName, password));
      binding.setHandlerChain(handlerChain);
    }
    return port;
  }
}
