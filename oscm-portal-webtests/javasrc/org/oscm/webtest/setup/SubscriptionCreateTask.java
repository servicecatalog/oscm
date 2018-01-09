/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.*;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.*;

/**
 * @author weiser
 * 
 */
public class SubscriptionCreateTask extends WebtestTask {

    private String bcId;
    private String piId;
    private String subId;
    private String pon;
    private String marketplaceId;
    private String locale;
    private Long serviceKey;
    private String userIds;
    private String userRole;
    private String udas;

    @Override
    public void executeInternal() throws Exception {
        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId(subId);
        sub.setPurchaseOrderNumber(pon);
        SubscriptionService ss = getServiceInterface(SubscriptionService.class);
        VOService svc = getService();
        ArrayList<VOUda> udaValues = processUdas();
        ss.subscribeToService(sub, svc, getUsers(ss, svc), getPaymentInfo(),
                getBillingContact(), udaValues);
    }

    private ArrayList<VOUda> processUdas() {
        ArrayList<VOUda> udaValues = new ArrayList<>();
        if (udas != null && udas.length() != 0) {
            AccountService as = getServiceInterface(AccountService.class);
            List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();
            for (VOUdaDefinition udaDefinition : udaDefinitions) {
                if (udas.contains(udaDefinition.getUdaId())) {
                    VOUda voUda = new VOUda();
                    voUda.setUdaDefinition(udaDefinition);
                    voUda.setUdaValue(udaDefinition.getDefaultValue());
                    udaValues.add(voUda);
                }
            }
        }
        return udaValues;
    }

    private VOBillingContact getBillingContact() {
        if (!isEmpty(bcId)) {
            AccountService as = getServiceInterface(AccountService.class);
            List<VOBillingContact> list = as.getBillingContacts();
            Set<String> foundIds = new HashSet<String>();
            for (VOBillingContact bc : list) {
                foundIds.add(bc.getId());
                if (bc.getId().equals(bcId)) {
                    return bc;
                }
            }
            log(String.format("Billing contact '%s' not found. Found only: %s",
                    bcId, foundIds), 0);
        }
        return null;
    }

    private VOPaymentInfo getPaymentInfo() {
        if (!isEmpty(piId)) {
            AccountService as = getServiceInterface(AccountService.class);
            List<VOPaymentInfo> list = as.getPaymentInfos();
            Set<String> foundIds = new HashSet<String>();
            for (VOPaymentInfo pi : list) {
                foundIds.add(pi.getId());
                if (pi.getId().equals(piId)) {
                    return pi;
                }
            }
            log(String.format("Payment info '%s' not found. Found only: %s",
                    piId, foundIds), 0);
        }
        return null;
    }

    private List<VOUsageLicense> getUsers(SubscriptionService ss, VOService svc)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArrayList<VOUsageLicense> list = new ArrayList<VOUsageLicense>();
        if (isEmpty(userIds)) {
            return list;
        }
        VORoleDefinition role = null;
        if (!isEmpty(userRole)) {
            List<VORoleDefinition> roles = ss.getServiceRolesForService(svc);
            for (VORoleDefinition r : roles) {
                if (r.getRoleId().equals(userRole)) {
                    role = r;
                    break;
                }
            }
            if (role == null) {
                log(String.format("Role '%s' not found.", userRole), 0);
            }
        }
        HashSet<String> ids = new HashSet<String>();
        ids.addAll(Arrays.asList(userIds.split(",")));
        IdentityService is = getServiceInterface(IdentityService.class);
        List<VOUserDetails> users = is.getUsersForOrganization();
        for (VOUserDetails u : users) {
            if (ids.remove(u.getUserId())) {
                VOUsageLicense lic = new VOUsageLicense();
                lic.setUser(u);
                lic.setRoleDefinition(role);
                list.add(lic);
            }
        }
        if (!ids.isEmpty()) {
            log(String.format("Users '%s' not found/assigned.", ids), 0);
        }
        return list;
    }

    private VOService getService() throws Exception {
        ServiceProvisioningService sps = getServiceInterface(ServiceProvisioningService.class);
        VOServiceEntry entry = sps.getServiceForMarketplace(serviceKey,
                marketplaceId, locale);
        return entry;
    }

    public void setBcId(String bcId) {
        this.bcId = bcId;
    }

    public void setPiId(String piId) {
        this.piId = piId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public void setPon(String pon) {
        this.pon = pon;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = Long.valueOf(serviceKey);
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUdas() {
        return udas;
    }

    public void setUdas(String udas) {
        this.udas = udas;
    }
}
