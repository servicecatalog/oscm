/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;

/**
 * Value object representing a PSP setting.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VOPSPSetting extends BaseVO {

    private static final long serialVersionUID = -5259400577447474766L;

    private String settingKey;
    private String settingValue;

    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

}
