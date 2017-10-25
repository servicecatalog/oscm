/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                    
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 30, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.EmailType;

/**
 * Payload data object for send mail task handling.
 * 
 * @author tokoda
 * 
 */
public class SendMailPayload implements TaskPayload {

    private static final long serialVersionUID = -7301740970588294388L;

    List<MailDefiniton> mailObjects = new ArrayList<MailDefiniton>();

    public SendMailPayload() {
    }

    public SendMailPayload(long userKey, EmailType type, Object[] params,
            Long marketplaceKey) {
        addMailObjectForUser(userKey, type, params, marketplaceKey);
    }

    public void addMailObjectForUser(long userKey, EmailType type,
            Object[] params, Long marketplaceKey) {
        MailDefiniton mailParams = new MailDefiniton(Long.valueOf(userKey),
                type, params, marketplaceKey);
        mailObjects.add(mailParams);
    }

    public List<MailDefiniton> getMailObjects() {
        return mailObjects;
    }

    @Override
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        for (MailDefiniton mailObject : mailObjects) {
            sb.append(mailObject.getInfo() + "    ");
        }
        return sb.toString();
    }
}
