/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 8, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.facade;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;

/**
 * The facade to provide the service objects for task handling
 * 
 * @author tokoda
 */
public class ServiceFacade {

    private LocalizerServiceLocal localizerService;

    private CommunicationServiceLocal communicationService;

    private ApplicationServiceLocal applicationService;

    private IdentityServiceLocal identityService;

    private ConfigurationServiceLocal configurationService;

    private TaskQueueServiceLocal taskQueueService;

    private DataService dataService;

    private SubscriptionServiceLocal subscriptionService;
    
    private ExternalPriceModelService externalPriceModelService;

    public SubscriptionServiceLocal getSubscriptionService() {
        return subscriptionService;
    }

    public void setSubscriptionService(
            SubscriptionServiceLocal subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public LocalizerServiceLocal getLocalizerService() {
        return localizerService;
    }

    public void setLocalizerService(LocalizerServiceLocal localizerService) {
        this.localizerService = localizerService;
    }

    public CommunicationServiceLocal getCommunicationService() {
        return communicationService;
    }

    public void setCommunicationService(
            CommunicationServiceLocal communicationService) {
        this.communicationService = communicationService;
    }

    public void setApplicationService(ApplicationServiceLocal applicationService) {
        this.applicationService = applicationService;
    }

    public ApplicationServiceLocal getApplicationService() {
        return applicationService;
    }

    public void setIdentityService(IdentityServiceLocal identityService) {
        this.identityService = identityService;
    }

    public IdentityServiceLocal getIdentityService() {
        return identityService;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public ConfigurationServiceLocal getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(
            ConfigurationServiceLocal configurationService) {
        this.configurationService = configurationService;
    }

    public TaskQueueServiceLocal getTaskQueueService() {
        return taskQueueService;
    }

    public void setTaskQueueService(TaskQueueServiceLocal taskQueueService) {
        this.taskQueueService = taskQueueService;
    }

    public ExternalPriceModelService getExternalPriceModelService() {
        return externalPriceModelService;
    }

    public void setExternalPriceModelService(
            ExternalPriceModelService externalPriceModelService) {
        this.externalPriceModelService = externalPriceModelService;
    }
}
