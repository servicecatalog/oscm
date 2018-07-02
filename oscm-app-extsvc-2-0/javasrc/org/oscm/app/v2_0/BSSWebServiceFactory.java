/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-03-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.security.SOAPSecurityHandler;

/**
 * Factory for creating instances of OSCM Web services in the context of the
 * <code>APPlatformService</code>.
 */
public class BSSWebServiceFactory {

    /**
     * Creates a OSCM Web service with the given parameters.
     * 
     * @param serviceClass
     *            the class of the Web service to be created
     * @param authentication
     *            a <code>PasswordAuthentication</code> object specifying the
     *            credentials to be used for authentication
     * @return the service class
     * @throws ConfigurationException
     *             if the configuration of the platform is incorrect
     * @throws MalformedURLException
     *             if the base URL of the OSCM configuration is malformed
     */
    public static <T> T getBSSWebService(Class<T> serviceClass,
            PasswordAuthentication authentication)
            throws ConfigurationException, MalformedURLException {

        String targetNamespace = serviceClass.getAnnotation(WebService.class)
                .targetNamespace();
        QName serviceQName = new QName(targetNamespace,
                serviceClass.getSimpleName());

        String wsdlUrl = APPlatformServiceFactory.getInstance()
                .getBSSWebServiceWSDLUrl();
        wsdlUrl = wsdlUrl.replace("{SERVICE}", serviceClass.getSimpleName());
        String serviceUrl = APPlatformServiceFactory.getInstance()
                .getBSSWebServiceUrl();
        serviceUrl = serviceUrl.replace("{SERVICE}",
                serviceClass.getSimpleName());
        Service service = Service.create(new URL(wsdlUrl), serviceQName);

        boolean isSsoMode = wsdlUrl != null
                && wsdlUrl.toLowerCase().endsWith("/sts?wsdl");
        String portSuffix = isSsoMode ? "PortSTS" : "PortBASIC";

        T client = service.getPort(
                new QName(targetNamespace,
                        serviceClass.getSimpleName() + portSuffix),
                serviceClass);

        String usernameConstant = isSsoMode ? "username"
                : BindingProvider.USERNAME_PROPERTY;
        String passwordConstant = isSsoMode ? "password"
                : BindingProvider.PASSWORD_PROPERTY;

        setUserCredentialsInContext(((BindingProvider) client),
                authentication.getUserName(), authentication.getPassword(),
                isSsoMode);

        setEndpointInContext(serviceUrl, client);

        setBinding((BindingProvider) client, authentication.getUserName(),
                authentication.getPassword());
        return client;
    }

    private static <T> void setEndpointInContext(String serviceUrl, T client) {
        Map<String, Object> clientRequestContext = ((BindingProvider) client)
                .getRequestContext();

        clientRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceUrl);
    }

    private static void setBinding(BindingProvider client, String userName,
            String password) {
        final Binding binding = client.getBinding();
        @SuppressWarnings("rawtypes")
        List<Handler> handlerList = binding.getHandlerChain();
        if (handlerList == null)
            handlerList = new ArrayList<>();
        handlerList.add(new SOAPSecurityHandler(userName, password));
        binding.setHandlerChain(handlerList);
    }

    private static void setUserCredentialsInContext(BindingProvider client,
            String user, String password, boolean isSSO) {
        Map<String, Object> clientRequestContext = client.getRequestContext();
        clientRequestContext.put(getUsernameConstant(isSSO), user);
        clientRequestContext.put(getPasswordConstant(isSSO), password);
    }

    private static String getUsernameConstant(boolean isSSO) {
        if (isSSO) {
            return "username";
        } else {
            return BindingProvider.USERNAME_PROPERTY;
        }
    }

    private static String getPasswordConstant(boolean isSso) {
        if (isSso) {
            return "password";
        } else {
            return BindingProvider.PASSWORD_PROPERTY;
        }
    }
}
