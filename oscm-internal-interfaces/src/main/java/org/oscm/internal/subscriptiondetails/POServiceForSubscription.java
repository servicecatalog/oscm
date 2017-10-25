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

import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

public class POServiceForSubscription implements Serializable {

    private static final long serialVersionUID = -6212333485876532308L;

    private VOServiceEntry service;
    private List<VOSubscription> subscriptions;
    private List<VOUdaDefinition> definitions;
    private List<VOUda> organizationUdas;
    private VODiscount discount;

    public VOServiceEntry getService() {
        return service;
    }

    public void setService(VOServiceEntry service) {
        this.service = service;
    }

    public List<VOSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<VOSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<VOUdaDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<VOUdaDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<VOUda> getOrganizationUdas() {
        return organizationUdas;
    }

    public void setOrganizationUdas(List<VOUda> organizationUdas) {
        this.organizationUdas = organizationUdas;
    }

    public void setDiscount(VODiscount discount) {
        this.discount = discount;
    }

    public VODiscount getDiscount() {
        return discount;
    }

}
