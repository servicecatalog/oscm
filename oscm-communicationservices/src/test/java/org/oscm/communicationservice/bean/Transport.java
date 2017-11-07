/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: 09.01.2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.communicationservice.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * @author afschar
 * 
 */
public class Transport extends javax.mail.Transport {
    String lastMail;

    Transport() {
        super(Session.getDefaultInstance(System.getProperties()), null);
    }

    @Override
    public void sendMessage(Message msg, Address[] addresses)
            throws MessagingException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            msg.writeTo(out);
            lastMail = new String(out.toByteArray(), "UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
