/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import java.util.List;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.apiversioning.enums.ModificationPart;
import org.oscm.apiversioning.enums.ModificationType;
import org.oscm.apiversioning.upgrade.info.FieldInfo;
import org.oscm.apiversioning.upgrade.info.ModificationDetail;
import org.oscm.apiversioning.upgrade.info.UpdateFieldDetail;

/**
 * @author qiu
 * 
 */
public class UpdateFieldAdapter implements IAdapter {

    private static final String PREFIX = "ns2:";

    @Override
    public void exec(SOAPMessageContext context, ModificationDetail detail)
            throws SOAPException {
        UpdateFieldDetail upd_detail = (UpdateFieldDetail) detail;

        if (!detail.getType().equals(ModificationType.UPDATEFIELD)) {
            return;
        }
        if (detail.getPart().equals(ModificationPart.EXCEPTION)
                || detail.getPart().equals(ModificationPart.METHOD)) {
            return;
        }

        if (detail.getPart().equals(ModificationPart.RETURNVALUE)) {
            updateForResponse(context, upd_detail);
        }

        if (detail.getPart().equals(ModificationPart.PARAMETER)) {
            updateForRequest(context, upd_detail);
        }
    }

    private void updateForResponse(SOAPMessageContext context,
            UpdateFieldDetail upd_detail) throws SOAPException {
        List<FieldInfo> fields = upd_detail.getFields();
        updateNode(context, fields, "", true);
    }

    private void updateForRequest(SOAPMessageContext context,
            UpdateFieldDetail upd_detail) throws SOAPException {
        String variableName = upd_detail.getVariable().getVariableName();
        List<FieldInfo> fields = upd_detail.getFields();
        updateNode(context, fields, variableName, false);
    }

    private void updateNode(SOAPMessageContext context, List<FieldInfo> fields,
            String variableName, boolean isResponse) throws SOAPException {

        Node variableNode;
        if (isResponse) {
            variableNode = context.getMessage().getSOAPBody().getFirstChild()
                    .getFirstChild();
        } else {
            variableNode = context.getMessage().getSOAPBody()
                    .getElementsByTagName(variableName).item(0);
        }

        NodeList fieldsList = variableNode.getChildNodes();
        int fieldsSize = fieldsList.getLength();

        for (int i = 0; i < fieldsSize; i++) {
            Node fieldNode = fieldsList.item(i);
            for (FieldInfo field : fields) {
                String oldNodeName = "";
                String updatedNodeName = "";
                if (isResponse) {
                    oldNodeName = PREFIX
                            + field.getNewField().getVariableName();
                    updatedNodeName = PREFIX
                            + field.getOldField().getVariableName();
                } else {
                    oldNodeName = PREFIX
                            + field.getOldField().getVariableName();
                    updatedNodeName = PREFIX
                            + field.getNewField().getVariableName();

                }
                if (fieldNode.getNodeName().equals(oldNodeName)) {
                    Element newNode = variableNode
                            .getOwnerDocument()
                            .createElementNS(
                                    variableNode.getFirstChild()
                                            .getNamespaceURI(), updatedNodeName);
                    newNode.setTextContent(fieldNode.getTextContent());
                    variableNode.replaceChild(newNode, fieldNode);
                }
            }
        }
    }
}
