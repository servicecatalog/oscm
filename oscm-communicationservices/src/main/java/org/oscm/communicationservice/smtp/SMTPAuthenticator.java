/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.communicationservice.smtp;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Authenticator class for smtp connections of the communication service.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SMTPAuthenticator extends Authenticator {

    private final String userName;
    private final String password;

    private SMTPAuthenticator(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Creates a new Authenticator instance in case the userName and password
     * parameters are set accordingly.
     * 
     * @param userName
     *            The username to be used for authenication against the smtp
     *            system.
     * @param password
     *            The password to be used for authentication against the smtp
     *            system.
     * @return An instance of the SMTPAuthenticator, <code>null</code> if
     *         userName or password are not set.
     */
    public static Authenticator getInstance(String userName, String password) {
        if (userName != null && userName.length() > 0 && password != null) {
            return new SMTPAuthenticator(userName, password);
        }
        return null;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }

}
