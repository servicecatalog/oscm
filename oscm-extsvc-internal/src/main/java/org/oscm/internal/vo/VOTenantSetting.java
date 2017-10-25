/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.internal.vo;

import org.oscm.internal.types.enumtypes.IdpSettingType;

public class VOTenantSetting extends BaseVO {
    public IdpSettingType name;
    public String value;
    public VOTenant voTenant;

    public IdpSettingType getName() {
        return name;
    }

    public void setName(IdpSettingType name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public VOTenant getVoTenant() {
        return voTenant;
    }

    public void setVoTenant(VOTenant voTenant) {
        this.voTenant = voTenant;
    }
}
