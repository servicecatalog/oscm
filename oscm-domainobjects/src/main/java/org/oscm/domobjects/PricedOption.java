/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Dec 28, 2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * It stores the all information regarding the PricedOptions.
 * 
 * @author PRavi
 * 
 */
@Entity
public class PricedOption extends DomainObjectWithHistory<PricedOptionData> {

    private static final long serialVersionUID = -7155165300532282598L;

    @Column(name = "parameterOptionKey")
    private long parameterOptionKey;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PricedParameter pricedParameter;

    @OneToMany(mappedBy = "pricedOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedProductRole> roleSpecificUserPrices = new ArrayList<PricedProductRole>();

    public PricedOption() {
        super();
        dataContainer = new PricedOptionData();
    }

    public void setPricedParameter(PricedParameter pricedParameter) {
        this.pricedParameter = pricedParameter;
    }

    public PricedParameter getPricedParameter() {
        return pricedParameter;
    }

    public long getParameterOptionKey() {
        return parameterOptionKey;
    }

    public void setParameterOptionKey(long parameterOptionKey) {
        this.parameterOptionKey = parameterOptionKey;
    }

    public BigDecimal getPricePerUser() {
        return dataContainer.getPricePerUser();
    }

    public void setPricePerUser(BigDecimal pricePerUser) {
        dataContainer.setPricePerUser(pricePerUser);
    }

    public BigDecimal getPricePerSubscription() {
        return dataContainer.getPricePerSubscription();
    }

    public void setPricePerSubscription(BigDecimal pricePerSubscription) {
        dataContainer.setPricePerSubscription(pricePerSubscription);
    }

    public PricedOption copy(PricedParameter refPriceParameter) {
        PricedOption copy = new PricedOption();
        copy.setDataContainer(new PricedOptionData());
        copy.setPricePerSubscription(this.getPricePerSubscription());
        copy.setPricePerUser(this.getPricePerUser());
        copy.setPricedParameter(refPriceParameter);
        copy.setParameterOptionKey(this.getParameterOptionKey());
        return copy;
    }

    public List<PricedProductRole> getRoleSpecificUserPrices() {
        return roleSpecificUserPrices;
    }

    public void setRoleSpecificUserPrices(
            List<PricedProductRole> roleSpecificUserPrices) {
        this.roleSpecificUserPrices = roleSpecificUserPrices;
    }
}
