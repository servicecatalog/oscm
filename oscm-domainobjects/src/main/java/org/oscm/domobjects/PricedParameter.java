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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * It stores the all information regarding the PricedParameter.
 * 
 * @author PRavi
 * 
 */

@Entity
@NamedQueries({ @NamedQuery(name = "PricedParameter.getForParameter", query = "SELECT pm FROM PricedParameter pm WHERE pm.parameter = :parameter") })
public class PricedParameter extends
        DomainObjectWithHistory<PricedParameterData> {

    private static final long serialVersionUID = -5159318393660918660L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priceModelKey")
    private PriceModel priceModel;

    @ManyToOne(fetch = FetchType.LAZY)
    private Parameter parameter;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pricedParameter", fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedOption> pricedOptionList = new ArrayList<PricedOption>();

    @OneToMany(mappedBy = "pricedParameter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedProductRole> roleSpecificUserPrices = new ArrayList<PricedProductRole>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pricedParameter", fetch = FetchType.LAZY)
    @OrderBy
    private List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();

    public PricedParameter() {
        super();
        dataContainer = new PricedParameterData();
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

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public List<PricedOption> getPricedOptionList() {
        return pricedOptionList;
    }

    public void setPricedOptionList(List<PricedOption> pricedOptionList) {
        this.pricedOptionList = pricedOptionList;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    /**
     * The copy of the PricedOptionList is necessary since it raises the cyclic
     * reference exception if the identical pricedOptionsList is passed in the
     * copy of the PricedParameter.
     * 
     * @param refPricedParameter
     *            - the copied pricedParmeter which will have the copied
     *            pricedOptions
     * @return List<PricedOption> - the list of the copied priced options.
     */
    private List<PricedOption> getCopiedPricedOptionsList(
            PricedParameter refPricedParameter) {
        List<PricedOption> copiedOptionsList = new ArrayList<PricedOption>();
        for (PricedOption option : getPricedOptionList()) {
            PricedOption copy = option.copy(refPricedParameter);
            // copy all role prices
            List<PricedProductRole> copiedPricedProductRoles = new ArrayList<PricedProductRole>();
            for (PricedProductRole pricedProdRole : option
                    .getRoleSpecificUserPrices()) {
                PricedProductRole copiedPricedProductRole = pricedProdRole
                        .copy(copy);
                copiedPricedProductRoles.add(copiedPricedProductRole);
            }
            copy.setRoleSpecificUserPrices(copiedPricedProductRoles);
            copiedOptionsList.add(copy);
        }
        return copiedOptionsList;
    }

    public PricedParameter copy(PriceModel refPM,
            ParameterSet copiedParameterSet) {
        PricedParameter copy = new PricedParameter();
        copy.setDataContainer(new PricedParameterData());
        copy.setPricePerSubscription(this.getPricePerSubscription());
        copy.setPricePerUser(this.getPricePerUser());
        copy.setPriceModel(refPM);
        Parameter copiedParameter = getCopiedParameter(copiedParameterSet,
                getParameter());
        copy.setParameter(copiedParameter);
        copy.setPricedOptionList(getCopiedPricedOptionsList(copy));

        // copy all role prices
        List<PricedProductRole> copiedPricedProductRoles = new ArrayList<PricedProductRole>();
        for (PricedProductRole pricedProdRole : getRoleSpecificUserPrices()) {
            PricedProductRole copiedPricedProductRole = pricedProdRole
                    .copy(copy);
            copiedPricedProductRoles.add(copiedPricedProductRole);
        }
        copy.setRoleSpecificUserPrices(copiedPricedProductRoles);
        // copy stepped prices
        List<SteppedPrice> copiedSteppedUserPrices = new ArrayList<SteppedPrice>();
        for (SteppedPrice sp : getSteppedPrices()) {
            SteppedPrice spCopy = sp.copy();
            spCopy.setPricedParameter(copy);
            copiedSteppedUserPrices.add(spCopy);
        }
        copy.setSteppedPrices(copiedSteppedUserPrices);
        return copy;
    }

    /**
     * Maps the existing parameter to the one of the copied parameter set.
     * 
     * @param copiedParameterSet
     *            the parameter set copy
     * @param existingParameter
     *            the existing parameter
     * @return the copied parameter
     */
    private static Parameter getCopiedParameter(
            ParameterSet copiedParameterSet, Parameter existingParameter) {
        for (Parameter param : copiedParameterSet.getParameters()) {
            if (param.getParameterDefinition().getKey() == existingParameter
                    .getParameterDefinition().getKey()) {
                return param;
            }
        }
        return null;
    }

    public List<PricedProductRole> getRoleSpecificUserPrices() {
        return roleSpecificUserPrices;
    }

    public void setRoleSpecificUserPrices(
            List<PricedProductRole> roleSpecificUserPrices) {
        this.roleSpecificUserPrices = roleSpecificUserPrices;
    }

    public void setSteppedPrices(List<SteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    public List<SteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    @Override
    String toStringAttributes() {
        return String
                .format(", pricePerSubscription='%s', pricePerUser='%s', %nsteppedPrices='%s'",
                        getPricePerSubscription(), getPricePerUser(),
                        getSteppedPrices());
    }
}
