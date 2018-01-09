/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2017-05-05
 *
 *******************************************************************************/
package org.oscm.app.v2_0.service;

import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformController;

/**
 * Created by BadziakP on 2017-05-04.
 *
 * This class is a timer which gets number of VMs from the cloud platform and
 * updates respective subscription parameters
 */

@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@LocalBean
@Startup
public class TimerRefreshSubscriptions {

	private static final String VM_TIMER_INFO = "abc2dac0-5f81-11e4-9803-0800200c9a66";
	private static final long DEFAULT_TIMER_INTERVAL = 86400000;

	@Inject
	protected ServiceInstanceServiceBean serviceInstanceService;

	@Resource
	protected TimerService timerService;

	@EJB
	protected APPConfigurationServiceBean configService;

	@Inject
	protected transient Logger logger;

	@PostConstruct
	public void setTimer() {
		Collection<Timer> timers = timerService.getTimers();
		for (Timer timerVM : timers) {
			if (VM_TIMER_INFO.equals(timerVM.getInfo())) {
				timerVM.cancel();
			}
		}
		logger.info("Timer for subscription VMs will be created.");
		try {
			String timerIntervalSetting = configService
					.getProxyConfigurationSetting(PlatformConfigurationKey.APP_TIMER_REFRESH_SUBSCRIPTIONS);
			long interval = Long.parseLong(timerIntervalSetting);
			timerService.createTimer(0, interval, VM_TIMER_INFO);
			// timerService.createIntervalTimer(new Date(), interval,
			// new TimerConfig());
		} catch (ConfigurationException e) {
			timerService.createTimer(0, DEFAULT_TIMER_INTERVAL, VM_TIMER_INFO);
			logger.info("Timer interval for refreshing subcription VMs not set, switch to default 10 min.");
		}
	}

	@Timeout
	public void execute(Timer timer) {
		if (!VM_TIMER_INFO.equals(timer.getInfo())) {
			return;
		}
		List<ServiceInstance> instances = serviceInstanceService.getInstances();
		for (ServiceInstance serviceInstance : instances) {
			try {
				final APPlatformController controller = APPlatformControllerFactory
						.getInstance(serviceInstance.getControllerId());

				int vmsNumber = controller.getServersNumber(serviceInstance.getInstanceId(),
						serviceInstance.getSubscriptionId(), serviceInstance.getOrganizationId());

				ServiceInstance updatedServiceInstance = serviceInstanceService.updateVmsNumber(serviceInstance,
						vmsNumber);
				serviceInstanceService.notifySubscriptionAboutVmsNumber(updatedServiceInstance);
			} catch (APPlatformException e) {
				logger.error("Subscription cannot be notified about VMs number: ", e);
			}
		}
	}

}
