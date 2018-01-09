/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.11.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ws;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oscm.security.SOAPSecurityHandler;


/**
 * @author stavreva
 * 
 */
public class WebServiceProxy {

    private static final String TENANT_ID = "tenantID";
    private static final String CONTEXT_ROOT = "/oscm-webservices/"; 

    public static <T> T get(String baseUrl, String version, String auth,
            String namespace, Class<T> remoteInterface, String userName,
            String password, String tenantId, String orgId) throws Exception {
        return get(baseUrl, version, version, auth, namespace, remoteInterface,
                userName, password, tenantId, orgId);
    }

    public static <T> T get(String baseUrl, final String versionWSDL,
            String versionHeader, String auth, String namespace,
            Class<T> remoteInterface, String userName, String password,
            String tenantId, String orgId) throws Exception {
        // String wsdlUrl = baseUrl + "/oscm/" + versionWSDL + "/"
        // + remoteInterface.getSimpleName() + "/" + auth + "?wsdl";

        String wsdlUrl = baseUrl + CONTEXT_ROOT + remoteInterface.getSimpleName() + "/"
                + auth + "?wsdl";

        if (tenantId != null) {
            wsdlUrl += "&" + TENANT_ID + "=" + tenantId;
        }

        URL url = new URL(wsdlUrl);
        QName qName = new QName(namespace, remoteInterface.getSimpleName());
        Service service = Service.create(url, qName);
//        if ("v1.7".equals(versionWSDL) || "v1.8".equals(versionWSDL)) {
//            service.setHandlerResolver(new HandlerResolver() {
//                @SuppressWarnings("rawtypes")
//                @Override
//                public List<Handler> getHandlerChain(PortInfo portInfo) {
//                    List<Handler> handlerList = new ArrayList<Handler>();
//                    handlerList.add(new VersionHandlerCtmg(versionWSDL));
//                    return handlerList;
//                }
//            });
//        } else {
//            ClientVersionHandler versionHandler = new ClientVersionHandler(
//                    versionHeader);
//            service = versionHandler.addVersionInformationToClient(service);
//        }
        
        
  

        T port = service.getPort(remoteInterface);
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> clientRequestContext = bindingProvider
                .getRequestContext();
        
      
        
        if ("STS".equals(auth)) {
            clientRequestContext.put("username", userName);
            clientRequestContext.put("password", password);
            
            clientRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    baseUrl + CONTEXT_ROOT + remoteInterface.getSimpleName() + "/"
                            + auth);

            Map<String, List<String>> headers = new HashMap<String, List<String>>();

            if (tenantId != null) {
                headers.put(TENANT_ID, Collections.singletonList(tenantId));
            }

            if (orgId != null) {
                headers.put("organizationId", Collections.singletonList(orgId));
            }

            clientRequestContext.put(MessageContext.HTTP_REQUEST_HEADERS,
                    headers);

        } else {
            Binding binding = bindingProvider.getBinding();
            List<Handler> handlerChain = binding.getHandlerChain();
            if (handlerChain == null) {
                handlerChain = new ArrayList<>();
            }

            handlerChain.add(new SOAPSecurityHandler(userName, password));
            handlerChain.add(new SOAPHandler<SOAPMessageContext>() {

                @Override
                public Set<QName> getHeaders() {
                    return null;
                }

                @Override
                public boolean handleMessage(SOAPMessageContext smc) {
                    StringBuffer sbuf = new StringBuffer();
                    sbuf.append("\n------------------------------------\n");
                    sbuf.append("In SOAPHandler :handleMessage()\n");

                    Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

                    if (outboundProperty.booleanValue()) {
                        sbuf.append("\ndirection = outbound ");
                    }
                    else {
                        sbuf.append("\ndirection = inbound ");
                    }

                    SOAPMessage message = smc.getMessage();
                    try {
                        sbuf.append("\n");
                        sbuf.append(message.toString());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        message.writeTo(baos);
                        sbuf.append("\nMessage Desc:"+baos.toString());
                        sbuf.append("\n");
                    }
                    catch (Exception e) {
                        sbuf.append("Exception in SOAP Handler: " + e);
                    }

                    sbuf.append("Exiting SOAPHandler :handleMessage()\n");
                    sbuf.append("------------------------------------\n");
                    System.out.println(sbuf.toString());
                    return true;
                }

                @Override
                public boolean handleFault(SOAPMessageContext soapMessageContext) {
                    return false;
                }

                @Override
                public void close(MessageContext messageContext) {

                }
            });
            binding.setHandlerChain(handlerChain);
        }
        return port;
    }
}
