/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 08.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.oscm.app.v2_0.service.APPCommunicationServiceBean.DEFAULT_MAIL_RESOURCE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.oscm.test.ejb.TestNamingContextFactoryBuilder;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.service.APPCommunicationServiceBean;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;

public class APPCommunicationServiceBeanTest {

    private Session mailMock;
    private APPCommunicationServiceBean commService;
    private APPConfigurationServiceBean configurationService;

    @Before
    public void setup() throws Exception {

        // container.enableInterfaceMocking(true);
        if (!NamingManager.hasInitialContextFactoryBuilder()) {
            NamingManager
                    .setInitialContextFactoryBuilder(new TestNamingContextFactoryBuilder());
        }
        InitialContext initialContext = new InitialContext();
        Properties properties = new Properties();
        properties.put("mail.smtp.from", "test@ess.intern");
        mailMock = Session.getInstance(properties);
        initialContext.bind("java:openejb/Resource/" + DEFAULT_MAIL_RESOURCE, mailMock);
        configurationService = mock(APPConfigurationServiceBean.class);
        commService = spy(new APPCommunicationServiceBean());
        commService.configService = configurationService;
        doNothing().when(commService).transportMail(
                Matchers.any(MimeMessage.class));
    }

    @Test
    public void testSendMail() throws Exception {
        // given

        // when
        commService.sendMail(Collections.singletonList("test@noreply.de"),
                "subject", "text");

        // then
        verify(commService, times(1)).transportMail(any(MimeMessage.class));
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailInvalidFromAddress() throws Exception {
        // enforce InvalidAddressException
        mailMock.getProperties().put("mail.smtp.from", "");

        commService.sendMail(Collections.singletonList("test@noreply.de"),
                "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailEmptyRecipientAddress() throws Exception {
        // enforce InvalidAddressException
        commService.sendMail(Collections.singletonList(""), "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailNullRecipientAddress() throws Exception {
        // enforce InvalidAddressException
        commService.sendMail(Collections.singletonList((String) null),
                "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailNullRecipientList() throws Exception {
        // enforce InvalidAddressException
        commService.sendMail(null, "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailEmptyRecipientList() throws Exception {
        // enforce InvalidAddressException
        commService.sendMail(new ArrayList<String>(), "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailTransportException() throws Exception {
        // simulate some mail exception
        // mailMessageException = new MessagingException("Transport propblem");
        doThrow(new MessagingException("Transport problem")).when(commService)
                .transportMail(Matchers.any(MimeMessage.class));
        commService.sendMail(Collections.singletonList("test@noreply.de"),
                "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testSendMailInvalidCustomMailResource() throws Exception {
        when(
                configurationService
                        .getProxyConfigurationSetting(PlatformConfigurationKey.APP_MAIL_RESOURCE))
                .thenReturn("mail/notexisting");
        commService.sendMail(Collections.singletonList("test@noreply.de"),
                "subject", "text");
    }

    @Test
    public void testComposeMessage() throws Exception {
        // given

        // when
        MimeMessage message = commService
                .composeMessage(Collections.singletonList("test@noreply.de"),
                        "subject", "text");

        // then
        Assert.assertNotNull(message);
        assertEquals("subject", message.getSubject());
        assertEquals("text", message.getContent());
        assertEquals("test@ess.intern", message.getFrom()[0].toString());
        Assert.assertNotNull(message.getAllRecipients());
        assertEquals(1, message.getAllRecipients().length);
        assertEquals("test@noreply.de",
                message.getAllRecipients()[0].toString());

    }

    @Test
    public void testComposeMessageCustomMailResource() throws Exception {
        // given
        when(
                configurationService
                        .getProxyConfigurationSetting(PlatformConfigurationKey.APP_MAIL_RESOURCE))
                .thenReturn(DEFAULT_MAIL_RESOURCE);

        // when
        MimeMessage message = commService
                .composeMessage(Collections.singletonList("test@noreply.de"),
                        "subject", "text");

        // then
        Assert.assertNotNull(message);
        assertEquals("subject", message.getSubject());
        assertEquals("text", message.getContent());
    }

    @Test
    public void testComposeMessageEmptyCustomMailResource() throws Exception {
        // given
        when(
                configurationService
                        .getProxyConfigurationSetting(PlatformConfigurationKey.APP_MAIL_RESOURCE))
                .thenReturn("");
        // when
        MimeMessage message = commService
                .composeMessage(Collections.singletonList("test@noreply.de"),
                        "subject", "text");

        // then
        Assert.assertNotNull(message);
        assertEquals("subject", message.getSubject());
        assertEquals("text", message.getContent());
    }

    @Test(expected = APPlatformException.class)
    public void testComposeMessage_mailInitFails() throws Exception {
        // given
        MimeMessage mockMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Mail initialization fails")).when(
                mockMessage).setFrom(any(Address.class));
        doReturn(mockMessage).when(commService).getMimeMessage(
                any(Session.class));

        // when
        commService
                .composeMessage(Collections.singletonList("test@noreply.de"),
                        "subject", "text");
    }

    @Test(expected = APPlatformException.class)
    public void testComposeMessage_addRecipientsFails() throws Exception {
        // given
        MimeMessage mockMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Adding recipient addresses fails."))
                .when(mockMessage).addRecipients(any(RecipientType.class),
                        any(Address[].class));
        doReturn(mockMessage).when(commService).getMimeMessage(
                any(Session.class));

        // when
        commService
                .composeMessage(Collections.singletonList("test@noreply.de"),
                        "subject", "text");
    }

    @Test
    public void removeDuplicates() {
        // given
        final String RECIPIENT1 = "abc";
        final String RECIPIENT2 = "xyz";
        List<String> recipients = Arrays.asList(RECIPIENT1, RECIPIENT2,
                RECIPIENT1, RECIPIENT2);

        // when
        recipients = commService.removeDuplicates(recipients);

        // then
        assertEquals(2, recipients.size());
        assertTrue(recipients.contains(RECIPIENT1));
        assertTrue(recipients.contains(RECIPIENT2));
    }
}
