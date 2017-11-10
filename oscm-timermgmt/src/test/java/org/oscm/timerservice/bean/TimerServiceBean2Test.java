/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.timerservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TimerProcessing;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.timerservice.stubs.TimerServiceStub;
import org.oscm.timerservice.stubs.TimerStub;
import org.oscm.types.enumtypes.Period;
import org.oscm.types.enumtypes.TimerType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTimerInfo;

/**
 * JUnit tests for the timer management service.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TimerServiceBean2Test {

    private TimerServiceBean tm;
    private SessionContext ctx;
    private TimerServiceStub tss;
    private ConfigurationServiceStub cfs;
    private DataService dm;
    private Query query;
    private BillingServiceLocal bss;
    private TimerStub timer;
    private AccountServiceLocal as;

    private long now;

    private IdentityServiceStub idService;

    @Before
    public void setUp() {
        tm = new TimerServiceBean();
        tss = new TimerServiceStub();
        cfs = new ConfigurationServiceStub();
        idService = new IdentityServiceStub();
        as = mock(AccountServiceLocal.class);

        query = mock(Query.class);
        when(query.getResultList())
                .thenReturn(new ArrayList<TimerProcessing>());

        dm = mock(DataService.class);
        when(dm.createNamedQuery(Matchers.anyString())).thenReturn(query);

        ctx = mock(SessionContext.class);
        when(ctx.getTimerService()).thenReturn(tss);

        bss = mock(BillingServiceLocal.class);

        tm.ctx = ctx;
        tm.cfgMgmt = cfs;
        tm.dm = dm;
        tm.bm = bss;
        tm.idServiceLocal = idService;
        tm.accMgmt = as;

        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION, "12");
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION, "12");
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_TENANT_PROVISIONING_TIMEOUT,
                "12");
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_INACTIVE_ON_BEHALF_USERS, "12");
        cfs.setConfigurationSetting(ConfigurationKey.TIMER_INTERVAL_USER_COUNT,
                "43200000");
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET, "345600000");
        now = System.currentTimeMillis();
    }

    @Test
    public void initTimers_interval() throws Exception {
        // given
        TimerServiceStub timeServiceStub = mock(TimerServiceStub.class);
        when(ctx.getTimerService()).thenReturn(timeServiceStub);
        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setInfo(TimerType.BILLING_INVOCATION);
        doReturn(new TimerStub(null, timerConfig)).when(timeServiceStub)
                .createCalendarTimer(any(ScheduleExpression.class),
                        any(TimerConfig.class));

        // when
        tm.initTimers();

        // then
        verify(timeServiceStub, times(4)).createTimer(any(Date.class),
                eq(10000L), any(TimerType.class));
    }

    @Test
    public void initTimers_allTimers() throws Exception {
        // given

        // when
        tm.initTimers();

        // then
        assertEquals("wrong number of timers", TimerType.values().length, tss
                .getTimers().size());

        for (TimerStub timer : ParameterizedTypes.iterable(tss.getTimers(),
                TimerStub.class)) {
            TimerType timerType = (TimerType) timer.getInfo();
            if (timerType == TimerType.BILLING_INVOCATION) {
                assertEquals("Wrong interval specified for timer", 0,
                        timer.getIntervalDuration());

                Calendar calExec = Calendar.getInstance();
                calExec.setTime(timer.getExecDate());

                Calendar calNow = Calendar.getInstance();
                calNow.setTimeInMillis(now);

                // billing timers runs each day. Default execution time is
                // 00:00:00.0000.
                if (calNow.get(Calendar.HOUR_OF_DAY) == 0
                        && calNow.get(Calendar.MINUTE) == 0
                        && calNow.get(Calendar.SECOND) == 0
                        && calNow.get(Calendar.MILLISECOND) == 0) {
                    assertEquals("Wrong exec date for specified billing timer",
                            calNow.get(Calendar.DAY_OF_YEAR),
                            calExec.get(Calendar.DAY_OF_YEAR));
                } else {
                    // should be started for the next day
                    assertEquals("Wrong exec date for specified billing timer",
                            calNow.get(Calendar.DAY_OF_YEAR) + 1,
                            calExec.get(Calendar.DAY_OF_YEAR));
                }

                // ensure that the execution time is ahead from now
                assertTrue("Wrong execution time for timer", timer
                        .getExecDate().getTime() >= now);
            } else {
                if (timerType == TimerType.DISCOUNT_END_CHECK) {
                    assertEquals("Wrong interval specified for timer", 0,
                            timer.getIntervalDuration());
                } else if (timerType == TimerType.USER_NUM_CHECK) {
                    assertEquals("Wrong interval specified for timer",
                            43200000, timer.getIntervalDuration());
                } else {
                    assertEquals("Wrong interval specified for timer", 10000,
                            timer.getIntervalDuration());
                }
            }
        }
    }

    private TimerStub getTimerForType(TimerType timerType) {
        for (TimerStub timer : ParameterizedTypes.iterable(tss.getTimers(),
                TimerStub.class)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(timer.getExecDate());
            TimerType tType = (TimerType) timer.getInfo();
            if (tType == timerType) {
                return timer;
            }
        }
        return null;
    }

    @Test
    public void initTimers_billingNegativeOffset() throws Exception {
        // given
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET, "-123156100215");

        // when
        tm.initTimers();

        // then
        TimerStub timer = getTimerForType(TimerType.BILLING_INVOCATION);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timer.getExecDate());
        Calendar calNow = Calendar.getInstance();
        calNow.setTimeInMillis(now);

        // although the initialization data is invalid (negative
        // offset), the timer must be initialized with offset 0
        // (that means timer starts next day at 00:00:00.0000)
        assertEquals(calNow.get(Calendar.DAY_OF_YEAR) + 1,
                cal.get(Calendar.DAY_OF_YEAR));

        // ensure that the execution time is ahead from now
        assertTrue(timer.getExecDate().getTime() >= now);
    }

    // related to bug 7482
    @Test
    public void initTimers_withinNextOffset() throws ValidationException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        long interval = 1000L * 60L * 60L; // every hour
        Date current = new Date();
        current.setTime(interval);

        long offset = 1000L * 60L * 60L * 24L; // one day offset
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION,
                String.valueOf(current.getTime()));
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION_OFFSET,
                String.valueOf(offset));
        tm.initTimers();

        TimerStub timer = getTimerForType(TimerType.ORGANIZATION_UNCONFIRMED);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timer.getExecDate());

        assertTrue(
                "TIMER SHOULD BE EXECUTED WITHIN THE NEXT OFFSET\ncurrent time= "
                        + (new Date()) + "\nnext execution= "
                        + (new Date(timer.getExecDate().getTime()))
                        + "\ncurrent + offset= "
                        + (new Date((new Date().getTime()) + offset)), timer
                        .getExecDate().getTime() <= (new Date()).getTime()
                        + offset);
    }

    // related to bug 7482
    @Test
    public void initTimers_offsetTooSmall() throws InterruptedException,
            ValidationException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        long interval = 1000L * 60L * 60L; // every hour
        Date current = new Date();
        current.setTime(interval);

        long offset = 10L;
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION,
                String.valueOf(current.getTime()));
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION_OFFSET,
                String.valueOf(offset));
        Thread.sleep(offset);
        tm.initTimers();

        TimerStub timer = getTimerForType(TimerType.ORGANIZATION_UNCONFIRMED);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timer.getExecDate());

        assertTrue((timer.getExecDate().getTime() > (new Date()).getTime()
                + offset)
                && (timer.getExecDate().getTime() < (new Date()).getTime()
                        + interval + offset));
    }

    @Test
    public void getTimerDetailsForTimerType() {
        // given
        ConfigurationServiceLocal cfs_mock = Mockito
                .mock(ConfigurationServiceLocal.class);
        tm.cfgMgmt = cfs_mock;
        // when
        tm.getTimerDetailsForTimerType(TimerType.USER_NUM_CHECK);

        // then
        Mockito.verify(cfs_mock, Mockito.never()).getBillingRunStartTimeInMs();
        Mockito.verify(cfs_mock, Mockito.times(1)).getLongConfigurationSetting(
                Mockito.any(ConfigurationKey.class), Mockito.anyString());
        tm.cfgMgmt = cfs;
    }

    @Test
    public void handleTimer_billing() throws Exception {
        // given
        TimerStub timer = new TimerStub();
        timer.setInfo(TimerType.BILLING_INVOCATION);

        // when
        tm.handleTimer(timer);

        // then
        verify(bss, times(1)).startBillingRun(Matchers.anyLong());

    }

    @Test
    public void handleTimer_userNum() throws Exception {
        // given
        TimerStub timer = new TimerStub();
        timer.setInfo(TimerType.USER_NUM_CHECK);

        // when
        tm.handleTimer(timer);

        // then
        Mockito.verify(as, Mockito.times(1)).checkUserNum();

    }

    @Test
    public void handleTimer_billingNotHandledCheckReinitOfTimer()
            throws Exception {
        // given
        TimerStub timer = new TimerStub();
        timer.setInfo(TimerType.BILLING_INVOCATION);

        when(query.getResultList()).thenReturn(
                Arrays.asList(new TimerProcessing()));

        // when
        tm.handleTimer(timer);

        // then
        verifyZeroInteractions(bss);

        assertEquals("wrong number of timers", 0, tss.getTimers().size());

    }

    @Test
    public void handleTimer_handleTimerPeriodicVerifyNoReinitialization()
            throws Exception {
        TimerStub timer = new TimerStub();
        TimerType timerType = TimerType.ORGANIZATION_UNCONFIRMED;
        timer.setInfo(timerType);
        tss.createTimer(new Date(), timerType);

        tm.handleTimer(timer);

        Iterable<Timer> timers = ParameterizedTypes.iterable(tss.getTimers(),
                Timer.class);
        List<Timer> timerList = new ArrayList<Timer>();
        for (Timer t : timers) {
            timerList.add(t);
        }

        assertEquals(
                "No additional timer must have been initialized, as the present one is already periodid with a fix interval",
                1, timerList.size());
    }

    @Test
    public void initTimers_getCurrentTimerExpirationDates()
            throws ValidationException {

        tss = new TimerServiceStub() {
            @Override
            public Timer createTimer(Date arg0, Serializable arg1)
                    throws IllegalArgumentException, IllegalStateException,
                    EJBException {
                initTimer((TimerType) arg1, arg0);
                getTimers().add(timer);
                return null;
            }
        };
        when(ctx.getTimerService()).thenReturn(tss);
        List<VOTimerInfo> expirationDates;
        tm.initTimers();
        expirationDates = tm.getCurrentTimerExpirationDates();
        tm.initTimers();
        assertEquals(7, expirationDates.size());
    }

    @Test(expected = ValidationException.class)
    public void initTimers_nextExpirationDateNegative()
            throws ValidationException {

        tss = new TimerServiceStub() {
            @Override
            public Timer createTimer(Date arg0, Serializable arg1)
                    throws IllegalArgumentException, IllegalStateException,
                    EJBException {
                initTimer((TimerType) arg1, arg0);
                getTimers().add(timer);
                return null;
            }
        };
        when(ctx.getTimerService()).thenReturn(tss);
        cfs.setConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION,
                "9223372036854775807");

        tm.initTimers();
    }

    @Test(expected = ValidationException.class)
    public void initTimers_nextExpirationDateNegative_userCount()
            throws ValidationException {
        // given
        tss = new TimerServiceStub() {
            @Override
            public Timer createTimer(Date arg0, Serializable arg1)
                    throws IllegalArgumentException, IllegalStateException,
                    EJBException {
                initTimer((TimerType) arg1, arg0);
                getTimers().add(timer);
                return null;
            }
        };
        when(ctx.getTimerService()).thenReturn(tss);
        cfs.setConfigurationSetting(ConfigurationKey.TIMER_INTERVAL_USER_COUNT,
                "9223372036854775807");
        // when
        tm.initTimers();

    }

    @Test
    public void createTimerWithPeriod_discountEndCheckTimerCreated() {

        // when
        tm.createTimerWithPeriod(tss, TimerType.DISCOUNT_END_CHECK, 0L,
                Period.DAY);
        // then
        assertEquals(1, tss.getTimers().size());

    }

    @Test
    public void createTimerWithPeriod_discountEndCheckTimerNotCreated() {
        // given
        prepareTimerList(TimerType.DISCOUNT_END_CHECK,
                new Date(System.currentTimeMillis() + 10000));
        // when
        tm.createTimerWithPeriod(tss, TimerType.DISCOUNT_END_CHECK, 0L,
                Period.DAY);
        // then
        assertEquals(1, tss.getTimers().size());

    }

    @Test
    public void createTimerWithPeriod_billingInvocationCreated() {

        // when
        tm.createTimerWithPeriod(tss, TimerType.BILLING_INVOCATION, 0L,
                Period.DAY);
        // then
        assertEquals(1, tss.getTimers().size());
    }

    @Test
    public void createTimerWithPeriod_billingInvocationNotCreated() {
        // given
        prepareTimerList(TimerType.BILLING_INVOCATION,
                new Date(System.currentTimeMillis() + 10000));

        // when
        tm.createTimerWithPeriod(tss, TimerType.BILLING_INVOCATION, 0L,
                Period.DAY);
        // then
        assertEquals(1, tss.getTimers().size());
    }

    @Test
    public void isTimerCreated_noTypeMatch() {
        // given
        prepareTimerList(TimerType.BILLING_INVOCATION,
                new Date(System.currentTimeMillis()));
        prepareTimerList(TimerType.DISCOUNT_END_CHECK,
                new Date(System.currentTimeMillis()));
        // when
        boolean result = tm.isTimerCreated(TimerType.ORGANIZATION_UNCONFIRMED,
                tss);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));

    }

    @Test
    public void isTimerCreated_timerInvalid() {
        // given
        prepareTimerList(TimerType.BILLING_INVOCATION,
                new Date(System.currentTimeMillis()));
        // when
        boolean result = tm.isTimerCreated(TimerType.BILLING_INVOCATION, tss);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));

    }

    @Test
    public void isTimerCreated() {
        // given
        prepareTimerList(TimerType.DISCOUNT_END_CHECK,
                new Date(System.currentTimeMillis() + 10000));
        // when
        boolean result = tm.isTimerCreated(TimerType.DISCOUNT_END_CHECK, tss);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));

    }

    @Test
    public void isTimerCreated_cancelTimer() {
        // given
        prepareTimerList(TimerType.BILLING_INVOCATION,
                new Date(System.currentTimeMillis() - 10000));
        // when
        tm.isTimerCreated(TimerType.BILLING_INVOCATION, tss);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(timer.isCanceled()));

    }

    @SuppressWarnings({ "deprecation" })
    @Test
    public void getExpressionForPeriod_dayPeriod() {
        // given
        Date startDate = new Date(System.currentTimeMillis() + 10000);
        // when
        ScheduleExpression result = tm.getExpressionForPeriod(Period.DAY,
                startDate);
        // then
        assertEquals(startDate.getTime(), result.getStart().getTime());
        assertEquals(String.valueOf(startDate.getSeconds()), result.getSecond());
        assertEquals(String.valueOf(startDate.getMinutes()), result.getMinute());
        assertEquals(String.valueOf(startDate.getHours()), result.getHour());
    }

    @Test
    public void getExpressionForPeriod_dayPeriod_midnight() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        // when
        ScheduleExpression result = tm.getExpressionForPeriod(Period.DAY,
                startDate);
        // then
        assertEquals(startDate.getTime(), result.getStart().getTime());
        assertEquals("0", result.getSecond());
        assertEquals("0", result.getMinute());
        assertEquals("0", result.getHour());
    }

    @SuppressWarnings({ "deprecation" })
    @Test
    public void getExpressionForPeriod_monthPeriod() {
        // given
        Date startDate = new Date(System.currentTimeMillis() + 10000);
        // when
        ScheduleExpression result = tm.getExpressionForPeriod(Period.MONTH,
                startDate);
        // then
        assertEquals(startDate.getTime(), result.getStart().getTime());
        assertEquals(String.valueOf(startDate.getSeconds()), result.getSecond());
        assertEquals(String.valueOf(startDate.getMinutes()), result.getMinute());
        assertEquals(String.valueOf(startDate.getHours()), result.getHour());
        assertEquals(String.valueOf(startDate.getDate()),
                result.getDayOfMonth());
    }

    @SuppressWarnings({ "deprecation" })
    @Test
    public void getExpressionForPeriod_monthPeriod_midnightOfFirstDay() {
        // given
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        // when
        ScheduleExpression result = tm.getExpressionForPeriod(Period.MONTH,
                startDate);
        // then
        assertEquals(startDate.getTime(), result.getStart().getTime());
        assertEquals("0", result.getSecond());
        assertEquals("0", result.getMinute());
        assertEquals("0", result.getHour());
        assertEquals(String.valueOf(startDate.getDate()),
                result.getDayOfMonth());
    }

    private void prepareTimerList(TimerType timerType, Date date) {
        initTimer(timerType, date);
        tss.getTimers().add(timer);
    }

    private void initTimer(TimerType timerType, Date date) {
        timer = new TimerStub(0, timerType, date, false) {
            @Override
            public Date getNextTimeout() throws EJBException,
                    IllegalStateException, NoSuchObjectLocalException {

                return getExecDate();
            }

        };
        timer.setInfo(timerType);
    }
}
