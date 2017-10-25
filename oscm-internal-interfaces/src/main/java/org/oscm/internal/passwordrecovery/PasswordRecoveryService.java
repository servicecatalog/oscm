/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import javax.ejb.Remote;

/**
 * Service providing the functionality for user to recover password
 * 
 * @author Mao
 * 
 */
@Remote
public interface PasswordRecoveryService {
    /**
     * Send mail to user to help him recover password
     * 
     * @param userId
     *            Id of a user
     * @param marketplaceId
     *            Id of current marketplace, can be null
     */
    public void startPasswordRecovery(String userId, String marketplaceId);

    /**
     * Verify if the password recovery link is valid or not. If valid return
     * <code>userId</code> of user
     * 
     * @param recoveryPasswordLink
     *            <code>recoveryPassworLink</code> is an encoded token sent to
     *            user for password recovery which contains userId,
     *            passwordRecoveryStartDate and marketplaceId
     * @param marketplaceId
     *            the marketplaceId need to be matched
     * @return String
     */
    public String confirmPasswordRecoveryLink(String recoveryPasswordLink,
            String marketplaceId);

    /**
     * Change user's password.
     * 
     * If successful return true, else return false.
     * 
     * @param userId
     *            <code>userId</code> Id of user
     * 
     * @param newPassword
     *            <code>newPassword</code> specifying the new password
     * 
     * @return boolean
     */
    public boolean completePasswordRecovery(String userId, String newPassword);

}
