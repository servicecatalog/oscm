/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.partnerservice;

import java.io.Serializable;

import org.oscm.internal.vo.VOOrganization;

public class POVendorAddress implements Serializable {

    private static final long serialVersionUID = 1141906959452673722L;

    public static final String SERVICE_SELLER_SUPPLIER = "SUPPLIER";
    public static final String SERVICE_SERVICE_PARTNER = "SERVICE_PARTNER";

    private String name;
    private String url;
    private long key;
    private String address;
    private String domicileCountryDisplay;
    private String phone;
    private String email;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomicileCountryDisplay() {
        return domicileCountryDisplay;
    }

    public void setDomicileCountryDisplay(String domicileCountryDisplay) {
        this.domicileCountryDisplay = domicileCountryDisplay;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public POVendorAddress(VOOrganization org) {
        super();
        this.name = org.getName();
        this.url = org.getUrl();
        this.key = org.getKey();
        this.address = org.getAddress();
        this.domicileCountryDisplay = org.getDomicileCountry();
        this.phone = org.getPhone();
        this.email = org.getEmail();
        this.description = org.getDescription();
    }

}
