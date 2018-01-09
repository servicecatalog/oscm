/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 13, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.oscm.internal.types.exception.NotExistentTenantException.Reason.MISSING_TEANT_ID_IN_SAML;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.SessionIndexNotFoundException;
import org.oscm.internal.types.exception.UserIdNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

//import com.sun.xml.ws.security.opt.impl.incoming.SAMLAssertion;
//import com.sun.xml.wss.XWSSecurityException;
//import com.sun.xml.wss.saml.util.SAMLUtil;

/**
 * Class for retrieving the userid from a saml:response received from an IdP.
 * 
 * @author farmaki
 * 
 */
public class SAMLResponseExtractor {

    private static final String USER_SAML2_ATTRIBUTE_USERID_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@Name='userid']" //
            + "/*[local-name()='AttributeValue']";

    private static final String USER_SAML2_ATTRIBUTE_TENANTID_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@Name='tenantID']" //
            + "/*[local-name()='AttributeValue']";

    private static final String USER_SAML2_ATTRIBUTE_ISSUER_ID_XPATH_EXPR = "//*[local-name()='Issuer']";

    private static final String USER_SAML2_ATTRIBUTE_NAME_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@Name='name']" //
            + "/*[local-name()='AttributeValue']";

    private static final String USER_SAML1_ATTRIBUTE_USERID_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@AttributeName='userid']" //
            + "/*[local-name()='AttributeValue']";

    private static final String USER_SAML1_ATTRIBUTE_TENANTID_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@AttributeName='tenantID']" //
            + "/*[local-name()='AttributeValue']";

    private static final String USER_SAML1_ATTRIBUTE_NAME_XPATH_EXPR = "//*[local-name()='Assertion']" //
            + "//*[local-name()='AttributeStatement']" //
            + "//*[local-name()='Attribute'][@AttributeName='name']" //
            + "/*[local-name()='AttributeValue']";

    private static final String SESSION_INDEX_SAML2_ATTRIBUTE_NAME_XPATH_EXPR = "//*[local-name()='Assertion']//*[local-name()='AuthnStatement']//@*[local-name()='SessionIndex']";

    private static final String STATUS_LOGOUT_RESPONSE_SAML2_XPATH_EXPR = "//*[local-name()='Status']//*[local-name()='StatusCode']//@*[local-name()='Value']";

    /**
     * Retrieves the userid from an encoded saml:Response String.
     * 
     * @param encodedSamlResponse
     *            the encoded saml response
     * @return the userid as a String
     * @throws UnsupportedEncodingException
     */
    public String getUserId(String encodedSamlResponse)
            throws UserIdNotFoundException {

        String userId;

        try {
            userId = getUserIdDecoded(new String(decode(encodedSamlResponse)));
        } catch (UnsupportedEncodingException exception) {
            throw new UserIdNotFoundException(
                    String.format(
                            "An exception occurred while Base64-decoding the SAML response:\n%s",
                            encodedSamlResponse),
                    UserIdNotFoundException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception, new String[] { encodedSamlResponse });
        }

        return userId;
    }

    /**
     * Retrieves the userid from an encoded saml:Response String.
     *
     * @param encodedSamlResponse
     *            the encoded saml response
     * @return the userid as a String
     * @throws UnsupportedEncodingException
     */
    public String getTenantID(String encodedSamlResponse) {

        String tenantID = null;

        try {
            tenantID = getTenantIdDecoded(new String(decode(encodedSamlResponse)));
        } catch (UnsupportedEncodingException exception) {
            // do nothing. Default tenatn will be used.
        }

        return tenantID;
    }

    /**
     * Retrieves the sessionIndex from an encoded saml:Response String.
     *
     * @param encodedSamlResponse
     *            the encoded saml response
     * @return the sessionIndex as a String
     * @throws UnsupportedEncodingException
     */
    public String getSessionIndex(String encodedSamlResponse)
            throws SessionIndexNotFoundException {

        String sessionIndex;

        try {
            sessionIndex = getSessionIndexDecoded(new String(decode(encodedSamlResponse)));
        } catch (UnsupportedEncodingException exception) {
            throw new SessionIndexNotFoundException(
                    String.format(
                            "An exception occurred while Base64-decoding the SAML response:\n%s",
                            encodedSamlResponse),
                    SessionIndexNotFoundException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception, new String[] { encodedSamlResponse });
        }

        return sessionIndex;
    }

    private String getSessionIndexDecoded(String samlResponse)
            throws SessionIndexNotFoundException {
        String sessionIndex;

        try {
            Document document = XMLConverter.convertToDocument(samlResponse,
                    true);

            sessionIndex = extractSessionIndex(document);
        } catch (XPathExpressionException | ParserConfigurationException
                | SAXException | IOException exception) {
            throw new SessionIndexNotFoundException(
                    String.format(
                            "An exception occurred while retrieving the sessionIndex from the saml response:\n%s",
                            samlResponse),
                    SessionIndexNotFoundException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception, new String[] { samlResponse });
        }

        if (sessionIndex == null || sessionIndex.trim().length() == 0) {
            throw new SessionIndexNotFoundException(
                    String.format(
                            "The sessionIndex attribute was not found for the saml response:\n%s",
                            samlResponse),
                    SessionIndexNotFoundException.ReasonEnum.SESSION_ATTRIBUTE_NOT_FOUND,
                    new String[] { samlResponse });

        }
        return sessionIndex;
    }

    private String getUserIdDecoded(String samlResponse)
            throws UserIdNotFoundException {
        String userid;

        try {
            Document document = XMLConverter.convertToDocument(samlResponse,
                    true);

            userid = extractUserId(document);
        } catch (XPathExpressionException | ParserConfigurationException
                | SAXException | IOException exception) {
            throw new UserIdNotFoundException(
                    String.format(
                            "An exception occurred while retrieving the userid from the saml response:\n%s",
                            samlResponse),
                    UserIdNotFoundException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception, new String[] { samlResponse });
        }

        if (userid == null || userid.trim().length() == 0) {
            throw new UserIdNotFoundException(
                    String.format(
                            "The userid attribute was not found for the saml response:\n%s",
                            samlResponse),
                    UserIdNotFoundException.ReasonEnum.USERID_ATTRIBUTE_NOT_FOUND,
                    new String[] { samlResponse });

        }
        return userid;
    }

    private String getTenantIdDecoded(String samlResponse) {
        String tenantID = null;

        try {
            Document document = XMLConverter.convertToDocument(samlResponse,
                    true);

            tenantID = extractTenantId(document);
        } catch (XPathExpressionException | ParserConfigurationException
                | SAXException | IOException exception) {
            //do nothing. Default tenant will be used.
        }
        return tenantID;
    }

    private String getIssuerIdDecoded(String samlResponse) throws NotExistentTenantException {
        String issuerID;

        try {
            Document document = XMLConverter.convertToDocument(samlResponse,
                    true);
            issuerID = extractIssuerId(document);
        } catch (XPathExpressionException | ParserConfigurationException
                | SAXException | IOException exception) {
            throw new NotExistentTenantException(MISSING_TEANT_ID_IN_SAML);
        }
        return issuerID;
    }

    String extractUserId(Document samlResponse) throws XPathExpressionException {
        String userId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML2_ATTRIBUTE_USERID_XPATH_EXPR);
        if (userId != null) {
            return userId;
        }

        userId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML1_ATTRIBUTE_USERID_XPATH_EXPR);
        if (userId != null) {
            return userId;
        }

        userId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML2_ATTRIBUTE_NAME_XPATH_EXPR);
        if (userId != null) {
            return userId;
        }

        userId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML1_ATTRIBUTE_NAME_XPATH_EXPR);
        return userId;
    }

    String extractTenantId(Document samlResponse)
            throws XPathExpressionException {

        String tenantId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML2_ATTRIBUTE_TENANTID_XPATH_EXPR);
        if (tenantId != null) {
            return tenantId;
        }

        tenantId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML1_ATTRIBUTE_TENANTID_XPATH_EXPR);
        return tenantId;
    }

    String extractIssuerId(Document samlResponse) throws XPathExpressionException {
        String userId = XMLConverter.getNodeTextContentByXPath(samlResponse,
                USER_SAML2_ATTRIBUTE_ISSUER_ID_XPATH_EXPR);
        return userId;
    }

    String extractSessionIndex(Document samlResponse)
            throws XPathExpressionException {
        String samlSessionId = XMLConverter.getNodeTextContentByXPath(
                samlResponse, SESSION_INDEX_SAML2_ATTRIBUTE_NAME_XPATH_EXPR);
        return samlSessionId;
    }

