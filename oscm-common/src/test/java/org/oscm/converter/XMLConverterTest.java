/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for {@link XMLConverter}.
 * 
 * @author hoffmann
 */
public class XMLConverterTest {

    private DocumentBuilderFactory builderFactory;
    private DocumentBuilder builder;

    @Before
    public void setup() throws Exception {
        builderFactory = DocumentBuilderFactory.newInstance();
        builder = builderFactory.newDocumentBuilder();
    }

    @Test
    public void testGetDocAsString1() throws Exception {
        final Document doc = builder.newDocument();
        final Element element = doc.createElement("test");
        element.setAttribute("attr", "mäßig");
        doc.appendChild(element);
        assertEquals(String.format("<test attr=\"mäßig\"/>%n"),
                XMLConverter.convertToString(doc, false));
    }

    @Test
    public void testGetDocAsString2() throws Exception {
        final Document doc = builder.newDocument();
        final Element element = doc.createElement("test");
        element.setAttribute("attr", "\u306f\u3044");
        doc.appendChild(element);
        assertEquals(
                String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n<test attr=\"\u306f\u3044\"/>%n"),
                XMLConverter.convertToString(doc, true));
    }

    @Test
    public void testGetStringAsDocNull() throws Exception {
        assertNull(XMLConverter.convertToDocument(null, false));
    }

    @Test
    public void testGetStringAsDoc() throws Exception {
        final String s = "<?xml version='1.0' encoding='UTF-8'?><test attr='\u306f\u3044'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Element test = doc.getDocumentElement();
        assertEquals("test", test.getNodeName());
        assertEquals("\u306f\u3044", test.getAttribute("attr"));
    }

    @Test
    public void testGetStringAsDocWithNamespaceDisabled() throws Exception {
        final String s = "<?xml version='1.0' encoding='UTF-8'?><test xmlns='http://example.com/'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Element test = doc.getDocumentElement();
        assertEquals("test", test.getNodeName());
        assertNull(test.getNamespaceURI());
    }

    @Test
    public void testGetStringAsDocWithNamespaceEnabled() throws Exception {
        final String s = "<?xml version='1.0' encoding='UTF-8'?><test xmlns='http://example.com/'/>";
        final Document doc = XMLConverter.convertToDocument(s, true);
        final Element test = doc.getDocumentElement();
        assertEquals("test", test.getNodeName());
        assertEquals("http://example.com/", test.getNamespaceURI());
    }

    @Test
    public void testGetNodeByXpath1() throws Exception {
        final String s = "<test><e/><e attr='here'/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Node node = XMLConverter.getNodeByXPath(doc, "//test/e[2]/@attr");
        assertEquals("here", node.getNodeValue());
    }

    @Test
    public void testGetNodeByXpath2() throws Exception {
        final String s = "<test><e/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        assertNull(XMLConverter.getNodeByXPath(doc, "//test/f"));
    }

    @Test
    public void testGetNodeListByXpath1() throws Exception {
        final String s = "<test><e/><e/><e/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final NodeList nodelist = XMLConverter.getNodeListByXPath(doc,
                "//test/e");
        assertEquals(3, nodelist.getLength());
        assertEquals("e", nodelist.item(0).getNodeName());
        assertEquals("e", nodelist.item(1).getNodeName());
        assertEquals("e", nodelist.item(2).getNodeName());
    }

    @Test
    public void testGetNodeListByXpath2() throws Exception {
        final String s = "<test><e/><e/><e/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final NodeList nodelist = XMLConverter.getNodeListByXPath(doc,
                "//test/f");
        assertEquals(0, nodelist.getLength());
    }

    @Test
    public void testGetNodeTextContentByXPath1() throws Exception {
        final String s = "<test><a/><b>hello</b><c/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final String text = XMLConverter.getNodeTextContentByXPath(doc,
                "//test/b");
        assertEquals("hello", text);
    }

    @Test
    public void testGetNodeTextContentByXPath2() throws Exception {
        final String s = "<test><a/><b/><c/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final String text = XMLConverter.getNodeTextContentByXPath(doc,
                "//test/x");
        assertNull(text);
    }

    @Test
    public void testGetNumberByXPath1() throws Exception {
        final String s = "<test><a/><b code='12345'/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Number number = XMLConverter.getNumberByXPath(doc,
                "//test/b/@code");
        assertEquals(12345, number.intValue());
    }

    @Test
    public void testGetNumberByXPath2() throws Exception {
        final String s = "<test><a/><b code='12345'/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Number number = XMLConverter.getNumberByXPath(doc,
                "//test/a/@code");
        assertEquals(Double.valueOf(Double.NaN), number);
    }

    @Test
    public void testNewDocument() {
        final Document doc = XMLConverter.newDocument();
        assertNotNull(doc);
    }

    @Test
    public void testNewElement() throws Exception {
        final Document doc = builder.newDocument();
        final Element element = doc.createElement("test");
        doc.appendChild(element);
        final Element another = XMLConverter.newElement("another", element);
        assertSame(doc, another.getOwnerDocument());
        assertEquals("another", another.getNodeName());
    }

    @Test
    public void testGetChildNode1() throws Exception {
        final String s = "<test><a/><b/><c attr='here'/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Node node = XMLConverter.getLastChildNode(
                doc.getDocumentElement(), "c");
        assertEquals("c", node.getNodeName());
        assertEquals("here", ((Element) node).getAttribute("attr"));
    }

    @Test
    public void testGetChildNode2() throws Exception {
        final String s = "<test><a/><b/><c/></test>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Node node = XMLConverter.getLastChildNode(
                doc.getDocumentElement(), "d");
        assertNull(node);
    }

    @Test
    public void testGetStringAttValue1() throws Exception {
        final String s = "<test attr='value'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final String value = XMLConverter.getStringAttValue(
                doc.getDocumentElement(), "attr");
        assertEquals("value", value);
    }

    @Test
    public void testGetStringAttValue2() throws Exception {
        final String s = "<test attr='value'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final String value = XMLConverter.getStringAttValue(
                doc.getDocumentElement(), "none");
        assertNull(value);
    }

    @Test
    public void testGetDateAttValue1() throws Exception {
        final String s = "<test attr='100000'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Date value = XMLConverter.getDateAttValue(
                doc.getDocumentElement(), "attr");
        assertEquals(new Date(100000), value);
    }

    @Test
    public void testGetDateAttValue2() throws Exception {
        final String s = "<test attr='100000'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final Date value = XMLConverter.getDateAttValue(
                doc.getDocumentElement(), "none");
        assertNull(value);
    }

    @Test
    public void testGetLongAttValue1() throws Exception {
        final String s = "<test attr='9876543210'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final long value = XMLConverter.getLongAttValue(
                doc.getDocumentElement(), "attr");
        assertEquals(9876543210L, value);
    }

    @Test
    public void testGetLongAttValue2() throws Exception {
        final String s = "<test attr='1234567890'/>";
        final Document doc = XMLConverter.convertToDocument(s, false);
        final long value = XMLConverter.getLongAttValue(
                doc.getDocumentElement(), "none");
        assertEquals(0, value);
    }

    @Test
    public void testGetBigDecimalAttValue() throws Exception {
        String s = "<test attr='23.45'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        BigDecimal value = XMLConverter.getBigDecimalAttValue(
                doc.getDocumentElement(), "none");
        assertNull(value);
    }

    @Test
    public void testGetBigDecimalAttValue2() throws Exception {
        String s = "<test attr='23.45'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        BigDecimal value = XMLConverter.getBigDecimalAttValue(
                doc.getDocumentElement(), "attr");
        assertEquals(new BigDecimal("23.45"), value);
    }

    @Test
    public void testGetBigDecimalAttValue3() throws Exception {
        String s = "<test attr='23'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        BigDecimal value = XMLConverter.getBigDecimalAttValue(
                doc.getDocumentElement(), "attr");
        assertEquals(new BigDecimal("23"), value);
    }

    @Test
    public void testGetDoubleAttValue1() throws Exception {
        String s = "<test attr='23.45'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        double value = XMLConverter.getDoubleAttValue(doc.getDocumentElement(),
                "none");
        assertEquals(0, value, 0.1);
    }

    @Test
    public void testGetDoubleAttValue2() throws Exception {
        String s = "<test attr='23'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        double value = XMLConverter.getDoubleAttValue(doc.getDocumentElement(),
                "attr");
        assertEquals(23, value, 0.1);
    }

    @Test
    public void testGetDoubleAttValue3() throws Exception {
        String s = "<test attr='23.45'/>";
        Document doc = XMLConverter.convertToDocument(s, false);
        double value = XMLConverter.getDoubleAttValue(doc.getDocumentElement(),
                "attr");
        assertEquals(23.45, value, 0.1);
    }

    @Test
    public void testToUTF8() {
        assertArrayEquals(new byte[] { (byte) 0x61, (byte) 0xc3, (byte) 0x9f },
                XMLConverter.toUTF8("aß"));
    }

    @Test
    public void testFromUTF8() {
        assertEquals(
                "aß",
                XMLConverter.fromUTF8(new byte[] { (byte) 0x61, (byte) 0xc3,
                        (byte) 0x9f }));
    }

    @Test
    public void testCombine1() throws Exception {
        final List<String> empty = Collections.emptyList();
        byte[] xml = XMLConverter.combine("root", empty);
        String expected = String
                .format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<root>%n</root>%n");
        assertArrayEquals(expected.getBytes("UTF-8"), xml);
    }

    @Test
    public void testCombine2() throws Exception {
        final List<String> empty = Arrays.asList("<a/>", "<b/>", "<c/>");
        byte[] xml = XMLConverter.combine("root", empty);
        String expected = String
                .format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<root>%n<a/>%n<b/>%n<c/>%n</root>%n");
        assertArrayEquals(expected.getBytes("UTF-8"), xml);
    }

    @Test
    public void removeEOLCharsfromXML() throws Exception {
        // given
        String xml = "<a id=\"1\"\nname=\"n\">\n\r<b /></a>";

        // when
        String noLinebreaks = XMLConverter.removeEOLCharsFromXML(xml);

        // then
        assertEquals("<a id=\"1\" name=\"n\"><b /></a>", noLinebreaks);
        assertFalse(noLinebreaks.contains("\n"));
        assertFalse(noLinebreaks.contains("\r"));
    }
}
