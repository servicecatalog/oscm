/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Embeddable;

/**
 * The psp setting data container object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class PSPSettingData extends DomainDataContainer {

    private static final long serialVersionUID = -5963411873150963822L;

    /**
     * The key of the PSP related setting.
     */
    private String settingKey;

    /**
     * The value of the PSP related setting.
     */
    private String settingValue;

    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingKey(String key) {
        this.settingKey = key;
    }

    public void setSettingValue(String value) {
        this.settingValue = value;
    }

}
