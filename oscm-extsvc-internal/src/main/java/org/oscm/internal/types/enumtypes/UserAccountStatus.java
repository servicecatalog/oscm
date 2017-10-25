/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the statuses a user account can take on.
 * 
 */
public enum UserAccountStatus {
    /**
     * The account has been activated, and the user can work with the services
     * to which he is entitled.
     */
    ACTIVE(0),
    /**
     * The account has been created but the user cannot log in without changing
     * his password.
     */
    PASSWORD_MUST_BE_CHANGED(10),
    /**
     * The account has been created but is locked because the user has not yet
     * confirmed the registration.
     */
    LOCKED_NOT_CONFIRMED(20),
    /**
     * The account has been locked because several subsequent login attempts
     * failed.
     */
    LOCKED_FAILED_LOGIN_ATTEMPTS(21),
    /**
     * The user account has been locked explicitly by an organization
     * administrator or an operator.
     */
    LOCKED(30);

    /**
     * Specifies after how many unsuccessful login attempts a user cannot log in
     * without a special treatment of his account.
     */
    public static final int LOCK_LEVEL_LOGIN = 10;
    /**
     * Specifies after how many unsuccessful login attempts a user can no longer
     * log in at all and his account becomes invalid.
     */
    public static final int LOCK_LEVEL_LOCKED = 20;

    private int lockLevel;

    private UserAccountStatus(int level) {
        lockLevel = level;
    }

    /**
     * Returns the current lock level of the calling user's account. This allows
     * for comparing locks and determining the persons to revoke specific locks.
     * 
     * @return the current lock level
     */
    public int getLockLevel() {
        return lockLevel;
    }
}
