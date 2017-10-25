/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.oscm.net.BasicAuthLoader;

/**
 * A utility class to obtain a reference to a web service client for a certain
 * WSDL file. The class considers authentication aspects as well.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class WSPortConnector {

    private final WSPortDescription details;
    private final String userName;
    private final String password;

    /**
     * Defines the name of the JAX-WS property to set the timeout values for
     * requests.
     */
    private final String REQUEST_TIMEOUT = "com.sun.xml.ws.request.timeout";

    /**
     * Defines the name of the JAX-WS property to set the connection timeout.
     */
    private final String CONNECT_TIMEOUT = "com.sun.xml.ws.connect.timeout";

    /**
     * Creates an instance of port connector.
     * 
     * @param remoteWsdlUrl
     *            The URL at which the WSDL can be accessed.
     * 
     * @param userName
     *            The userName to be used for authentication. <code>null</code>
     *            if no authentication is required.
     * @param password
     *            The password for the user. <code>null</code> if not required.
     * @throws IOException
     * @throws WSDLException
     */
    public WSPortConnector(String remoteWsdlUrl, String userName,
            String password) throws IOException, WSDLException {
        this(remoteWsdlUrl, userName, password, null);
    }

    /**
     * Creates an instance of port connector.
     * 
     * @param remoteWsdlUrl
     *            The URL at which the WSDL can be accessed.
     * @param userName
     *            The userName to be used for authentication. <code>null</code>
     *            if no authentication is required.
     * @param password
     *            The password for the user. <code>null</code> if not required.
     * @param host
     *            optional - if specified, this host will be used instead the
     *            one from the wsdl
     * 
     * @throws IOException
     * @throws WSDLException
     */
    public WSPortConnector(String remoteWsdlUrl, String userName,
            String password, String host) throws IOException, WSDLException {
        this.userName = userName;
        this.password = password;
        URL url = new URL(remoteWsdlUrl);
        if (requiresUserAuthentication(userName, password)) {
            url = BasicAuthLoader.load(url, userName, password);
        }

        WSDLLocator locator = new BasicAuthWSDLLocator(remoteWsdlUrl, userName,
                password);

        try {
            details = getServiceDetails(locator, host);
        } finally {
            locator.close();
        }
    }

    /**
     * Determines the reference to a web service provided by a technical
     * service.
     * 
     * @param <T>
     *            The type of service obtained.
     * @param localWsdlUrl
     *            The URL to a local service-related WSDL. The WSDL should be
     *            provided as file in a bundled .jar file.
     * @param serviceClass
     *            The service class implemented by the WSDL.
     * @return The web service reference.
     * @throws ParserConfigurationException
     * @throws WebServiceException
     *             Has to be caught by a caller, although it's a runtime
     *             exception
     */
    public <T> T getPort(URL localWsdlUrl, Class<T> serviceClass)
            throws ParserConfigurationException, WebServiceException {

        Service service = getService(localWsdlUrl, serviceClass);

        //EndpointReference epr = determineEndpointReference();
        T port = service.getPort(serviceClass);
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> clientRequestContext = bindingProvider
                .getRequestContext();
        clientRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, details.getEndpointURL());

        if (requiresUserAuthentication(userName, password)) {
//            BindingProvider bindingProvider = (BindingProvider) port;
//            Map<String, Object> clientRequestContext = bindingProvider
//                    .getRequestContext();
            clientRequestContext.put(BindingProvider.USERNAME_PROPERTY,
                    userName);
            clientRequestContext.put(BindingProvider.PASSWORD_PROPERTY,
                    password);
        }
        return port;
    }

    public <T> T getPort(Service service, Class<T> serviceClass)
            throws ParserConfigurationException {
        // and determine the real endpoint belonging to the provisioning
        // URL, and create the port based on it. Doing so, we omit
        // parsing the remote WSDL twice and also related authentication
        // problems
        EndpointReference epr = determineEndpointReference();
        T port = service.getPort(epr, serviceClass);
        if (requiresUserAuthentication(userName, password)) {
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> clientRequestContext = bindingProvider
                    .getRequestContext();
            clientRequestContext.put(BindingProvider.USERNAME_PROPERTY,
                    userName);
            clientRequestContext.put(BindingProvider.PASSWORD_PROPERTY,
                    password);
        }
        return port;
    }

    public <T> Service getService(URL localWsdlUrl, Class<T> serviceClass)
            throws WebServiceException {
        QName serviceQName = new QName(details.getTargetNamespace(),
                serviceClass.getSimpleName());
        // as the real provisioning WSDL URL might require authentication,
        // we refer to a local version. The direct WSDL could be read with
        // appropriate credentials, but evaluation the imports will fail
        // with a 401. So we use a local version as workaround...
        Service service = Service.create(localWsdlUrl, serviceQName);

        return service;
    }

    /**
     * Determines the reference to a web service provided by a technical
     * service.
     * 
     * @param <T>
     *            The type of service obtained.
     * @param localWsdlUrl
     *            The URL to a local service-related WSDL. The WSDL should be
     *            provided as file in a bundled .jar file.
     * @param serviceClass
     *            The service class implemented by the WSDL.
     * @param wsTimeout
     *            defines the general timeout for service calls. The value will
     *            be used as timeout limit in the connection as well as in the
     *            request phase.
     * @return The web service reference.
     * @throws ParserConfigurationException
     * @throws WebServiceException
     *             Has to be caught by a caller, although it's a runtime
     *             exception
     */
    public <T> T getPort(URL localWsdlUrl, Class<T> serviceClass,
            Integer wsTimeout) throws ParserConfigurationException,
            WebServiceException {

        T port = getPort(localWsdlUrl, serviceClass);
        if (wsTimeout != null) {
            BindingProvider bindingProvider = (BindingProvider) port;
            bindingProvider.getRequestContext().put(REQUEST_TIMEOUT, wsTimeout);
            bindingProvider.getRequestContext().put(CONNECT_TIMEOUT, wsTimeout);
        }
        return port;
    }

    public <T> T getPort(Service service, Class<T> serviceClass,
            Integer wsTimeout) throws ParserConfigurationException,
            WebServiceException {

        T port = getPort(service, serviceClass);
        if (wsTimeout != null) {
            BindingProvider bindingProvider = (BindingProvider) port;
            bindingProvider.getRequestContext().put(REQUEST_TIMEOUT, wsTimeout);
            bindingProvider.getRequestContext().put(CONNECT_TIMEOUT, wsTimeout);
        }
        return port;
    }

    /**
     * Creates an end point reference corresponding to the service defined for
     * the technical product.
     * 
     * @return The endpoint reference.
     * @throws ParserConfigurationException
     */
    private EndpointReference determineEndpointReference()
            throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element eprNode = doc.createElementNS(
                "http://www.w3.org/2005/08/addressing", "EndpointReference");
        Element addressNode = doc.createElement("Address");
        Element metadataNode = doc.createElement("Metadata");
        addressNode.setTextContent(details.getEndpointURL());
        doc.appendChild(eprNode);
        eprNode.appendChild(addressNode);
        eprNode.appendChild(metadataNode);
        EndpointReference epr = EndpointReference.readFrom(new DOMSource(doc));
        return epr;
    }

    /**
     * Parses the real WSDL file provided by the technical service and
     * determines its details like the TNS.
     * 
     * @return The web service details.
     */
    public WSPortDescription getPortDescription() {
        return details;
    }

    /**
     * Determines whether the web service has to use the user credentials or
     * not.
     * 
     * @param userName
     *            The name of the user.
     * @param password
     *            The user's password.
     * @return <code>true</code> in case the user name is set and should be used
     *         for the web service operations, <code>false</code> otherwise.
     */
    private boolean requiresUserAuthentication(String userName, String password) {
        return userName != null && userName.length() > 0 && password != null;
    }

    /**
     * Reads the WSDL and determines the target namespace.
     * 
     * @param locator
     *            The URL of the WSDL.
     * @param host
     *            optional the host to be used when the one from the wsdl must
     *            not be used
     * @return The service's target namespace.
     * @throws WSDLException
     *             Thrown in case the WSDL could not be evaluated.
     */
    private WSPortDescription getServiceDetails(WSDLLocator locator, String host)
            throws WSDLException {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();

        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        Definition serviceDefinition = wsdlReader.readWSDL(locator);
        String tns = serviceDefinition.getTargetNamespace();
        // read the port name
        String endpointURL = null;
        final Map<?, ?> services = serviceDefinition.getServices();
        if (services != null) {
            for (Object serviceValue : services.values()) {
                javax.wsdl.Service service = (javax.wsdl.Service) serviceValue;
                Map<?, ?> ports = service.getPorts();
                if (ports != null) {
                    for (Object portValue : ports.values()) {
                        Port port = (Port) portValue;
                        List<?> extensibilityElements = port
                                .getExtensibilityElements();
                        for (Object ex : extensibilityElements) {
                            ExtensibilityElement ext = (ExtensibilityElement) ex;
                            if (ext instanceof SOAPAddress) {
                                SOAPAddress address = (SOAPAddress) ext;
                                endpointURL = address.getLocationURI();
                                if (host != null) {
                                    int idx = endpointURL.indexOf("//") + 2;
                                    String tmp = endpointURL.substring(0, idx)
                                            + host
                                            + endpointURL.substring(endpointURL
                                                    .indexOf(':', idx));
                                    endpointURL = tmp;
                                }
                            }
                        }
                    }
                }
            }
        }

        WSPortDescription result = new WSPortDescription();
        result.setTargetNamespace(tns);
        result.setEndpointURL(endpointURL);
        Element versionElement = serviceDefinition.getDocumentationElement();
        if (versionElement != null) {
            result.setVersion(versionElement.getTextContent());
        }
        return result;
    }
}
