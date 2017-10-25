/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptiondetails;

import java.io.Serializable;
import java.util.List;

import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;

public class POSubscriptionDetails implements Serializable {

    private static final long serialVersionUID = -6212333485876532308L;

    private VOSubscriptionDetails subscription;
    private List<VOUserDetails> usersForOrganization;
    private List<VOUserDetails> allUsers;
    private VOOrganization customer;
    private VOOrganization seller;
    private VOOrganization partner;
    private List<VOService> upgradeOptions;
    private List<VOUdaDefinition> udasDefinitions;
    private List<VOUda> udasOrganisation;
    private List<VOUda> udasSubscription;
    private List<VORoleDefinition> serviceRoles;
    private VODiscount discount;
    private int numberOfSessions;
    private SubscriptionStatus status;

    public VOSubscriptionDetails getSubscription() {
        return subscription;
    }

    public void setSubscription(VOSubscriptionDetails subscription) {
        this.subscription = subscription;
    }

    public List<VOUserDetails> getUsersForOrganization() {
        return usersForOrganization;
    }

    public void setUsersForOrganization(List<VOUserDetails> usersForOrganization) {
        this.usersForOrganization = usersForOrganization;
    }

    public List<VOUserDetails> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<VOUserDetails> allUsers) {
        this.allUsers = allUsers;
    }

    public VOOrganization getCustomer() {
        return customer;
    }

    public void setCustomer(VOOrganization customer) {
        this.customer = customer;
    }

    public VOOrganization getSeller() {
        return seller;
    }

    public void setSeller(VOOrganization seller) {
        this.seller = seller;
    }

    public List<VOService> getUpgradeOptions() {
        return upgradeOptions;
    }

    public void setUpgradeOptions(List<VOService> upgradeOptions) {
        this.upgradeOptions = upgradeOptions;
    }

    public VOOrganization getPartner() {
        return partner;
    }

    public void setPartner(VOOrganization partner) {
        this.partner = partner;
    }

    public List<VOUdaDefinition> getUdasDefinitions() {
        return udasDefinitions;
    }

    public void setUdasDefinitions(List<VOUdaDefinition> definitions) {
        this.udasDefinitions = definitions;
    }

    public List<VOUda> getUdasOrganisation() {
        return udasOrganisation;
    }

    public void setUdasOrganisation(List<VOUda> orgUdas) {
        this.udasOrganisation = orgUdas;
    }

    public List<VOUda> getUdasSubscription() {
        return udasSubscription;
    }

    public void setUdasSubscription(List<VOUda> subUdas) {
        this.udasSubscription = subUdas;
    }

    public void setServiceRoles(List<VORoleDefinition> serviceRoles) {
        this.serviceRoles = serviceRoles;
    }

    public List<VORoleDefinition> getServiceRoles() {
        return serviceRoles;
    }

    public VODiscount getDiscount() {
        return discount;
    }

    public void setDiscount(VODiscount discount) {
        this.discount = discount;
    }

    public int getNumberOfSessions() {
        return numberOfSessions;
    }

    public void setNumberOfSessions(int value) {
        numberOfSessions = value;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

}
