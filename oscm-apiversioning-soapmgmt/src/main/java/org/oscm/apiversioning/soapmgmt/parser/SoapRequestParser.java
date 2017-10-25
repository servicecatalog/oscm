/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 13, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.soapmgmt.parser;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author zhaoh.fnst
 * 
 */
public class SoapRequestParser {

    private static final String VERSION_CM = "cm.service.version";
    private static final String VERSION_CTMG = "ctmg.service.version";

    public static String parseApiVersion(SOAPMessageContext context)
            throws SOAPException {

        SOAPMessage soapMessage = context.getMessage();

        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        if (soapHeader == null) {
            return "";
        }

        Iterator<?> it = null;
        Iterator<?> itCm = soapHeader.extractHeaderElements(VERSION_CM);
        if (itCm == null || !itCm.hasNext()) {
            Iterator<?> itCtmg = soapHeader.extractHeaderElements(VERSION_CTMG);
            if (itCtmg == null || !itCtmg.hasNext()) {
                return "";
            } else {
                it = itCtmg;
            }
        } else {
            it = itCm;
        }

        Node node = (Node) it.next();
        String value = node == null ? null : node.getValue();

        return value;
    }

    /**
     * The SOAP message has a soap envelope element and soap body element which
     * are mandatory. The soap header is optional. The soap envelope and body
     * use the soap envelope namespace which can vary from framework to
     * framework. By Glassfish Metro it is "S", but other framework may be "s".
     * The parser identifies the body element independent of the namespace. The
     * actual message which contains the necessary information for the method
     * call can have its own namespace e.g. the default "ns2" by Glassfish
     * Metro.
     * 
     * @param context
     * @return
     * @throws SOAPException
     */
    public static String parseOperationName(SOAPMessageContext context)
            throws SOAPException {
        String operationName = "";
        org.w3c.dom.Node operationNode = context.getMessage().getSOAPBody()
                .getFirstChild();
        if (operationNode != null) {
            operationName = operationNode.getLocalName();
        }
        return operationName;
    }

    public static String parseServiceName(SOAPMessageContext context) {
        String serviceName = "";
        QName serviceQName = (QName) context.get(MessageContext.WSDL_SERVICE);
        if (serviceQName != null) {
            serviceName = serviceQName.getLocalPart();
        }
        return serviceName;
    }

    public static SOAPBodyElement getServiceParam(SOAPMessageContext context,
            String serviceName, String paramName) throws SOAPException {

        SOAPMessage soapMessage = context.getMessage();
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        SOAPBody soapBody = soapEnvelope.getBody();

        @SuppressWarnings("unchecked")
        Iterator<SOAPBodyElement> elements = soapBody.getChildElements();

        while (elements.hasNext()) {
            SOAPBodyElement element = elements.next();

            if (element.getNodeName().contains(serviceName)) {

                @SuppressWarnings("unchecked")
                Iterator<SOAPBodyElement> params = element.getChildElements();

                while (params.hasNext()) {
                    SOAPBodyElement param = params.next();

                    if (paramName.equals(param.getNodeName())) {
                        return param;
                    }
                }
            }
        }

        throw new SOAPException("Soap message param " + paramName
                + " not found.");
    }

    public static SOAPBodyElement getChildNode(SOAPBodyElement element,
            String name) throws SOAPException {

        @SuppressWarnings("unchecked")
        Iterator<SOAPBodyElement> elements = element.getChildElements();

        while (elements.hasNext()) {
            SOAPBodyElement childNode = elements.next();

            if (childNode.getNodeName().contains(name)) {
                return childNode;
            }
        }

        throw new SOAPException("Child element: " + name + " not found.");
    }
}
