/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.SettingType;

/**
 * The platform setting domain object.
 * 
 * @author groch
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "settingtype" }))
@NamedQueries({
        @NamedQuery(name = "PlatformSetting.findByBusinessKey", query = "SELECT obj FROM PlatformSetting obj WHERE obj.dataContainer.settingType = :settingType"),
        @NamedQuery(name = "PlatformSetting.getAll", query = "SELECT obj FROM PlatformSetting obj"),
        @NamedQuery(name = "PlatformSetting.removeAll", query = "DELETE FROM PlatformSetting") })
@BusinessKey(attributes = { "settingType" })
public class PlatformSetting extends
        DomainObjectWithVersioning<PlatformSettingData> {

    private static final long serialVersionUID = 3963938185142225585L;

    public PlatformSetting() {
        dataContainer = new PlatformSettingData();
    }

    public void setSettingType(SettingType settingType) {
        dataContainer.setSettingType(settingType);
    }

    public SettingType getSettingType() {
        return dataContainer.getSettingType();
    }

    public void setSettingValue(String settingValue) {
        dataContainer.setSettingValue(settingValue);
    }

    public String getSettingValue() {
        return dataContainer.getSettingValue();
    }

    public void setSettingValueFromEnum(Enum<?> value) {
        setSettingValue(value.name());
    }

    public <T extends Enum<T>> T getSettingAsEnumValue(Class<T> type) {
        return Enum.valueOf(type, getSettingValue());
    }
}
