/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Ronny Weiser                                                      
 *                                                                              
 *  Creation Date: 14.01.2010                                                      
 *                                                                              
 *  Completion Time: 14.01.2010                                        
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.SettingType;

/**
 * The organization setting domain object
 * 
 * @author weiser
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "organization_tkey", "settingtype" }))
@NamedQueries({
        @NamedQuery(name = "OrganizationSetting.findByBusinessKey", query = "SELECT obj FROM OrganizationSetting obj WHERE obj.dataContainer.settingType = :settingType and obj.organization = :organization"),
        @NamedQuery(name = "OrganizationSetting.removeAllForOrganization", query = "DELETE FROM OrganizationSetting obj WHERE obj.organization = :organization") })
@BusinessKey(attributes = { "organization", "settingType" })
public class OrganizationSetting extends
        DomainObjectWithVersioning<OrganizationSettingData> {

    private static final long serialVersionUID = -507878877790568440L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organization organization;

    public OrganizationSetting() {
        dataContainer = new OrganizationSettingData();
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
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
