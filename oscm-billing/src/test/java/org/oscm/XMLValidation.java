/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.oscm.billingservice.service.BillingServiceBean;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class for XML schema validation.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XMLValidation {

    /**
     * Validates the XML data against the specified schema.
     * 
     * @param schemaFileURL
     *            The URL to the schema file.
     * @param xmlContent
     *            The XML data to be validated.
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException 
     */
    public static void validateXML(URL schemaFileURL, Document xmlContent)
            throws SAXException, IOException, TransformerException {
        SchemaFactory factory = SchemaFactory
                .newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(schemaFileURL);
        Validator validator = schema.newValidator();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printDocument(xmlContent, byteArrayOutputStream);
        ByteArrayInputStream bis = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());
        validator.validate(new StreamSource(bis));
    }

    private static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        StreamResult sr = new StreamResult(osw);
        transformer.transform(new DOMSource(doc), 
                   sr);
    }

    /**
     * Validates the XML data against the specified schema.
     * 
     * @param schemaFileURL
     *            The URL to the schema file.
     * @param xmlContent
     *            The XML data to be validated.
     * @throws SAXException
     * @throws IOException
     */
    public static void validateXML(URL schemaFileURL, File file)
            throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory
                .newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(schemaFileURL);
        Validator validator = schema.newValidator();
        Source source = new StreamSource(file);
        validator.validate(source);
    }

    /**
     * Returns the URL of the billing service result XML schema file.
     * 
     * @return The URL of the schema file.
     */
    public static URL getBillingResultSchemaURL() {
        return BillingServiceBean.class.getResource("/BillingResult.xsd");
    }
}
