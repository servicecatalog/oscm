/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.mocksts.handler;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oscm.converter.DateConverter;
import org.oscm.converter.XMLConverter;

/**
 * @author gao
 * 
 */
@WebService(endpointInterface = "org.oscm.mocksts.handler.STSSoapHandlerService", portName = "STSSoapHandlerServiceImplPort", serviceName = "STSSoapHandlerService", targetNamespace = "http://oscm.org/xsd")
public class STSSoapHandlerServiceImpl implements
        SOAPHandler<SOAPMessageContext>, STSSoapHandlerService {

    private static final List<String> messageList = new CopyOnWriteArrayList<String>();

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        addMessage(context);
        return true;
    }

    private synchronized void addMessage(final SOAPMessageContext context) {
        final SOAPMessage msg = context.getMessage();
        final String msgText = soapMessage2String(msg);
        final String dateTime = DateConverter
                .convertLongToIso8601DateTimeFormat(System.currentTimeMillis(),
                        TimeZone.getTimeZone("GMT"));
        messageList.add(dateTime + "\n" + msgText);
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    @WebMethod
    public List<String> getCollectedMessages() {
        return messageList;
    }

    /**
     * convert SOAPMessage object to String
     */
    private String soapMessage2String(SOAPMessage msg) {
        if (msg == null)
            return "";
        try {
            return XMLConverter.convertToString(msg.getSOAPBody()
                    .getOwnerDocument(), false);
        } catch (TransformerException | SOAPException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod
    public void initMessageList() {
        messageList.clear();
    }
}
