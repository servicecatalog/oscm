/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-4-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * Unit test for testing the ReportIssueBean.
 * 
 * @author yuyin
 */
public class ReportIssueBeanTest {

    private static final String OUTCOME_SUBSCRIPTION_NOT_AVAILABLE = "subscriptionNotAccessible";
    private SubscriptionService subscriptionServiceMock;
    private SubscriptionDetailsService subscriptionDetailsService;
    private ReportIssueBean spy;
    private Message message = null;
    private VOSubscriptionDetails subscription;
    private static final long subscriptionKey = 1L;

    private class Message {
        String SERVERITY;
        String KEY;

        Message(String severity, String key) {
            SERVERITY = severity;
            KEY = key;
        }
    }

    @Before
    public void setUp() throws Exception {
        new FacesContextStub(Locale.ENGLISH);
        subscriptionServiceMock = mock(SubscriptionService.class);
        subscriptionDetailsService = mock(SubscriptionDetailsService.class);
        spy = spy(new ReportIssueBean());
        doReturn(subscriptionDetailsService).when(spy)
                .getSubscriptionDetailsService();
        doReturn(subscriptionServiceMock).when(spy).getSubscriptionService();
        doReturn(Long.valueOf(subscriptionKey)).when(spy)
                .getSelectedSubscriptionKey();
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String severity = args[1].toString();
                String key = (String) args[2];
                message = new Message(severity, key);
                return null;
            }
        }).when(spy).addMessage(anyString(), any(FacesMessage.Severity.class),
                anyString());

        spy.ui = mock(UiDelegate.class);
        subscription = new VOSubscriptionDetails();
        subscription.setSellerName("Supplier 1");
        subscription.setSubscriptionId("Subscription 0x234");
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        POSubscriptionDetails subscriptionDetails = new POSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(spy.ui.getViewLocale()).thenReturn(Locale.GERMAN);

    }

    private void assertMessage(String severity, String key) {
        assertNotNull(message);
        assertEquals(severity, message.SERVERITY);
        assertEquals(key, message.KEY);
    }

    @Test
    public void init() {
        spy.setSupportEmailTitle("previous title");
        spy.setSupportEmailContent("previous content");
        // when
        spy.init();

        // then
        verify(spy).resetUIInputChildren();
        assertEquals(null, spy.getSupportEmailContent());
        assertEquals(null, spy.getSupportEmailTitle());
    }

    @Test
    public void testReportIssue_InitialState() {
        ReportIssueBean bean = new ReportIssueBean();
        assertNull(bean.getSupportEmailContent());
        assertNull(bean.getSupportEmailTitle());
    }

    @Test
    public void testReportIssue_Success() throws Exception {
        final String supportMailTitle = "Problem sending emails";
        final String supportMailContent = "There is a problem when sending a support email.";

        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.ACTIVE));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull(args);
                assertEquals(subscription.getSubscriptionId(), args[0]);
                assertEquals(supportMailTitle, args[1]);
                assertEquals(supportMailContent, args[2]);
                return null;
            }

        }).when(subscriptionServiceMock).reportIssue(Matchers.anyString(),
                Matchers.anyString(), Matchers.anyString());

        spy.setSupportEmailTitle(supportMailTitle);
        spy.setSupportEmailContent(supportMailContent);

        doNothing().when(subscriptionServiceMock).reportIssue(anyString(),
                eq(supportMailTitle), eq(supportMailContent));

        assertEquals(BaseBean.OUTCOME_SUCCESS, spy.reportIssue("abcdef"));
        assertMessage(FacesMessage.SEVERITY_INFO.toString(),
                BaseBean.INFO_ORGANIZATION_SUPPORTMAIL_SENT);

        verify(subscriptionServiceMock, times(1)).reportIssue(
                Matchers.anyString(), Matchers.anyString(),
                Matchers.anyString());
    }

    @Test
    public void testReportIssue_ValidationException() throws Exception {
        doThrow(new ValidationException()).when(subscriptionServiceMock)
                .reportIssue(Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString());
        assertEquals(BaseBean.OUTCOME_ERROR,
                spy.reportIssue(Matchers.anyString()));
    }

    @Test
    public void testReportIssue_ObjectException() throws Exception {
        doThrow(new ObjectNotFoundException()).when(subscriptionServiceMock)
                .reportIssue(Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString());
        assertEquals(BaseBean.OUTCOME_ERROR,
                spy.reportIssue(Matchers.anyString()));
    }

    @Test
    public void testReportIssue_MailOperationException() throws Exception {
        doThrow(new MailOperationException()).when(subscriptionServiceMock)
                .reportIssue(Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString());
        assertEquals(BaseBean.OUTCOME_ERROR,
                spy.reportIssue(Matchers.anyString()));
    }

    @Test
    public void testReportIssue_OperationNotPermittedException()
            throws Exception {
        doThrow(new OperationNotPermittedException()).when(
                subscriptionServiceMock).reportIssue(Matchers.anyString(),
                Matchers.anyString(), Matchers.anyString());
        assertEquals(BaseBean.OUTCOME_ERROR,
                spy.reportIssue(Matchers.anyString()));
    }

    @Test
    public void testMaxInputLength() {
        assertEquals(ADMValidator.LENGTH_EMAIL_SUBJECT, spy.getSubjectLen());
        assertEquals(ADMValidator.LENGTH_EMAIL_CONTENT, spy.getContentLen());
    }

    @Test
    public void refreshSendSuccessMessage() {
        assertEquals(BaseBean.OUTCOME_SUCCESS, spy.refreshSendSuccessMessage());
        assertMessage(FacesMessage.SEVERITY_INFO.toString(),
                BaseBean.INFO_ORGANIZATION_SUPPORTMAIL_SENT);
    }

    @Test
    public void testReportIssue_MissingSelectedSubscriptionId()
            throws Exception {
        assertEquals(BaseBean.OUTCOME_ERROR, spy.reportIssue(null));
    }

    @Test
    public void testReportIssue_EmptySelectedSubscriptionId() throws Exception {
        assertEquals(BaseBean.OUTCOME_ERROR, spy.reportIssue(""));
    }

    @Test
    public void reportIssue_subscriptionInvalid() throws Exception {
        // given
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.INVALID));

        // when
        String result = spy.reportIssue("subscriptionId");

        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, result);

    }

    @Test
    public void reportIssue_subscriptionDeactivated() throws Exception {
        // given
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenThrow(new ObjectNotFoundException());
        // when
        String result = spy.reportIssue("subscriptionId");

        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, result);

    }
}
