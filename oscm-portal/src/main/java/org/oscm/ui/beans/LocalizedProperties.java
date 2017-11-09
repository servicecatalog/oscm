/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld //TODO                                                      
 *                                                                              
 *  Creation Date: 10.07.2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.util.Properties;

/**
 * @author cheld
 *
 */
public class LocalizedProperties {

    String locale;

    Properties properties;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
