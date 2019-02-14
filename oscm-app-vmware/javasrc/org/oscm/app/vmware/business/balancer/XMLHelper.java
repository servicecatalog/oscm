/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/
package org.oscm.app.vmware.business.balancer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;

/**
 * @author goebel
 *
 */
public class XMLHelper {

    protected static String getAttributeValue(Node node, String name) {
        return getAttributeValue(node, name, "");
    }

    public static String getAttributeValue(Node node, String name,
            String defaultValue) {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr != null)
            return attr.getNodeValue();
        return defaultValue;

    }

    public static Document convertToDocument(String string, boolean checkSchema)
            throws SAXException, ParserConfigurationException, IOException {

        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        if (checkSchema) {
            URL schemaURL = XMLHelper.class.getClassLoader()
                    .getResource("META-INF/Loadbalancer_schema.xsd");
            String constant = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory xsdFactory = SchemaFactory.newInstance(constant);
            Schema schema = xsdFactory.newSchema(schemaURL);
            dfactory.setSchema(schema);
        }
        
        dfactory.setValidating(false);
        dfactory.setIgnoringElementContentWhitespace(true);
        dfactory.setNamespaceAware(true);
        DocumentBuilder builder = dfactory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultErrorHandler());
        Document doc = builder.parse(new InputSource(new StringReader(string)));
        return doc;
    }

    public static List<Node> getChildrenByTag(Node parent, String tagName) {
        NodeList childs = parent.getChildNodes();
        List<Node> children = new ArrayList<Node>();
        for (int j = 0; j < childs.getLength(); j++) {
            if (tagName.equals(childs.item(j).getNodeName())) {
                children.add(childs.item(j));
            }
        }
        return children;
    }

}