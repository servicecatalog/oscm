/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                       
 *                                                                              
 *  Creation Date: 21.11.2011                                                      
 *                                                                              
 *  Completion Time: 21.11.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.either;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author kulle
 * 
 */
public class XMLSecureParsingTest {

    private final static String FILE_TEST = "org/oscm/converter/xmlBomb.txt";

    @Test
    public void testSecureParsing() throws IOException,
            ParserConfigurationException, SAXException {
        // given
        byte[] fileAsByteArray = XMLConverter.getFileAsByteArray(
                this.getClass(), FILE_TEST);

        try {
            // when
            XMLConverter.convertToDocument(new String(fileAsByteArray), true);
            fail();
        } catch (SAXParseException e) {
            // then
            assertThatMaxEntitiesExceeded(e);
        }
    }

    @Test
    public void testSaxParsing() throws ParserConfigurationException,
            SAXException, IOException {
        // given
        byte[] fileAsByteArray = XMLConverter.getFileAsByteArray(
                this.getClass(), FILE_TEST);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        assertTrue(spf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        SAXParser saxParser = spf.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();

        try {
            // when
            reader.parse(new InputSource(new ByteArrayInputStream(
                    fileAsByteArray)));
            fail();
        } catch (SAXParseException e) {
            // then
            assertThatMaxEntitiesExceeded(e);
        }
    }

    private void assertThatMaxEntitiesExceeded(SAXParseException exception) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.applyPattern("##,###");
        // Java7u21 and before
        String maxEntitiesExceededMessage_le21 = df.format(64000);
        // after Java7u21
        String maxEntitiesExceededMessage_gt21 = "64000";

        assertThat(
                exception.getMessage(),
                either(containsString(maxEntitiesExceededMessage_le21)).or(
                        containsString(maxEntitiesExceededMessage_gt21)));
    }
}
