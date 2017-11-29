/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 03.07.15 09:54
 *
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.logging.Log4jLogger;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.BillingInputFactory;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorLocal;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingPeriodData;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.billingservice.service.model.BillingSubscriptionChunk;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.test.DateTimeHandling;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class BillingServiceBeanTest {

    private static final String BILLING_RESULT_XML = "<BillingDetails></BillingDetails>";

    @Mock
    private ConfigurationServiceLocal cfgMgmt;

    @Mock
    private BillingDataRetrievalServiceLocal bdr;

    @Mock
    private SharesCalculatorLocal sharesCalculator;

    @Mock
    protected RevenueCalculatorLocal revenueCalculator;

    @Mock
    private DataService dm;

    @Mock
    private UserLicenseDao userLicenseDao;

    @Mock
    private CommunicationServiceLocal communicationServiceLocal;

    @Mock
    TriggerQueueServiceLocal triggerQS;

    @InjectMocks
    private BillingServiceBean billingServiceBean = spy(new BillingServiceBean());

    long currentTime;
    BillingResult billingResult;
    List<BillingSubscriptionData> validBillingData;
    private PlatformUser user;

    @Before
    public void setup() {

        currentTime = System.currentTimeMillis();
        billingResult = getSimpleBillingResult();
        validBillingData = getValidSubscriptionDataList();
        user = getSimpleUser();

        List<PlatformUser> users = new ArrayList<>();
        users.add(user);
        doReturn(users).when(userLicenseDao).getPlatformOperators();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartBillingRun_withValidBillingData()
            throws MailOperationException {

        // given
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        billingServiceBean.startBillingRun(DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00"));

        // then
        verify(billingServiceBean, times(5)).executeBilling(
                any(BillingSubscriptionChunk.class), anySet());
        Object[] params = new Object[4];
        long currentTime = DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00");
        params[1] = new SimpleDateFormat(DateTimeHandling.DATE_FORMAT_PATTERN)
                .format(new Date(currentTime));
        params[3] = user.getLastName();
        verify(communicationServiceLocal, never()).sendMail(user,
                EmailType.BILLING_FAILED, params, null);
    }

    @Test
    public void testStartBillingRun_withSuccesfullShareCalculation() {

        // given
        doReturn(Boolean.TRUE).when(billingServiceBean)
                .performShareCalculationRun(any(DataProviderTimerBased.class));
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertTrue(billingRun);
    }

    @Test
    public void testStartBillingRun_withUnsuccesfullBrokerSharesCalculation() {

        // given
        doReturn(Boolean.FALSE).when(sharesCalculator)
                .performBrokerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performMarketplacesSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performResellerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performSupplierSharesCalculationRun(anyLong(), anyLong());
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertFalse(billingRun);
    }

    @Test
    public void testStartBillingRun_withUnsuccesfullMarketplacesSharesCalculation() {

        // given
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performBrokerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.FALSE).when(sharesCalculator)
                .performMarketplacesSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performResellerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performSupplierSharesCalculationRun(anyLong(), anyLong());
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertFalse(billingRun);
    }

    @Test
    public void testStartBillingRun_withUnsuccesfullResellerSharesCalculation() {

        // given
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performBrokerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performMarketplacesSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.FALSE).when(sharesCalculator)
                .performResellerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performSupplierSharesCalculationRun(anyLong(), anyLong());
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertFalse(billingRun);
    }

    @Test
    public void testStartBillingRun_withUnsuccesfullSupplierSharesCalculation() {

        // given
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performBrokerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performMarketplacesSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.TRUE).when(sharesCalculator)
                .performResellerSharesCalculationRun(anyLong(), anyLong());
        doReturn(Boolean.FALSE).when(sharesCalculator)
                .performSupplierSharesCalculationRun(anyLong(), anyLong());
        doReturn(validBillingData).when(bdr).getSubscriptionsForBilling(
                anyLong(), anyLong(), anyLong());

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertFalse(billingRun);
    }

    @Test
    public void testStartBillingRun_withEmptyBillingData() {

        // given
        doReturn(Boolean.TRUE).when(billingServiceBean)
                .performShareCalculationRun(any(DataProviderTimerBased.class));

        // when
        boolean billingRun = billingServiceBean.startBillingRun(currentTime);

        // then
        assertTrue(billingRun);
    }

    @Test
    public void testExecuteBilling_withEmptyBillingData() {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunkWithoutBillingInput();
        Set<Long> failedSubscriptions = new HashSet<>();

        // when
        BillingRun billingRun = billingServiceBean.executeBilling(chunk,
                failedSubscriptions);

        // then
        assertTrue(billingRun.isSuccessful());
        assertTrue(billingRun.getBillingResultList().isEmpty());
        assertTrue(failedSubscriptions.isEmpty());
    }

    @Test
    public void testExecuteBilling_withInvalidBillingData() throws Exception {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();
        Set<Long> failedSubscriptions = new HashSet<>();
        doThrow(new ObjectNotFoundException()).when(revenueCalculator)
                .performBillingRunForSubscription(any(BillingInput.class));

        // when
        BillingRun billingRun = billingServiceBean.executeBilling(chunk,
                failedSubscriptions);

        // then
        assertFalse(billingRun.isSuccessful());
        assertTrue(billingRun.getBillingResultList().isEmpty());
        assertFalse(failedSubscriptions.isEmpty());
    }

    @Test
    public void testExecuteBilling_withValidBillingData() throws Exception {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();
        Set<Long> failedSubscriptions = new HashSet<>();
        doReturn(billingResult).when(revenueCalculator)
                .performBillingRunForSubscription(any(BillingInput.class));

        // when
        BillingRun billingRun = billingServiceBean.executeBilling(chunk,
                failedSubscriptions);

        // then
        assertTrue(billingRun.isSuccessful());
        assertFalse(billingRun.getBillingResultList().isEmpty());
        assertTrue(failedSubscriptions.isEmpty());
    }

    @Test
    public void testExecuteBilling_numberOfBillingRunsForSubscription()
            throws Exception {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();
        Set<Long> failedSubscriptions = new HashSet<>();

        // when
        billingServiceBean.executeBilling(chunk, failedSubscriptions);

        // then
        verify(revenueCalculator, times(chunk.getBillingInputList().size()))
                .performBillingRunForSubscription(any(BillingInput.class));
    }

    @Test
    public void testExecuteBilling_numberOfFailedSubscriptions()
            throws Exception {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();
        Set<Long> failedSubscriptions = new HashSet<>();
        doThrow(new ObjectNotFoundException()).when(revenueCalculator)
                .performBillingRunForSubscription(any(BillingInput.class));

        // when
        billingServiceBean.executeBilling(chunk, failedSubscriptions);

        // then
        assertEquals(failedSubscriptions.size(), chunk.getBillingInputList()
                .size());
    }

    @Test
    public void testExecuteBilling_emailSent() throws Exception {

        // given
        Object[] params = new Object[4];
        long currentTime = DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00");
        params[0] = new SimpleDateFormat(DateTimeHandling.DATE_FORMAT_PATTERN)
                .format(new Date(currentTime));
        params[3] = user.getLastName();

        DataProviderTimerBased billingRunProvider = mock(DataProviderTimerBased.class);
        doReturn(billingRunProvider).when(billingServiceBean)
                .getBillingRunProvider(anyLong());
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();
        try {
            dm = mock(DataService.class);
            doThrow(new ObjectNotFoundException()).when(dm)
                    .getReference(Mockito.eq(Subscription.class), anyLong());
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        doReturn(chunk).when(billingRunProvider).getSubscriptionHistories(
                any(BillingPeriodData.class), any(DataService.class));
        List<BillingPeriodData> billingPeriodDatas = new ArrayList<>();
        BillingPeriodData data = new BillingPeriodData(0, 0, null);
        billingPeriodDatas.add(data);
        doReturn(billingPeriodDatas).when(billingRunProvider).loadBillingData();

        doThrow(new ObjectNotFoundException()).when(revenueCalculator)
                .performBillingRunForSubscription(any(BillingInput.class));

        // when
        billingServiceBean.startBillingRun(DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00"));

        // then
        verify(communicationServiceLocal).sendMail(user,
                EmailType.BILLING_FAILED, params, null);
    }

    @Test
    public void testExecuteBilling_numberOfFailedSubscriptionsWithDifferentKeys()
            throws Exception {

        // given
        BillingSubscriptionChunk chunk = getBillingSubscriptionChunk();

        BillingInput billingInput = BillingInputFactory.newBillingInput(
                DateTimeHandling.calculateMillis("2015-06-11 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-15 00:00:00"), 11);

        chunk.getBillingInputList().add(billingInput);

        Set<Long> failedSubscriptions = new HashSet<>();
        doThrow(new ObjectNotFoundException()).when(revenueCalculator)
                .performBillingRunForSubscription(any(BillingInput.class));

        // when
        billingServiceBean.executeBilling(chunk, failedSubscriptions);

        // then
        assertFalse(failedSubscriptions.size() == chunk.getBillingInputList()
                .size());
        assertEquals(failedSubscriptions.size(), 3);
    }

    @Test
    public void testCreateTriggerMessage_nullBillingRun() {

        // given
        BillingRun billingRun = null;

        // when
        List<TriggerMessage> messages = billingServiceBean
                .createTriggerMessagesForAllCustomers(billingRun);

        // then
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testCreateTriggerMessage_emptyBillingResults() {

        // given
        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-06-11 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-07-11 00:00:00");
        BillingRun billingRun = new BillingRun(billingPeriodStart,
                billingPeriodEnd);

        // when
        List<TriggerMessage> messages = billingServiceBean
                .createTriggerMessagesForAllCustomers(billingRun);

        // then
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testCreateTriggerMessage_notEmptyBillingResults() {

        // given
        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-06-11 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-07-11 00:00:00");
        BillingRun billingRun = new BillingRun(billingPeriodStart,
                billingPeriodEnd);
        billingRun.addBillingResult(billingResult);

        // when
        List<TriggerMessage> messages = billingServiceBean
                .createTriggerMessagesForAllCustomers(billingRun);

        // then
        assertFalse(messages.isEmpty());

        TriggerMessage message = messages.get(0);
        TriggerProcessParameter parameter = message.getParams().get(0);

        assertEquals(TriggerType.START_BILLING_RUN, message.getTriggerType());
        assertEquals(TriggerProcessParameterName.XML_BILLING_DATA,
                parameter.getName());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTriggerMessage_notEmptyReceivers() {

        // given
        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-06-11 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-07-11 00:00:00");
        BillingRun billingRun = new BillingRun(billingPeriodStart,
                billingPeriodEnd);
        billingRun.addBillingResult(billingResult);

        doReturn(user.getOrganization()).when(dm).find(any(Class.class),
                anyLong());

        // when
        List<TriggerMessage> messages = billingServiceBean
                .createTriggerMessagesForAllCustomers(billingRun);

        // then
        List<Organization> receivers = messages.get(0).getReceiverOrgs();
        assertFalse(receivers.isEmpty());

    }

    @Test
    public void bug11720_startBillingRunLogs() {
        // given
        Log4jLogger mockedLogger = mock(Log4jLogger.class);
        try {
            setFinalStatic(BillingServiceBean.class.getDeclaredField("LOGGER"),
                    mockedLogger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // when
        billingServiceBean.startBillingRun(DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00"));
        // then
        verify(mockedLogger, times(1)).logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_BILLING_RUN_STARTED);
        verify(mockedLogger, times(1)).logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_BILLING_RUN_FINISHED);
    }

    private BillingSubscriptionData createBillingSubscriptionData(
            long subscriptionKey, long activationDate, int cutOffDay,
            Long endOfLastBilledPeriod) {

        BillingSubscriptionData billingSubscriptionData = new BillingSubscriptionData();
        billingSubscriptionData.setSubscriptionKey(subscriptionKey);
        billingSubscriptionData.setActivationDate(activationDate);
        billingSubscriptionData.setCutOffDay(cutOffDay);
        billingSubscriptionData.setEndOfLastBilledPeriod(endOfLastBilledPeriod);

        return billingSubscriptionData;
    }

    private List<BillingSubscriptionData> getValidSubscriptionDataList() {

        List<BillingSubscriptionData> subscriptionData = new ArrayList<BillingSubscriptionData>();

        subscriptionData.add(createBillingSubscriptionData(1, DateTimeHandling
                .calculateMillis("2015-03-10 10:30:00"), 1, Long
                .valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:00"))));

        subscriptionData.add(createBillingSubscriptionData(2,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 5,
                null));

        subscriptionData.add(createBillingSubscriptionData(3,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 24,
                null));

        return subscriptionData;
    }

    private BillingSubscriptionChunk getBillingSubscriptionChunkWithoutBillingInput() {

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-06-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00");

        BillingSubscriptionChunk chunk = new BillingSubscriptionChunk(
                billingPeriodStart, billingPeriodEnd);

        return chunk;
    }

    private BillingSubscriptionChunk getBillingSubscriptionChunk() {

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-06-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-07-01 00:00:00");

        BillingSubscriptionChunk chunk = new BillingSubscriptionChunk(
                billingPeriodStart, billingPeriodEnd);

        List<BillingInput> list = new ArrayList<BillingInput>();

        list.add(BillingInputFactory.newBillingInput(
                DateTimeHandling.calculateMillis("2015-06-15 00:00:00"),
                billingPeriodEnd, 10));

        list.add(BillingInputFactory.newBillingInput(
                DateTimeHandling.calculateMillis("2015-06-11 00:00:00"),
                billingPeriodEnd, 11));

        list.add(BillingInputFactory.newBillingInput(
                DateTimeHandling.calculateMillis("2015-06-09 00:00:00"),
                billingPeriodEnd, 12));

        chunk.setBillingInputList(list);
        return chunk;
    }

    private BillingResult getSimpleBillingResult() {

        BillingResult result = new BillingResult();
        result.setResultXML(BILLING_RESULT_XML);
        return result;
    }

    private PlatformUser getSimpleUser() {

        PlatformUser platformUser = new PlatformUser();
        platformUser.setLastName("Luck");
        platformUser.setFirstName("Lucky");

        Organization org = new Organization();
        org.setName("LuckyLuckOrg");
        platformUser.setOrganization(org);

        return platformUser;
    }

    private static void setFinalStatic(Field field, Object newValue)
            throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
