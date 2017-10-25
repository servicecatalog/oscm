/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.timerservice.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TimerProcessing;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.Period;
import org.oscm.types.enumtypes.TimerType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTimerInfo;

/**
 * The timer management service bean that initializes the timers and controls
 * the invocation of the business logic as soon as a timer is expired.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@LocalBean
public class TimerServiceBean {

    /**
     * The time that has to pass before another timer of an already handled type
     * can be handled again.
     */
    private static final int TIMER_HANDLING_DISTANCE = 10000;

    /**
     * The factor that determines the time corridor to realize timer processing
     * entries as related to the current timer expiration.
     */
    private static final double DEVIATION_FACTOR = 0.95;

    private static Log4jLogger logger = LoggerFactory
            .getLogger(TimerServiceBean.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cfgMgmt;

    @EJB(beanInterface = AccountServiceLocal.class)
    protected AccountServiceLocal accMgmt;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    private SubscriptionServiceLocal subMgmt;

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = BillingServiceLocal.class)
    protected BillingServiceLocal bm;

    @EJB(beanInterface = PaymentServiceLocal.class)
    protected PaymentServiceLocal ps;

    @EJB(beanInterface = IdentityServiceLocal.class)
    protected IdentityServiceLocal idServiceLocal;

    @Resource
    protected SessionContext ctx;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void reInitTimers() throws ValidationException {
        cancelAllObsoleteTimer();
        initAllTimers();
    }

    /**
     * Reads the configuration settings for the timer intervals to be used and
     * creates the timers accordingly.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initTimers() throws ValidationException {
        initAllTimers();
    }

    private void initAllTimers() throws ValidationException {
        TimerType[] supportedTimers = TimerType.values();
        TimerService timerService = ctx.getTimerService();

        for (TimerType timerType : supportedTimers) {
            TimerIntervalDetails timerDetails = getTimerDetailsForTimerType(timerType);
            long timerInterval = timerDetails.getIntervalTime();
            long timerOffset = timerDetails.getIntervalOffset();
            Period period = timerDetails.getPeriod();

            // create required timers
            if (timerInterval > 0) {
                createTimerWithInterval(timerService, timerType, timerInterval,
                        timerOffset);
            } else if (period != null) {
                // if there is only a period, start a calendar timer.
                createTimerWithPeriod(timerService, timerType, timerOffset,
                        period);
            } else {
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.INFO_TIMER_NOT_INITIALIZED,
                        String.valueOf(timerType));
            }
        }

        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_TIMER_INITIALIZED);
    }

    private void createTimerWithInterval(TimerService timerService,
            TimerType timerType, long timerInterval, long timerOffset)
            throws ValidationException {
        // FIXME: this is quick fix for bug 11241. To avoid generate the
        // same constraint of timer, set timerInterval to
        // TIMER_HANDLING_DISTANCE if the timerInterval is smaller than
        // TIMER_HANDLING_DISTANCE.
        if (timerInterval <= TIMER_HANDLING_DISTANCE) {
            timerInterval = TIMER_HANDLING_DISTANCE;
        }

        Date nextStart = getDateForNextTimerExpiration(timerInterval,
                timerOffset);
        ValidationException validationException;
        String[] params;
        if (nextStart.getTime() < 0) {
            if (timerType.name().equals(TimerType.USER_NUM_CHECK.name())) {
                params = new String[] { timerType.name(),
                        timerType.getKeyForIntervalTime().name() };
                validationException = new ValidationException(
                        ValidationException.ReasonEnum.TIMER_USERCOUNT_EXPIRATIONDATE_INVALID,
                        null, params);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        validationException,
                        LogMessageIdentifier.ERROR_TIMER_USERCOUNT_EXPIRATIONDATE_INVALID,
                        params);
            } else {
                params = new String[] { timerType.name(),
                        timerType.getKeyForIntervalTime().name(),
                        timerType.getKeyForIntervalOffset().name() };
                validationException = new ValidationException(
                        ValidationException.ReasonEnum.TIMER_EXPIRATIONDATE_INVALID,
                        null, params);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        validationException,
                        LogMessageIdentifier.ERROR_TIMER_EXPIRATIONDATE_INVALID,
                        params);
            }
            throw validationException;
        }

        cancelObsoleteTimer(timerService, timerType);

        timerService.createTimer(nextStart, timerInterval, timerType);

        SimpleDateFormat sdf = new SimpleDateFormat();
        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_TIMER_CREATED_WITH_INTERVAL,
                timerType.toString(), sdf.format(nextStart),
                Long.toString(timerInterval));
    }

    void createTimerWithPeriod(TimerService timerService, TimerType timerType,
            long timerOffset, Period period) {
        if (isTimerCreated(timerType, timerService)) {
            return;
        }
        TimerConfig config = new TimerConfig();
        config.setInfo(timerType);
        Date startDate = getDateForNextTimerExpiration(period, timerOffset);
        ScheduleExpression schedleExpression = getExpressionForPeriod(period,
                startDate);
        Timer timer = timerService.createCalendarTimer(schedleExpression,
                config);
        Date nextStart = timer.getNextTimeout();
        SimpleDateFormat sdf = new SimpleDateFormat();
        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_TIMER_CREATED,
                String.valueOf(timerType), sdf.format(nextStart));
    }

    ScheduleExpression getExpressionForPeriod(Period period, Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        ScheduleExpression schedleExpression = new ScheduleExpression();
        schedleExpression.start(startDate);
        if (period == Period.DAY) {
            schedleExpression.second(cal.get(Calendar.SECOND));
            schedleExpression.minute(cal.get(Calendar.MINUTE));
            schedleExpression.hour(cal.get(Calendar.HOUR_OF_DAY));
        } else if (period == Period.MONTH) {
            schedleExpression.second(cal.get(Calendar.SECOND));
            schedleExpression.minute(cal.get(Calendar.MINUTE));
            schedleExpression.hour(cal.get(Calendar.HOUR_OF_DAY));
            schedleExpression.dayOfMonth(cal.get(Calendar.DAY_OF_MONTH));
        }
        return schedleExpression;
    }

    boolean isTimerCreated(TimerType timerType, TimerService timerService) {
        for (Timer timer : ParameterizedTypes.iterable(
                timerService.getTimers(), Timer.class)) {
            TimerType tType = (TimerType) timer.getInfo();
            if ((TimerType.BILLING_INVOCATION.equals(tType) && TimerType.BILLING_INVOCATION
                    .equals(timerType))
                    || (TimerType.DISCOUNT_END_CHECK.equals(tType) && TimerType.DISCOUNT_END_CHECK
                            .equals(timerType))) {
                long currentTime = System.currentTimeMillis();
                if (timer.getNextTimeout().getTime() - currentTime > 0) {
                    return true;
                } else {
                    timer.cancel();
                }
            }
        }
        return false;
    }

    /**
     * Determines all currently queued timers and cancel timer with target type.
     * 
     * @param timerService
     *            The timer service.
     * @param timerType
     *            The timer type.
     */
    private void cancelObsoleteTimer(TimerService timerService,
            TimerType timerType) {

        for (Timer timer : ParameterizedTypes.iterable(
                timerService.getTimers(), Timer.class)) {
            Serializable info = timer.getInfo();
            if (info != null && info instanceof TimerType && timerType == info) {
                TimerType type = (TimerType) info;
                timer.cancel();
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.INFO_TIMER_REMOVED,
                        String.valueOf(type));
            }
        }

    }

    /**
     * Determines all currently queued timers and cancels them.
     */
    private void cancelAllObsoleteTimer() {
        for (Timer timer : ParameterizedTypes.iterable(ctx.getTimerService()
                .getTimers(), Timer.class)) {
            Serializable info = timer.getInfo();
            if (info != null && info instanceof TimerType) {
                TimerType type = (TimerType) info;
                timer.cancel();
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.INFO_TIMER_REMOVED,
                        String.valueOf(type));
            }
        }

    }

    /**
     * Returns the timer details for the given timer type as specified in the
     * database.
     * 
     * @param timerType
     *            The type of the timer.
     * @return The interval details for the timer type.
     */
    TimerIntervalDetails getTimerDetailsForTimerType(final TimerType timerType) {
        final long timerInterval = cfgMgmt
                .getLongConfigurationSetting(timerType.getKeyForIntervalTime(),
                        Configuration.GLOBAL_CONTEXT);

        long timerOffset = 0;
        if (TimerType.BILLING_INVOCATION.equals(timerType)) {
            timerOffset = cfgMgmt.getBillingRunStartTimeInMs();
        } else if (TimerType.USER_NUM_CHECK.equals(timerType)) {
            timerOffset = 0;
        } else {
            timerOffset = cfgMgmt.getLongConfigurationSetting(
                    timerType.getKeyForIntervalOffset(),
                    Configuration.GLOBAL_CONTEXT);
        }

        final Period period = timerType.getPeriodSetting();
        return new TimerIntervalDetails(timerInterval, timerOffset, period);
    }

    /**
     * Handles the timers as soon as they are expired and the container invokes
     * this callback method. If the timer is not a periodic timer, it will also
     * be re-initialized.
     * 
     * @param timer
     *            The expired timer provided by the system.
     */
    @Timeout
    public void handleTimer(Timer timer) {

        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_TIMER_TIMEOUT_RETRIEVED,
                String.valueOf(timer.getInfo()));

        // initial assumption on the outcome of the business logic invocation is
        // to that it failed
        boolean outcome = false;

        TimerType timerType = (TimerType) timer.getInfo();
        long currentTime = System.currentTimeMillis();

        // 1. create the timer processing data entry in the database, required
        // to avoid other nodes from handling the same task
        TimerProcessing processingData = createTimerProcessing(timerType,
                currentTime);

        // 2. handle the timer
        if (processingData != null) {
            try {
                switch (timerType) {
                case ORGANIZATION_UNCONFIRMED:
                    outcome = accMgmt.removeOverdueOrganizations(currentTime);
                    break;
                case RESTRICTED_SUBSCRIPTION_USAGE_PERIOD:
                    outcome = subMgmt.expireOverdueSubscriptions(currentTime);
                    break;
                case TENANT_PROVISIONING_TIMEOUT:
                    outcome = subMgmt
                            .notifyAboutTimedoutSubscriptions(currentTime);
                    break;
                case BILLING_INVOCATION:
                    outcome = bm.startBillingRun(currentTime);
                    outcome = ps.chargeForOutstandingBills() && outcome;
                    break;
                case DISCOUNT_END_CHECK:
                    outcome = accMgmt
                            .sendDiscountEndNotificationMail(currentTime);
                    break;
                case INACTIVE_ON_BEHALF_USERS_REMOVAL:
                    outcome = idServiceLocal.removeInactiveOnBehalfUsers();
                    break;
                case USER_NUM_CHECK:
                    outcome = accMgmt.checkUserNum();
                    break;
                default:
                    logger.logError(LogMessageIdentifier.ERROR_TIMER_TIMEOUT_FOR_UNKNOWN_TYPE);

                }

            } catch (Exception e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_HANDLE_TIMER_FAILED);
            }

            // 3. update the created timer processing entry, update the duration
            // field and the success flag
            updateTimerProcessing(processingData, outcome);
        } else {
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_TIMER_NO_HANDLING);

        }

    }

    /**
     * Tries to create a timer processing data entry. If there has already been
     * a handling for the same timer type within 95% of the timer interval time,
     * or if the database entry cannot be written (concurrent handling
     * scenario), the method will return <code>null</code> to indicate that the
     * handling can be aborted.
     * 
     * @param type
     *            The type of the timer.
     * @param startTime
     *            The start time for the timer related task handling.
     * @return <code>null</code> in case the handling should be aborted, the
     *         timer processing data otherwise.
     */
    TimerProcessing createTimerProcessing(TimerType type, long startTime) {

        TimerProcessing data = null;

        // 1. check if another node has already handled the task first
        TimerIntervalDetails timerIntervalDetails = getTimerDetailsForTimerType(type);
        long timerInterval = timerIntervalDetails.getIntervalTime();
        if (timerInterval == 0 && timerIntervalDetails.getPeriod() != null) {
            timerInterval = timerIntervalDetails.getPeriod().getDuration(
                    startTime);
        }
        long maxDeviation = (long) (DEVIATION_FACTOR * timerInterval);
        long lowerTimeBound = startTime - maxDeviation;
        Query query = dm
                .createNamedQuery("TimerProcessing.findDataForTimeAndTimeframe");
        query.setParameter("timerType", type);
        query.setParameter("lowerTimeBound", Long.valueOf(lowerTimeBound));
        List<?> resultList = query.getResultList();

        if (resultList.isEmpty()) {
            // 2. try to create the entry.
            data = new TimerProcessing();
            data.setNodeName(cfgMgmt.getNodeName());
            data.setTimerType(type);
            data.setStartTime(startTime);
            data.setSuccess(true);
            data.setStartTimeMutex(startTime / TIMER_HANDLING_DISTANCE);
            try {
                dm.persist(data);
                dm.flush();
            } catch (Exception e) {
                // insertion failed, so another node is already working on the
                // task..., simply abort by returning null
                data = null;
            }
        }

        return data;
    }

    private void updateTimerProcessing(TimerProcessing data, boolean result) {

        long duration = System.currentTimeMillis() - data.getStartTime();
        data.setDuration(duration);
        data.setSuccess(result);

    }

    /**
     * Determines the next time the timer will expire. To do so the first day of
     * the current year, at 0:00:00 time will be used. The intervalTime will be
     * added until we have a date that lies ahead from now. Then this date will
     * be returned.
     * 
     * @param timerInterval
     *            Interval for the timer in milliseconds, is expected to not be
     *            below 0.
     * @return The date representing the next timer expiration time.
     */
    private Date getDateForNextTimerExpiration(long timerInterval,
            long offsetTime) {

        // init a calendar settled for the first day of the years, midnight
        Calendar cal = initCalendarForFirstDayOfYear();

        long startPoint = cal.getTimeInMillis();
        long currentTime = GregorianCalendar.getInstance().getTimeInMillis();

        long difference = currentTime - startPoint;

        // calculate how many times the timer would have been executed if
        // started at the first day of the year
        long numberPossibleExecutions = difference / timerInterval;

        // calculate possible new date and return it
        long nextExecutionTimeWithinOffset = startPoint
                + (numberPossibleExecutions * timerInterval) + offsetTime;
        if (nextExecutionTimeWithinOffset - currentTime > 0) {
            // offset already in future
            cal.setTimeInMillis(nextExecutionTimeWithinOffset);
        } else {
            // next guaranteed possible execution is the one we need
            numberPossibleExecutions = numberPossibleExecutions + 1;
            long nextExecutionTime = startPoint
                    + (numberPossibleExecutions * timerInterval) + offsetTime;
            cal.setTimeInMillis(nextExecutionTime);
        }

        return cal.getTime();
    }

    /**
     * Determines the next time the timer will expire. To do so the first day of
     * the current year, at 0:00:00 time will be used. The period will be added
     * until we have a date that lies ahead from now. Then this date will be
     * returned. The offset will be added to this date as well.
     * 
     * @param period
     *            The period to be considered.
     * @param timerOffset
     *            The offset to be added to the starting time.
     * @return The Date the timer has to expire.
     */
    private Date getDateForNextTimerExpiration(Period period, long timerOffset) {

        Calendar cal = initCalendarForFirstDayOfYear();

        // now consider the offset with this date
        // as the calendar API does not support adding ms as long parameter, we
        // implement it on our own.
        if (timerOffset >= 0) {
            long initialStartTime = cal.getTimeInMillis() + timerOffset;
            cal.setTimeInMillis(initialStartTime);
        } else {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_NEGATIVE_TIME_SPECIFIED_FOR_TIMER,
                    Long.toString(timerOffset));
        }

        int field = getCalendarFieldForPeriod(period);

        Calendar currentCal = Calendar.getInstance();
        cal.add(field, currentCal.get(field) - cal.get(field));
        if (cal.before(currentCal)) {
            cal.add(field, 1);
        }

        long nextExecTime = cal.getTimeInMillis();

        cal.setTimeInMillis(nextExecTime);

        return cal.getTime();
    }

    /**
     * Determines the calendar field object that corresponds to the given
     * period.
     * 
     * @param period
     *            The specifie period.
     * @return The calendar field.
     */
    private int getCalendarFieldForPeriod(Period period) {
        int field;
        switch (period) {
        case MONTH:
            field = Calendar.MONTH;
            break;
        case DAY:
            field = Calendar.DAY_OF_YEAR;
            break;
        default:
            // init the default period to day of year.
            field = Calendar.DAY_OF_YEAR;
        }
        return field;
    }

    /**
     * Initializes and returns a calendar representing the first day of the
     * current years, at time 0:00.
     * 
     * @return The calendar for the 1st Jan of current year.
     */
    private Calendar initCalendarForFirstDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Wraps the timer interval and the timer offset settings for a particular
     * timer type.
     * 
     * @author Mike J&auml;ger
     * 
     */
    private class TimerIntervalDetails {

        private final long intervalTime;
        private final long intervalOffset;
        private final Period period;

        TimerIntervalDetails(long intervalTime, long intervalOffset,
                Period period) {
            this.intervalTime = intervalTime;
            this.intervalOffset = intervalOffset;
            this.period = period;
        }

        long getIntervalTime() {
            return intervalTime;
        }

        long getIntervalOffset() {
            return intervalOffset;
        }

        Period getPeriod() {
            return period;
        }

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<VOTimerInfo> getCurrentTimerExpirationDates() {
        List<VOTimerInfo> result = new ArrayList<VOTimerInfo>();

        for (Timer timer : ParameterizedTypes.iterable(ctx.getTimerService()
                .getTimers(), Timer.class)) {
            Serializable info = timer.getInfo();
            if (info != null && info instanceof TimerType) {
                TimerType type = (TimerType) info;
                long expirationTime = timer.getTimeRemaining()
                        + System.currentTimeMillis();
                VOTimerInfo timerInfo = new VOTimerInfo();
                timerInfo.setTimerType(type.name());
                timerInfo.setExpirationDate(new Date(expirationTime));
                result.add(timerInfo);
            }
        }

        return result;
    }
}
