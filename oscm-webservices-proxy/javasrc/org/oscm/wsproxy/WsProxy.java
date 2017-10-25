/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.wsproxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oscm.apiversioning.handler.ClientVersionHandler;

/**
 * This class is responsible for creating a web service proxy for consuming Open
 * Service Catalog Manager web services.
 * 
 */
public class WsProxy {
    private static Log logger = LogFactory.getLog(WsProxy.class);
    private static String NAMESPACE_URI = "http://oscm.org/xsd";

    /**
     * Returns a web service proxy for a BASIC or CLIENTCERT port of the
     * specified web service.
     * 
     * @param info
     *            - the properties for accessing the web service
     * @param user
     *            - user credentials for authentication
     * @param type
     *            - web service described with the corresponding type
     * @return the web service proxy
     */
    public static <T> T getProxyInternal(WsInfo info, UserCredentials user,
            Class<T> type) {
        checkInputParameter(info, user, type);
        checkInternal(info);
        T proxy = createProxy(info, type);

        Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
        ctx.put(BindingProvider.USERNAME_PROPERTY, user.getUser());
        ctx.put(BindingProvider.PASSWORD_PROPERTY, user.getPassword());
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                info.getEndpointAddress());
        return proxy;
    }

    /**
     * Returns a web service proxy for a STS port of the specified web service.
     * 
     * @param info
     *            - the properties for accessing the web service
     * @param user
     *            - user credentials for authentication
     * @param type
     *            - web service described with the corresponding type
     * @return the web service proxy
     */
    public static <T> T getProxySTS(WsInfo info, UserCredentials user,
            Class<T> type) {
        checkInputParameter(info, user, type);
        checkSTSPort(info);
        T proxy = createProxy(info, type);

        Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
        ctx.put("username", user.getUser());
        ctx.put("password", user.getPassword());
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                info.getEndpointAddress());
        return proxy;
    }

    public static <T> T getProxySTSForCredentialsCallback(WsInfo info,
            Class<T> type) {
        checkInputParameter(info, type);
        T proxy = createProxy(info, type);
        return proxy;
    }

    /**
     * Helper method for creating a web service proxy. The service version
     * information is added into the header of the outbound SOAP message.
     * 
     * @param info
     *            - the properties for accessing the web service
     * @param type
     *            - web service described with the corresponding type
     * @return the web service proxy
     * @throws MalformedURLException
     */
    private static <T> T createProxy(WsInfo info, final Class<T> type) {
        Service service = null;
        try {
            service = Service.create(new URL(info.getRemoteBssWsUrl()),
                    new QName(NAMESPACE_URI, type.getSimpleName()));
        } catch (MalformedURLException e) {
            String text = "Error:Malformed URL";
            logger.error(text);
        }
        service = addVersionInformation(service);
        return service.getPort(type);
    }

    /**
     * Helper method to check if the expected BASIC or CLIENTCERT service port
     * is set into the model of the web service proxy.
     * 
     * @param info
     *            - the properties for accessing the web service
     */
    private static void checkInternal(WsInfo info) {
        if (info.getServicePort() == ServicePort.STS) {
            String text = "Service port should be " + ServicePort.BASIC
                    + " or " + ServicePort.CLIENTCERT;
            logger.error(text);
            throw new WebServiceException(text);
        }
    }

    /**
     * Helper method to check if the expected STS service port is set into the
     * model of the web service proxy.
     * 
     * @param info
     *            - the properties for accessing the web service
     */
    private static void checkSTSPort(WsInfo info) {
        if (info.getServicePort() != ServicePort.STS) {
            String text = "Service port should be " + ServicePort.STS;
            logger.error(text);
            throw new WebServiceException(text);
        }
    }

    /**
     * Helper method to assure not null parameters.
     * 
     * @param info
     * @param user
     * @param type
     */
    private static <T> void checkInputParameter(WsInfo info,
            UserCredentials user, Class<T> type) {
        if (info == null || user == null || type == null) {
            String text = "Error: missing input parameter";
            logger.error(text);
            throw new WebServiceException(text);
        }
    }

    /**
     * Helper method to assure not null parameters.
     * 
     * @param info
     * @param type
     */
    private static <T> void checkInputParameter(WsInfo info, Class<T> type) {
        if (info == null || type == null) {
            String text = "Error: missing input parameter";
            logger.error(text);
            throw new WebServiceException(text);
        }
    }

    /**
     * Helper method to add a version information into the header of the
     * outbound SOAP message. The version information is read from a property
     * file.
     * 
     * @param service
     * @return
     */
    private static Service addVersionInformation(Service service) {
        ClientVersionHandler versionHandler = new ClientVersionHandler();
        return versionHandler.addVersionInformationToClient(service);
    }

}
