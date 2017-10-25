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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Represents a payment service provider available in the BES installation.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "PSP.getAll", query = "SELECT psp FROM PSP psp WHERE tkey <> 1 ORDER BY tkey"),
        @NamedQuery(name = "PSP.findByBusinessKey", query = "SELECT psp FROM PSP psp WHERE psp.dataContainer.identifier = :identifier") })
@BusinessKey(attributes = "identifier")
public class PSP extends DomainObjectWithHistory<PSPData> {

    private static final long serialVersionUID = 904260628751855983L;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "psp", cascade = CascadeType.ALL)
    @OrderBy
    private List<PSPSetting> settings = new ArrayList<PSPSetting>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "psp")
    @OrderBy
    private List<PaymentType> paymentTypes = new ArrayList<PaymentType>();

    public PSP() {
        super();
        dataContainer = new PSPData();
    }

    public String getIdentifier() {
        return dataContainer.getIdentifier();
    }

    public String getWsdlUrl() {
        return dataContainer.getWsdlUrl();
    }

    public void setIdentifier(String identifier) {
        dataContainer.setIdentifier(identifier);
    }

    public void setWsdlUrl(String wsdlUrl) {
        dataContainer.setWsdlUrl(wsdlUrl);
    }

    public List<PSPSetting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public void addPSPSetting(PSPSetting setting) {
        setting.setPsp(this);
        this.settings.add(setting);
    }

    public void addPSPSettings(Collection<PSPSetting> settings) {
        for (PSPSetting pspSetting : settings) {
            pspSetting.setPsp(this);
            this.settings.add(pspSetting);
        }
    }

    public void removePSPSetting(PSPSetting setting) {
        this.settings.remove(setting);
    }

    public List<PaymentType> getPaymentTypes() {
        return Collections.unmodifiableList(paymentTypes);
    }

    public void addPaymentType(PaymentType pt) {
        paymentTypes.add(pt);
    }

    public String getDistinguishedName() {
        return dataContainer.getDistinguishedName();
    }

    public void setDistinguishedName(String distinguishedName) {
        dataContainer.setDistinguishedName(distinguishedName);
    }
}
