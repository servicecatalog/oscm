/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.usage;

import static java.lang.Long.parseLong;
import static org.oscm.app.openstack.controller.OpenStackController.ID;
import static org.oscm.app.openstack.controller.PropertyHandler.TIMER_INTERVAL;

import java.util.Collection;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class EventTimer {

    private static final Logger LOG = LoggerFactory.getLogger(EventTimer.class);

    /**
     * The default timer interval in milliseconds, used when the corresponding
     * controller setting is undefined.
     */
    private static final long DEFAULT_INTERVAL = 1000 * 60 * 60 * 4;

    private static final String TIMER_INFO = "OSCM_OPENSTACK_EVENT_GENERATION_TIMER";

    @Resource
    private TimerService timerService;

    private long timerInterval;

    @PostConstruct
    public void initializeTimer() {
	cancelTimer();
	timerInterval = getTimerInterval();
	timerService.createTimer(0, timerInterval, TIMER_INFO);
	LOG.info("Openstack event timer is up and running with an interval of " + timerInterval);
    }

    /**
     * Handles the timer event.
     */
    @Timeout
    public void handleTimer(Timer timer) {
	adaptTimerToNewInterval();
	try {
	    AppDb appDb = new AppDb();
	    PasswordAuthentication auth = appDb.loadTechnologyProviderCredentials().toPasswordAuthentication();
	    APPlatformService app = APPlatformServiceFactory.getInstance();
	    for (String instanceId : app.listServiceInstances(ID, auth)) {
		ProvisioningSettings settings = app.getServiceInstanceDetails(ID, instanceId, auth);
		PropertyHandler ph = new PropertyHandler(settings);
		if ("OS::Keystone::Project".equals(ph.getResourceType())) {
		    ph.setInstanceId(instanceId);
		    if (ph.isCharging()) {
			new UsageConverter(ph).registerUsageEvents();
		    }
		}
	    }
	} catch (Exception e) {
	    LOG.error("handleTimer", e);
	}
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
     * Returns the timer interval which is stored in the configuration settings. If
     * the setting does not exist, e.g. bootstrapping case, then the default value
     * is returned.
     */
    private long getTimerInterval() {
	try {
	    HashMap<String, String> controllerSettings = new AppDb().getControllerSettings();
	    String interval = controllerSettings.get(TIMER_INTERVAL);
	    return interval != null ? parseLong(interval) : DEFAULT_INTERVAL;
	} catch (Exception e) {
	    // either bootstrap case, settings cannot be read yet, or the
	    // configuration setting cannot be parsed as a long value
	}

	return DEFAULT_INTERVAL;
    }
}
