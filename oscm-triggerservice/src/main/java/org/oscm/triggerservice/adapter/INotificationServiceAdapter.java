/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.adapter;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.notification.intf.NotificationService;

/**
 * Interface to represent the version independent functionality of the
 * notification service.
 * 
 * @author weiser
 * 
 */
public interface INotificationServiceAdapter extends NotificationService {

    /**
     * Sets the reference to the web service , that implements the
     * version-appropriate NotificationService.
     * 
     * @param notificationService
     *            the reference to the notification service
     */
    public void setNotificationService(Object notificationService);

    /**
     * Sets the reference to the configuration service that may be required for
     * reading certain configuration settings for compatibility reasons.
     * 
     * @param configurationService
     *            the {@link ConfigurationServiceLocal}
     */
    public void setConfigurationService(
            ConfigurationServiceLocal configurationService);

    /**
     * Sets the reference to the data service that may be required for map an
     * VOOrganization object to V1.1
     * 
     * @param dataService
     *            the {@link DataService}
     */
    public void setDataService(DataService dataService);
}
