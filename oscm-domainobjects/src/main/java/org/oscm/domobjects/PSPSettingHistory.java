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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The history object for a psp setting.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "PSPSettingHistory.findByObject", query = "SELECT c FROM PSPSettingHistory c WHERE c.objKey=:objKey ORDER BY objversion"),
        @NamedQuery(name = "PSPSettingHistory.findForPSP", query = "SELECT psh FROM PSPSettingHistory psh WHERE psh.pspObjKey = :pspObjKey AND psh.objVersion = (SELECT MAX(ipsh.objVersion) FROM PSPSettingHistory ipsh WHERE psh.objKey = ipsh.objKey)") })
public class PSPSettingHistory extends DomainHistoryObject<PSPSettingData> {

    private static final long serialVersionUID = 5576603249617875704L;

    @Column(nullable = false)
    private long pspObjKey;

    public PSPSettingHistory() {
        dataContainer = new PSPSettingData();
    }

    /**
     * Constructs PSPSettingHistory from a PSPSetting domain object
     * 
     * @param c
     *            - the psp setting
     */
    public PSPSettingHistory(PSPSetting c) {
        super(c);
        if (c.getPsp() != null) {
            setPspObjKey(c.getPsp().getKey());
        }
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

    public void setPspObjKey(long pspObjKey) {
        this.pspObjKey = pspObjKey;
    }

    public long getPspObjKey() {
        return pspObjKey;
    }

}
