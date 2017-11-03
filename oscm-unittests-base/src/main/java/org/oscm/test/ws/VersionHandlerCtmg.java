/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 13, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ws;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This class is an implementation of a SOAP Handler responsible for adding
 * version information in the header of the outbound SOAP message. The version
 * information is read from a property file.
 */
public class VersionHandlerCtmg implements SOAPHandler<SOAPMessageContext> {

    private static final String VERSION = "ctmg.service.version";

    private static String message = null;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VersionHandlerCtmg(String version) {
        super();
        this.version = version;
    }

    /**
     * The method is invoked for normal processing of outbound messages.
     * 
     * @param context
     *            the message context.
     * @return An indication of whether handler processing should continue for
     *         the current message. Return <code>true</code> to continue
     *         processing.
     * 
     * @throws Exception
     *             Causes the JAX-WS runtime to cease fault message processing.
     **/
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean request_p = (Boolean) context
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (request_p.booleanValue()) {
            try {
                SOAPMessage msg = context.getMessage();
                SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
                SOAPHeader hdr = env.getHeader();
                if (hdr == null) {
                    hdr = env.addHeader();
                }
                QName qname_user = new QName("http://com/auth/", "auth");
                SOAPHeaderElement helem_user = hdr.addHeaderElement(qname_user);
                helem_user.setActor(VERSION);
                if (version != null && version.trim().length() != 0) {
                    helem_user.addTextNode(version);
                }

                msg.saveChanges();
                message = soapMessage2String(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * The method is invoked for fault message processing.
     * 
     * @param context
     *            the message context
     * @return An indication of whether handler fault processing should continue
     *         for the current message. Return <code>false</code> to block
     *         processing.
     * 
     **/
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    /**
     * Called at the conclusion of a message exchange pattern.
     * 
     * @param context
     *            the message context
     **/
    @Override
    public void close(MessageContext context) {
    }

    /**
     * Gets the header blocks
     * 
     * @return Set of <code>QNames</code> of header blocks.Return
     *         <code>null</code> Don't return any header block.
     **/
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    /**
     * Gets the SOAP message pretty printed as escaped xml for displaying in
     * browser
     * 
     * @param msg
     * @return
     */
    private String soapMessage2String(SOAPMessage msg) {
        if (msg == null)
            return "";

        try (ByteArrayOutputStream streamOut = new ByteArrayOutputStream();) {
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            Source sc = msg.getSOAPPart().getContent();
            StreamResult result = new StreamResult(streamOut);
            transformer.transform(sc, result);

            String strMessage = streamOut.toString();
            return escapeXmlString(strMessage);
        } catch (Exception e) {
            System.out.println("Exception in printing SOAP message: "
                    + e.getMessage());
            return "";
        }

    }

    private String escapeXmlString(String text) {
        if (text == null)
            return "";
        String message = "";
        for (int i = 0; i < text.length(); i++) {
            switch (text.charAt(i)) {
            case '<':
                message += "&lt;";
                break;
            case '>':
                message += "&gt;";
                break;
            case ' ':
                message += "&nbsp;";
                break;
            case '\n':
                message += "</br>";
                break;
            default:
                message += text.charAt(i);
                break;
            }

        }
        return message;
    }

    public static String getMessage() {
        return message;
    }

}
