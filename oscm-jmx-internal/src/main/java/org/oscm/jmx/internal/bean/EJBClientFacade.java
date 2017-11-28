/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.jmx.internal.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.ConfigurationService;

@Stateless
public class EJBClientFacade {
    @EJB
    private BillingService billingService;

    @EJB
    private ConfigurationService configurationService;

    public BillingService getBillingService() {
        return billingService;
    }

    public boolean startBillingRun(long startTime) {
        return billingService.startBillingRun(startTime);
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationSetting(String informationId, String value) {
        configurationService.setConfigurationSetting(informationId, value);
    }
}
