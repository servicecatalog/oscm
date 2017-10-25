/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 02.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.control;

import org.oscm.domobjects.Marketplace;

/**
 * @author weiser
 * 
 */
public class SendMailControl {

    private static final ThreadLocal<Boolean> SEND_MAIL = new ThreadLocal<Boolean>();
    private static final ThreadLocal<String> PASSWORD = new ThreadLocal<String>();
    private static final ThreadLocal<Marketplace> MARKETPLACE = new ThreadLocal<Marketplace>();

    private SendMailControl() {

    }

    public static void setSendMail(Boolean sendMail) {
        SEND_MAIL.set(sendMail);
    }

    public static boolean isSendMail() {
        Boolean b = SEND_MAIL.get();
        if (b != null) {
            return b.booleanValue();
        }
        return true;
    }

    public static void setMailData(String pwd, Marketplace mp) {
        PASSWORD.set(pwd);
        MARKETPLACE.set(mp);
    }

    public static String getPassword() {
        return PASSWORD.get();
    }

    public static Marketplace getMarketplace() {
        return MARKETPLACE.get();
    }

    public static void clear() {
        SEND_MAIL.remove();
        PASSWORD.remove();
        MARKETPLACE.remove();
    }

}
