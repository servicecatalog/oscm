/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Soehnges                                             
 *                                                                              
 *  Creation Date: 17.05.2011                                                      
 *                                                                              
 *  Completion Time: 17.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import org.junit.Test;
import org.oscm.converter.ResourceLoader;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class ServiceProvisioningServiceBeanImportExportSchemaIT extends
        EJBTestBase {
    private final String tsIDXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<tns:TechnicalServices "
            + "xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd ./TechnicalServices.xsd\">"
            + "<tns:TechnicalService id=\"{0}\" "
            + "accessType=\"LOGIN\" "
            + "provisioningType=\"SYNCHRONOUS\" "
            + "provisioningUrl=\"http://xyz.de\">"
            + "<AccessInfo locale=\"en\" />"
            + "<LocalizedDescription locale=\"en\" />"
            + "<LocalizedLicense locale=\"de\" />"
            + "</tns:TechnicalService></tns:TechnicalServices>";

    static class MyErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }

    }

    @Override
    public void setup(TestContainer container) throws Exception {
    }

    private void verifyXML(byte[] xml) throws IOException, SAXException,
            ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        SchemaFactory sf = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }

        final Schema schema;
        try (InputStream inputStream = ResourceLoader.getResourceAsStream(
                getClass(), "TechnicalServices.xsd")) {
            schema = sf.newSchema(new StreamSource(inputStream));
        }
        spf.setSchema(schema);

        SAXParser saxParser = spf.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();
        ErrorHandler errorHandler = new MyErrorHandler();
        reader.setErrorHandler(errorHandler);
        reader.parse(new InputSource(new ByteArrayInputStream(xml)));
    }

    @Test
    public void testSchemaStrId() throws UnsupportedEncodingException,
            IOException, SAXException, ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml, "1 00").getBytes("UTF-8"));
        verifyXML(MessageFormat
                .format(tsIDXml,
                        "()-09@AZ_a\uD7FF\uE000\uFFFD\u10000\u10FFFF ()-09@AZ_a\uD7FF\uE000\uFFFD\u10000\u10FFFF")
                .getBytes("UTF-8"));
        verifyXML(MessageFormat.format(tsIDXml, "üöäß").getBytes("UTF-8"));
        verifyXML(MessageFormat.format(tsIDXml, "1").getBytes("UTF-8"));
    }

    @Test(expected = SAXException.class)
    public void testSchemaStrIdIllegalChars1()
            throws UnsupportedEncodingException, IOException, SAXException,
            ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml, "1 00 ").getBytes("UTF-8"));
    }

    @Test(expected = SAXException.class)
    public void testSchemaStrIdIllegalChars2()
            throws UnsupportedEncodingException, IOException, SAXException,
            ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml, " 1 00").getBytes("UTF-8"));
    }

    @Test(expected = SAXException.class)
    public void testSchemaStrIdIllegalChars3()
            throws UnsupportedEncodingException, IOException, SAXException,
            ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml, "1=00").getBytes("UTF-8"));
    }

    @Test(expected = SAXException.class)
    public void testSchemaStrIdTooLong() throws UnsupportedEncodingException,
            IOException, SAXException, ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml,
                "01234567890123456789012345678901234567890123456789").getBytes(
                "UTF-8"));
    }

    @Test(expected = SAXException.class)
    public void testSchemaStrIdIllegalChars4()
            throws UnsupportedEncodingException, IOException, SAXException,
            ParserConfigurationException {
        verifyXML(MessageFormat.format(tsIDXml,
                "01234567890123456789012345678901234567890123=").getBytes(
                "UTF-8"));
    }
}
