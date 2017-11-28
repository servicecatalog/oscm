/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileInputStream;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.exception.AssertionValidationException;

/**
 * @author kulle
 * 
 */
public class AssertionContentVerifierTest {

    private final String FILE_UNSIGNED_ASSERTION = "javares/unsignedAssertion.xml";
    private final String FILE_MISSING_CONFIRMATION_DATA = "javares/unsignedAssertion_noConfirmationData_noUserid.xml";
    private AssertionContentVerifier verifier;
    private Document assertion;

    @Before
    public void setup() throws Exception {
        verifier = spy(new AssertionContentVerifier(new VerifierConfiguration(
                "identifier_1", "", "", Calendar.getInstance(), "8f96dede")));
        assertion = loadDocument(FILE_UNSIGNED_ASSERTION);
    }

    private Document loadDocument(String file) throws Exception {
        FileInputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(file);
            document = XMLConverter.convertToDocument(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return document;
    }

    @Test(expected = AssertionValidationException.class)
    public void verifyConfirmationData_error() throws Exception {
        // given
        assertion = loadDocument(FILE_UNSIGNED_ASSERTION);
        doThrow(new XPathExpressionException("")).when(verifier)
                .loadConfirmationData(any(Node.class));

        // when
        verifier.verifyConfirmationData(assertion);

        // then exception expected
    }

    @Test
    public void verifyConfirmationData() throws Exception {
        // given
        assertion = loadDocument(FILE_UNSIGNED_ASSERTION);
        doNothing().when(verifier).verifyRecipient(any(Node.class),
                any(Node.class));
        doNothing().when(verifier).verifyAssertionExpirationDate(
                any(Node.class), any(Node.class));
        doNothing().when(verifier).verifyInResponseTo(any(Node.class),
                any(Node.class));

        // when
        verifier.verifyConfirmationData(assertion);

        // then
        verify(verifier, times(1)).verifyRecipient(any(Node.class),
                any(Node.class));
        verify(verifier, times(1)).verifyAssertionExpirationDate(
                any(Node.class), any(Node.class));
        verify(verifier, times(1)).verifyInResponseTo(any(Node.class),
                any(Node.class));
    }

    @Test
    public void verifyAssertionContent_delegate() throws Exception {
        // given
        Node node = null;
        doNothing().when(verifier).verifyConfirmationData(any(Node.class));

        // when
        verifier.verifyAssertionContent(node);

        // then
        verify(verifier, times(1)).verifyConfirmationData(any(Node.class));
    }

    @Test
    public void loadConfirmationData_missingConfirmationData() throws Exception {
        // given
        assertion = loadDocument(FILE_MISSING_CONFIRMATION_DATA);
        Node nodeAssertion = getActualFirstChild(assertion);

        // when
        NodeList confirmationData = verifier
                .loadConfirmationData(nodeAssertion);

        // then
        assertEquals(0, confirmationData.getLength());
    }

    @Test
    public void loadConfirmationData() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);

        // when
        NodeList confirmationData = verifier
                .loadConfirmationData(nodeAssertion);

        // then
        assertEquals(1, confirmationData.getLength());
    }

    @Test
    public void verifyRecipient() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");
        verifier.acsUrl = XMLConverter.getStringAttValue(nodeConfirmationData,
                SamlXmlTags.ATTRIBUTE_RECIPIENT);

        // when
        verifier.verifyRecipient(nodeAssertion, nodeConfirmationData);

        // then no exception expected
    }

    @Test(expected = AssertionValidationException.class)
    public void verifyRecipient_error() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");
        verifier.acsUrl = "wrong url";

        // when
        verifier.verifyRecipient(nodeAssertion, nodeConfirmationData);
    }

    @Test
    public void verifyAssertionExpirationDate() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");
        Calendar now = Calendar.getInstance();
        now.set(Calendar.YEAR, 2000);
        verifier.now = now;

        // when
        verifier.verifyAssertionExpirationDate(nodeAssertion,
                nodeConfirmationData);

        // then no exception expected
    }

    @Test(expected = AssertionValidationException.class)
    public void verifyAssertionExpirationDate_equals() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");
        verifier.now = readExpirationDateFromXml(nodeConfirmationData);

        // when
        verifier.verifyAssertionExpirationDate(nodeAssertion,
                nodeConfirmationData);
    }

    private Calendar readExpirationDateFromXml(Node nodeConfirmationData) {
        return DatatypeConverter.parseDateTime(XMLConverter.getStringAttValue(
                nodeConfirmationData, SamlXmlTags.ATTRIBUTE_NOT_ON_OR_AFTER));
    }

    @Test(expected = AssertionValidationException.class)
    public void verifyAssertionExpirationDate_error() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");

        // when
        verifier.verifyAssertionExpirationDate(nodeAssertion,
                nodeConfirmationData);
    }

    @Test
    public void verifyInResponseTo() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");

        // when
        verifier.verifyInResponseTo(nodeAssertion, nodeConfirmationData);

        // then no exception is expected
    }

    @Test(expected = AssertionValidationException.class)
    public void verifyInResponseTo_error() throws Exception {
        // given
        Node nodeAssertion = getActualFirstChild(assertion);
        Node nodeConfirmationData = XMLConverter.getNodeByXPath(nodeAssertion,
                "//saml2:SubjectConfirmationData");
        verifier.requestId = "wrong request id";

        // when
        verifier.verifyInResponseTo(nodeAssertion, nodeConfirmationData);
    }

    private Node getActualFirstChild(Document doc) {
        Node firstNode = doc.getFirstChild();
        if (firstNode.getNodeName().equals("#comment")) {
            firstNode = firstNode.getNextSibling();
        }
        return firstNode;
    }

}
