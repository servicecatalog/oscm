/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 07.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.oscm.xml.Transformers;

/**
 * Class for basic conversion of Strings to Documents and vice versa.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XMLConverter {

    private static final String ENCODING_UTF8 = "UTF-8";

    public static final String HEADER = String.format(
            "<?xml version=\"1.0\" encoding=\"%s\"?>%n", ENCODING_UTF8);

    /**
     * Converts a given string into its document representation.
     * 
     * @param string
     *            The String to be converted.
     * @param nameSpaceAware
     *            Indicates whether namespaces have to be considered or not.
     * @return The document representation of the string, <code>null</code> in
     *         case the input string was <code>null</code>.
     * @throws ParserConfigurationException
     *             Thrown in case the conversion cannot be initiated.
     * @throws IOException
     *             Thrown in case the reading of the string fails.
     * @throws SAXException
     *             Thrown in case the string cannot be parsed.
     */
    public static Document convertToDocument(String string,
            boolean nameSpaceAware) throws ParserConfigurationException,
            SAXException, IOException {
        if (string == null) {
            return null;
        }
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(nameSpaceAware);
        dfactory.setValidating(false);
        dfactory.setIgnoringElementContentWhitespace(true);
        dfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = dfactory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(string)));
        return doc;
    }

    public static Document convertToDocument(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    /**
     * Converts a given document into its string representation.
     * 
     * @param document
     *            The document to be converted.
     * @param includeXmlDeclaration
     *            should the output contain a XML declaration?
     * @return The string representation of the document.
     * @throws TransformerException
     *             Thrown in case the conversion fails.
     */
    public static String convertToString(final Node document,
            final boolean includeXmlDeclaration) throws TransformerException {

        DOMSource domSource = new DOMSource(document);
        return convertToString(domSource, includeXmlDeclaration);
    }

    public static String convertToString(Source source,
            boolean includeXmlDeclaration) throws TransformerException {

        Transformer transformer = Transformers.newFormatingTransformer();
        if (!includeXmlDeclaration) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
        }

        StringWriter stringWriter = new StringWriter();
        try {
            transformer.transform(source, new StreamResult(stringWriter));
            return stringWriter.toString();
        } finally {
            try {
                stringWriter.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 
     * @param input
     *            String an XML document.
     * @return the document cleaned from any carriage return ('\r') and line
     *         feed ('\n') characters.
     */
    public static String removeEOLCharsFromXML(final String input) {
        String output = input.replaceAll("\"\\n", "\" ");
        output = output.replaceAll("(\\r|\\n)", "");
        return output;
    }

    /**
     * Returns the node in the given document at the specified XPath.
     * 
     * @param node
     *            The document to be checked.
     * @param xpathString
     *            The xpath to look at.
     * @return The node at the given xpath.
     * @throws XPathExpressionException
     */
    public static Node getNodeByXPath(Node node, String xpathString)
            throws XPathExpressionException {

        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new XmlNamespaceResolver(
                getOwningDocument(node)));
        final XPathExpression expr = xpath.compile(xpathString);
        return (Node) expr.evaluate(node, XPathConstants.NODE);
    }

    private static Document getOwningDocument(Node node) {
        if (node instanceof Document) {
            return (Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }

    /**
     * Returns the node list in the given document at the specified XPath.
     * 
     * @param node
     *            The document to be checked.
     * @param xpathString
     *            The xpath to look at.
     * @return The node list at the given XPath.
     * @throws XPathExpressionException
     */
    public static NodeList getNodeListByXPath(Node node, String xpathString)
            throws XPathExpressionException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new XmlNamespaceResolver(
                getOwningDocument(node)));
        final XPathExpression expr = xpath.compile(xpathString);
        return (NodeList) expr.evaluate(node, XPathConstants.NODESET);
    }

    /**
     * Returns the text content of the text node specified.
     * 
     * @param doc
     *            The document containing the text node.
     * @param xpath
     *            The xpath for the document to the text node.
     * @return The text value of the text node.
     * @throws XPathExpressionException
     */
    public static String getNodeTextContentByXPath(Node doc, String xpath)
            throws XPathExpressionException {
        final Node nodeByXpath = getNodeByXPath(doc, xpath);
        if (nodeByXpath == null) {
            return null;
        }
        return nodeByXpath.getTextContent();
    }

    /**
     * Returns the number value of an XPath evaluation.
     * 
     * @param doc
     *            The document to be checked.
     * @param xpathString
     *            The XPath to search at.
     * @return The number according to the XPath.
     * @throws XPathExpressionException
     */
    public static Number getNumberByXPath(Document doc, String xpathString)
            throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        XPathExpression expr = xpath.compile(xpathString);

        Number result = (Number) expr.evaluate(doc, XPathConstants.NUMBER);
        return result;
    }

    /**
     * Returns a new XML document.
     * 
     * @return A document.
     */
    public static Document newDocument() {
        try {
            final DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a new element compatible for the elements of the document the
     * parameter belongs to.
     * 
     * @param elementName
     *            The name of the element to be created.
     * @param parentElement
     *            The element used to identify the owning document.
     * @return A new element.
     */
    public static Element newElement(String elementName, Element parentElement) {
        return parentElement.getOwnerDocument().createElement(elementName);
    }

    /**
     * Returns the last child node with the provided name if existing.
     * 
     * @param parentNode
     *            the node to find the wanted child in
     * @param nodeName
     *            the wanted child node name
     * @return the matching child node or <code>null</code> if not found
     */
    public static Node getLastChildNode(Node parentNode, String nodeName) {
        NodeList childNodes = parentNode.getChildNodes();
        Node node = null;
        for (int index = 0; index < childNodes.getLength(); index++) {
            if (nodeName.equals(childNodes.item(index).getNodeName())) {
                node = childNodes.item(index);
            }
        }
        return node;
    }

    /**
     * Reads the attribute with the given name from the given node.
     * 
     * @param node
     *            the node to get the attribute from
     * @param attName
     *            the attribute name
     * @return the attribute value or <code>null</code> if the attribute doesn't
     *         exist
     */
    public static String getStringAttValue(Node node, String attName) {
        String result = null;
        Node attNode = node.getAttributes().getNamedItem(attName);
        if (attNode != null) {
            result = attNode.getNodeValue();
        }
        return result;
    }

    /**
     * Reads the attribute with the given name from the given node and tries to
     * convert it to a <code>Date</code>. The value must be a <code>long</code>
     * not equal to 0 to get a <code>Date</code> object.
     * 
     * @param node
     *            the node to get the attribute from
     * @param attName
     *            the attribute name
     * @return the attribute value or <code>null</code> if the attribute doesn't
     *         exist
     */
    public static Date getDateAttValue(Node node, String attName) {
        long result = XMLConverter.getLongAttValue(node, attName);
        if (result != 0) {
            return new Date(result);
        }
        return null;
    }

    /**
     * Reads the attribute with the given name from the given node and tries to
     * convert it to a <code>long</code> value.
     * 
     * @param node
     *            the node to get the attribute from
     * @param attName
     *            the attribute name
     * @return the attribute value or 0 if the attribute doesn't exist
     */
    public static long getLongAttValue(Node node, String attName) {
        long result = 0;
        Node attNode = node.getAttributes().getNamedItem(attName);
        if (attNode != null) {
            result = Long.parseLong(attNode.getNodeValue());
        }
        return result;
    }

    /**
     * Reads the attribute with the given name from the given node and tries to
     * convert it to a <code>BigDecimal</code> value.
     * 
     * @param node
     *            the node to get the attribute from
     * @param attName
     *            the attribute name
     * @return the attribute value or 0 if the attribute doesn't exist
     */
    public static BigDecimal getBigDecimalAttValue(Node node, String attName) {
        Node attNode = node.getAttributes().getNamedItem(attName);
        if (attNode == null) {
            return null;
        }
        return new BigDecimal(attNode.getNodeValue());
    }

    /**
     * Reads the attribute with the given name from the given node and tries to
     * convert it to a <code>double</code> value.
     * 
     * @param node
     *            the node to get the attribute from
     * @param attName
     *            the attribute name
     * @return the attribute value or 0 if the attribute doesn't exist
     */
    public static double getDoubleAttValue(Node node, String attName) {
        double result = 0;
        Node attNode = node.getAttributes().getNamedItem(attName);
        if (attNode != null) {
            result = Double.parseDouble(attNode.getNodeValue());
        }
        return result;
    }

    /**
     * Encodes the given String in UTF-8.
     * 
     * @param s
     * @return
     */
    public static byte[] toUTF8(String s) {
        try {
            return s.getBytes(ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 must be available on all Java VMs
            throw new AssertionError(e);
        }
    }

    /**
     * Decodes the given binary data with UTF-8.
     * 
     * @param bytes
     * @return
     */
    public static String fromUTF8(byte[] bytes) {
        try {
            return new String(bytes, ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 must be available on all Java VMs
            throw new AssertionError(e);
        }
    }

    /**
     * Combines the given list of fragments into a single XML file with the
     * given root element.
     * 
     * @param rootname
     *            name of the root element
     * @param fragments
     *            XML fragments to include
     * @return UTF-8 encoded XML file
     */
    public static byte[] combine(String rootname, List<String> fragments) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(HEADER);
        buffer.append(String.format("<%s>%n", rootname));
        for (final String f : fragments) {
            buffer.append(String.format("%s%n", f));
        }
        buffer.append(String.format("</%s>%n", rootname));
        return toUTF8(buffer.toString());
    }

    /**
     * Combines the given list of fragments into a single XML file with the
     * given root element. The schema info is added after header
     * 
     * @param rootname
     *            name of the root element
     * @param fragments
     *            XML fragments to include
     * @return UTF-8 encoded XML file
     */
    public static byte[] combine(String rootname, List<String> fragments,
            String schemaHeader) {

        final StringBuilder buffer = new StringBuilder();
        buffer.append(HEADER);
        buffer.append(String.format("<%s %s>%n", rootname, schemaHeader));
        for (final String f : fragments) {
            buffer.append(String.format("%s%n", f));
        }
        buffer.append(String.format("</%s>%n", rootname));
        return toUTF8(buffer.toString());
    }

    /**
     * @param nodeList
     * @param nodeName
     * @return all nodes with the given name
     */
    public static List<Node> getNodeList(NodeList nodeList, String nodeName) {
        List<Node> result = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (nodeName.equals(node.getNodeName())) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Wraps Doument.getElementsByTagNameNS(String namespaceURI, String
     * localName) and returns the result as a java.util.list
     */
    public static List<Element> getElementsByTagNameNS(Document document,
            String namespaceURI, String localName) {
        List<Element> result = new ArrayList<Element>();
        NodeList nodeList = document.getElementsByTagNameNS(namespaceURI,
                localName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add((Element) nodeList.item(i));
        }
        return result;
    }

    /**
     * calculates the sum of the selected nodes
     * 
     * @param node
     * @param expression
     * @return
     * @throws XPathExpressionException
     */
    public static Double sumup(Node node, String expression)
            throws XPathExpressionException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        final XPathExpression expr = xpath.compile("sum(" + expression + ')');
        return (Double) expr.evaluate(node, XPathConstants.NUMBER);
    }

    /**
     * Count nodes which are given via xpath expression
     */
    public static Double countNodes(Node node, String nodePath)
            throws XPathExpressionException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        final XPathExpression expr = xpath.compile("count(" + nodePath + ')');
        return (Double) expr.evaluate(node, XPathConstants.NUMBER);
    }

}
