/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Note: if a namespace is redefined the last url is stored and provided by getNamsepaceUri(prefix);
 * 
 * @author kulle
 */
public class XmlNamespaceResolver implements NamespaceContext {

    private Map<String, String> nsToUrl = new HashMap<String, String>();
    private Map<String, String> urlToNs = new HashMap<String, String>();

    public XmlNamespaceResolver(Document document) {
        extractNamespaces(document);
    }

    private void extractNamespaces(Node node) {
        readNamespaces(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            extractNamespaces(childNodes.item(i));
        }
    }

    private void readNamespaces(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);

                if (XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getPrefix())) {
                    nsToUrl.put(attribute.getLocalName(), attribute.getValue());
                    urlToNs.put(attribute.getValue(), attribute.getLocalName());
                }
            }
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return nsToUrl.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return urlToNs.get(namespaceURI);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }
}
