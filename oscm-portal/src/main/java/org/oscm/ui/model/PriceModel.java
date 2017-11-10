/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 30.11.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.math.BigDecimal;
import java.util.List;

import org.oscm.converter.DateConverter;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Wrapper for the vo price model object.
 * 
 * @author kulle
 * 
 */
public class PriceModel {

    private VOPriceModel vo;

    public PriceModel(VOPriceModel priceModel) {
        this.vo = priceModel;
    }

    public long getKey() {
        return vo.getKey();
    }

    public int getVersion() {
        return vo.getVersion();
    }

    public void setKey(long key) {
        vo.setKey(key);
    }

    public void setVersion(int version) {
        vo.setVersion(version);
    }

    public List<VOPricedEvent> getConsideredEvents() {
        return vo.getConsideredEvents();
    }

    public void setConsideredEvents(List<VOPricedEvent> consideredEvents) {
        vo.setConsideredEvents(consideredEvents);
    }

    public void setDescription(String description) {
        vo.setDescription(description);
    }

    public String getDescription() {
        return vo.getDescription();
    }

    public boolean isChargeable() {
        return vo.isChargeable();
    }

    public boolean isExternal() {
        return vo.isExternal();
    }

    public boolean isFree() {
        return vo.isFree();
    }

    public PricingPeriod getPeriod() {
        return vo.getPeriod();
    }

    public BigDecimal getPricePerPeriod() {
        return vo.getPricePerPeriod();
    }

    public BigDecimal getPricePerUserAssignment() {
        return vo.getPricePerUserAssignment();
    }

    public String getCurrencyISOCode() {
        return vo.getCurrencyISOCode();
    }

    public void setPeriod(PricingPeriod period) {
        vo.setPeriod(period);
    }

    public int getFreePeriod() {
        return vo.getFreePeriod();
    }

    public void setFreePeriod(int freePeriod) {
        vo.setFreePeriod(freePeriod);
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        vo.setPricePerPeriod(pricePerPeriod);
    }

    public void setPricePerUserAssignment(BigDecimal pricePerUserAssignment) {
        vo.setPricePerUserAssignment(pricePerUserAssignment);
    }

    public void setCurrencyISOCode(String currencyISOCode) {
        vo.setCurrencyISOCode(currencyISOCode);
    }

    public BigDecimal getOneTimeFee() {
        return vo.getOneTimeFee();
    }

    public void setOneTimeFee(BigDecimal oneTimeFee) {
        vo.setOneTimeFee(oneTimeFee);
    }

    public List<VOPricedParameter> getSelectedParameters() {
        return vo.getSelectedParameters();
    }

    public void setSelectedParameters(
            List<VOPricedParameter> selectedParameters) {
        vo.setSelectedParameters(selectedParameters);
    }

    public String getCurrency() {
        return vo.getCurrency();
    }

    public List<VOPricedRole> getRoleSpecificUserPrices() {
        return vo.getRoleSpecificUserPrices();
    }

    public void setRoleSpecificUserPrices(
            List<VOPricedRole> roleSpecificUserPrices) {
        vo.setRoleSpecificUserPrices(roleSpecificUserPrices);
    }

    public void setSteppedPrices(List<VOSteppedPrice> steppedPrices) {
        vo.setSteppedPrices(steppedPrices);
    }

    public List<VOSteppedPrice> getSteppedPrices() {
        return vo.getSteppedPrices();
    }

    public void setLicense(String license) {
        vo.setLicense(license);
    }

    public String getLicense() {
        return vo.getLicense();
    }

    public PriceModelType getType() {
        return vo.getType();
    }

    public void setType(PriceModelType type) {
        vo.setType(type);
    }

    public VOPriceModel getVo() {
        return vo;
    }

    public void setVo(VOPriceModel vo) {
        this.vo = vo;
    }

    public String getTimezone() {
        return DateConverter.getCurrentTimeZoneAsUTCString();
    }
}
