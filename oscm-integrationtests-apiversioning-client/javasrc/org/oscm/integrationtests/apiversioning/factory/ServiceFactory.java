/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月23日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.factory;

import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.oscm.apiversioning.handler.ClientVersionHandler;
import org.oscm.apiversioning.handler.PropertyFileReader;

/**
 * Service factory to create services
 */
public class ServiceFactory {

    private String userName = "";
    private String password = "";
    private String remoteWSDLUrl = "";
    private final URL localWSDLUrl;
    private static final String PROPERTY_FILE_NAME = "configuration.properties";
    private static final String BES_HTTPS_URL = "bes.https.url";

    public ServiceFactory(String userName, String password, URL localWSDLUrl) {
        this.userName = userName;
        this.password = password;
        Properties props = PropertyFileReader
                .getPropertiesFromFile(PROPERTY_FILE_NAME);
        this.remoteWSDLUrl = props.getProperty(BES_HTTPS_URL);
        this.localWSDLUrl = localWSDLUrl;
    }

    public <T> T getBESWebService(Class<T> serviceClass)
            throws ParserConfigurationException {
        T client = getServicePort(serviceClass);
        BindingProvider bindingProvider = (BindingProvider) client;
        Map<String, Object> clientRequestContext = bindingProvider
                .getRequestContext();
        clientRequestContext.put(BindingProvider.USERNAME_PROPERTY, userName);
        clientRequestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
        return client;
    }

    private Service addVersionInformation(Service service) {
        ClientVersionHandler versionHandler = new ClientVersionHandler();
        return versionHandler.addVersionInformationToClient(service);
    }

    private <T> T getServicePort(Class<T> serviceClass)
            throws ParserConfigurationException {

        String targetNamespace = serviceClass.getAnnotation(WebService.class)
                .targetNamespace();
        QName serviceQName = new QName(targetNamespace,
                serviceClass.getSimpleName());
        Service service = createWebService(localWSDLUrl, serviceQName);
        service = addVersionInformation(service);
        return service.getPort(
                determineEndpointReference(serviceClass.getSimpleName()),
                serviceClass);
    }

    private Service createWebService(URL wsdlUrl, QName serviceQName) {
        return Service.create(wsdlUrl, serviceQName);
    }

    private EndpointReference determineEndpointReference(String serviceName)
            throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element eprNode = doc.createElementNS(
                "http://www.w3.org/2005/08/addressing", "EndpointReference");
        Element addressNode = doc.createElement("Address");
        Element metadataNode = doc.createElement("Metadata");
        String wsdlURL = remoteWSDLUrl.replace("{service}", serviceName);
        addressNode.setTextContent(wsdlURL);
        doc.appendChild(eprNode);
        eprNode.appendChild(addressNode);
        eprNode.appendChild(metadataNode);
        EndpointReference epr = EndpointReference.readFrom(new DOMSource(doc));
        return epr;
    }

}
