/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.02.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.oscm.xml.Transformers;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * This general class represents an XML DOM document and provides basic methods
 * for parsing and transforming it.
 * 
 * @author goebel
 * 
 */
class XmlDocument {

    private Element root;
    private Document xmldoc;

    private String namespace;
    private String schemaLocation;
    private String namespacePrefix;
    private String rootNodeName;

    /**
     * Converts the given technical products to a dom document.
     * 
     * @param techProds
     *            the technical products to convert
     * @param localizer
     *            the localizer used to read localized values
     * @param dm
     *            the data service used to read other values
     * @return the dom document
     */
    public XmlDocument(String namespace, String namespacePrefix,
            String schemaLocation, String rootNodeName) {

        set(namespace, namespacePrefix, schemaLocation, rootNodeName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SaaSSystemException(e);
        }

        xmldoc = builder.getDOMImplementation().createDocument(getNameSpace(),
                getRootNodeName(), null);
        root = (Element) xmldoc.getFirstChild();
        root.setPrefix(getPrefix());
        root.setAttribute("xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", getSchemaLocation());
    }

    /**
     * Converts the given document to an XML string. Uses UTF-8 as character
     * encoding.
     * 
     * @return the XML as byte array
     */
    byte[] docToXml() {
        try {
            DOMSource domSource = new DOMSource(xmldoc);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(buffer);
            Transformer transformer = Transformers.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(domSource, result);
            return buffer.toByteArray();
        } catch (TransformerException e) {
            throw new SaaSSystemException(e);
        }
    }

    private void set(String namespace, String namespacePrefix,
            String schemaLocation, String rootNodeName) {
        this.namespace = namespace;
        this.namespacePrefix = namespacePrefix;
        this.schemaLocation = schemaLocation;
        this.rootNodeName = rootNodeName;
    }

    protected Document getDomDocument() {
        return xmldoc;
    }

    protected Element getRootNode() {
        return root;
    }

    private String getNameSpace() {
        return namespace;
    }

    private String getSchemaLocation() {
        return schemaLocation;
    }

    private String getPrefix() {
        return namespacePrefix;
    }

    private String getRootNodeName() {
        return rootNodeName;
    }
}
