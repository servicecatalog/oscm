/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 2, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;
import org.oscm.apiversioning.upgrade.info.ExceptionDetail;
import org.oscm.apiversioning.upgrade.info.ModificationDetail;

/**
 * @author zhaoh.fnst
 * 
 */
public class ExceptionAdapter implements IAdapter {

    private static final String PREFIX = "ns2:";
    private static final String MESSAGEKEYTAG = "messageKey";

    @Override
    public void exec(SOAPMessageContext context, ModificationDetail detail)
            throws SOAPException {
        if (detail instanceof ExceptionDetail) {
            ExceptionDetail upd_detail = (ExceptionDetail) detail;
            if (ModificationPart.EXCEPTION.equals(upd_detail.getPart())
                    && upd_detail.getType().equals(
                            ModificationType.ADDEXCEPTION)) {
                convertException(context,
                        ((ExceptionDetail) detail).getNewExceptionName());
            }
        }
    }

    private void convertException(SOAPMessageContext context,
            String newExceptionName) throws SOAPException {
        String saaSApplicationExceptionName = getSaaSApplicationExceptionName();
        NodeList nodeList = context.getMessage().getSOAPBody()
                .getElementsByTagName(PREFIX + newExceptionName);
        if (null == nodeList) {
            return;
        }
        Node node = nodeList.item(0);
        Element newNode = node.getOwnerDocument().createElementNS(
                node.getNamespaceURI(), PREFIX + saaSApplicationExceptionName);

        NodeList list = node.getChildNodes();
        if (null != list) {
            int childrenSize = list.getLength();
            for (int i = 0; i < childrenSize; i++) {
                Node child = list.item(0);
                if (child.getNodeName().equals(PREFIX + MESSAGEKEYTAG)) {
                    String content = child.getTextContent().replaceAll(
                            newExceptionName, saaSApplicationExceptionName);
                    child.setTextContent(content);
                }
                newNode.appendChild(child);
            }
            node.getParentNode().replaceChild(newNode, node);
        }
    }

    private String getSaaSApplicationExceptionName() {
        String exception = org.oscm.types.exceptions.SaaSApplicationException.class
                .getName();
        String exceptionName = exception
                .substring(exception.lastIndexOf(".") + 1);
        return exceptionName;
    }
}
