/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.v2_0.service;

import static java.lang.Long.parseLong;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a timer implementation which polls the controller to gather
 * usage data and send back to BES respective events, which can be priced. This
 * provides the base for charging uses based on true cloud resource consumption.
 * 
 * @author goebel
 *
 */
@Singleton
@Startup
public class TimerHandleUsageData {

    private static final Logger LOG = LoggerFactory
            .getLogger(TimerHandleUsageData.class);

    /**
     * The default timer interval in milliseconds, used when the corresponding
     * controller setting is undefined.
     */
    private static final long DEFAULT_INTERVAL = 1000 * 60 * 60 * 4;

    private static final String TIMER_INFO = "EVENT_GENERATION_TIMER";

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @Inject
    protected ServiceInstanceServiceBean serviceInstanceService;
    
    @Inject
    protected ServiceInstanceDAO siDao;

    @EJB
    protected APPConfigurationServiceBean configService;

    @Resource
    private TimerService timerService;

    private long timerInterval;

    @PostConstruct
    public void initializeTimer() {
        cancelTimer();
        timerInterval = getTimerInterval();
        timerService.createTimer(0, timerInterval, TIMER_INFO);
        LOG.info("Event timer is up and running with an interval of "
                + timerInterval);
    }

    /**
     * Handles the timer event.
     */
    @Timeout
    public void handleTimer(Timer timer) {
        adaptTimerToNewInterval();

        APPlatformService ps = APPlatformServiceFactory.getInstance();
        List<ServiceInstance> instances = serviceInstanceService.getInstances();
        for (ServiceInstance serviceInstance : instances) {
            try {

                final APPlatformController controller = APPlatformControllerFactory
                        .getInstance(serviceInstance.getControllerId());

                ProvisioningSettings settings = ps.getServiceInstanceDetails(
                        serviceInstance.getControllerId(),
                        serviceInstance.getInstanceId(),
                        serviceInstance.getSubscriptionId(),
                        serviceInstance.getOrganizationId());

                // handle usage data
                String startTime = getStartTime(serviceInstance);
                String endTime = getEndTime(serviceInstance);

                handleUsageData(serviceInstance, controller, settings,
                        startTime, endTime);

            } catch (Exception e) {
                LOG.error("handleTimer", e);
            }
        }
    }

    private void handleUsageData(ServiceInstance serviceInstance,
            final APPlatformController controller,
            ProvisioningSettings settings, String startTime, String endTime)
            throws SQLException, Exception {

        boolean updated = handleUsage(serviceInstance, controller, settings,
                startTime, endTime);

        if (updated) {
            updateLastUsageFetch(serviceInstance, endTime);
        }
    }

    protected boolean handleUsage(ServiceInstance serviceInstance,
            final APPlatformController controller,
            ProvisioningSettings settings, String startTime, String endTime) {
        boolean updated = false;
        try {
            updated = controller.gatherUsageData(
                    serviceInstance.getControllerId(),
                    serviceInstance.getInstanceId(), startTime, endTime,
                    settings);
        } catch (Exception ex) {
            LOG.error("handleUsageData", ex);
        }
        return updated;
    }

    private void adaptTimerToNewInterval() {
        long newInterval = getTimerInterval();
        if (timerInterval != newInterval) {
            cancelTimer();
            timerInterval = newInterval;
            timerService.createTimer(0, timerInterval, TIMER_INFO);
        }
    }

    private void cancelTimer() {
        Collection<Timer> timers = timerService.getTimers();
        for (Timer th : timers) {
            if (TIMER_INFO.equals(th.getInfo())) {
                th.cancel();
                return;
            }
        }
    }

    /**
     * Returns the timer interval which is stored in the configuration settings.
     * If the setting does not exist, e.g. bootstrapping case, then the default
     * value is returned.
     */
    private long getTimerInterval() {
        try {
            String timerIntervalSetting = configService
                    .getProxyConfigurationSetting(
                            PlatformConfigurationKey.APP_TIMER_REFRESH_USAGEDATA);

            return timerIntervalSetting != null
                    ? parseLong(timerIntervalSetting)
                    : DEFAULT_INTERVAL;
        } catch (Exception e) {
            // either bootstrap case, settings cannot be read yet, or the
            // configuration setting cannot be parsed as a long value
        }

        return DEFAULT_INTERVAL;
    }

    String getStartTime(ServiceInstance serviceInstance) throws Exception {
        String startTime = getLastUsageTime(serviceInstance);
        if (null == startTime || startTime.trim().length() == 0) {
            startTime = getRequestTime(serviceInstance);
        }
        return startTime;
    }

    String getRequestTime(ServiceInstance serviceInstance) throws Exception {
        long requestTime = serviceInstance.getRequestTime();
        return ofInstant(ofEpochMilli(requestTime), of("UTC"))
                .format(ISO_LOCAL_DATE_TIME);
    }

   
    String getLastUsageTime(ServiceInstance instance) {
        try {

            em.refresh(instance);
            Setting lastUsageFetch = instance.getParameterMap().get("LAST_USAGE_FETCH");
            if (lastUsageFetch != null) {
                return lastUsageFetch.getValue();
            }
        } catch (BadResultException e) {
            LOG.debug(e.getMessage());
        }

        return null;
    }

    String getEndTime(ServiceInstance serviceInstance) {
        return now(of("UTC")).format(ISO_LOCAL_DATE_TIME);
    }

    void updateLastUsageFetch(ServiceInstance serviceInstance, String endTime)
            throws SQLException, Exception {
        siDao.updateParam(serviceInstance, endTime, "LAST_USAGE_FETCH");
    }

}
