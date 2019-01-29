/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/
package org.oscm.app.vmware.business.balancer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

    public static Document convertToDocument(String string)
            throws Exception {

        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setValidating(false);
        dfactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = dfactory.newDocumentBuilder();
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