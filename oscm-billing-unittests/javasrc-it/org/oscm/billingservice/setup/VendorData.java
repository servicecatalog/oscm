/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * Data object holding vendor (supplier, broker or reseller) information.
 * 
 * @author baumann
 * 
 */
public class VendorData {

    private VOOrganization organization;
    private List<OrganizationRoleType> orgRoles = new ArrayList<OrganizationRoleType>();
    private long adminKey;
    private List<VOMarketplace> marketplaces = new ArrayList<VOMarketplace>();
    private List<VOServiceDetails> services = new ArrayList<VOServiceDetails>();
    private Map<String, String> srvIdToMpId = new HashMap<String, String>();
    private List<CustomerData> customers = new ArrayList<CustomerData>();

    public VOOrganization getOrganization() {
        return organization;
    }

    public void setOrganization(VOOrganization organization) {
        this.organization = organization;
    }

    public long getOrganizationKey() {
        return organization.getKey();
    }

    public String getOrganizationId() {
        return organization.getOrganizationId();
    }

    public String getOrganizationName() {
        return organization.getName();
    }

    public List<OrganizationRoleType> getOrgRoles() {
        return orgRoles;
    }

    public void setOrgRoles(OrganizationRoleType... roles) {
        orgRoles.addAll(Arrays.asList(roles));
    }

    public long getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(long adminKey) {
        this.adminKey = adminKey;
    }

    public String[] getAdminUserRoles() {
        if (orgRoles.contains(OrganizationRoleType.BROKER)) {
            return new String[] { UserRoleType.ORGANIZATION_ADMIN.name(),
                    UserRoleType.BROKER_MANAGER.name() };
        } else if (orgRoles.contains(OrganizationRoleType.RESELLER)) {
            return new String[] { UserRoleType.ORGANIZATION_ADMIN.name(),
                    UserRoleType.RESELLER_MANAGER.name() };
        } else {
            return new String[] { UserRoleType.ORGANIZATION_ADMIN.name(),
                    UserRoleType.SERVICE_MANAGER.name() };
        }
    }

    /**
     * Get the offering type for the vendor's services
     */
    public OfferingType getOfferingType() {
        if (orgRoles.contains(OrganizationRoleType.BROKER)) {
            return OfferingType.BROKER;
        } else if (orgRoles.contains(OrganizationRoleType.RESELLER)) {
            return OfferingType.RESELLER;
        } else {
            return OfferingType.DIRECT;
        }
    }

    public VOMarketplace getMarketplace(int index) {
        return marketplaces.get(index);
    }

    public String getMarketplaceId(int index) {
        return marketplaces.get(index).getMarketplaceId();
    }

    public void addMarketplace(VOMarketplace marketplace) {
        marketplaces.add(marketplace);
    }

    public VOServiceDetails getService(int index) {
        return services.get(index);
    }

    public void addService(VOServiceDetails service) {
        services.add(service);
    }

    public String getMarketplaceId(VOService service) {
        if (service != null) {
            return srvIdToMpId.get(service.getServiceId());
        } else {
            return null;
        }
    }

    public void setMarketplaceForService(String serviceId, String marketplaceId) {
        if (marketplaceId != null && serviceId != null) {
            srvIdToMpId.put(serviceId, marketplaceId);
        }
    }

    public void addCustomers(CustomerData... customersData) {
        for (CustomerData customerData : customersData) {
            customers.add(customerData);
        }
    }

    public CustomerData getCustomer(int index) {
        return customers.get(index);
    }

}
