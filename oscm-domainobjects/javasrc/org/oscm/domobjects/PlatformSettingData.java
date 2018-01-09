/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.SettingTypeConverter;
import org.oscm.domobjects.converters.UserAccountStatusConverter;
import org.oscm.internal.types.enumtypes.SettingType;

/**
 * Data container for the PlatformSetting domain object
 * 
 * @author groch
 * 
 */
@Embeddable
public class PlatformSettingData extends DomainDataContainer {

    private static final long serialVersionUID = 3881765988772914637L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettingType settingType;

    private String settingValue;

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingValue() {
        return settingValue;
    }

}
