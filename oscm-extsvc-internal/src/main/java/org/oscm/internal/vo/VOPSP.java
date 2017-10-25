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

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Value object to represent a PSP.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VOPSP extends BaseVO {

    private static final long serialVersionUID = 5009488619617867496L;

    private String wsdlUrl;
    private String id;
    private String distinguishedName;
    private List<VOPSPSetting> pspSettings = new ArrayList<VOPSPSetting>();
    private List<VOPaymentType> paymentTypes = new ArrayList<VOPaymentType>();

    public List<VOPSPSetting> getPspSettings() {
        return pspSettings;
    }

    public void setPspSettings(List<VOPSPSetting> pspSettings) {
        this.pspSettings = pspSettings;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public String getId() {
        return id;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public void setId(String identifier) {
        this.id = identifier;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public List<VOPaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<VOPaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VOPSP)) return false;

        VOPSP vopsp = (VOPSP) o;

        if (getKey() != vopsp.getKey()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getKey()).hashCode();
    }
}
