package org.oscm.build.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;

/** @author goebel */
public class XMLHelper {

  protected static String getAttributeValue(Node node, String name) {
    return getAttributeValue(node, name, "");
  }

  public static String getAttributeValue(Node node, String name, String defaultValue) {
    Node attr = node.getAttributes().getNamedItem(name);
    if (attr != null) return attr.getNodeValue();
    return defaultValue;
  }

  public static Document convertToDocument(String string, boolean checkSchema)
      throws SAXException, ParserConfigurationException, IOException {

    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    if (checkSchema) {
      URL schemaURL =
          XMLHelper.class.getClassLoader().getResource("META-INF/Loadbalancer_schema.xsd");
      String constant = XMLConstants.W3C_XML_SCHEMA_NS_URI;
      SchemaFactory xsdFactory = SchemaFactory.newInstance(constant);
      Schema schema = xsdFactory.newSchema(schemaURL);
      dfactory.setSchema(schema);
    }

    dfactory.setValidating(false);
    dfactory.setIgnoringElementContentWhitespace(true);
    dfactory.setNamespaceAware(true);
    DocumentBuilder builder = dfactory.newDocumentBuilder();

    if (checkSchema) {
      builder.setErrorHandler(new DefaultErrorHandler());
    }

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

  public static void writeToFS(Document doc, String outfile) {
    try {
      String xml = toString(doc, true);
      doc = convertToDocument(xml, false);
      xml = toString(doc, false);
      System.out.println(xml);
      writeFile(new File(outfile), xml);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static String toString(Document doc, boolean stripBlanks) {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      DOMSource domSource = new DOMSource(doc);

      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter w = new StringWriter();
      StreamResult result = new StreamResult(w);

      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      String xml = w.toString();
      if (stripBlanks) xml = xml.replaceAll(">\\s+?<", "><");
      return xml;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void writeFile(File tf, String text) {

    PrintStream fos = null;
    try {
      tf.createNewFile();
      fos = new PrintStream(new FileOutputStream(tf));
      fos.println(text);
    } catch (Exception e) {
      if (fos != null) {
        fos.close();
      }
    }
  }
}
