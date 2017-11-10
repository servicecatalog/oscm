/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 30, 2011                                                      
 *                                                                              
 *  Creation Date: Nov 30, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.exception.MailOperationException;

/**
 * @author tokoda
 */
@SuppressWarnings("boxing")
public class SendMailHandlerTest {

    private static final long USER_KEY1 = 11111;
    private static final long USER_KEY2 = 11112;
    private static final long MARKETPLACE_KEY = 22222;

    private static final String SUBSCRIPTION_ID = "sub1";

    CommunicationServiceLocal communicationServiceMock;
    DataService dataServiceMock;

    SendMailHandler handler;

    PlatformUser user1;
    PlatformUser user2;
    Marketplace marketplace;

    @Before
    public void setUp() throws Exception {

        handler = new SendMailHandler();
        handler.setServiceFacade(createServiceFacade());
    }

    private ServiceFacade createServiceFacade() throws Exception {
        ServiceFacade facade = new ServiceFacade();

        communicationServiceMock = mock(CommunicationServiceLocal.class);
        facade.setCommunicationService(communicationServiceMock);

        dataServiceMock = createDataServiceMock();
        facade.setDataService(dataServiceMock);
        return facade;
    }

    private DataService createDataServiceMock() throws Exception {

        user1 = new PlatformUser();
        user2 = new PlatformUser();
        marketplace = new Marketplace();

        DataService spyDataService = mock(DataService.class);
        when(spyDataService.getReference(PlatformUser.class, USER_KEY1))
                .thenReturn(user1);
        when(spyDataService.getReference(PlatformUser.class, USER_KEY2))
                .thenReturn(user2);
        when(spyDataService.getReference(Marketplace.class, MARKETPLACE_KEY))
                .thenReturn(marketplace);
        return spyDataService;
    }

    @Test
    public void execute_oneMail() throws Exception {

        // given payload with one user key
        handler.setPayload(new SendMailPayload(USER_KEY1,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID }, Long.valueOf(MARKETPLACE_KEY)));

        // when
        handler.execute();

        // then one mail is sent
        verify(communicationServiceMock).sendMail(user1,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID }, marketplace);
    }

    @Test
    public void execute_twoMails() throws Exception {

        // given payload with two user keys
        List<Long> userKeys = new ArrayList<Long>();
        userKeys.add(USER_KEY1);
        userKeys.add(USER_KEY2);
        EmailType emailType = EmailType.SUBSCRIPTION_USER_REMOVED;
        Long marketplaceKey = Long.valueOf(MARKETPLACE_KEY);

        // mail 1
        SendMailPayload payload = new SendMailPayload(userKeys.get(0),
                emailType, new Object[] { SUBSCRIPTION_ID, "accessInfo1" },
                marketplaceKey);

        // mail 2
        payload.addMailObjectForUser(userKeys.get(1), emailType, new Object[] {
                SUBSCRIPTION_ID, "accessInfo2" }, marketplaceKey);
        handler.setPayload(payload);

        // when
        handler.execute();

        // then two mails must be sent
        verify(communicationServiceMock).sendMail(user1,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID, "accessInfo1" }, marketplace);
        verify(communicationServiceMock).sendMail(user2,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID, "accessInfo2" }, marketplace);
    }

    @Test
    public void handleErrorTestWithMailOperationException() throws Exception {
        handler.setPayload(new SendMailPayload(USER_KEY1,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID }, Long.valueOf(MARKETPLACE_KEY)));
        handler.handleError(new MailOperationException());
    }

    @Test(expected = Exception.class)
    public void handleErrorTestWithException() throws Exception {
        handler.setPayload(new SendMailPayload(USER_KEY1,
                EmailType.SUBSCRIPTION_USER_REMOVED,
                new Object[] { SUBSCRIPTION_ID }, Long.valueOf(MARKETPLACE_KEY)));
        handler.handleError(new Exception());
    }
}
