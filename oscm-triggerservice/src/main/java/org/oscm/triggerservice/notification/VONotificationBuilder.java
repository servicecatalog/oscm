/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.notification;

import java.util.LinkedList;
import java.util.List;

import org.oscm.notification.vo.VONotification;
import org.oscm.notification.vo.VOProperty;

/**
 * Class needed to create a VONotification.
 * 
 * @author Enes Sejfi
 */
public class VONotificationBuilder {
    private List<VOProperty> properties;

    public VONotificationBuilder() {
        reset();
    }

    public void reset() {
        properties = new LinkedList<VOProperty>();
    }

    public VONotificationBuilder addParameter(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Parameter name is null.");
        }
        if (containsKey(name)) {
            throw new IllegalArgumentException("Parameter name " + name
                    + " already added.");
        }
        VOProperty nameValue = new VOProperty();
        nameValue.setName(name);
        nameValue.setValue(value);
        properties.add(nameValue);
        return this;
    }

    public VONotification build() {
        VONotification notification = new VONotification();
        notification.setProperties(properties);
        return notification;
    }

    private boolean containsKey(String name) {
        for (VOProperty nameValue : properties) {
            if (nameValue.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
