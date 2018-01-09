/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 23.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class RegisterCustomerTask extends WebtestTask {

    private String password = "secret";
    private Long serviceKey;
    private String marketplaceId;
    private String supplierId;

    // admin user
    private String userId;

    // organization details
    private String orgName;
    private String country = "DE";
    private String phone;
    private String url;
    private String address;
    private String description;
    private String locale;
    private boolean createUID = true;

    @Override
    public void executeInternal() throws Exception {
        AccountService accSvc = getServiceInterface(AccountService.class);

        VOOrganization organization = new VOOrganization();
        organization.setAddress(getAddress());
        organization.setDescription(getDescription());
        organization.setDomicileCountry(getCountry());
        organization.setName(getOrgName());
        organization.setPhone(getPhone());
        organization.setUrl(getUrl());
        organization.setLocale(getLocale());

        String createdUserId = createUserId();
        VOUserDetails admin = new VOUserDetails();
        admin.setUserId(createdUserId);
        admin.setEMail(getProject().getProperty(COMMON_EMAIL));
        admin.setLocale(getLocale());

        organization = accSvc.registerCustomer(organization, admin, password,
                serviceKey, marketplaceId, supplierId);

        getProject().setProperty("createdUserId", createdUserId);
        getProject().setProperty("createdCustomerOrgId",
                organization.getOrganizationId());
    }

    private String createUserId() {
        if (createUID) {
            return String.format("%s_%s", userId,
                    String.valueOf(System.currentTimeMillis()));
        }
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(Long serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgName() {
        if (orgName == null) {
            orgName = getProject().getProperty(COMMON_ORG_NAME);
        }
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        if (phone == null) {
            phone = getProject().getProperty("test.organization.phone");
        }
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        if (url == null) {
            url = getProject().getProperty("test.organization.url");
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddress() {
        if (address == null) {
            address = getProject().getProperty(TEST_ORGANIZATION_ADDRESS);
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            description = getProject().getProperty(
                    "test.organization.description");
        }
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocale() {
        if (locale == null) {
            locale = getProject().getProperty("common.locale");
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setCreateUID(boolean createUID) {
        this.createUID = createUID;
    }

    public boolean isCreateUID() {
        return createUID;
    }

}
