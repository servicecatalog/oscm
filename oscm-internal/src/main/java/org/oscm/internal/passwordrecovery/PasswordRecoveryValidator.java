/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import java.util.Date;

/**
 * @author Mao
 * 
 */
class PasswordRecoveryValidator {

    private static long INTERVAL = 300000;
    private static long EXPIRY = 86400000;

    /**
     * 
     * Returns if the other password recovery request date is valid
     * 
     * @param pwdRecoveryStartDate
     * @return <code>true</code> or <code>false</code>.
     */
    static boolean isValidInterval(long pwdRecoveryStartDate) {
        Date current = new Date();
        long currentDate = current.getTime();
        long minutes = currentDate - pwdRecoveryStartDate;
        return (minutes > INTERVAL);
    }

    /**
     * 
     * Returns if the password recovery request date is expired.
     * 
     * @param pwdRecoveryStartDate
     * @return <code>true</code> or <code>false</code>.
     */
    static boolean isExpired(long pwdRecoveryStartDate) {
        Date current = new Date();
        long currentDate = current.getTime();
        long hours = currentDate - pwdRecoveryStartDate;
        return (hours > EXPIRY);
    }
}
