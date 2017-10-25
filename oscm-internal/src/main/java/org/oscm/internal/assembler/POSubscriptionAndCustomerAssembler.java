/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.io.Serializable;

import org.oscm.converter.TimeStampUtil;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;

/**
 * @author Mao
 * 
 */
public class POSubscriptionAndCustomerAssembler implements Serializable {

    private static final long serialVersionUID = -3683281046693667016L;

    public static POSubscriptionAndCustomer toPOSubscriptionAndCustomer(
            Subscription subscription) {

        POSubscriptionAndCustomer poSubscriptionAndCustomer = new POSubscriptionAndCustomer();
        poSubscriptionAndCustomer.setSubscriptionId(subscription
                .getSubscriptionId());
        poSubscriptionAndCustomer.setServiceId(getProductId(subscription
                .getProduct()));
        poSubscriptionAndCustomer.setCustomerId(subscription.getOrganization()
                .getOrganizationId());
        poSubscriptionAndCustomer.setCustomerName(subscription
                .getOrganization().getName());
        poSubscriptionAndCustomer.setTkey(subscription.getKey());
        if (subscription.getActivationDate() != null)
            poSubscriptionAndCustomer.setActivation(subscription
                    .getActivationDate().toString());
        
        
        return poSubscriptionAndCustomer;
    }
    
    public static POSubscriptionAndCustomer toPOSubscriptionAndCustomer(
            Subscription subscription, LocalizerFacade facade) {

        POSubscriptionAndCustomer poSubscriptionAndCustomer = new POSubscriptionAndCustomer();
        poSubscriptionAndCustomer.setSubscriptionId(subscription
                .getSubscriptionId());
        poSubscriptionAndCustomer.setServiceId(getProductId(subscription
                .getProduct()));
        poSubscriptionAndCustomer.setCustomerId(subscription.getOrganization()
                .getOrganizationId());
        poSubscriptionAndCustomer.setCustomerName(subscription
                .getOrganization().getName());
        poSubscriptionAndCustomer.setTkey(subscription.getKey());
        if (subscription.getActivationDate() != null)
            poSubscriptionAndCustomer.setActivation(subscription
                    .getActivationDate().toString());
        
        Product product = subscription.getProduct();
        poSubscriptionAndCustomer.setServiceName(facade.getText(product.getKey(), 
        		LocalizedObjectTypes.PRODUCT_MARKETING_NAME));
        return poSubscriptionAndCustomer;
    }

    public static String getProductId(final Product product) {
        String productId = product.getTemplate() == null ? product
                .getProductId() : product.getTemplate().getProductId();
        return TimeStampUtil.removeTimestampFromId(productId);
    }
}
