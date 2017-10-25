/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.exception.AssertionValidationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author kulle
 * 
 */
class AssertionContentVerifier {

    private final static String CONFIRMATION_DATA_XPATH_EXPR = "//*[local-name()='SubjectConfirmation']" //
            + "/*[local-name()='SubjectConfirmationData']";
    private static final String ATTRIBUTE_TENANTID_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@Name='tenantID']" //
            + "/*[local-name()='AttributeValue']";
    private final String tenantID;

    String acsUrl;
    String acsUrlHttps;
    String requestId;
    Calendar now;

    public AssertionContentVerifier(VerifierConfiguration configuration) {
        this.requestId = configuration.getRequestId();
        this.acsUrl = configuration.getAcsUrl();
        this.now = configuration.getReferenceTime();
        this.acsUrlHttps = configuration.getAcsUrlHttps();
        this.tenantID = configuration.getTenantID();
    }

    public void verifyAssertionContent(Node nodeAssertion)
            throws AssertionValidationException {
        verifyConfirmationData(nodeAssertion);
    }

    void verifyConfirmationData(Node nodeAssertion)
            throws AssertionValidationException {

        try {
            NodeList nodesConfirmationData = loadConfirmationData(nodeAssertion);
            for (int i = 0; i < nodesConfirmationData.getLength(); i++) {
                Node nodeConfirmationData = nodesConfirmationData.item(i);
                verifyRecipient(nodeAssertion, nodeConfirmationData);
                verifyAssertionExpirationDate(nodeAssertion,
                        nodeConfirmationData);
                verifyInResponseTo(nodeAssertion, nodeConfirmationData);
                verifyTenantID(nodeAssertion);
            }
        } catch (XPathExpressionException exception) {
            throw new AssertionValidationException(
                    "Error occurred during assertion content validation",
                    AssertionValidationException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception);
        }

    }

    private void verifyTenantID(Node nodeAssertion)
            throws AssertionValidationException {
        try {
            if (!tenantID.equals(XMLConverter
                    .getNodeListByXPath(nodeAssertion,
                            ATTRIBUTE_TENANTID_XPATH_EXPR)
                    .item(0).getFirstChild().getNodeValue())) {
                AssertionValidationException exception = new AssertionValidationException(
                        String.format(
                                "The attribute with the name tenantID does not correspond to the request tenantID: %s.",
                                tenantID),
                        AssertionValidationException.ReasonEnum.WRONG_TENANT,
                        new String[] {tenantID });
                throw exception;
            }
        } catch (Exception e) {
            AssertionValidationException exception = new AssertionValidationException(
                    String.format(
                            "Cannot retrieve attribute tenantID for request attribute %s.",
                            tenantID),
                    AssertionValidationException.ReasonEnum.MISSING_TENANT,
                    new String[] { tenantID });
            throw exception;
        }
    }

    NodeList loadConfirmationData(Node nodeAssertion)
            throws XPathExpressionException {
        return XMLConverter.getNodeListByXPath(nodeAssertion,
                CONFIRMATION_DATA_XPATH_EXPR);
    }

    /**
     * Verify that the Recipient attribute in any bearer
     * <SubjectConfirmationData> matches the assertion consumer service URL to
     * which the <Response> or artifact was delivered.
     */
    void verifyRecipient(Node nodeAssertion, Node nodeConfirmationData)
            throws AssertionValidationException {
        String acsFromRequest = XMLConverter.getStringAttValue(
                nodeConfirmationData, SamlXmlTags.ATTRIBUTE_RECIPIENT);
        if (!doesAcsMatch(acsFromRequest)) {
            String assertionId = XMLConverter.getStringAttValue(nodeAssertion,
                    SamlXmlTags.ATTRIBUTE_ID);

            AssertionValidationException exception = new AssertionValidationException(
                    String.format(
                            "Found incorrect recipient for assertion id=%s, expected is %s or %s, but was %s",
                            assertionId, acsUrl, acsUrlHttps, acsFromRequest),
                    AssertionValidationException.ReasonEnum.INVALID_RECIPIENT,
                    new String[] { assertionId });
            throw exception;
        }
    }

    boolean doesAcsMatch(String acsFromRequest) {
        return (acsUrl.equalsIgnoreCase(acsFromRequest) || acsUrlHttps
                .equalsIgnoreCase(acsFromRequest));
    }

    /**
     * Verify that the NotOnOrAfter attribute in any bearer
     * <SubjectConfirmationData> has not passed, subject to allowable clock skew
     * between the providers
     * 
     * @throws AssertionValidationException
     */
    void verifyAssertionExpirationDate(Node nodeAssertion,
            Node nodeConfirmationData) throws AssertionValidationException {

        Calendar expirationDate = DatatypeConverter.parseDateTime(XMLConverter
                .getStringAttValue(nodeConfirmationData,
                        SamlXmlTags.ATTRIBUTE_NOT_ON_OR_AFTER));
        if (now.equals(expirationDate) || now.after(expirationDate)) {
            String assertionId = XMLConverter.getStringAttValue(nodeAssertion,
                    SamlXmlTags.ATTRIBUTE_ID);
            AssertionValidationException exception = new AssertionValidationException(
                    String.format("Assertion (id=%s) expired", assertionId),
                    AssertionValidationException.ReasonEnum.ASSERTION_EXPIRED,
                    new String[] { assertionId });
            throw exception;
        }
    }

    /**
     * Verify that the InResponseTo attribute in the bearer
     * <SubjectConfirmationData> equals the ID of its original <AuthnRequest>
     * message, unless the response is unsolicited (see Section 4.1.5 of
     * saml-profiles-2.0-os), in which case the attribute MUST NOT be present
     */
    void verifyInResponseTo(Node nodeAssertion, Node nodeConfirmationData)
            throws AssertionValidationException {
        if (!requestId.equals(XMLConverter.getStringAttValue(
                nodeConfirmationData, SamlXmlTags.ATTRIBUTE_IN_RESPONSE_TO))) {
            String assertionId = XMLConverter.getStringAttValue(nodeAssertion,
                    SamlXmlTags.ATTRIBUTE_ID);
            AssertionValidationException exception = new AssertionValidationException(
                    String.format(
                            "The assertion (id=%s) with the InResponse attribute: %s does not correspond to the request id: %s.",
                            assertionId, XMLConverter.getStringAttValue(
                                    nodeConfirmationData,
                                    SamlXmlTags.ATTRIBUTE_IN_RESPONSE_TO),
                            requestId),
                    AssertionValidationException.ReasonEnum.WRONG_REQUEST,
                    new String[] {
                            assertionId,
                            XMLConverter.getStringAttValue(
                                    nodeConfirmationData,
                                    SamlXmlTags.ATTRIBUTE_IN_RESPONSE_TO),
                            requestId });
            throw exception;
        }
    }

}
