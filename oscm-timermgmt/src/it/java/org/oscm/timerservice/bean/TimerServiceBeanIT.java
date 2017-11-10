/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.timerservice.bean;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TimerProcessing;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.timerservice.stubs.AccountServiceStub;
import org.oscm.timerservice.stubs.TimerServiceStub;
import org.oscm.timerservice.stubs.TimerStub;
import org.oscm.types.enumtypes.Period;
import org.oscm.types.enumtypes.TimerType;

@SuppressWarnings("boxing")
public class TimerServiceBeanIT extends EJBTestBase {

    private AccountServiceStub accountManagementStub;
    private TimerServiceBean tm;
    private DataService mgr;
    ConfigurationServiceLocal cfgs;
    private TimerServiceStub tss;
    private SessionContext ctx;

    @Override
    public void setup(TestContainer container) throws Exception {

        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(accountManagementStub = new AccountServiceStub());
        container.addBean(Mockito.mock(SubscriptionServiceLocal.class));
        container.addBean(Mockito.mock(BillingServiceLocal.class));
        container.addBean(new PaymentServiceStub());
        container.addBean(new IdentityServiceStub());
        container.addBean(tm = new TimerServiceBean());
        tss = new TimerServiceStub() {
            @Override
            public Timer createTimer(Date arg0, Serializable arg1)
                    throws IllegalArgumentException, IllegalStateException,
                    EJBException {
                getTimers().add(new TimerStub(0, arg1, arg0, false) {
                    @Override
                    public Date getNextTimeout() throws EJBException,
                            IllegalStateException, NoSuchObjectLocalException {

                        return getExecDate();
                    }

                });
                return null;
            }
        };
        ctx = Mockito.mock(SessionContext.class);
        Mockito.when(ctx.getTimerService()).thenReturn(tss);
        tm.ctx = ctx;
        mgr = container.get(DataService.class);
        cfgs = container.get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfgs);
    }

    @Test
    public void testHandleTimer() throws Exception {
        // invoke the timer
        final TimerStub timerStub = new TimerStub();
        timerStub.setInfo(TimerType.ORGANIZATION_UNCONFIRMED);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.handleTimer(timerStub);
                return null;
            }
        });
        Assert.assertTrue("business logic was not invoked by the timer",
                accountManagementStub.hasCalledRemoveOverdueCustomers);
        // also check if the timer related entry was created and updated in the
        // database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkTimerHandlingEntryExistence(true,
                        TimerType.ORGANIZATION_UNCONFIRMED);
                return null;
            }
        });
    }

    @Test
    public void testHandleTimer_userNum() throws Exception {
        // invoke the timer
        final TimerStub timerStub = new TimerStub();
        timerStub.setInfo(TimerType.USER_NUM_CHECK);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.handleTimer(timerStub);
                return null;
            }
        });
        Assert.assertTrue("business logic was not invoked by the timer",
                accountManagementStub.hasCalledCheckUserNum);
        // also check if the timer related entry was created and updated in the
        // database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkTimerHandlingEntryExistence(true,
                        TimerType.USER_NUM_CHECK);
                return null;
            }
        });
    }

    private void checkTimerHandlingEntryExistence(
            boolean expectedSuccessSetting, TimerType type) {
        Query query = mgr.createQuery("select t from TimerProcessing t");
        @SuppressWarnings("unchecked")
        List<TimerProcessing> finds = query.getResultList();
        TimerProcessing find = null;
        if (finds != null && !finds.isEmpty()) {
            for (TimerProcessing t : finds)
                if (type.equals(t.getTimerType())) {
                    find = t;
                    break;
                }
        }
        Assert.assertNotNull("Timer entry was not created", find);
        Assert.assertEquals("Wrong timer type stored", type,
                find.getTimerType());
        Assert.assertEquals("Wrong result of timer handling stored",
                expectedSuccessSetting, find.isSuccess());
    }

    @Test
    public void testHandleTimerException() throws Exception {
        // invoke the timer
        accountManagementStub.shouldMethodCallFail = true;
        final TimerStub timerStub = new TimerStub();
        timerStub.setInfo(TimerType.ORGANIZATION_UNCONFIRMED);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.handleTimer(timerStub);
                return null;
            }
        });
        Assert.assertTrue("business logic was not invoked by the timer",
                accountManagementStub.hasCalledRemoveOverdueCustomers);
        // also check if the timer related entry was created and updated in the
        // database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkTimerHandlingEntryExistence(false,
                        TimerType.ORGANIZATION_UNCONFIRMED);
                return null;
            }
        });
    }

    @Test
    public void testHandleTimerFailureInBL() throws Exception {
        // invoke the timer
        accountManagementStub.shouldMethodCallCauseException = false;
        accountManagementStub.shouldMethodCallFail = true;
        final TimerStub timerStub = new TimerStub();
        timerStub.setInfo(TimerType.ORGANIZATION_UNCONFIRMED);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.handleTimer(timerStub);
                return null;
            }
        });
        Assert.assertTrue("business logic was not invoked by the timer",
                accountManagementStub.hasCalledRemoveOverdueCustomers);
        // also check if the timer related entry was created and updated in the
        // database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkTimerHandlingEntryExistence(false,
                        TimerType.ORGANIZATION_UNCONFIRMED);
                return null;
            }
        });
    }

    @Test
    public void testHandleTimerInHandledTimeFrame() throws Exception {
        accountManagementStub.numberOfCalls = 0;
        accountManagementStub.shouldMethodCallCauseException = false;
        accountManagementStub.shouldMethodCallFail = false;
        final TimerStub timerStub = new TimerStub();
        timerStub.setInfo(TimerType.ORGANIZATION_UNCONFIRMED);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.handleTimer(timerStub);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Must not be executed
                tm.handleTimer(timerStub);
                return null;
            }
        });
        Assert.assertEquals(
                "Timer was handled twice, although the timeframe was too short",
                1, accountManagementStub.numberOfCalls);
    }

    @Test
    public void createTimer() throws Exception {
        // given
        final long startTime = DateTimeHandling
                .calculateMillis("2013-02-05 00:00:00");

        // when
        TimerProcessing result = runTX(new Callable<TimerProcessing>() {
            @Override
            public TimerProcessing call() throws Exception {
                return tm.createTimerProcessing(TimerType.BILLING_INVOCATION,
                        startTime);
            }
        });

        // then
        Assert.assertEquals(TimerType.BILLING_INVOCATION,
                result.getTimerType());
        Assert.assertEquals(startTime, result.getStartTime());
    }

    @Test
    public void createTimer_MaxDeviation() throws Exception {
        // given

        // when
        TimerProcessing result = runTX(new Callable<TimerProcessing>() {
            @Override
            public TimerProcessing call() throws Exception {
                tm.createTimerProcessing(TimerType.BILLING_INVOCATION,
                        DateTimeHandling
                                .calculateMillis("2013-02-05 00:00:00"));
                return tm.createTimerProcessing(TimerType.BILLING_INVOCATION,
                        DateTimeHandling
                                .calculateMillis("2013-02-05 20:00:00"));
            }
        });

        // then
        Assert.assertNull(result);
    }

    @Test
    public void createTimerWithPeriod_discountEndCheckTimerCreatedConcurrently()
            throws Exception {

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.createTimerWithPeriod(tss, TimerType.DISCOUNT_END_CHECK, 0L,
                        Period.DAY);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tm.createTimerWithPeriod(tss, TimerType.DISCOUNT_END_CHECK, 0L,
                        Period.DAY);
                return null;
            }
        });

        // then
        assertEquals(1, tss.getTimers().size());

    }

}
