/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOOrganization;

/**
 * Wrapper Class for VOOrganization which holds additional view attributes.
 * 
 */
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param voOrganization
     *            the organization
     * @return the organization id if the name of the organization is empty
     *         otherwise return the name concatenated with the organization id
     *         in brackets.
     */
    public static String getNameWithOrganizationId(VOOrganization voOrganization) {
        if (voOrganization.getName() != null
                && voOrganization.getName().length() > 0) {
            return voOrganization.getName() + " ("
                    + voOrganization.getOrganizationId() + ")";
        }
        return voOrganization.getOrganizationId();
    }

    private VOOrganization voOrganization;

    private boolean selected = false;

    public String getNameWithOrganizationId() {
        return getNameWithOrganizationId(voOrganization);
    }

    public Organization(VOOrganization voOrganization) {
        this.voOrganization = voOrganization;
    }

    public String getAddress() {
        return voOrganization.getAddress();
    }

    public String getOrganizationId() {
        return voOrganization.getOrganizationId();
    }

    public String getEmail() {
        return voOrganization.getEmail();
    }

    public String getName() {
        return voOrganization.getName();
    }

    public String getPhone() {
        return voOrganization.getPhone();
    }

    public void setAddress(String address) {
        voOrganization.setAddress(address);
    }

    public void setOrganizationId(String organizationId) {
        voOrganization.setOrganizationId(organizationId);
    }

    public void setEmail(String email) {
        voOrganization.setEmail(email);
    }

    public void setName(String name) {
        voOrganization.setName(name);
    }

    public void setPhone(String phone) {
        voOrganization.setPhone(phone);
    }

    public String toString() {
        return voOrganization.toString();
    }

    public VOOrganization getVOOrganization() {
        return voOrganization;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    private VODiscount getDiscount() {
        if (voOrganization.getDiscount() == null) {
            voOrganization.setDiscount(new VODiscount());
        }
        return voOrganization.getDiscount();
    }

    /**
     * Setter for discount value.
     * 
     * @param discountValue
     */
    public void setDiscountValue(BigDecimal discountValue) {
        getDiscount().setValue(discountValue);
    }

    /**
     * Getter for discount value.
     * 
     * @return Discount value.
     */
    public BigDecimal getDiscountValue() {
        return getDiscount().getValue();
    }

    /**
     * Setter for discount begin.
     * 
     * @param discountBegin
     */
    public void setDiscountBegin(Long discountBegin) {
        getDiscount().setStartTime(discountBegin);
    }

    /**
     * Getter for discount begin.
     * 
     * @return Discount value.
     */
    public Long getDiscountBegin() {
        return getDiscount().getStartTime();
    }

    /**
     * Setter for discount end.
     * 
     * @param discountEnd
     */
    public void setDiscountEnd(Long discountEnd) {
        getDiscount().setEndTime(discountEnd);
    }

    /**
     * Getter for discount end.
     * 
     * @return Discount value.
     */
    public Long getDiscountEnd() {
        return getDiscount().getEndTime();
    }

    /**
     * Setter for VOOrganization.
     * 
     * @param voOrganization
     */
    public void setVOOrganization(VOOrganization voOrganization) {
        this.voOrganization = voOrganization;
    }

    /**
     * Sets the domicile country for this organization. A supplier may restrict
     * the list of supported countries this organization can choose from.
     * 
     * @param countryCode
     *            The country code in ISO 3166.
     */
    public void setDomicileCountry(String countryCode) {
        voOrganization.setDomicileCountry(countryCode);
    }

    /**
     * Returns the domicile of this organization.
     * 
     * @return String The country code in ISO 3166.
     */
    public String getDomicileCountry() {
        return voOrganization.getDomicileCountry();
    }

    public String getDomicileCountryDisplay() {
        if (getDomicileCountry() == null
                || getDomicileCountry().trim().length() == 0) {
            return null;
        }
        return new Locale("", getDomicileCountry()).getDisplayCountry(JSFUtils
                .getViewLocale());
    }

    public String getUrl() {
        return voOrganization.getUrl();
    }

    public void setUrl(String url) {
        voOrganization.setUrl(url);
    }

    public String getDescription() {
        return voOrganization.getDescription();
    }

    public void setDescription(String description) {
        voOrganization.setDescription(description);
    }

    public boolean isImageDefined() {
        return voOrganization.isImageDefined();
    }

    public void setImageDefined(boolean imageDefined) {
        voOrganization.setImageDefined(imageDefined);
    }

    public long getKey() {
        return voOrganization.getKey();
    }

}
