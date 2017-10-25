/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.upgrade.info.ModificationDetail;
import org.oscm.apiversioning.upgrade.info.UpdateDetail;

/**
 * @author qiu
 * 
 */
public class UpdateAdapter implements IAdapter {

    private static final String METHODPREFIX = "ns2:";
    private static final String RESPONSESUFFIX = "Response";

    @Override
    public void exec(SOAPMessageContext soapMessageContext,
            ModificationDetail detail) throws SOAPException {
        if (detail instanceof UpdateDetail) {
            UpdateDetail upd_detail = (UpdateDetail) detail;
            if (ModificationPart.PARAMETER.equals(upd_detail.getPart())) {
                renameParameter(soapMessageContext, upd_detail.getOldVariable()
                        .getVariableName(), upd_detail.getVariable()
                        .getVariableName());
            } else if (ModificationPart.METHOD.equals(upd_detail.getPart())) {
                if (upd_detail.isRequest()) {
                    renameMethod(soapMessageContext,
                            METHODPREFIX + upd_detail.getOldMethodName(),
                            METHODPREFIX + upd_detail.getNewMethodName());
                } else {
                    renameMethod(soapMessageContext,
                            METHODPREFIX + upd_detail.getOldMethodName()
                                    + RESPONSESUFFIX,
                            METHODPREFIX + upd_detail.getNewMethodName()
                                    + RESPONSESUFFIX);
                }
            }
        }
    }

    private void renameParameter(SOAPMessageContext context, String oldName,
            String newName) throws SOAPException {
        NodeList nodelist = context.getMessage().getSOAPBody()
                .getElementsByTagName(oldName);
        Node node;
        if (null != nodelist) {
            int size = nodelist.getLength();
            for (int j = 0; j < size; j++) {
                node = nodelist.item(0);
                if (null != node) {
                    Element newNode = node.getOwnerDocument().createElement(
                            newName);
                    NodeList list = node.getChildNodes();
                    int childrenSize = list.getLength();
                    for (int i = 0; i < childrenSize; i++) {
                        newNode.appendChild(list.item(0));
                    }
                    node.getParentNode().replaceChild(newNode, node);
                }
            }
        }
    }

    private void renameMethod(SOAPMessageContext context, String oldName,
            String newName) throws SOAPException {
        Node node = context.getMessage().getSOAPBody()
                .getElementsByTagName(oldName).item(0);
        Element newNode = node.getOwnerDocument().createElementNS(
                node.getNamespaceURI(), newName);

        NodeList list = node.getChildNodes();
        int childrenSize = list.getLength();
        for (int j = 0; j < childrenSize; j++) {
            Node n = list.item(0);
            newNode.appendChild(n);
        }
        node.getParentNode().replaceChild(newNode, node);
    }
}
