/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-03-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.exceptions.ConfigurationException;



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
        serviceUrl=serviceUrl.replace("{SERVICE}", serviceClass.getSimpleName());
        Service service = Service.create(new URL(wsdlUrl), serviceQName);

        boolean isSsoMode = wsdlUrl != null
                && wsdlUrl.toLowerCase().endsWith("/sts?wsdl");
        String portSuffix = isSsoMode ? "PortSTS" : "PortBASIC";

        T client = service.getPort(
                new QName(targetNamespace, serviceClass.getSimpleName()
                        + portSuffix), serviceClass);

        String usernameConstant = isSsoMode ? "username"
                : BindingProvider.USERNAME_PROPERTY;
        String passwordConstant = isSsoMode ? "password"
                : BindingProvider.PASSWORD_PROPERTY;

        Map<String, Object> clientRequestContext = ((BindingProvider) client)
                .getRequestContext();
        clientRequestContext
                .put(usernameConstant, authentication.getUserName());
        clientRequestContext
                .put(passwordConstant, authentication.getPassword());
        clientRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serviceUrl );
        return client;
    }
}
