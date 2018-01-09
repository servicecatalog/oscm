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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.xml.sax.SAXException;

/**
 * This servlet has some convenience methods for the derived servlets to display
 * XML text inside of a HTML page and send the actual Paypal request.
 * 
 * @author afschar
 * 
 */
abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private HttpClient client = new HttpClient();

    /**
     * Do get is not supported, will raise an runtime exception.
     * 
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected final void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        throw new RuntimeException("Nix da!");
    }

    /**
     * Sets paypalRequest, paypalResponse, backgroundResponse, and error for use
     * in JSP pages
     * 
     * @param request
     *            the attributes are set in this request
     * @param paypalRequest
     *            the actual Paypal request
     * @param paypalResponse
     *            the actual Paypal response
     */
    protected void setDefaultAttributes(HttpServletRequest request,
            PaypalRequest paypalRequest, PaypalResponse paypalResponse) {
        request.setAttribute("paypalRequest",
                formatXML(paypalRequest.getLastRequest()));
        request.setAttribute("paypalResponse",
                formatXML(paypalResponse.getOriginalResponse()));
        request.setAttribute("backgroundResponse",
                paypalResponse.isSuccess() ? "aaffaa" : "ffaaaa");
        request.setAttribute("error", paypalResponse.getError());
    }

    /**
     * Sends the request to Paypal.
     * 
     * @param paypalRequest
     *            the request
     * @return the Paypal response
     * @throws IOException
     *             thrown by PaypalResponse
     * @throws HttpException
     *             thrown by HttpClient
     * @throws SAXException
     *             thrown by PaypalResponse
     * @throws ParserConfigurationException
     *             thrown by PaypalResponse
     */
    protected PaypalResponse sendPaypalRequest(PostMethod paypalRequest)
            throws HttpException, IOException, ParserConfigurationException,
            SAXException {
        // client.getHostConfiguration().setProxy("192.168.210.82", 8080);
        client.executeMethod(paypalRequest);
        return new PaypalResponse(paypalRequest.getResponseBodyAsString());
    }

    /**
     * Format some XML to more or less nice XML in HTML
     * 
     * @param unformattedXml
     *            pure XML
     * @return HTML which looks like XML with some color marking and simple line
     *         formatting
     */
    protected String formatXML(String unformattedXml) {
        final StringBuilder s = new StringBuilder(unformattedXml);
        replace(s, "<", "&lt;");
        replace(s, ">", "></div>");
        replace(s, "&lt;", "<div style=\"color:#777;\">&lt;");
        return s.toString();
    }

    /**
     * Replace the value in the string.
     * 
     * @param s
     *            the source
     * @param from
     *            from token
     * @param to
     *            to token
     */
    private void replace(StringBuilder s, String from, String to) {
        int i = 0;
        while ((i = s.indexOf(from, i)) > -1) {
            s.delete(i, i + from.length());
            s.insert(i, to);
            i += to.length();
        }
    }

}