//    public String getUserId(SAMLAssertion samlResponse)
    public String getUserId(Object samlResponse)
            throws UserIdNotFoundException {
//        String samlAssertionString;
//        try {
//            Element samlAssertion = SAMLUtil.createSAMLAssertion(samlResponse
//                    .getSamlReader());
//            samlAssertionString = XMLConverter.convertToString(samlAssertion,
//                    false);
//        } catch (XWSSecurityException | XMLStreamException
//                | TransformerException exception) {
//            throw new UserIdNotFoundException(
//                    "An XML exception occurred while processing the SAML response",
//                    UserIdNotFoundException.ReasonEnum.EXCEPTION_OCCURRED,
//                    exception);
//        }
//        return getUserIdDecoded(samlAssertionString);
        return "";
    }
    
//    public String getTenantId(SAMLAssertion samlResponse)
    public String getTenantId(Object samlResponse)
            throws NotExistentTenantException {
//        String samlAssertionString;
//        try {
//            Element samlAssertion = SAMLUtil.createSAMLAssertion(samlResponse
//                    .getSamlReader());
//            samlAssertionString = XMLConverter.convertToString(samlAssertion,
//                    false);
//        } catch (XWSSecurityException | XMLStreamException
//                | TransformerException exception) {
//            throw new NotExistentTenantException(MISSING_TEANT_ID_IN_SAML);
//        }
//        return getTenantIdDecoded(samlAssertionString);
        return "";
    }

    public byte[] decode(String encodedString)
            throws UnsupportedEncodingException {
        return new Base64().decode(encodedString.getBytes("UTF-8"));
    }

    public boolean isFromLogout(String encodedSamlResponse) {
        byte[] decodedSamlResponse;
        String pureSamlResponse;
        try {
            decodedSamlResponse = decode(encodedSamlResponse);
            pureSamlResponse = new String(decodedSamlResponse);
        } catch (UnsupportedEncodingException e) {
            // TODO: log exception and create specific one
            throw new RuntimeException(e);
        }
        try {
            pureSamlResponse = inflate(decodedSamlResponse);
        } catch (IOException e) {
            // TODO: log exception as info
        }
        return pureSamlResponse.contains("LogoutResponse");
    }

    private String inflate(byte[] decodedBytes) throws IOException {
        ByteArrayOutputStream inflatedBytes = new ByteArrayOutputStream();
        Inflater inflater = new Inflater(true);
        InflaterOutputStream inflaterStream = new InflaterOutputStream(inflatedBytes, inflater);
        inflaterStream.write(decodedBytes);
        inflaterStream.finish();
        return new String(inflatedBytes.toByteArray());
    }

    public String getSAMLLogoutResponseStatusCode(String encodedSamlResponse) {
        Document document;
        try {
            document = XMLConverter.convertToDocument(inflate(decode(encodedSamlResponse)),
                    true);
            String resultWithNameSpace = XMLConverter.getNodeTextContentByXPath(document,
                    STATUS_LOGOUT_RESPONSE_SAML2_XPATH_EXPR);
            return removeNameSpaceFromStatus(resultWithNameSpace);
        } catch (ParserConfigurationException | SAXException | IOException |XPathExpressionException e) {
            // TODO: Add specific exception
            throw new RuntimeException(e);
        }
    }

    private String removeNameSpaceFromStatus(String statusWithNameSpace) {
        String[] splitted = statusWithNameSpace.split(":");
        String result = splitted[splitted.length - 1];
        return result;
    }

    public boolean isFromLogin(String samlResponse) {
        byte[] decodedSamlResponse;
        String pureSamlResponse;
        try {
            decodedSamlResponse = decode(samlResponse);
            pureSamlResponse = new String(decodedSamlResponse);
        } catch (UnsupportedEncodingException e) {
            // TODO: log exception and create specific one
            throw new RuntimeException(e);
        }
        try {
            pureSamlResponse = inflate(decodedSamlResponse);
        } catch (IOException e) {
            // TODO: log exception as info
        }
        return pureSamlResponse.contains("samlp:Response") && pureSamlResponse.contains("SessionIndex");

    }

    public String getIssuer(String samlResponse)
            throws NotExistentTenantException {
        String issuer;
        try {
            issuer = getIssuerIdDecoded(new String(decode(samlResponse)));
        } catch (UnsupportedEncodingException exception) {
            throw new NotExistentTenantException(MISSING_TEANT_ID_IN_SAML);
        }

        return issuer;
    }
}
