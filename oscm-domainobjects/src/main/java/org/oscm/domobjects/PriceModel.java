/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

import org.hibernate.annotations.Type;

/**
 * PriceModel is the base class for the price models provided by the platform.
 * Subclasses of PriceModel will represent a certain pricing (e.g. pay per use,
 * named user licenses etc.). Instances of PriceModel will either be linked to a
 * Product object (used as master data) or to a Subscription object (the
 * concrete "copy" of a master data's PriceModel, which may be adjusted for a
 * real organization).
 * 
 * @author schmid
 */
@Entity
public class PriceModel extends DomainObjectWithHistory<PriceModelData> {

    private static final long serialVersionUID = 1L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                    LocalizedObjectTypes.PRICEMODEL_LICENSE));

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "priceModel", fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedEvent> consideredEvents = new ArrayList<PricedEvent>();

    @OneToOne(mappedBy = "priceModel", optional = false, fetch = FetchType.LAZY)
    private Product product;

    /**
     * The currency valid for the price model. Only has to be set if the price
     * model is chargeable.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private SupportedCurrency currency;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "priceModel", fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedParameter> selectedParameters = new ArrayList<PricedParameter>();

    @OneToMany(mappedBy = "priceModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedProductRole> roleSpecificUserPrices = new ArrayList<PricedProductRole>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "priceModel", fetch = FetchType.LAZY)
    @OrderBy
    private List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
    
    @Column(name = "uuid")
    @Type(type = "uuid-char")
    private UUID uuid;
    
    public PriceModel() {
        super();
        dataContainer = new PriceModelData();
    }

    /**
     * Copy Constructor to create a copy from a master data's PriceModel to use
     * it for a concrete Subscription
     * 
     * @param src
     */
    public PriceModel(PriceModel src) {
        super();
        dataContainer = new PriceModelData();
        setType(src.getType());
        setCurrency(src.getCurrency());
        setPeriod(src.getPeriod());
        setFreePeriod(src.getFreePeriod());
        setPricePerPeriod(src.getPricePerPeriod());
        setPricePerUserAssignment(src.getPricePerUserAssignment());
        setOneTimeFee(src.getOneTimeFee());
        setExternal(src.isExternal());
        setUuid(src.getUuid());
    }

    public List<PricedEvent> getConsideredEvents() {
        return consideredEvents;
    }

    public void setConsideredEvents(List<PricedEvent> consideredEvents) {
        this.consideredEvents = consideredEvents;
    }

    /**
     * Copy Method to create a copy from this PriceModel to use it for a
     * concrete Subscription
     */
    public PriceModel copy(ParameterSet copiedParameterSet) {
        PriceModel copy = new PriceModel(this);
        // now all priced events have to be copied as well
        List<PricedEvent> copiedEvents = new ArrayList<PricedEvent>();
        for (PricedEvent event : getConsideredEvents()) {
            copiedEvents.add(event.copy(copy));
        }
        copy.setConsideredEvents(copiedEvents);

        // copy all priced parameter
        List<PricedParameter> copiedParameters = new ArrayList<PricedParameter>();
        for (PricedParameter parameter : getSelectedParameters()) {
            PricedParameter pricedParameterCopy = parameter.copy(copy,
                    copiedParameterSet);
            copiedParameters.add(pricedParameterCopy);
        }
        copy.setSelectedParameters(copiedParameters);

        // copy all role prices
        List<PricedProductRole> copiedPricedProductRoles = new ArrayList<PricedProductRole>();
        for (PricedProductRole pricedProdRole : getRoleSpecificUserPrices()) {
            PricedProductRole copiedPricedProductRole = pricedProdRole
                    .copy(copy);
            copiedPricedProductRoles.add(copiedPricedProductRole);
        }
        copy.setRoleSpecificUserPrices(copiedPricedProductRoles);

        // copy stepped prices
        List<SteppedPrice> copiedSteppedPrices = new ArrayList<SteppedPrice>();
        for (SteppedPrice sp : getSteppedPrices()) {
            SteppedPrice spCopy = sp.copy();
            spCopy.setPriceModel(copy);
            copiedSteppedPrices.add(spCopy);
        }
        copy.setSteppedPrices(copiedSteppedPrices);
        return copy;
    }

    @Override
    public PriceModelData getDataContainer() {
        return dataContainer;
    }

    public PriceModelType getType() {
        return dataContainer.getType();
    }

    public void setType(PriceModelType type) {
        dataContainer.setType(type);
    }

    public boolean isChargeable() {
        PriceModelType pmType = dataContainer.getType();
        return pmType != PriceModelType.FREE_OF_CHARGE && pmType != PriceModelType.UNKNOWN;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PricingPeriod getPeriod() {
        return dataContainer.getPeriod();
    }

    public int getFreePeriod() {
        return dataContainer.getFreePeriod();
    }

    public void setFreePeriod(int freePeriod) {
        dataContainer.setFreePeriod(freePeriod);
    }

    public BigDecimal getPricePerPeriod() {
        return dataContainer.getPricePerPeriod();
    }

    public BigDecimal getPricePerUserAssignment() {
        return dataContainer.getPricePerUserAssignment();
    }

    public void setPeriod(PricingPeriod period) {
        dataContainer.setPeriod(period);
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        dataContainer.setPricePerPeriod(pricePerPeriod);
    }

    public void setPricePerUserAssignment(BigDecimal pricePerUser) {
        dataContainer.setPricePerUserAssignment(pricePerUser);
    }

    /**
     * Getting one-time fee for the price model.
     * 
     * @return oneTimeFee One-time fee for the price model.
     */
    public BigDecimal getOneTimeFee() {
        return dataContainer.getOneTimeFee();
    }

    /**
     * Setting one-time fee for the price model.
     * 
     * @param oneTimeFee
     *            One-time fee for the price model.
     */
    public void setOneTimeFee(BigDecimal oneTimeFee) {
        dataContainer.setOneTimeFee(oneTimeFee);
    }
    
    public void setExternal(boolean external) {
        dataContainer.setExternal(external);
    }

    public boolean isExternal() {
        return dataContainer.isExternal();
    }

    public SupportedCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(SupportedCurrency currency) {
        this.currency = currency;
    }

    public List<PricedParameter> getSelectedParameters() {
        return selectedParameters;
    }

    public void setSelectedParameters(List<PricedParameter> selectedParameters) {
        this.selectedParameters = selectedParameters;
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
    
    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    @Override
    String toStringAttributes() {
        return String
                .format(", isExternal='%s', isChargeable='%s', currency='%s', oneTimeFee='%s', pricePerPeriod='%s', pricePerUser='%s'",
                        Boolean.valueOf(isExternal()),
                        Boolean.valueOf(isChargeable()),
                        getCurrency(), getOneTimeFee(), getPricePerPeriod(),
                        getPricePerUserAssignment());
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public boolean isProvisioningCompleted() {
        return dataContainer.isProvisioningCompleted();
    }

    public void setProvisioningCompleted(boolean completed) {
        dataContainer.setProvisioningCompleted(completed);
    }
}
