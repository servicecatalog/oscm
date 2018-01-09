/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: afschar
 *
 *  Creation Date: Jul 27, 2011
 *                                                                              
 *  Completion Time: Jul 28, 2011
 *
 *******************************************************************************/

package org.oscm.paypalprototype.servlet;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Paypal request builder, supports PayRequest or Preapprovaal Request at the
 * moment.
 * 
 * @author afschar
 * 
 */
public class PaypalRequest {
    private static final String CALLBACK_URL = "http://79.125.22.217/oscm-paypal-prototype/";
    private static final String PAYPAL_URL = "https://svcs.sandbox.paypal.com/AdaptivePayments";
    private String lastRequest;
    private String remoteIpAddr = null;

    /**
     * Constructor with the IP from the client, must be set for the Paypal
     * request (I guess some sort of security features).
     * 
     * @param remoteIpAddr
     *            a valid IP address of the users browser location, will
     *            probably be checked by Paypal
     */
    public PaypalRequest(String remoteIpAddr) {
        this.remoteIpAddr = remoteIpAddr;
    }

    /**
     * Creates a pay request from the buyer (who is indirectly referenced by the
     * preapproval key) to the seller/receiver.
     * 
     * @param preapprovalKey
     *            The key from a former preapproval request
     * @param amount
     *            amount to be transfered in Euro
     * @return
     * @throws Exception
     */
    public PostMethod buildPayRequest(String preapprovalKey, String receiver,
            String amount) throws Exception {
        final Document doc = createBody("PayRequest");
        final Element payReq = doc.getDocumentElement();

        addElement(payReq, "actionType", "PAY");
        addElement(payReq, "preapprovalKey", preapprovalKey);
        addElement(payReq, "senderEmail",
                "buyer_1310720672_per@est.fujitsu.com");

        final Element receiverList = addElement(payReq, "receiverList", null);
        final Element receiverElement = addElement(receiverList, "receiver",
                null);

        addElement(receiverElement, "amount", amount);
        addElement(receiverElement, "email", receiver);
        return createPostMethod(doc, "Pay");
    }

    /**
     * convenience method to add one element to a parent, makes code more
     * readable
     * 
     * @param parent
     *            parent to which the new node is added, mandatory
     * @param name
     *            name of the new node, mandatory
     * @param value
     *            value of the new node, may be null
     * @return the new element
     */
    private Element addElement(Element parent, String name, String value) {
        final Element e = parent.getOwnerDocument().createElementNS("", name);
        if (value != null && value.trim().length() > 0) {
            e.setTextContent(value);
        }
        parent.appendChild(e);
        return e;
    }

    /**
     * A Paypal preapproval request.
     * 
     * @return PostMethod with all necessary info set, ready to be sent to
     *         Paypal
     * @throws ParserConfigurationException
     *             thrown by createBody
     * @throws DOMException
     *             thrown by createBody
     * @throws TransformerException
     *             thrown by createPostMethod
     */
    public PostMethod buildPreapprovalRequest() throws DOMException,
            ParserConfigurationException, TransformerException {
        final Document doc = createBody("PreapprovalRequest");
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'Z'");
        addElement(doc.getDocumentElement(), "startingDate",
                sdf.format(new Date()));
        return createPostMethod(doc, "Preapproval");
    }

    /**
     * A Paypal cancel preapproval request.
     * 
     * @return PostMethod with all necessary info set, ready to be sent to
     *         Paypal
     * @param preapprovalKey
     *            The key for which the cancellation will be performed
     * @throws ParserConfigurationException
     *             thrown by createBody
     * @throws DOMException
     *             thrown by createBody
     * @throws TransformerException
     *             thrown by createPostMethod
     */
    public PostMethod buildCancelPreapprovalRequest(String preapprovalKey)
            throws DOMException, ParserConfigurationException,
            TransformerException {
        final Document doc = createBody("CancelPreapprovalRequest");
        addElement(doc.getDocumentElement(), "preapprovalKey", preapprovalKey);
        return createPostMethod(doc, "CancelPreapproval");
    }

    /**
     * 
     * @param doc
     *            Document with body info
     * @param route
     *            Pay or Preapproval
     * @return the PostMethod to be invoked
     * @throws TransformerException
     *             on bad xml input or transformer exceptions
     */
    private PostMethod createPostMethod(Document doc, String route)
            throws TransformerException {
        final PostMethod postMethod = new PostMethod(PAYPAL_URL + '/' + route);

        // communication format
        postMethod.addRequestHeader("X-PAYPAL-REQUEST-DATA-FORMAT", "XML");
        postMethod.addRequestHeader("X-PAYPAL-RESPONSE-DATA-FORMAT", "XML");

        // authentication info
        postMethod.addRequestHeader("X-PAYPAL-SECURITY-USERID",
                "mercha_1310720134_biz_api1.est.fujitsu.com");
        postMethod.addRequestHeader("X-PAYPAL-SECURITY-PASSWORD", "1310720175");
        postMethod.addRequestHeader("X-PAYPAL-SECURITY-SIGNATURE",
                "AlTG0c2puvFWih-1mR5Tn9-Pbx6MAyndXBaCr0Cmgec8UBYC7Kty76vJ");
        postMethod.addRequestHeader("X-PAYPAL-APPLICATION-ID",
                "APP-80W284485P519543T");
        postMethod.addRequestHeader("X-PAYPAL-DEVICE-IPADDRESS", remoteIpAddr);
        try {
            postMethod.setRequestEntity(new StringRequestEntity(
                    getDocAsString(doc), "text/xml", "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 is always supported, so this exception should never been
            // thrown
            throw new RuntimeException(ex);
        }
        return postMethod;
    }

    /**
     * 
     * @param type
     *            PreapprovalRequest or PayRequest
     * @return Document with basic info set
     * @throws DOMException
     *             on xml exceptions
     * @throws ParserConfigurationException
     *             on xml parsing exceptions
     * 
     */
    private Document createBody(String type) throws DOMException,
            ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                .newInstance();
        final DocumentBuilder db = dbFactory.newDocumentBuilder();
        final Document doc = db.newDocument();

        final Element root = doc.createElementNS(
                "http://svcs.paypal.com/types/ap", type);
        doc.appendChild(root);

        final Element reqEnv = addElement(root, "requestEnvelope", null);
        addElement(reqEnv, "errorLanguage", "en_US");

        addElement(root, "cancelUrl", CALLBACK_URL + "cancel.jsp");
        addElement(root, "returnUrl", CALLBACK_URL + "success.jsp");
        addElement(root, "currencyCode", "EUR");
        addElement(root, "clientDetails", null);
        return doc;
    }

    /**
     * Used for debugging info
     * 
     * @return String representation of the XML sent to Paypal
     */
    public String getLastRequest() {
        return lastRequest;
    }

    /**
     * Converts a given document into its string representation.
     * 
     * @param doc
     *            The document to be converted.
     * @param xmlDecl
     *            should the output contain a XML declaration?
     * @return The string representation of the document.
     * @throws TransformerException
     *             Thrown in case the conversion fails.
     */
    private String getDocAsString(final Node doc) throws TransformerException {
        final DOMSource docSource = new DOMSource(doc);
        final TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        final StringWriter buffer = new StringWriter();
        transformer.transform(docSource, new StreamResult(buffer));
        lastRequest = buffer.toString();
        return lastRequest;
    }

}
