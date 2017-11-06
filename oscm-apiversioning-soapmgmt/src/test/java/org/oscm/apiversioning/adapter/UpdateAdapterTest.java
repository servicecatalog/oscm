/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.apiversioning.adapter.base.SOAPMessageContextStub;
import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;
import org.oscm.apiversioning.upgrade.info.UpdateDetail;
import org.oscm.apiversioning.upgrade.info.VariableInfo;

/**
 * @author zhaoh.fnst
 * 
 */
public class UpdateAdapterTest {
    private SOAPMessageContextStub context;
    private SOAPMessage message;
    private UpdateAdapter adapter;

    private final String oldName = "oldName";
    private final String newName = "newName";

    @Before
    public void setUp() throws Exception {
        adapter = new UpdateAdapter();
        context = new SOAPMessageContextStub();
        message = mock(SOAPMessage.class);
        context.setMessage(message);
    }

    @SuppressWarnings("boxing")
    @Test
    public void exec() throws Exception {
        // given
        NodeList list = mock(NodeList.class);
        SOAPBody body = mock(SOAPBody.class);
        Node node = mock(Node.class);
        Document doc = mock(Document.class);

        Element ele = mock(Element.class);

        doReturn(ele).when(doc).createElement(eq(newName));
        doReturn(doc).when(node).getOwnerDocument();
        doReturn(null).when(list).item(0);
        doReturn(1).when(list).getLength();
        doReturn(list).when(body).getElementsByTagName(eq(oldName));
        doReturn(body).when(message).getSOAPBody();

        // when
        UpdateDetail detail = new UpdateDetail(ModificationType.ADD,
                ModificationPart.PARAMETER, new VariableInfo("", newName),
                new VariableInfo("", oldName), false, "", "", true);

        // then
        adapter.exec(context, detail);

    }
}
