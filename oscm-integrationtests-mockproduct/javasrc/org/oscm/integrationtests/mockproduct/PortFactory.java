/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.integrationtests.mockproduct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.oscm.security.SOAPSecurityHandler;

/**
 * Factory to create a BSS ServicePortType to call BSS.
 * 
 * @author pock
 */
public class PortFactory {

    public static final String INTERNAL = "INTERNAL";
    public static final String SAML_SP = "SAML_SP";

    private static URL getWsdlLocation(final ConnectionInfo connection,
            final String serviceName) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(connection.getBaseUrl()).append("/oscm-webservices/");
        //s.append(connection.getVersion()).append("/");
        s.append(serviceName).append("/");
        if (connection.getAuthMode().equals(INTERNAL)) {
            if (connection.isClientCert()) {
                s.append("CLIENTCERT?wsdl");
            } else {
                s.append("BASIC?wsdl");
            }
        } else {
            s.append("STS?wsdl");
            String tenantId = connection.getTenantId();
            
            if(isTenantIdValid(tenantId)){
                s.append("&tenantID="+tenantId);
            }
        }
        return new URL(s.toString());
    }
    
    private static boolean isTenantIdValid(String tenantId) {

        if (tenantId == null) {
            return false;
        }

        if (tenantId.length() != 8) {
            return false;
        }

        return true;
    }

    private static URL getEndpointLocation(final ConnectionInfo connection,
            final String serviceName) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(connection.getBaseUrl());
        s.append("/oscm-webservices/");
        s.append(serviceName).append("/");
        if (INTERNAL.equals(connection.getAuthMode())) {
            if (connection.isClientCert()) {
                s.append("CLIENTCERT?wsdl");
            } else {
                s.append("BASIC?wsdl");
            }
        } else {
            s.append("STS?wsdl");
        }

        return new URL(s.toString());
    }

    public static <T> T getPort(final ConnectionInfo info, final Class<T> type)
            throws IOException {

        final String serviceName = type.getSimpleName();

        URL wsdlLocation = getWsdlLocation(info, serviceName);

        if (!info.isClientCert()) {
            wsdlLocation = withCredentials(wsdlLocation, info.getUsername(),
                    info.getPassword());
        }

        final Service service = Service.create(wsdlLocation, new QName(
                "http://oscm.org/xsd", serviceName));

        final T port = service.getPort(type);
        BindingProvider bindingProvider = (BindingProvider) port;
        final Map<String, Object> ctx = bindingProvider.getRequestContext();
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                getEndpointLocation(info, serviceName).toString());
        if (!info.isClientCert()) {
            Binding binding = bindingProvider.getBinding();
            @SuppressWarnings("rawtypes")
            List<Handler> handlerChain = binding.getHandlerChain();
            if (handlerChain == null) {
                handlerChain = new ArrayList<>();
            }

            handlerChain.add(new SOAPSecurityHandler(info.getUsername(), info.getPassword()));
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
            
        } else if (SAML_SP.equals(info.getAuthMode())) {
            ctx.put("username", info.getUsername());
            ctx.put("password", info.getPassword());
        }

        return port;
    }

    /**
     * Creates a new URL object that retrieves the content specified by the
     * given URL with credentials.. Internally the content is pre-fetched with
     * the given credentials. The returned URL has an custom protocol handler
     * that simply returns the pre-fetched content.
     * 
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private static URL withCredentials(final URL url, final String username,
            final String password) throws IOException {

        final byte[] content = getUrlContent(url, username, password);

        final URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(url) {
                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(content);
                    }
                };
            }
        };
        return new URL(url.getProtocol(), url.getHost(), url.getPort(),
                url.getFile(), handler);
    }

    /**
     * Retrieves the content under the given URL with username and passwort
     * authentication.
     * 
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private static byte[] getUrlContent(URL url, String username,
            String password) throws IOException {
        final HttpClient client = new HttpClient();

        // Set credentials:
        client.getParams().setAuthenticationPreemptive(true);
        final Credentials credentials = new UsernamePasswordCredentials(
                username, password);
        client.getState()
                .setCredentials(
                        new AuthScope(url.getHost(), url.getPort(),
                                AuthScope.ANY_REALM), credentials);

        // Retrieve content:
        final GetMethod method = new GetMethod(url.toString());
        final int status = client.executeMethod(method);
        if (status != HttpStatus.SC_OK) {
            throw new IOException("Error " + status + " while retrieving "
                    + url);
        }
        return method.getResponseBody();
    }
}
