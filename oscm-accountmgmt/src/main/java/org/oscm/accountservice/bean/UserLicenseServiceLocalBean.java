/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.data.SendMailStatus.SendMailStatusItem;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.PlatformUser;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.MailOperationException;

/**
 * @author qiu
 * 
 */

@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
@LocalBean
public class UserLicenseServiceLocalBean {

    @EJB(beanInterface = UserLicenseDao.class)
    UserLicenseDao userLicenseDao;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    CommunicationServiceLocal cs;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal configService;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserLicenseServiceLocalBean.class);

    @RolesAllowed("PLATFORM_OPERATOR")
    public long countRegisteredUsers() {
        return userLicenseDao.countRegisteredUsers();
    }

    /**
     * Check is user number bigger than max value.
     * 
     * @throws MailOperationException
     * 
     */
    public boolean checkUserNum() throws MailOperationException {
        long currentNum = countRegisteredUsers();
        long maxNum = configService.getLongConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_ALLOWED_USERS,
                Configuration.GLOBAL_CONTEXT);
        if (currentNum > maxNum) {
            sendMailToOperators(currentNum, maxNum);
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_USER_NUM_EXCEEDED,
                    String.valueOf(maxNum), String.valueOf(currentNum));
        }
        return true;
    }

    private void sendMailToOperators(long currentNum, long maxNum)
            throws MailOperationException {
        List<PlatformUser> recipients = userLicenseDao.getPlatformOperators();
        SendMailStatus<PlatformUser> mailStatus = cs
                .sendMail(
                        EmailType.USER_NUM_EXCEEDED,
                        new Object[] { Long.valueOf(maxNum),
                                Long.valueOf(currentNum) }, null,
                        recipients.toArray(new PlatformUser[recipients.size()]));
        if (mailStatus != null) {
            for (SendMailStatusItem<PlatformUser> sendMailStatusItem : mailStatus
                    .getMailStatus()) {
                if (sendMailStatusItem.errorOccurred()) {
                    MailOperationException mpe = new MailOperationException();
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            sendMailStatusItem.getException(),
                            LogMessageIdentifier.WARN_MAIL_USER_NUM_EXCEEDED_FAILED);
                    throw mpe;
                }
            }
        }
    }
}
