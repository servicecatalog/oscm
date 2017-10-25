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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Represents a setting for a particular PSP.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
public class PSPSetting extends DomainObjectWithHistory<PSPSettingData> {

    private static final long serialVersionUID = 1243604247108490960L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PSP psp;

    public PSPSetting() {
        super();
        dataContainer = new PSPSettingData();
    }

    public String getSettingKey() {
        return dataContainer.getSettingKey();
    }

    public String getSettingValue() {
        return dataContainer.getSettingValue();
    }

    public void setSettingKey(String key) {
        dataContainer.setSettingKey(key);
    }

    public void setSettingValue(String value) {
        dataContainer.setSettingValue(value);
    }

    public void setPsp(PSP psp) {
        this.psp = psp;
    }

    public PSP getPsp() {
        return psp;
    }

}
