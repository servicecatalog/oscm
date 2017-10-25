/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.local;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;

/**
 * Container mapping a {@link Subscription} to a list of {@link RoleDefinition}s
 * defined on the subscription's {@link TechnicalProduct}.
 * 
 * @author weiser
 * 
 */
public class SubscriptionWithRoles {

    Subscription subscription;
    List<RoleDefinition> roles = new ArrayList<RoleDefinition>();

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public List<RoleDefinition> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDefinition> roles) {
        this.roles = roles;
    }
}
