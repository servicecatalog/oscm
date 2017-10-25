/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 29.04.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.data;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;

/**
 * Presents the response information contained in a heidelpay response xml
 * structure.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class HeidelpayResponse {

    private String processingResult;
    private String processingReturnCode;

    /**
     * Reads the result document and identifies the result related information.
     * The object will be initialized with them.
     * 
     * @param response
     *            The PSP response.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public HeidelpayResponse(String response)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        Document responseDoc = XMLConverter.convertToDocument(response, true);
        this.processingResult = XMLConverter.getNodeTextContentByXPath(
                responseDoc, "/Response/Transaction/Processing/Result");
        this.processingReturnCode = XMLConverter.getNodeTextContentByXPath(
                responseDoc, "/Response/Transaction/Processing/Return");
    }

    public String getProcessingResult() {
        return processingResult;
    }

    public String getProcessingReturnCode() {
        return processingReturnCode;
    }

}
