/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 13.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author roderus
 * 
 */
public class SpMetadataGeneratorTest {

    private SpMetadataGenerator metadataGenerator;

    private final static String ENTITY_ID = "some_entityid";
    private final static String ACS0 = "https://base.bes.url.de:8181/oscm-portal/";
    private final static String ACS1 = "http://base.bes.url.de:8180/oscm-portal/";

    @Before
    public void setup() {
        metadataGenerator = new SpMetadataGenerator(ENTITY_ID, ACS0, ACS1);
    }

    @Test
    public void constructor_BaseUrlHTTP_null() throws Exception {
        // given
        SpMetadataGenerator mdGen = new SpMetadataGenerator(ENTITY_ID, ACS0,
                null);

        // when
        Document metadata = mdGen.generate();

        // then
        String location = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "Location");
        assertNull(location);
    }

    @Test
    public void constructor_BaseUrlHTTP_empty() throws Exception {
        // given
        SpMetadataGenerator mdGen = new SpMetadataGenerator(ENTITY_ID, ACS0,
                " ");

        // when
        Document metadata = mdGen.generate();

        // then
        String location = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "Location");
        assertNull(location);
    }

    @Test
    public void constructor_BaseUrlHTTPS_null() throws Exception {
        // given
        SpMetadataGenerator mdGen = new SpMetadataGenerator(ENTITY_ID, null,
                ACS1);

        // when
        Document metadata = mdGen.generate();

        // then
        String location = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "Location");
        assertNull(location);
    }

    @Test
    public void constructor_BaseUrlHTTPS_empty() throws Exception {
        // given
        SpMetadataGenerator mdGen = new SpMetadataGenerator(ENTITY_ID, " ",
                ACS1);

        // when
        Document metadata = mdGen.generate();

        // then
        String location = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "Location");
        assertNull(location);
    }

    @Test
    public void generate_acs0() throws Exception {
        // given

        // when
        Document metadata = metadataGenerator.generate();

        // then
        String location = extractAttribute(metadata,
                "md:AssertionConsumerService", 0, "Location");
        String index = extractAttribute(metadata,
                "md:AssertionConsumerService", 0, "index");
        String isDefault = extractAttribute(metadata,
                "md:AssertionConsumerService", 0, "isDefault");

        assertEquals(ACS0, location);
        assertEquals("0", index);
        assertEquals("true", isDefault);

    }

    @Test
    public void generate_acs1() throws Exception {
        // given

        // when
        Document metadata = metadataGenerator.generate();

        // then
        String acutal = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "Location");
        String index = extractAttribute(metadata,
                "md:AssertionConsumerService", 1, "index");

        assertEquals(ACS1, acutal);
        assertEquals("1", index);
    }

    @Test
    public void generate_entityId() throws Exception {
        // given

        // when
        Document metadata = metadataGenerator.generate();

        // then
        String acutal = extractAttribute(metadata, "md:EntityDescriptor",
                "entityID");
        assertEquals(ENTITY_ID, acutal);
    }

    @Test
    public void generate_spSsoDescriptor() throws Exception {
        // given

        // when
        Document metadata = metadataGenerator.generate();

        // then
        String authnRequestsSigned = extractAttribute(metadata,
                "md:SPSSODescriptor", "AuthnRequestsSigned");
        String wantAssertionsSigned = extractAttribute(metadata,
                "md:SPSSODescriptor", "WantAssertionsSigned");
        String protocolSupportEnumeration = extractAttribute(metadata,
                "md:SPSSODescriptor", "protocolSupportEnumeration");

        assertEquals("false", authnRequestsSigned);
        assertEquals("true", wantAssertionsSigned);
        assertEquals("urn:oasis:names:tc:SAML:2.0:protocol",
                protocolSupportEnumeration);
    }

    @Test
    public void generate_nameIdFormat() throws Exception {
        // given

        // when
        Document metadata = metadataGenerator.generate();

        // then
        String acutal = extractValue(metadata, "md:NameIDFormat");
        assertEquals("urn:oasis:names:tc:SAML:2.0:nameid-format:transient",
                acutal);
    }

    private String extractAttribute(Document doc, String tagName,
            String attributeName) {
        return extractAttribute(doc, tagName, 0, attributeName);
    }

    private String extractAttribute(Document doc, String tagName, int tagIndex,
            String attributeName) {
        Node node = extractNode(doc, tagName, tagIndex);
        if (node == null) {
            return null;
        }
        return node.getAttributes().getNamedItem(attributeName).getNodeValue();
    }

    private String extractValue(Document doc, String tagName) {
        Node node = extractNode(doc, tagName, 0);
        return node.getTextContent();
    }

    private Node extractNode(Document doc, String tagName, int tagIndex) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        return nodes.item(tagIndex);
    }
}
