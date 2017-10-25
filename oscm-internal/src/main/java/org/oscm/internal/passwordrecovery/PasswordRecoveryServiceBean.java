/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.authorization.PasswordHash;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Bean implementation for the password recovery service.
 * 
 * @author Mao
 */
@Stateless
@Remote(PasswordRecoveryService.class)
@Interceptors({ InvocationDateContainer.class })
public class PasswordRecoveryServiceBean implements PasswordRecoveryService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PasswordRecoveryServiceBean.class);

    private static final Random random = new SecureRandom();

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    CommunicationServiceLocal cs;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal configs;

    @EJB(beanInterface = IdentityServiceLocal.class)
    IdentityServiceLocal ids;

    @Resource
    protected SessionContext sessionCtx;

    @Override
    public void startPasswordRecovery(String userId, String marketplaceId) {
        ArgumentValidator.notNull("userId", userId);

        try {

            PlatformUser pUser = ids.getPlatformUser(userId, false);
            if (!verifyUserPermission(pUser, marketplaceId)) {
                return;
            }
            if (!PasswordRecoveryValidator.isValidInterval(pUser
                    .getPasswordRecoveryStartDate())) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            PasswordRecoveryLink passwordRecoveryLink = new PasswordRecoveryLink(
                    !pUser.hasManagerRole(), configs);
            String confirmationURL = passwordRecoveryLink
                    .encodePasswordRecoveryLink(pUser, currentTime,
                            marketplaceId);

            pUser.setPasswordRecoveryStartDate(currentTime);
            dm.flush();
            sendPasswordRecoveryMails(pUser,
                    EmailType.RECOVERPASSWORD_CONFIRM_URL, marketplaceId,
                    new Object[] { confirmationURL });
            logger.logInfo(Log4jLogger.ACCESS_LOG,
                    LogMessageIdentifier.INFO_USER_PWDRECOVERY_REQUEST, userId);
        } catch (MailOperationException e) {
            sessionCtx.setRollbackOnly();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_MAIL_OPERATION_FAILED);
        } catch (UnsupportedEncodingException | OperationNotPermittedException
                | EJBTransactionRolledbackException | ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ENCODE_URL_FAILED);

        }
    }

    @Override
    public String confirmPasswordRecoveryLink(String recoveryPasswordLink,
            String marketplaceId) {
       
        String[] decodedParams = PasswordRecoveryLink
                .decodeRecoveryPasswordLink(recoveryPasswordLink);
        if (decodedParams == null || decodedParams.length < 2) {
            return null;
        }
        try {
            PlatformUser pUser = ids.getPlatformUser(decodedParams[0], false);
            return (verifyRecoveryPasswordToken(pUser, decodedParams,
                    marketplaceId) ? pUser.getUserId() : null);
        } catch (ObjectNotFoundException | OperationNotPermittedException e) {
            return null;
        }
    }

    @Override
    public boolean completePasswordRecovery(String userId, String newPassword) {
        try {
            ArgumentValidator.notNull("userId", userId);
            ArgumentValidator.notNull("newPassword", newPassword);
            BLValidator.isPassword("newPassword", newPassword);
        } catch (IllegalArgumentException | ValidationException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_CHANGE_PASSWORD_FAILED);
            return false;
        }

        try {
            PlatformUser pUser = ids.getPlatformUser(userId, false);
            final long salt = random.nextLong();
            pUser.setPasswordSalt(salt);
            pUser.setPasswordHash(PasswordHash.calculateHash(salt, newPassword));
            pUser.setPasswordRecoveryStartDate(0);
            pUser.setStatus(UserAccountStatus.ACTIVE);
            dm.flush();

            sendPasswordRecoveryMails(pUser, EmailType.RECOVERPASSWORD_CONFIRM,
                    null, new Object[] {});
            logger.logInfo(Log4jLogger.ACCESS_LOG,
                    LogMessageIdentifier.INFO_USER_PWDRECOVERY_COMPLETE, userId);
            return true;
        } catch (ObjectNotFoundException | OperationNotPermittedException e1) {
            return false;
        } catch (MailOperationException e) {
            sessionCtx.setRollbackOnly();
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_MAIL_OPERATION_FAILED);
            return false;
        } catch (EJBTransactionRolledbackException e) {
            return false;
        }

    }

    /**
     * Verify if PasswordRecoveryStartDate is valid.
     * 
     * @param pUser
     * @param decodedParam
     * @param marketplaceId
     * @return boolean - <code>true</code> if valid, otherwise
     *         <code>false</code>
     */
    boolean verifyRecoveryPasswordToken(PlatformUser pUser,
            String[] decodedParam, String marketplaceId) {
        boolean isTokenCorrect = decodedParam[1].equals(Long.toString(pUser
                .getPasswordRecoveryStartDate()));
        boolean isMarketplaceIdCorrect = false;
        if (marketplaceId == null && decodedParam.length == 2) {
            isMarketplaceIdCorrect = true;
        } else if (marketplaceId != null && decodedParam.length == 3) {
            isMarketplaceIdCorrect = marketplaceId.equals(decodedParam[2]);
        }
        boolean isTokenExpired = PasswordRecoveryValidator.isExpired(pUser
                .getPasswordRecoveryStartDate());
        return (isTokenCorrect && isMarketplaceIdCorrect && (!isTokenExpired));
    }

    /**
     * Sends an email to the given platform user.
     * 
     * @param pUser
     *            The platform user the mail will be sent to.
     * @param mailType
     *            The email content type.
     * @param marketplaceId
     *            the marketplaceId to get marketplace - can be
     *            <code>null</code>
     * @param obj
     *            Parameters for the email content text.
     * @throws MailOperationException
     *             Thrown in case the mail cannot be initialized or sent.
     */
    void sendPasswordRecoveryMails(PlatformUser pUser, EmailType mailType,
            String marketplaceId, Object[] obj) throws MailOperationException {
        Marketplace marketplace = null;
        if (marketplaceId != null) {
            marketplace = new Marketplace();
            marketplace.setMarketplaceId(marketplaceId);
            marketplace = (Marketplace) dm.find(marketplace);
        }
        cs.sendMail(pUser, mailType, obj, marketplace);
    }

    /**
     * Verify if user can recover his/her password, checking all criteria and
     * send corresponding mail in case of failure. Return <code>false</code>,
     * <ul>
     * <li>if the user has logged in</li>
     * <li>if the user status is locked</li>
     * <li>if the user is a LDAP user</li>
     * <li>if the user is pure customer and request to recover password from
     * blue portal</li>
     * <li>if the user is a manager and request to recover password from
     * marketplace</li>
     * </ul>
     * 
     * @param pUser
     * @param marketplaceId
     * @return boolean - <code>true</code> if verification succeeded, otherwise
     *         <code>false</code>.
     * @throws MailOperationException
     *             - if sending mail was not successful.
     */
    boolean verifyUserPermission(PlatformUser pUser, String marketplaceId)
            throws MailOperationException {
        if (ids.isUserLoggedIn(pUser.getKey())) {
            return false;
        }
        if (isAccountLocked(pUser.getStatus())) {
            sendPasswordRecoveryMails(pUser,
                    EmailType.RECOVERPASSWORD_USER_LOCKED, marketplaceId,
                    new Object[] {});
            return false;
        }
        if (pUser.getOrganization().isRemoteLdapActive()) {
            sendPasswordRecoveryMails(pUser,
                    EmailType.RECOVERPASSWORD_FAILED_LDAP, marketplaceId,
                    new Object[] {});
            return false;
        }
        if (!pUser.hasManagerRole() && (marketplaceId == null)) {
            sendPasswordRecoveryMails(pUser,
                    EmailType.RECOVERPASSWORD_CLASSICPORTAL_FAILED, null,
                    new Object[] {});
            return false;
        }
        if ((pUser.hasManagerRole()) && (marketplaceId != null)
                && (!marketplaceId.equals(""))) {
            sendPasswordRecoveryMails(pUser,
                    EmailType.RECOVERPASSWORD_MARKETPLACE_FAILED,
                    marketplaceId, new Object[] {});
            return false;
        }
        return true;
    }

    boolean isAccountLocked(UserAccountStatus status) {
        return status.getLockLevel() > UserAccountStatus.LOCK_LEVEL_LOGIN;
    }

}
