/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.soapmgmt.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.junit.Before;
import org.junit.Test;
import org.oscm.apiversioning.adapter.base.SOAPMessageContextStub;

/**
 * @author zhaoh.fnst
 * 
 */
public class SoapRequestParserTest {
    private SOAPMessageContextStub context;
    private SOAPMessage message;

    @Before
    public void setUp() throws Exception {
        context = new SOAPMessageContextStub();
        message = mock(SOAPMessage.class);
        context.setMessage(message);
    }

    @Test
    public void parseApiVersion_empty() throws Exception {
        // given
        SOAPPart part = mock(SOAPPart.class);
        SOAPEnvelope envelope = mock(SOAPEnvelope.class);
        doReturn(envelope).when(part).getEnvelope();
        doReturn(part).when(message).getSOAPPart();
        // when
        String result = SoapRequestParser.parseApiVersion(context);

        // then
        assertEquals("", result);
    }

    @Test
    public void parseApiVersion_cm_1_8() throws Exception {
        // given
        SOAPPart part = mock(SOAPPart.class);
        SOAPEnvelope envelope = mock(SOAPEnvelope.class);
        SOAPHeader soapHeader = mock(SOAPHeader.class);
        List<Node> version = new ArrayList<Node>();
        Node node = mock(Node.class);
        doReturn("testVersion").when(node).getValue();
        version.add(node);
        Iterator<?> it = version.iterator();
        doReturn(it).when(soapHeader).extractHeaderElements(
                eq("cm.service.version"));
        doReturn(soapHeader).when(envelope).getHeader();
        doReturn(envelope).when(part).getEnvelope();
        doReturn(part).when(message).getSOAPPart();
        // when
        String result = SoapRequestParser.parseApiVersion(context);

        // then
        assertEquals("testVersion", result);
    }

    @Test
    public void parseApiVersion_ctmg_1_8() throws Exception {
        // given
        SOAPPart part = mock(SOAPPart.class);
        SOAPEnvelope envelope = mock(SOAPEnvelope.class);
        SOAPHeader soapHeader = mock(SOAPHeader.class);
        List<Node> version = new ArrayList<Node>();
        Node node = mock(Node.class);
        doReturn("testVersion").when(node).getValue();
        version.add(node);
        Iterator<?> it = version.iterator();
        doReturn(it).when(soapHeader).extractHeaderElements(
                eq("ctmg.service.version"));
        doReturn(soapHeader).when(envelope).getHeader();
        doReturn(envelope).when(part).getEnvelope();
        doReturn(part).when(message).getSOAPPart();
        // when
        String result = SoapRequestParser.parseApiVersion(context);

        // then
        assertEquals("testVersion", result);
    }

}
