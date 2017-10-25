/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-07-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.notification.vo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a notification with data on specific actions which are forwarded
 * to a notification service.
 */
public class VONotification implements Serializable {
    private static final long serialVersionUID = -5132459359639723769L;

    /**
     * The data of the notification as a list of properties.
     */
    private List<VOProperty> properties = new LinkedList<VOProperty>();

    /**
     * Retrieves the data of the notification.
     * 
     * @return the data as a list of properties
     */
    public List<VOProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the data of the notification.
     * 
     * @param properties
     *            the data as a list of properties
     */
    public void setProperties(List<VOProperty> properties) {
        this.properties = properties;
    }
}
