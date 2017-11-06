/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.junit.Before;
import org.junit.Test;

import org.oscm.apiversioning.adapter.base.SOAPMessageContextStub;
import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;
import org.oscm.apiversioning.upgrade.info.ExceptionDetail;
import org.oscm.apiversioning.upgrade.info.VariableInfo;

/**
 * @author zhaoh.fnst
 * 
 */
public class ExceptionAdapterTest {
    private ExceptionAdapter adapter;

    private SOAPMessageContextStub context;
    private SOAPMessage message;

    @Before
    public void setUp() throws Exception {
        adapter = new ExceptionAdapter();
        context = new SOAPMessageContextStub();
        message = mock(SOAPMessage.class);
        context.setMessage(message);
    }

    @Test(expected = SOAPException.class)
    public void exec_SOAPException() throws Exception {
        // given
        doThrow(new SOAPException()).when(message).getSOAPBody();
        // when
        adapter.exec(context, new ExceptionDetail(
                ModificationType.ADDEXCEPTION, ModificationPart.EXCEPTION,
                new VariableInfo("", ""), "newExceptionName"));
    }

    @Test
    public void exec_null() throws Exception {
        // given
        SOAPBody body = mock(SOAPBody.class);
        doReturn(null).when(body).getElementsByTagName("ns2:newExceptionName");
        doReturn(body).when(message).getSOAPBody();
        // when
        adapter.exec(context, new ExceptionDetail(
                ModificationType.ADDEXCEPTION, ModificationPart.EXCEPTION,
                new VariableInfo("", ""), "newExceptionName"));
    }
}
