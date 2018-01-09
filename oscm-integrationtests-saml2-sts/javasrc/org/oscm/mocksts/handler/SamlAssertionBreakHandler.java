/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 08.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.mocksts.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author gao
 * 
 */
public class SamlAssertionBreakHandler implements
        SOAPHandler<SOAPMessageContext> {
    
    private final Map<String, String> userAndTagMap = new HashMap<String, String>();
       
    public SamlAssertionBreakHandler() {
        userAndTagMap.put("MockSTSTest_Issuer", "saml2:Issuer");
        userAndTagMap.put("MockSTSTest_DigestValue", "ds:DigestValue");
        userAndTagMap.put("MockSTSTest_SignatureValue", "ds:SignatureValue");
        userAndTagMap.put("MockSTSTest_X509Certificate", "ds:X509Certificate");
        userAndTagMap.put("MockSTSTest_NameID", "saml2:NameID");
        userAndTagMap.put("MockSTSTest_KeyIdentifier", "wsse:KeyIdentifier");
        userAndTagMap.put("MockSTSTest_CipherValue", "xenc:CipherValue");
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        try {
            modifyAssertion(context);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
        return null;
    }

    private void modifyAssertion(SOAPMessageContext context) throws Exception {
        
        if (isOutboundMessage(context).booleanValue()) {
            Document messageDoc = context.getMessage().getSOAPBody()
                    .getOwnerDocument();
            String tagName = getModifyTagName(messageDoc);

            if (tagName != null && tagName.length() != 0) {
                NodeList nodes = messageDoc.getElementsByTagName(tagName);
                if (nodes.getLength() == 0) {
                    throw new Exception("Can not modify SAML assertion.");
                }
                Node node = nodes.item(0);
                removeLastCharacter(node);
            }
            
        }
    }

    private String getModifyTagName(Document messageDoc) throws Exception {
        String userId = getUserId(messageDoc);
        return userAndTagMap.get(userId);
    }

    private String getUserId(Document messageDoc) throws Exception {
        NodeList nodes = messageDoc.getElementsByTagName("saml2:NameID");
        if (nodes.getLength() == 0) {
            throw new Exception("There is no userId");
        }
        return nodes.item(0).getTextContent();
    }

    private void removeLastCharacter(Node node) {
        String value = node.getTextContent();
        if (value.length() > 0) {
            String textContent = value.substring(0, (value.length() - 1));
            node.setTextContent(textContent);
        }
    }
    
    protected Boolean isOutboundMessage(SOAPMessageContext context) {
        Boolean outBoundProperty = (Boolean) context
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return outBoundProperty;
    }

}
