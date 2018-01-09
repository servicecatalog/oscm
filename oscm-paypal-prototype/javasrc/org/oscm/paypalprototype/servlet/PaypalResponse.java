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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The response of a paypal call, which will be parsed. Status indicates Failure
 * or Success.
 * 
 * @author afschar
 */
public class PaypalResponse {
    private String preapprovalKey;
    private String status;
    private String error;
    private final String originalResponse;

    /**
     * Parses the response for Status and Preapproval Key.
     * 
     * @param response
     *            The Response which is received in
     *            BaseServlet.sendPaypalRequest()
     * @throws ParserConfigurationException
     *             SAX may throw this
     * @throws SAXException
     *             SAX may throw this
     * @throws IOException
     *             SAX may throw this
     */
    public PaypalResponse(String response) throws ParserConfigurationException,
            SAXException, IOException {
        originalResponse = response;
        final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        final InputStream responseStream = new ByteArrayInputStream(
                response.getBytes());

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document responseDocument = builder.parse(responseStream);

        final Node documentElement = responseDocument.getDocumentElement();
        final NodeList childs = documentElement.getChildNodes();

        for (int i = 0; i < childs.getLength(); i++) {
            final Node child = childs.item(i);
            if (checkNodeName(child, "preapprovalKey")) {
                preapprovalKey = child.getTextContent().trim();
            } else if (checkNodeName(child, "responseEnvelope")) {
                final NodeList childs2 = child.getChildNodes();
                for (int i2 = 0; i2 < childs2.getLength(); i2++) {
                    final Node child2 = childs2.item(i2);
                    if (checkNodeName(child2, "ack")) {
                        status = child2.getTextContent().trim();
                    } else if (checkNodeName(child2, "message")) {
                        error = child2.getTextContent().trim();
                    }
                }
            }
        }
    }

    /**
     * The original response from Paypal
     * 
     * @return response as XML received from Paypal
     */
    public String getOriginalResponse() {
        return originalResponse;
    }

    /**
     * Preapproval key of paypal
     * 
     * @return a key like PA-28W60227S8113380R
     */
    public String getPreapprovalKey() {
        return preapprovalKey;
    }

    /**
     * Status can be Success or Failure. Here we check for Success
     * 
     * @return true if Paypal returns Success
     */
    public boolean isSuccess() {
        return "Success".equals(status);
    }

    /**
     * The error message of Paypal.
     * 
     * @return the text message in English.
     */
    public String getError() {
        return error;
    }

    /**
     * Check the name of a XML-node
     * 
     * @param node
     *            the XML-node
     * @param name
     *            a node name
     * @return true, if the node name or the local name of the node matches the
     *         given name
     */
    private boolean checkNodeName(Node node, String name) {
        if (node.getNodeName().equals(name)) {
            return true;
        }
        final String localName = node.getLocalName();
        return localName != null && localName.equals(name);
    }

}
