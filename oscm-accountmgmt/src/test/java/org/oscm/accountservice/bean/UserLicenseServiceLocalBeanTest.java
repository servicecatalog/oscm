/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.MailOperationException;

/**
 * @author qiu
 * 
 */
public class UserLicenseServiceLocalBeanTest {
    private UserLicenseServiceLocalBean userLicenseService;

    @Before
    public void setup() {
        userLicenseService = spy(new UserLicenseServiceLocalBean());
        userLicenseService.userLicenseDao = mock(UserLicenseDao.class);
        userLicenseService.cs = mock(CommunicationServiceLocal.class);
        userLicenseService.configService = mock(ConfigurationServiceLocal.class);
        doReturn(Long.valueOf(10L)).when(userLicenseService.configService)
                .getLongConfigurationSetting(
                        eq(ConfigurationKey.MAX_NUMBER_ALLOWED_USERS),
                        eq(Configuration.GLOBAL_CONTEXT));
    }

    @Test
    public void countRegisteredUsers() throws Exception {

        // when
        userLicenseService.countRegisteredUsers();

        // then
        verify(userLicenseService.userLicenseDao, times(1))
                .countRegisteredUsers();
    }

    @Test
    public void checkUserNum_currentNumLessThanMaxNum() throws Exception {

        // given
        doReturn(Long.valueOf(9L)).when(userLicenseService.userLicenseDao)
                .countRegisteredUsers();
        // when
        userLicenseService.checkUserNum();

        // then
        verify(userLicenseService.userLicenseDao, never())
                .getPlatformOperators();
    }

    @Test
    public void checkUserNum_currentNumEqualsMaxNum() throws Exception {

        // given
        doReturn(Long.valueOf(10L)).when(userLicenseService.userLicenseDao)
                .countRegisteredUsers();
        // when
        userLicenseService.checkUserNum();

        // then
        verify(userLicenseService.userLicenseDao, never())
                .getPlatformOperators();
    }

    @Test
    public void checkUserNum_currentNumExceedsMaxNum() throws Exception {

        // given
        doReturn(Long.valueOf(11L)).when(userLicenseService.userLicenseDao)
                .countRegisteredUsers();
        prepareOperators();
        // when
        userLicenseService.checkUserNum();

        // then
        verify(userLicenseService.userLicenseDao, times(1))
                .getPlatformOperators();
        verify(userLicenseService.cs, times(1)).sendMail(any(EmailType.class),
                any(Object[].class), any(Marketplace.class),
                any(PlatformUser[].class));
    }

    @Test(expected = MailOperationException.class)
    public void checkUserNum_MailOperationException() throws Exception {

        // given
        doReturn(Long.valueOf(11L)).when(userLicenseService.userLicenseDao)
                .countRegisteredUsers();
        List<PlatformUser> recipient = prepareOperators();
        prepareMailStatus(recipient);
        // when
        userLicenseService.checkUserNum();

    }

    private void prepareMailStatus(List<PlatformUser> recipient) {
        SendMailStatus<PlatformUser> sendMailStatus = new SendMailStatus<PlatformUser>();
        sendMailStatus.addMailStatus(recipient.get(0),
                new MailOperationException());
        doReturn(sendMailStatus).when(userLicenseService.cs).sendMail(
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class), any(PlatformUser[].class));
    }

    private List<PlatformUser> prepareOperators() {
        PlatformUser user = new PlatformUser();
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        users.add(user);
        doReturn(users).when(userLicenseService.userLicenseDao)
                .getPlatformOperators();
        return users;
    }
}
